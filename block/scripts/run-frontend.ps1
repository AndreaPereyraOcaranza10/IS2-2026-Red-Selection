$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$portableNode = "$projectRoot/.tools/node-v22.22.0-win-x64"
if (Test-Path "$portableNode/node.exe") { $env:PATH = "$portableNode;$env:PATH" }
if (-not (Get-Command npm.cmd -ErrorAction SilentlyContinue)) { throw 'Instalá Node 22.12+ y agregalo al PATH.' }
Push-Location "$projectRoot/frontend"
try {
    if (-not (Test-Path node_modules)) { npm.cmd ci; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE } }
    npm.cmd run dev
    exit $LASTEXITCODE
} finally { Pop-Location }
