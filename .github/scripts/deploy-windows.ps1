[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$WebServiceName,

    [Parameter(Mandatory = $true)]
    [string]$WebJarPath,

    [Parameter(Mandatory = $true)]
    [string]$EtlServiceName,

    [Parameter(Mandatory = $true)]
    [string]$EtlJarPath,

    [Parameter(Mandatory = $true)]
    [string]$WebHealthUrl,

    [Parameter(Mandatory = $true)]
    [string]$EtlHealthUrl,

    [Parameter(Mandatory = $true)]
    [string]$ExpectedProfile,

    [Parameter(Mandatory = $true)]
    [string]$DeployRoot,

    [int]$ExpectedWebPort = 0,

    [int]$ExpectedEtlPort = 0,

    [int]$HealthAttempts = 36,

    [int]$HealthDelaySeconds = 30,

    [switch]$Rollback
)

$ErrorActionPreference = 'Stop'

function Get-NssmValue {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,

        [Parameter(Mandatory = $true)]
        [string]$Key
    )

    $value = & nssm get $Name $Key 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel ler '$Key' do servico '$Name' via nssm."
    }

    return ($value | Out-String).Trim()
}

function Get-ServiceEnvironment {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $environment = @{}
    $raw = Get-NssmValue -Name $Name -Key 'AppEnvironmentExtra'

    foreach ($line in ($raw -split "`r?`n")) {
        if ($line -match '^(?<key>[^=]+)=(?<value>.*)$') {
            $environment[$matches['key'].Trim()] = $matches['value']
        }
    }

    return $environment
}

function Get-EffectiveEnvironmentValue {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,

        [Parameter(Mandatory = $true)]
        [hashtable]$ServiceEnvironment
    )

    if ($ServiceEnvironment.ContainsKey($Name) -and
        -not [string]::IsNullOrWhiteSpace($ServiceEnvironment[$Name])) {
        return $ServiceEnvironment[$Name]
    }

    $machineValue = [Environment]::GetEnvironmentVariable($Name, 'Machine')
    if (-not [string]::IsNullOrWhiteSpace($machineValue)) {
        return $machineValue
    }

    $userValue = [Environment]::GetEnvironmentVariable($Name, 'User')
    if (-not [string]::IsNullOrWhiteSpace($userValue)) {
        return $userValue
    }

    return $null
}

function Get-NssmStatus {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    return (& nssm status $Name | Out-String).Trim()
}

function Get-ServiceSnapshot {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,

        [Parameter(Mandatory = $true)]
        [string]$JarPath
    )

    $application = Get-NssmValue -Name $Name -Key 'Application'
    $appParameters = Get-NssmValue -Name $Name -Key 'AppParameters'
    $serviceEnvironment = Get-ServiceEnvironment -Name $Name
    $stdoutPath = Get-NssmValue -Name $Name -Key 'AppStdout'
    $stderrPath = Get-NssmValue -Name $Name -Key 'AppStderr'
    $referencesExpectedJar =
        $application -eq $JarPath -or
        $appParameters -like "*$JarPath*" -or
        $appParameters -like "*$(Split-Path -Leaf $JarPath)*"

    if (-not $referencesExpectedJar) {
        throw "O servico '$Name' nao aponta para o jar esperado. Application='$application' AppParameters='$appParameters' JarEsperado='$JarPath'"
    }

    $configuredProfile = Get-EffectiveEnvironmentValue `
        -Name 'SPRING_PROFILES_ACTIVE' `
        -ServiceEnvironment $serviceEnvironment
    $profileInParameters =
        $appParameters -like "*--spring.profiles.active=$ExpectedProfile*" -or
        $appParameters -like "*-Dspring.profiles.active=$ExpectedProfile*"

    if (($configuredProfile -ne $ExpectedProfile) -and (-not $profileInParameters)) {
        throw "O servico '$Name' nao esta configurado para o profile esperado '$ExpectedProfile'."
    }

    return @{
        Name = $Name
        JarPath = $JarPath
        Stdout = $stdoutPath
        Stderr = $stderrPath
        InitiallyRunning = (Get-NssmStatus -Name $Name) -match 'SERVICE_RUNNING|START_PENDING'
    }
}

function Wait-NssmStatus {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,

        [Parameter(Mandatory = $true)]
        [string]$ExpectedPattern,

        [int]$Attempts = 30
    )

    for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
        $status = Get-NssmStatus -Name $Name
        if ($status -match $ExpectedPattern) {
            return
        }

        Start-Sleep -Seconds 1
    }

    throw "O servico '$Name' nao atingiu o estado esperado '$ExpectedPattern'. Estado atual: '$(Get-NssmStatus -Name $Name)'."
}

