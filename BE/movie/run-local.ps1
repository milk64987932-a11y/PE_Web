param([switch]$Test, [switch]$Build)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (!(Test-Path -LiteralPath '.env.local')) { throw 'BE/movie/.env.local에 DB_USER와 DB_PW를 입력해 주세요.' }
Get-Content -LiteralPath '.env.local' | ForEach-Object {
    if ($_ -match '^(DB_USER|DB_PW|DB_URL|SERVER_PORT|JWT_SECRET|JWT_ACCESS_EXPIRATION)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
if ($Test) { & .\gradlew.bat test --no-daemon }
elseif ($Build) { & .\gradlew.bat build --no-daemon }
else { & .\gradlew.bat bootRun --no-daemon }
exit $LASTEXITCODE
