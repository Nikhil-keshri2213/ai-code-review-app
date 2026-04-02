# test-webhook.ps1

# CONFIG - change these
$secret      = "my-webhook-secret"
$webhookUrl  = "http://localhost:8081/webhook/github"
$repoName    = "Nikhil-keshri2213/web-servers"
$prNumber    = 1
$headSha     = "45f67fbacfe1bf165078115d01f741ca29a4b4f5"
$headRef     = "feature/ai-review-test"
$githubUser  = "Nikhil-keshri2213"

# Build payload
$payload = @"
{
  "action": "opened",
  "number": $prNumber,
  "pull_request": {
    "number": $prNumber,
    "title": "Test PR for code review",
    "state": "open",
    "head": {
      "sha": "$headSha",
      "ref": "$headRef"
    },
    "base": {
      "ref": "main"
    },
    "user": {
      "login": "$githubUser"
    }
  },
  "repository": {
    "full_name": "$repoName",
    "name": "$($repoName.Split('/')[1])",
    "private": false
  }
}
"@

# Generate HMAC-SHA256 signature
$hmac      = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key  = [System.Text.Encoding]::UTF8.GetBytes($secret)
$hashBytes = $hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($payload))
$signature = "sha256=" + ([BitConverter]::ToString($hashBytes) -replace "-", "").ToLower()

Write-Host ""
Write-Host "Payload:" -ForegroundColor Cyan
Write-Host $payload

Write-Host ""
Write-Host "Signature: $signature" -ForegroundColor Yellow

# Send request
Write-Host ""
Write-Host "Sending webhook to $webhookUrl..." -ForegroundColor Green

try {
    $response = Invoke-WebRequest -Uri $webhookUrl `
        -Method POST `
        -Headers @{
            "Content-Type"        = "application/json"
            "X-GitHub-Event"      = "pull_request"
            "X-Hub-Signature-256" = $signature
        } `
        -Body $payload

    Write-Host ""
    Write-Host "Response: $($response.StatusCode) - $($response.Content)" -ForegroundColor Green

} catch {
    Write-Host ""
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}