function Stop-ServiceIfRunning {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $status = Get-NssmStatus -Name $Name
    if ($status -match 'SERVICE_RUNNING|START_PENDING') {
        & nssm stop $Name | Out-Host
        if ($LASTEXITCODE -ne 0) {
            throw "Nao foi possivel parar o servico '$Name'."
        }

        Wait-NssmStatus -Name $Name -ExpectedPattern 'SERVICE_STOPPED'
    }
}

function Start-ServiceAndCaptureLogs {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Snapshot
    )

    & nssm start $($Snapshot.Name) | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Nao foi possivel iniciar o servico '$($Snapshot.Name)'."
    }

    Wait-NssmStatus -Name $Snapshot.Name -ExpectedPattern 'SERVICE_RUNNING'
    return $Snapshot
}

function Show-LogTail {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Label,

        [string]$Path
    )

    if ([string]::IsNullOrWhiteSpace($Path) -or $Path -eq 'NUL') {
        return
    }

    if (Test-Path -LiteralPath $Path) {
        Write-Host "===== $Label ($Path) ====="
        Get-Content -LiteralPath $Path -Tail 50
    }
}

function Test-Port {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,

        [Parameter(Mandatory = $true)]
        [string]$Label
    )

    if ($Port -le 0) {
        return
    }

    $result = Test-NetConnection -ComputerName '127.0.0.1' -Port $Port -WarningAction SilentlyContinue
    if (-not $result.TcpTestSucceeded) {
        throw "A porta esperada do $Label nao esta aceitando conexoes: $Port."
    }
}

function Assert-NssmRunning {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $status = Get-NssmStatus -Name $Name
    if ($status -notmatch 'SERVICE_RUNNING') {
        throw "O servico '$Name' nao permaneceu em execucao. Estado atual: '$status'."
    }
}

function Assert-PromotedArtifacts {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FinalWebJarPath,

        [Parameter(Mandatory = $true)]
        [string]$FinalEtlJarPath,

        [Parameter(Mandatory = $true)]
        [string]$StagedWebJarPath,

        [Parameter(Mandatory = $true)]
        [string]$StagedEtlJarPath
    )

    foreach ($finalPath in @($FinalWebJarPath, $FinalEtlJarPath)) {
        if (-not (Test-Path -LiteralPath $finalPath -PathType Leaf)) {
            throw "O artifact final nao foi encontrado em '$finalPath'."
        }
    }

    foreach ($stagedPath in @($StagedWebJarPath, $StagedEtlJarPath)) {
        if (Test-Path -LiteralPath $stagedPath) {
            throw "O artifact temporario nao foi removido de '$stagedPath'."
        }
    }
}

function Wait-ForHealth {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Label,

        [Parameter(Mandatory = $true)]
        [string]$Url
    )

    for ($attempt = 1; $attempt -le $HealthAttempts; $attempt++) {
        try {
            Write-Host "Health check $Label tentativa $attempt/$HealthAttempts em $Url"
            $response = Invoke-RestMethod -Uri $Url -TimeoutSec 10
            Write-Host "Status $Label recebido: $($response.status)"

            if ($response.status -eq 'UP') {
                return
            }
        } catch {
            Write-Host "Health check $Label falhou: $($_.Exception.Message)"
        }

        if ($attempt -lt $HealthAttempts) {
            Start-Sleep -Seconds $HealthDelaySeconds
        }
    }

    throw "$Label nao ficou saudavel em '$Url'."
}

function New-ReleaseBackup {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$WebSnapshot,

        [Parameter(Mandatory = $true)]
        [hashtable]$EtlSnapshot
    )

    $backupRoot = Join-Path $DeployRoot 'backups'
    New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
    $backupPath = Join-Path $backupRoot (Get-Date -Format 'yyyyMMdd-HHmmssfff')
    New-Item -ItemType Directory -Path $backupPath -Force | Out-Null

    Copy-Item -LiteralPath $WebJarPath -Destination (Join-Path $backupPath 'Web.jar') -Force
    Copy-Item -LiteralPath $EtlJarPath -Destination (Join-Path $backupPath 'Etl.jar') -Force

    $metadata = @{
        createdAt = (Get-Date).ToUniversalTime().ToString('o')
        webService = $WebSnapshot.Name
        etlService = $EtlSnapshot.Name
        webJar = $WebJarPath
        etlJar = $EtlJarPath
    } | ConvertTo-Json

    Set-Content -LiteralPath (Join-Path $backupPath 'metadata.json') -Value $metadata -Encoding UTF8
    Set-Content -LiteralPath (Join-Path $backupRoot 'latest.txt') -Value $backupPath -Encoding UTF8 -NoNewline

    return $backupPath
}

