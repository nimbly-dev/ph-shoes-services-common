package com.nimbly.phshoesbackend.services.common.core.api.rate;

import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties;
import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties.LimitProperties;
import com.nimbly.phshoesbackend.services.common.core.api.props.ApiRateLimitProperties.RouteProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultApiRateLimiter implements ApiRateLimiter {

    private static final Duration DEFAULT_WINDOW = Duration.ofHours(24);

    private final ApiRateLimitProperties properties;
    private final Clock clock;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<RouteConfig> specificRoutes;
    private final RouteConfig fallbackRoute;
    private final ConcurrentHashMap<String, WindowState> counters = new ConcurrentHashMap<>();

    public DefaultApiRateLimiter(ApiRateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        RouteSet routes = buildRoutes(properties);
        this.specificRoutes = routes.routes();
        this.fallbackRoute = routes.fallback();
    }

    @Override
    public void assertWithinLimit(ApiRateLimitRequest request) {
        if (!properties.isEnabled()) {
            return;
        }
        RouteConfig route = resolveRoute(request.path(), request.method());
        Instant now = Instant.now(clock);

        enforceLimit(route, "global", route.global(), routeKey(route.name(), "global"), request.permits(), now);

        String userId = normalizeKey(request.userId());
        if (userId != null) {
            enforceLimit(route, "user:" + userId, route.perUser(),
                    routeKey(route.name(), "user:" + userId), request.permits(), now);
        }

        String clientIp = normalizeKey(request.clientIp());
        if (clientIp != null) {
            enforceLimit(route, "ip:" + clientIp, route.perIp(),
                    routeKey(route.name(), "ip:" + clientIp), request.permits(), now);
        }
    }

    private void enforceLimit(RouteConfig route,
                              String scopeLabel,
                              LimitDefinition limit,
                              String counterKey,
                              int permits,
                              Instant now) {
        if (!limit.active()) {
            return;
        }

        counters.compute(counterKey, (key, state) -> {
            WindowState current = state;
            if (current == null || current.isExpired(now) || !current.window().equals(limit.window())) {
                current = new WindowState(alignWindowStart(now, limit.window()), limit.window());
            }

            int attempted = current.count() + permits;
            if (attempted > limit.limit()) {
                logViolation(route.name(), scopeLabel, limit.limit(), attempted, permits);
                if (!properties.isLogOnly()) {
                    throw new RateLimitExceededException(route.name() + ":" + scopeLabel,
                            limit.limit(), attempted, permits);
                }
            } else {
                current.increment(permits);
            }
            return current;
        });
    }

    private void logViolation(String routeName, String scope, int limit, int attempted, int permits) {
        if (properties.isLogOnly()) {
            log.warn("[API-RATE] limit exceeded but allowed (logOnly=true) route={} scope={} limit={} attempted={} (+{})",
                    routeName, scope, limit, attempted, permits);
        } else {
            log.warn("[API-RATE] limit exceeded route={} scope={} limit={} attempted={} (+{})",
                    routeName, scope, limit, attempted, permits);
        }
    }

    private RouteConfig resolveRoute(String path, HttpMethod method) {
        HttpMethod httpMethod = method == null ? HttpMethod.GET : method;
        for (RouteConfig route : specificRoutes) {
            if (route.matches(path, httpMethod, matcher)) {
                return route;
            }
        }
        return fallbackRoute;
    }

    private static RouteSet buildRoutes(ApiRateLimitProperties props) {
        List<RouteConfig> configs = new ArrayList<>();
        if (props.getRoutes() != null) {
            for (RouteProperties routeProps : props.getRoutes()) {
                if (routeProps == null) {
                    continue;
                }
                configs.add(toRouteConfig(routeProps, props));
            }
        }
        RouteConfig fallback = createFallbackRoute(props);
        return new RouteSet(Collections.unmodifiableList(configs), fallback);
    }

    private static RouteConfig createFallbackRoute(ApiRateLimitProperties props) {
        return toRouteConfig(new RouteProperties(), props, "default", true);
    }

    private static RouteConfig toRouteConfig(RouteProperties routeProps, ApiRateLimitProperties props) {
        String name = routeProps.getName();
        boolean enabled = routeProps.getEnabled() == null || routeProps.getEnabled();
        return toRouteConfig(routeProps, props, name, enabled);
    }

    private static RouteConfig toRouteConfig(RouteProperties routeProps,
                                             ApiRateLimitProperties props,
                                             String overrideName,
                                             boolean enabled) {
        String pattern = StringUtils.hasText(routeProps.getPattern()) ? routeProps.getPattern() : "/**";
        String name = StringUtils.hasText(overrideName) ? overrideName : pattern;
        Set<HttpMethod> methods = new HashSet<>();
        if (routeProps.getMethods() != null) {
            methods.addAll(routeProps.getMethods());
        }
        Duration routeWindow = routeProps.getWindow();
        LimitDefinition global = buildLimit(props.getGlobal(), routeProps.getGlobal(), routeWindow, props.getDefaultWindow());
        LimitDefinition perIp = buildLimit(props.getPerIp(), routeProps.getPerIp(), routeWindow, props.getDefaultWindow());
        LimitDefinition perUser = buildLimit(props.getPerUser(), routeProps.getPerUser(), routeWindow, props.getDefaultWindow());
        return new RouteConfig(name, pattern, Collections.unmodifiableSet(methods), global, perIp, perUser, enabled);
    }

    private static LimitDefinition buildLimit(LimitProperties defaults,
                                              LimitProperties override,
                                              Duration routeWindow,
                                              Duration defaultWindow) {
        LimitProperties base = defaults == null ? new LimitProperties() : defaults;
        boolean enabled = base.isEnabled();
        int limit = Math.max(base.getLimit(), 0);
        Duration window = firstNonNull(base.getWindow(), routeWindow, defaultWindow, DEFAULT_WINDOW);

        if (override != null) {
            enabled = override.isEnabled();
            limit = Math.max(override.getLimit(), 0);
            window = firstNonNull(override.getWindow(), routeWindow, defaultWindow, DEFAULT_WINDOW);
        }

        return new LimitDefinition(enabled, limit, window);
    }

    private static Duration firstNonNull(Duration... candidates) {
        for (Duration candidate : candidates) {
            if (candidate != null && !candidate.isZero() && !candidate.isNegative()) {
                return candidate;
            }
        }
        return DEFAULT_WINDOW;
    }

    private static String routeKey(String routeName, String scope) {
        return routeName + "|" + scope;
    }

    private static String normalizeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static Instant alignWindowStart(Instant now, Duration window) {
        long windowMillis = Math.max(1, window.toMillis());
        long epochMillis = now.toEpochMilli();
        long aligned = epochMillis - (epochMillis % windowMillis);
        return Instant.ofEpochMilli(aligned);
    }

    private record RouteSet(List<RouteConfig> routes, RouteConfig fallback) {}

    private record RouteConfig(String name,
                               String pattern,
                               Set<HttpMethod> methods,
                               LimitDefinition global,
                               LimitDefinition perIp,
                               LimitDefinition perUser,
                               boolean enabled) {
        private boolean matches(String path, HttpMethod method, AntPathMatcher matcher) {
            if (!enabled) {
                return false;
            }
            if (!methods.isEmpty()) {
                HttpMethod methodToMatch = method == null ? HttpMethod.GET : method;
                if (!methods.contains(methodToMatch)) {
                    return false;
                }
            }
            return matcher.match(pattern, path);
        }
    }

    private record LimitDefinition(boolean enabled, int limit, Duration window) {
        private boolean active() {
            return enabled && limit > 0 && window != null && !window.isZero() && !window.isNegative();
        }
    }

    private static final class WindowState {
        private Instant windowStart;
        private final Duration window;
        private int count;

        private WindowState(Instant windowStart, Duration window) {
            this.windowStart = windowStart;
            this.window = window;
            this.count = 0;
        }

        private boolean isExpired(Instant now) {
            return now.isAfter(windowStart.plus(window));
        }

        private int count() {
            return count;
        }

        private Duration window() {
            return window;
        }

        private void increment(int delta) {
            this.count += delta;
        }
    }
}
