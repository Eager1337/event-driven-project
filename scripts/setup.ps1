$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Install and start Docker Desktop first.' }
function New-LocalPassword {
    $bytes = New-Object byte[] 24
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return (($bytes | ForEach-Object { $_.ToString('x2') }) -join '')
}
if (-not (Test-Path '.env')) {
    @("DB_PASSWORD=$(New-LocalPassword)", "MYSQL_ROOT_PASSWORD=$(New-LocalPassword)") | Set-Content -Encoding ASCII '.env'
}
if (-not (Test-Path 'config.properties')) {
    $line = Get-Content '.env' | Where-Object { $_ -match '^DB_PASSWORD=' } | Select-Object -First 1
    if (-not $line) { throw '.env must contain DB_PASSWORD.' }
    $password = $line.Substring('DB_PASSWORD='.Length)
    @('db.url=jdbc:mysql://127.0.0.1:3307/marketflow?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000', 'db.user=marketflow', "db.password=$password") | Set-Content -Encoding ASCII 'config.properties'
}
docker compose up -d --wait --wait-timeout 240
if ($LASTEXITCODE -ne 0) { throw 'MySQL did not become ready. See docker compose logs mysql.' }
Write-Host 'Database ready. Run scripts\run.bat to build and open MarketFlow.'