function Get-LatestBackup {
    $latestFile = Join-Path (Join-Path $DeployRoot 'backups') 'latest.txt'
    if (-not (Test-Path -LiteralPath $latestFile)) {
        throw "Nenhum backup anterior foi encontrado em '$latestFile'."
    }

    $backupPath = (Get-Content -LiteralPath $latestFile -Raw).Trim()
    if ([string]::IsNullOrWhiteSpace($backupPath) -or
        -not (Test-Path -LiteralPath (Join-Path $backupPath 'Web.jar')) -or
        -not (Test-Path -LiteralPath (Join-Path $backupPath 'Etl.jar'))) {
        throw "O backup registrado em '$backupPath' esta incompleto."
    }

    return $backupPath
}

function Restore-Backup {
    param(
        [Parameter(Mandatory = $true)]
        [string]$BackupPath,

        [Parameter(Mandatory = $true)]
        [hashtable]$WebSnapshot,

        [Parameter(Mandatory = $true)]
        [hashtable]$EtlSnapshot
    )

    Write-Host "Restaurando backup '$BackupPath'."
    Stop-ServiceIfRunning -Name $WebServiceName
    Stop-ServiceIfRunning -Name $EtlServiceName

    Copy-Item -LiteralPath (Join-Path $BackupPath 'Web.jar') -Destination $WebJarPath -Force
    Copy-Item -LiteralPath (Join-Path $BackupPath 'Etl.jar') -Destination $EtlJarPath -Force

    Start-ServiceAndCaptureLogs -Snapshot $EtlSnapshot | Out-Null
    Start-ServiceAndCaptureLogs -Snapshot $WebSnapshot | Out-Null
    Test-Port -Port $ExpectedEtlPort -Label 'ETL restaurado'
    Test-Port -Port $ExpectedWebPort -Label 'Web restaurado'
    Wait-ForHealth -Label 'ETL restaurado' -Url $EtlHealthUrl
    Wait-ForHealth -Label 'Web restaurado' -Url $WebHealthUrl
}

if (-not (Get-Command nssm -ErrorAction SilentlyContinue)) {
    throw 'nssm nao esta disponivel no PATH do servidor.'
}

