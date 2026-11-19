# Usado por mvnw.cmd: garante a distribuição do Maven e imprime o MAVEN_HOME.
$ErrorActionPreference = 'Stop'
$props = Join-Path $PSScriptRoot 'maven-wrapper.properties'
$url = (Get-Content $props | Where-Object { $_ -match '^distributionUrl=' }) -replace '^distributionUrl=', ''
$distName = Split-Path $url -Leaf
$distDir = $distName -replace '-bin\..*$', ''
$userHome = if ($env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME } else { Join-Path $env:USERPROFILE '.m2' }
$mavenHome = Join-Path $userHome "wrapper\dists\$distDir"

if (-not (Test-Path (Join-Path $mavenHome 'bin\mvn.cmd'))) {
    [Console]::Error.WriteLine("Baixando $url")
    $tmp = Join-Path ([IO.Path]::GetTempPath()) ([Guid]::NewGuid())
    New-Item -ItemType Directory $tmp | Out-Null
    try {
        $file = Join-Path $tmp $distName
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $file
        if ($distName -like '*.zip') { Expand-Archive $file -DestinationPath $tmp } else { tar -xzf $file -C $tmp }
        New-Item -ItemType Directory -Force (Split-Path $mavenHome) | Out-Null
        if (-not (Test-Path $mavenHome)) { Move-Item (Join-Path $tmp $distDir) $mavenHome }
    } finally {
        Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
    }
}
Write-Output $mavenHome
