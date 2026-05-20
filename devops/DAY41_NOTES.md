# Day 41 — GitHub Actions CI/CD Pipeline

## Repository Secrets Added
(values never stored here — see GitHub repo Settings → Secrets → Actions)

- [ ] GHCR_TOKEN          — PAT with write:packages scope
- [ ] KUBE_CONFIG          — base64-encoded ~/.kube/config
- [ ] GROQ_API_KEY         — Groq API key
- [ ] GITHUB_TOKEN_APP     — PAT with repo scope (GITHUB_TOKEN is reserved by Actions)
- [ ] JWT_SECRET           — JWT signing secret
- [ ] WEBHOOK_SECRET       — GitHub webhook secret
- [ ] POSTGRES_PASSWORD    — Postgres password

## Generate KUBE_CONFIG value (PowerShell)
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$HOME\.kube\config"))
```

## Workflow Files
- `.github/workflows/ci.yml`  — build + test + push images on push/PR; image push only on main
- `.github/workflows/cd.yml`  — deploy to K8s after CI passes on main

## Branch Protection Rules (main)
- Require PR before merging (1 approval)
- Require status checks: build-and-test
- Require branches up to date
- Do not allow bypassing

## CI/CD Run URLs
(fill in after pipeline runs)
- CI run:
- CD run:

## Verification
- [ ] build-and-test job green
- [ ] build-and-push-images pushed 8 images to ghcr.io
- [ ] CD triggered after merge to main
- [ ] All kubectl rollout statuses green
- [ ] Images visible at GitHub profile → Packages tab
