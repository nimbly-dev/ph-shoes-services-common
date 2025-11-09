# ph-shoes-services-common
Contains Common Classes used in the PH-SHOES project

## API Rate Limiting

`ph-shoes-services-common-core` now ships with an auto-configured Spring MVC interceptor that applies
API-level rate limiting so downstream services can protect endpoints that trigger SES email sends.

Add the module as a dependency and configure the guardrails via `phshoes.api.rate-limit.*`
properties, for example:

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
