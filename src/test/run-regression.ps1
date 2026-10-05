param(
    [string]$ServletJar = "$env:USERPROFILE\.m2\repository\jakarta\servlet\jakarta.servlet-api\6.0.0\jakarta.servlet-api-6.0.0.jar",
    [string]$H2Jar = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
foreach ($dependency in @($ServletJar, $H2Jar)) {
    if (-not (Test-Path -LiteralPath $dependency)) { throw "Dependency not found: $dependency. Supply -ServletJar and -H2Jar paths." }
}
$testOutput = Join-Path ([IO.Path]::GetTempPath()) ('edutrack-crud-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $testOutput | Out-Null
$classes = Join-Path $testOutput 'classes'
New-Item -ItemType Directory -Path $classes | Out-Null
@'
db.driver=org.h2.Driver
db.url=jdbc:h2:mem:rolecrud;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE
db.username=sa
db.password=
'@ | Set-Content -Encoding ASCII (Join-Path $classes 'db.properties')
$sources = (Get-ChildItem (Join-Path $projectRoot 'src\main\java'), (Join-Path $projectRoot 'src\test\java') -Recurse -Filter '*.java').FullName
& javac -encoding UTF-8 --release 21 -cp $ServletJar -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
& java -cp "$classes;$ServletJar;$H2Jar" com.edutrack.servlet.RoleCrudRegression $projectRoot
if ($LASTEXITCODE -ne 0) { throw 'Regression checks failed.' }
Write-Host "Verification output: $testOutput"
