# predictivetrading.net

## Decisions
- use [DAPR multi-app run](https://docs.dapr.io/developing-applications/local-development/multi-app-dapr-run/multi-app-overview/)
- use [devcontainers/vscode](https://containers.dev/guide/dockerfile) as main dev tool

## Hints

- run app stack
  ```
  dapr run -f dapr.yaml
  ```
- see local zipking traces:
  ```
  http://localhost:9411
  ```