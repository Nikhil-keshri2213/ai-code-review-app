$body = '{"action":"opened","number":1,"pull_request":{"title":"Test PR Day 12","head":{"ref":"feature/ai-review-test","sha":"HEAD"},"base":{"ref":"main","sha":"base"}},"repository":{"id":1,"name":"web-servers","full_name":"Nikhil-keshri2213/web-servers","private":false},"sender":{"login":"Nikhil-keshri2213","id":1}}'
[System.IO.File]::WriteAllText("$PWD\payload.json", $body, [System.Text.UTF8Encoding]::new($false))

$secret = 'my-webhook-secret'
$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [System.Text.Encoding]::UTF8.GetBytes($secret)
$bytes = [System.IO.File]::ReadAllBytes("$PWD\payload.json")
$hash = $hmac.ComputeHash($bytes)
$sig = 'sha256=' + [BitConverter]::ToString($hash).Replace('-','').ToLower()
Write-Host "Signature: $sig"

curl.exe -X POST http://localhost:8081/webhook/github `
  -H "Content-Type: application/json" `
  -H "X-GitHub-Event: pull_request" `
  -H "X-Hub-Signature-256: $sig" `
  --data-binary "@payload.json"
