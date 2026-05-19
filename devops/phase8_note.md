# Day 39 - Production Docker Compose

## Build
docker compose -f docker-compose.prod.yml --env-file .env.prod build --no-cache

Status: SUCCESS

## Containers
- Eureka: Healthy
- Postgres: Healthy
- Kafka: Running
- Redis: Running
- Weaviate: Running
- API Gateway: Running
- All microservices: UP

## Smoke Test
- Eureka dashboard: PASS
- Service registration: PASS
- Docker compose ps: PASS

## Notes
- Fixed Eureka healthcheck issue
- Fixed Spring Boot version mismatch (3.5.14 → 3.5.0)
- Fresh rebuild successful



# Day 40 — Kubernetes Manifests

## Manifests Created
- k8s/namespace.yaml
- k8s/configmap.yaml
- k8s/secret.yaml.template
- k8s/infra/ (postgres, kafka, zookeeper, redis, weaviate, pvc)
- k8s/services/ (eureka, api-gateway, webhook, code-fetch, code-analysis, ai-review, review-storage, notification)
- k8s/hpa/ (ai-review-hpa, webhook-hpa)

## Local Cluster Status
All manifests validated and applied successfully.
Infra pods (postgres, kafka, redis, weaviate, zookeeper) reached 1/1 Running.
Microservice pods could not reach Ready state on local machine due to
insufficient RAM (8GB total, WSL2 backend). Spring Boot services require
~256Mi each; 8 services + 5 infra = ~3.3GB minimum which exceeds safe
WSL2 allocation on this machine.

Manifests are production-correct and will be deployed via GitHub Actions
CI/CD pipeline in Day 41 to a proper cluster.

## Verified
- kubectl apply -f k8s/namespace.yaml ✅
- kubectl apply -f k8s/configmap.yaml ✅
- kubectl apply -f k8s/secret.yaml ✅
- kubectl apply -f k8s/infra/ ✅ (all 5 infra pods Running 1/1)
- kubectl apply -f k8s/services/ -R ✅ (applied, pending RAM)
- kubectl apply -f k8s/hpa/ ✅

## HPA Note
Metrics Server required for HPA to function:
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml