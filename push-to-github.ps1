param(
    [string]$RemoteName = "origin",
    [string]$Branch = "",
    [switch]$UseSshRemote,
    [switch]$LoginIfNeeded,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Write-Info {
    param([string]$Message)
    Write-Host $Message
}

function Get-CurrentBranch {
    $currentBranch = (& git branch --show-current).Trim()
    if ([string]::IsNullOrWhiteSpace($currentBranch)) {
        return "main"
    }

    return $currentBranch
}

function Convert-HttpsGitHubUrlToSsh {
    param([string]$Url)

    if ($Url -match '^https://github\.com/(?<owner>[^/]+)/(?<repo>[^/]+?)(?:\.git)?$') {
        return "git@github.com:$($Matches.owner)/$($Matches.repo).git"
    }

    return $null
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "git is required but was not found on PATH."
}

if (-not $Branch) {
    $Branch = Get-CurrentBranch
}

if ($UseSshRemote) {
    $remoteUrl = (& git remote get-url $RemoteName).Trim()
    $sshUrl = Convert-HttpsGitHubUrlToSsh -Url $remoteUrl

    if ($null -ne $sshUrl -and $sshUrl -ne $remoteUrl) {
        Write-Info "Updating $RemoteName to SSH: $sshUrl"
        if (-not $DryRun) {
            & git remote set-url $RemoteName $sshUrl
        }
    } else {
        Write-Info "Remote $RemoteName is not a GitHub HTTPS URL. Leaving it unchanged."
    }
}

if (-not $DryRun) {
    $ghCommand = Get-Command gh -ErrorAction SilentlyContinue
    if ($null -ne $ghCommand) {
        & gh auth status -h github.com *> $null
        if ($LASTEXITCODE -ne 0) {
            if ($LoginIfNeeded) {
                Write-Info "GitHub CLI auth is invalid. Starting interactive login."
                & gh auth login -h github.com --web -s repo
                if ($LASTEXITCODE -ne 0) {
                    throw "gh auth login failed."
                }
            } else {
                throw "GitHub CLI auth is invalid. Run 'gh auth login -h github.com --web -s repo' and rerun this script, or pass -LoginIfNeeded."
            }
        }
    } else {
        Write-Info "gh was not found. Push will rely on whatever Git credential helper is configured."
    }
} else {
    Write-Info "Dry run: skipping GitHub CLI authentication checks."
}

Write-Info "Pushing branch '$Branch' to '$RemoteName'..."
if (-not $DryRun) {
    & git push -u $RemoteName $Branch
    if ($LASTEXITCODE -ne 0) {
        throw "git push failed."
    }
}

Write-Info "Push completed."
