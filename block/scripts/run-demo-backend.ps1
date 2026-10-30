param(
    [string]$UserEmail = 'demo@atlas.local',
    [string]$UserPassword = ''
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $UserPassword) { $UserPassword = 'Atlas' + [Guid]::NewGuid().ToString('N').Substring(0,16) + '9' }
$env:DEBUG = ''
$env:JWT_SECRET = [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
$env:DB_URL = 'jdbc:h2:mem:demo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1'
$env:DB_USER = 'sa'
$env:DB_PASSWORD = ''
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'create-drop'
$env:SPRING_FLYWAY_ENABLED = 'false'
$env:FRONTEND_URL = 'http://localhost:5173'
$env:DEMO_USER_EMAIL = $UserEmail
$env:DEMO_USER_PASSWORD = $UserPassword
Write-Host "DEMO TEMPORAL: los datos se pierden al detener este proceso."
Write-Host "Email: $UserEmail"
Write-Host "Contraseña de esta demostración: $UserPassword"
$maven = Get-Command mvn -ErrorAction SilentlyContinue
if ($maven) { $mavenPath = $maven.Source }
elseif (Test-Path "$projectRoot/backend/mvnw.cmd") { $mavenPath = "$projectRoot/backend/mvnw.cmd" }
else {
    $cached = Get-ChildItem "$env:USERPROFILE/.m2/wrapper/dists" -Filter mvn.cmd -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $cached) { throw 'Instalá Maven 3.9+ y agregalo al PATH.' }
    $mavenPath = $cached.FullName
}
Push-Location "$projectRoot/backend"
try {
    & $mavenPath spring-boot:run '-Dspring-boot.run.useTestClasspath=true' '-Dspring-boot.run.profiles=dev'
    exit $LASTEXITCODE
} finally { Pop-Location }
