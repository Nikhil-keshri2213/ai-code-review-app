# run-service.ps1
param(
    [Parameter(Mandatory=$true)]
    [string]$ServiceName
)

# Load root .env file
if (Test-Path .\.env) {
    Get-Content .\.env | ForEach-Object {
        if ($_ -match '^\s*([^#=][^=]*)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "Loaded: $key"
        }
    }
    Write-Host "✅ .env loaded successfully`n" -ForegroundColor Green
} else {
    Write-Host "❌ .env file not found in root!" -ForegroundColor Red
    exit 1
}

# Run the service
Set-Location $ServiceName
mvn spring-boot:run
Set-Location ..