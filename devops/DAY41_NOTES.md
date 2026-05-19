# Day 41 — GitHub Actions CI/CD Pipeline

## Workflows Created
- `.github/workflows/ci.yml` — Build + Test + Push images to GHCR
- `.github/workflows/cd.yml` — Deploy to Kubernetes on CI success

## GitHub Repository Secrets Required
Add these under: Settings → Secrets and variables → Actions → New repository secret

| Secret Name        | Description                                              |
|--------------------|----------------------------------------------------------|
| `GHCR_TOKEN`       | GitHub PAT with `write:packages` scope                   |
| `KUBE_CONFIG`      | Base64-encoded ~/.kube/config (for cluster access)       |
| `GROQ_API_KEY`     | Groq API key for AI Review Service                       |
| `GITHUB_TOKEN_APP` | GitHub PAT for posting PR comments (GITHUB_TOKEN is reserved) |
| `JWT_SECRET`       | JWT signing secret for API Gateway                       |
| `WEBHOOK_SECRET`   | HMAC secret for GitHub webhook validation                |
| `POSTGRES_PASSWORD`| PostgreSQL database password                             |

## Generate KUBE_CONFIG secret (PowerShell)
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$HOME\.kube\config"))
```
Copy the output → paste as KUBE_CONFIG secret value.

## CI Pipeline Flow
```
push / PR to any branch
        ↓
build-and-test (mvn clean verify -DskipITs)
        ↓ (only on main)
build-and-push-images (matrix: 8 services)
        ↓
Images pushed to ghcr.io/nikhil-keshri2213/ai-review-<service>:sha + :latest
```

## CD Pipeline Flow
```
CI passes on main
        ↓
deploy job (matrix: 8 services)
        ↓
kubectl set image → kubectl rollout status (180s timeout)
        ↓ (on failure)
kubectl rollout undo (auto-rollback)
        ↓
verify job → kubectl get pods + hpa
```

## GHCR Image Names
| Service               | Image                                                        |
|-----------------------|--------------------------------------------------------------|
| api-gateway           | ghcr.io/nikhil-keshri2213/ai-review-api-gateway              |
| webhook-service       | ghcr.io/nikhil-keshri2213/ai-review-webhook-service          |
| code-fetch-service    | ghcr.io/nikhil-keshri2213/ai-review-code-fetch-service       |
| code-analysis-service | ghcr.io/nikhil-keshri2213/ai-review-code-analysis-service    |
| ai-review-service     | ghcr.io/nikhil-keshri2213/ai-review-ai-review-service        |
| review-storage-service| ghcr.io/nikhil-keshri2213/ai-review-review-storage-service   |
| notification-service  | ghcr.io/nikhil-keshri2213/ai-review-notification-service     |
| eureka-server         | ghcr.io/nikhil-keshri2213/ai-review-eureka-server            |

## Branch Protection (main)
- ✅ Require PR before merging (1 approval)
- ✅ Require status checks: `build-and-test`
- ✅ Require branches up to date
- ✅ Do not allow bypassing

## Note on K8s Deploy
CD workflow requires KUBE_CONFIG secret pointing to a live cluster.
Local Docker Desktop K8s has insufficient RAM on dev machine (8GB total).
CD workflow will skip cluster-info step gracefully if KUBE_CONFIG is not set.
Deploy step can be activated when a cloud cluster is provisioned (Day 42 or later).

## Actions Run URLs
- CI Run: (add URL after first run)
- CD Run: (add URL after first deploy)