if ($Rollback) {
    $rollbackWebSnapshot = Get-ServiceSnapshot -Name $WebServiceName -JarPath $WebJarPath
    $rollbackEtlSnapshot = Get-ServiceSnapshot -Name $EtlServiceName -JarPath $EtlJarPath
    $latestBackup = Get-LatestBackup

    Restore-Backup `
        -BackupPath $latestBackup `
        -WebSnapshot $rollbackWebSnapshot `
        -EtlSnapshot $rollbackEtlSnapshot

    Write-Host 'Rollback do release concluido com health check valido.'
    Write-Host 'ROLLBACK_RESULT=SUCCESS'
    exit 0
}

$webStagedJarPath = "$WebJarPath.new"
$etlStagedJarPath = "$EtlJarPath.new"
$backupPath = $null
$webSnapshot = $null
$etlSnapshot = $null
$bootstrapDeployment = $false

try {
    $webJarExists = Test-Path -LiteralPath $WebJarPath -PathType Leaf
    $etlJarExists = Test-Path -LiteralPath $EtlJarPath -PathType Leaf

    if ($webJarExists -xor $etlJarExists) {
        throw "Estado inconsistente: os JARs atuais de Web e ETL devem existir juntos ou ambos devem estar ausentes. Web='$webJarExists' ETL='$etlJarExists'."
    }

    $bootstrapDeployment = -not $webJarExists

    if (-not (Test-Path -LiteralPath $webStagedJarPath)) {
        throw "Artifact do Web nao encontrado em '$webStagedJarPath'."
    }

    if (-not (Test-Path -LiteralPath $etlStagedJarPath)) {
        throw "Artifact do ETL nao encontrado em '$etlStagedJarPath'."
    }

    $webSnapshot = Get-ServiceSnapshot -Name $WebServiceName -JarPath $WebJarPath
    $etlSnapshot = Get-ServiceSnapshot -Name $EtlServiceName -JarPath $EtlJarPath

    if ($bootstrapDeployment) {
        Write-Host 'Primeiro deploy detectado: nenhum JAR anterior existe; o deploy seguira sem backup.'
    } else {
        $backupPath = New-ReleaseBackup -WebSnapshot $webSnapshot -EtlSnapshot $etlSnapshot
        Write-Host "Backup anterior criado em '$backupPath'."
    }

    Stop-ServiceIfRunning -Name $WebServiceName
    Stop-ServiceIfRunning -Name $EtlServiceName

    Copy-Item -LiteralPath $webStagedJarPath -Destination $WebJarPath -Force
    Remove-Item -LiteralPath $webStagedJarPath -Force
    Copy-Item -LiteralPath $etlStagedJarPath -Destination $EtlJarPath -Force
    Remove-Item -LiteralPath $etlStagedJarPath -Force

    Assert-PromotedArtifacts `
        -FinalWebJarPath $WebJarPath `
        -FinalEtlJarPath $EtlJarPath `
        -StagedWebJarPath $webStagedJarPath `
        -StagedEtlJarPath $etlStagedJarPath

    Start-ServiceAndCaptureLogs -Snapshot $etlSnapshot | Out-Null
    Start-ServiceAndCaptureLogs -Snapshot $webSnapshot | Out-Null
    Test-Port -Port $ExpectedEtlPort -Label 'ETL'
    Test-Port -Port $ExpectedWebPort -Label 'Web'
    Wait-ForHealth -Label 'ETL' -Url $EtlHealthUrl
    Wait-ForHealth -Label 'Web' -Url $WebHealthUrl

    Start-Sleep -Seconds 5
    Assert-NssmRunning -Name $EtlServiceName
    Assert-NssmRunning -Name $WebServiceName
    Test-Port -Port $ExpectedEtlPort -Label 'ETL apos estabilizacao'
    Test-Port -Port $ExpectedWebPort -Label 'Web apos estabilizacao'
    Wait-ForHealth -Label 'ETL apos estabilizacao' -Url $EtlHealthUrl
    Wait-ForHealth -Label 'Web apos estabilizacao' -Url $WebHealthUrl
    Assert-PromotedArtifacts `
        -FinalWebJarPath $WebJarPath `
        -FinalEtlJarPath $EtlJarPath `
        -StagedWebJarPath $webStagedJarPath `
        -StagedEtlJarPath $etlStagedJarPath

    Write-Host 'Deploy concluido com os dois health checks validos.'
    Write-Host 'DEPLOY_RESULT=SUCCESS'
} catch {
    $deploymentError = $_.Exception
    Write-Host "Falha no deploy: $($deploymentError.Message)"

    if ($null -ne $backupPath) {
        try {
            Restore-Backup `
                -BackupPath $backupPath `
                -WebSnapshot $webSnapshot `
                -EtlSnapshot $etlSnapshot
            Write-Host 'Rollback automatico do JAR concluido.'
        } catch {
            Write-Host "Falha no rollback automatico: $($_.Exception.Message)"
        }
    } elseif ($bootstrapDeployment) {
        try {
            Stop-ServiceIfRunning -Name $WebServiceName
            Stop-ServiceIfRunning -Name $EtlServiceName

            if (Test-Path -LiteralPath $WebJarPath) {
                Remove-Item -LiteralPath $WebJarPath -Force
            }

            if (Test-Path -LiteralPath $EtlJarPath) {
                Remove-Item -LiteralPath $EtlJarPath -Force
            }

            Write-Host 'Primeiro deploy falhou; os JARs finais criados nesta tentativa foram removidos.'
        } catch {
            Write-Host "Falha ao restaurar o estado anterior ao primeiro deploy: $($_.Exception.Message)"
        }
    }

    if ($null -ne $webSnapshot) {
        Show-LogTail -Label 'WEB STDOUT' -Path $webSnapshot.Stdout
        Show-LogTail -Label 'WEB STDERR' -Path $webSnapshot.Stderr
    }

    if ($null -ne $etlSnapshot) {
        Show-LogTail -Label 'ETL STDOUT' -Path $etlSnapshot.Stdout
        Show-LogTail -Label 'ETL STDERR' -Path $etlSnapshot.Stderr
    }

    throw $deploymentError
} finally {
    if (Test-Path -LiteralPath $webStagedJarPath) {
        Remove-Item -LiteralPath $webStagedJarPath -Force
    }

    if (Test-Path -LiteralPath $etlStagedJarPath) {
        Remove-Item -LiteralPath $etlStagedJarPath -Force
    }
}
