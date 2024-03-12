# predictivetrading.net

## Decisions
- use [DAPR multi-app run](https://docs.dapr.io/developing-applications/local-development/multi-app-dapr-run/multi-app-overview/)

## Hints

- run local pypi server
  ```
  cd api
  python -m venv .venv
  . .venv/bin/activate
  pip install -r requirements.txt
  mkdir ~/packages  
  pypi-server run -p 8080 ~/packages &
  ```
- run applications
  ```
  dapr run -f dapr.yaml
  ```
- see local zipking traces:
  ```
  http://localhost:9411
  ```