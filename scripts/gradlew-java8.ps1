param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $GradleArgs
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $scriptDir
$localEnvFile = Join-Path $rootDir '.local\java8.env'

function Read-LocalJava8Home {
    if (-not (Test-Path -LiteralPath $localEnvFile)) {
        return $null
    }

    foreach ($line in Get-Content -LiteralPath $localEnvFile) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0 -or $trimmed.StartsWith('#')) {
            continue
        }

        $parts = $trimmed.Split('=', 2)
        if ($parts.Length -eq 2 -and $parts[0].Trim() -eq 'JAVA8_HOME') {
            return $parts[1].Trim().Trim('"').Trim("'")
        }
    }

    return $null
}

function Test-Java8Home {
    param([string] $JavaHome)

    if ([string]::IsNullOrWhiteSpace($JavaHome)) {
        return $false
    }

    $javaExe = Join-Path $JavaHome 'bin\java.exe'
    $javacExe = Join-Path $JavaHome 'bin\javac.exe'
    if (-not (Test-Path -LiteralPath $javaExe) -or -not (Test-Path -LiteralPath $javacExe)) {
        return $false
    }

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $versionText = (& $javaExe -version 2>&1 | Out-String)
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    return $versionText -match 'version "1\.8\.'
}

function Add-CandidatePath {
    param(
        [System.Collections.Generic.List[string]] $Candidates,
        [string] $Path
    )

    if (-not [string]::IsNullOrWhiteSpace($Path) -and -not $Candidates.Contains($Path)) {
        $Candidates.Add($Path)
    }
}

$candidates = [System.Collections.Generic.List[string]]::new()
Add-CandidatePath $candidates $env:JAVA8_HOME
Add-CandidatePath $candidates (Read-LocalJava8Home)
Add-CandidatePath $candidates $env:JAVA_HOME

@(
    'C:\Program Files\Java\jdk1.8.0_181',
    'C:\Program Files\Java\jdk8',
    "$env:USERPROFILE\.jdks\temurin-8*",
    'C:\Program Files\Eclipse Adoptium\jdk-8*',
    'C:\Program Files\AdoptOpenJDK\jdk-8*',
    'C:\Program Files\Zulu\zulu-8*',
    'C:\Program Files\Microsoft\jdk-8*'
) | ForEach-Object {
    if ($_ -match '[*?\[\]]') {
        Get-ChildItem -Path $_ -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            Add-CandidatePath $candidates $_.FullName
        }
    } else {
        Add-CandidatePath $candidates $_
    }
}

$javaHome = $null
foreach ($candidate in $candidates) {
    if (Test-Java8Home $candidate) {
        $javaHome = (Resolve-Path -LiteralPath $candidate).Path
        break
    }
}

if ($javaHome -eq $null) {
    Write-Error @"
Could not find a Java 8 JDK.

Set JAVA8_HOME, set JAVA_HOME to a Java 8 JDK, or create .local\java8.env with:
JAVA8_HOME=C:\Path\To\jdk8
"@
    exit 1
}

$env:JAVA_HOME = $javaHome
$env:Path = (Join-Path $javaHome 'bin') + [System.IO.Path]::PathSeparator + $env:Path

$gradlew = Join-Path $rootDir 'gradlew.bat'
if (-not (Test-Path -LiteralPath $gradlew)) {
    Write-Error "Could not find Gradle Wrapper at $gradlew"
    exit 1
}

& $gradlew "-Dorg.gradle.java.home=$javaHome" @GradleArgs
exit $LASTEXITCODE
