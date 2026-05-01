
 $sha = "45f67fbacfe1bf165078115d01f741ca29a4b4f5"
 
 $body = "{`"action`":`"opened`",`"number`":1,`"pull_request`":{`"title`":`"Test PR Day 19`",`"head`":{`"ref`":`"feature/ai-review-test`",`"sha`":`"$sha`"},`"base`":{`"ref`":`"main`",`"sha`":`"87dc36c08702103f52db43e113dada0021babb01`"}},`"repository`":{`"id`":1,`"name`":`"web-servers`",`"full_name`":`"Nikhil-keshri2213/web-servers`",`"private`":false},`"sender`":{`"login`":`"Nikhil-keshri2213`",`"id`":1}}"
 
 [System.IO.File]::WriteAllText("$PWD\payload.json", $body, [System.Text.UTF8Encoding]::new($false))
 
 $secret = 'my-webhook-secret'
 $hmac = New-Object System.Security.Cryptography.HMACSHA256
 $hmac.Key = [System.Text.Encoding]::UTF8.GetBytes($secret)
 $bytes = [System.IO.File]::ReadAllBytes("$PWD\payload.json")
 $hash = $hmac.ComputeHash($bytes)
 $sig = 'sha256=' + [BitConverter]::ToString($hash).Replace('-','').ToLower()
 
 curl.exe -X POST http://localhost:8081/webhook/github `
   -H "Content-Type: application/json" `
   -H "X-GitHub-Event: pull_request" `
   -H "X-Hub-Signature-256: $sig" `
   --data-binary "@payload.json"
