# ph-shoes-starter-services

Shared Spring Boot starters used across PH-SHOES microservices.

Current modules:

- `ph-shoes-starter-services-common-core` – core/common logic (models, config, utilities, Dynamo migration helpers).
- `ph-shoes-starter-services-common-web` – web glue (rate-limiting interceptor, `/system/status` controller).
- `ph-shoes-starter-services-common-security` – shared security primitives (JWT helpers, filters, config fragments).

## API Rate Limiting

`ph-shoes-starter-services-common-web` ships with an auto-configured Spring MVC interceptor that applies
API-level rate limiting so downstream services can protect endpoints that trigger SES email sends.

Add the module as a dependency and configure the guardrails via `phshoes.api.rate-limit.*`
properties, for example:

```yaml
phshoes:
  api:
    base-path: /api/v1
```

```yaml
phshoes:
  api:
    rate-limit:
      enabled: true
      default-window: 24h
      global:
        limit: 2000
      per-ip:
        limit: 120
      per-user:
        limit: 60
      routes:
        - name: signup
          pattern: /api/v1/user-accounts/verify
          per-user:
            limit: 3
            window: 1h
```

You can override the `ApiRateLimiter` bean or the interceptor entirely if a service needs custom
behaviour, but every service gets the default guardrails automatically once the dependency is present.

## Service Status Endpoint

The web starter also provides a lightweight `/system/status` controller so each microservice
can expose a warm-up friendly JSON payload without re-implementing boilerplate.

Enable and customize it via `phshoes.status.*`:

```yaml
phshoes:
  status:
    enabled: true
    path: /system/status
    service-id: user-accounts
    display-name: User Accounts Service
    environment: ${APP_ENV:local}
    version: ${APP_VERSION:local}
    description: Handles account creation and verification flows.
    metadata:
      region: ap-southeast-1
      owner: accounts
```

Add optional `ServiceStatusContributor` beans if a service wants to check downstream
dependencies:

```java
@Bean
ServiceStatusContributor dynamoContributor(DynamoDbClient client) {
    return builder -> {
        boolean reachable = client != null; // call DescribeTable, etc.
        builder.dependency("dynamo", reachable ? ServiceState.UP : ServiceState.DOWN, "Accounts table");
    };
}
```

Don’t forget to keep the configured path public in your security rules (e.g. permit GET
`/system/status` alongside `/actuator/health`).

### OpenAPI visibility

Each consumer microservice is responsible for its own Springdoc/OpenAPI configuration; the starter
does not add any OpenAPI groups by default.
