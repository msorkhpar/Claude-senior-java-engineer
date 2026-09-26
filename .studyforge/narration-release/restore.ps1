<#
.SYNOPSIS
  Restore this corpus's narration clips from its release volumes, on Windows.

.DESCRIPTION
  Written by studyforge when the clips were packed. Regenerate, never edit.
  The PowerShell twin of restore.sh beside it: same release, same result.

    powershell -ExecutionPolicy Bypass -File .studyforge\narration-release\restore.ps1

  Narration is optional: the site is complete without it, and this script is
  only for a reader who wants the voice. It downloads the release's volumes,
  checks each against the SHA256SUMS this corpus committed beside this script,
  joins them, refuses them unless their files are exactly the clips named in
  clips.sha256 beside it, extracts those into a staging directory, checks each
  clip's digest, moves each into the directory this corpus's pages play it
  from, and deletes the downloaded volumes. The pages ask their first clip
  themselves, so nothing else is written. Running it again gives the same
  tree. A site built into another directory than this corpus's
  root has its own copies: build it again after restoring.

  In a clone whose origin is the repository on GitHub this needs no argument:
  the repository is read from that remote and the tag is the one the clips were
  packed under. -Tag, -Repo, -LocalDir and -KeepDownloads override them, as do
  the environment variables restore.sh reads.

  A PRIVATE repository's release assets are not served at the public download
  address; they come through the API by asset id. Set $env:GITHUB_TOKEN (or
  GH_TOKEN), or install gh and run gh auth login. The token is read from the
  environment and never printed.

  The repository defaults to a placeholder: this file is committed, and an
  account name does not belong in it. Requires PowerShell 5.1 or later.
#>
[CmdletBinding()]
param(
    [string] $Tag = $(if ($env:NARRATION_TAG) { $env:NARRATION_TAG } else { 'narration-1.0.0' }),
    [string] $Repo = $env:NARRATION_REPO,
    [string] $LocalDir = $env:NARRATION_LOCAL_DIR,
    [switch] $KeepDownloads = [bool] $env:NARRATION_KEEP_DOWNLOADS
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProgressPreference = 'SilentlyContinue'

$Here = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = Split-Path -Parent (Split-Path -Parent $Here)
$Work = Join-Path $Here 'download'
$Volume = 'narration.zip'
$Sums = 'SHA256SUMS'
$Api = if ($env:NARRATION_API_URL) { $env:NARRATION_API_URL } else { 'https://api.github.com' }
$Token = if ($env:GITHUB_TOKEN) { $env:GITHUB_TOKEN }
    elseif ($env:GH_TOKEN) { $env:GH_TOKEN }
    else { $null }

function Fail([string] $Message) {
    [Console]::Error.WriteLine("restore: $Message")
    exit 1
}

# The clone knows which repository it came from, so it is asked rather than
# told: no account name has to live in this file for the default to work.
function Get-RepoFromRemote {
    try { $url = & git -C $Root remote get-url origin 2>$null } catch { return $null }
    if (-not $url) { return $null }
    $url = ([string] $url).Trim() -replace '\.git$', ''
    if ($url -match 'github\.com[:/]([A-Za-z0-9._-]+/[A-Za-z0-9._-]+)$') { return $Matches[1] }
    return $null
}

if (-not $Repo) { $Repo = Get-RepoFromRemote }
$BaseUrl = if ($env:NARRATION_BASE_URL) { $env:NARRATION_BASE_URL } else {
    $owner = if ($Repo) { $Repo } else { 'OWNER/REPO' }
    "https://github.com/$owner/releases/download/$Tag"
}
$Parts = if ($LocalDir) { $LocalDir } else { $Work }
$UseGh = (-not $LocalDir) -and (-not $Token) -and $Repo -and (-not $env:NARRATION_BASE_URL) -and
    [bool] (Get-Command gh -ErrorAction SilentlyContinue)

$script:AssetIds = $null
function Get-AssetIds {
    if ($script:AssetIds) { return }
    $headers = @{ Authorization = "Bearer $Token"; Accept = 'application/vnd.github+json' }
    try {
        $release = Invoke-RestMethod -UseBasicParsing -Headers $headers `
            -Uri "$Api/repos/$Repo/releases/tags/$Tag"
    } catch {
        Fail "cannot read release $Tag of $Repo; check the repository, the tag and the token"
    }
    $map = @{}
    foreach ($asset in $release.assets) { $map[[string] $asset.name] = $asset.id }
    $script:AssetIds = $map
}

function Test-Present([string] $Path) {
    return (Test-Path -LiteralPath $Path) -and ((Get-Item -LiteralPath $Path).Length -gt 0)
}

function Get-Asset([string] $Name) {
    $dest = Join-Path $Parts $Name
    if (Test-Present $dest) { return $true }
    if ($LocalDir) { return $false }
    $partial = "$dest.partial"
    try {
        if ($Token -and $Repo) {
            Get-AssetIds
            if (-not $script:AssetIds.ContainsKey($Name)) { return $false }
            $id = $script:AssetIds[$Name]
            # The octet-stream Accept header is what returns the file rather
            # than its description.
            $headers = @{ Authorization = "Bearer $Token"; Accept = 'application/octet-stream' }
            Invoke-WebRequest -UseBasicParsing -Headers $headers `
                -Uri "$Api/repos/$Repo/releases/assets/$id" -OutFile $partial
        } elseif ($UseGh) {
            & gh release download $Tag --repo $Repo --pattern $Name --dir $Parts --clobber 2>$null |
                Out-Null
            if (Test-Present $dest) { return $true }
            Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/$Name" -OutFile $partial
        } else {
            Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/$Name" -OutFile $partial
        }
    } catch {
        Remove-Item -Force -LiteralPath $partial -ErrorAction SilentlyContinue
        return $false
    }
    Move-Item -Force -LiteralPath $partial -Destination $dest
    return (Test-Present $dest)
}

# The release is checked against what THIS corpus committed when it was packed,
# never only against itself: a checksum file from the same release proves the
# download intact, not that the release belongs here.
$keptSums = Join-Path $Here $Sums
$keptClips = Join-Path $Here 'clips.sha256'
if (-not ((Test-Present $keptSums) -and (Test-Present $keptClips))) {
    Fail 'this corpus carries no record of a packed release; nothing was fetched'
}

$sumsByName = @{}
foreach ($line in Get-Content -LiteralPath $keptSums) {
    if ($line -match '^([0-9a-f]{64})  (\S+)$') {
        $digest, $name = $Matches[1], $Matches[2]
        if ($name -notmatch "^$([regex]::Escape($Volume))\.\d{3}$") {
            Fail "$Sums names a file that is not a volume"
        }
        $sumsByName[$name] = $digest
    } elseif ($line.Trim()) {
        Fail "$Sums holds a line that names no volume"
    }
}
$names = @($sumsByName.Keys | Sort-Object)
if ($names.Count -eq 0) { Fail "$Sums names no volume" }

$clipSums = [ordered]@{}
foreach ($line in Get-Content -LiteralPath $keptClips) {
    if ($line -notmatch '^([0-9a-f]{64})  (.+)$') {
        Fail 'clips.sha256 holds a line that names no clip'
    }
    $digest, $path = $Matches[1], $Matches[2]
    $segments = $path -split '/'
    $outside = $path.StartsWith('/') -or $path.Contains('\')
    if ($outside -or ($segments -contains '..') -or ($segments -contains '')) {
        Fail 'clips.sha256 names a path outside this corpus'
    }
    $clipSums[$path] = $digest
}
Write-Host "narration: $($names.Count) volume(s) at $Tag"

if (-not $LocalDir) { New-Item -ItemType Directory -Force -Path $Work | Out-Null }
foreach ($name in $names) {
    if (-not (Get-Asset $name)) {
        Fail ("could not get $name at $Tag; for a private repository set GITHUB_TOKEN " +
            "or run gh auth login, or pass -LocalDir with volumes on this disk")
    }
}

# Every volume is checked BEFORE anything is joined or extracted, against the
# digests this corpus committed.
foreach ($name in $names) {
    $path = Join-Path $Parts $name
    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $path).Hash.ToLowerInvariant()
    if ($actual -ne $sumsByName[$name]) {
        if (-not $LocalDir) { Remove-Item -Force -LiteralPath $path -ErrorAction SilentlyContinue }
        Fail ("checksum mismatch on ${name}: it is not the volume this corpus packed. " +
            'Nothing was extracted. Run again to download it afresh')
    }
}
Write-Host 'narration: checksums ok'

New-Item -ItemType Directory -Force -Path $Work | Out-Null
$joined = Join-Path $Work $Volume
$out = [System.IO.File]::Create($joined)
try {
    foreach ($name in $names) {
        $in = [System.IO.File]::OpenRead((Join-Path $Parts $name))
        try { $in.CopyTo($out) } finally { $in.Dispose() }
    }
} finally { $out.Dispose() }

# The volumes are redundant once joined. Only the download directory is ever
# emptied: a -LocalDir is the reader's own, not a cache made here.
if (-not $LocalDir -and -not $KeepDownloads) {
    foreach ($name in $names) {
        Remove-Item -Force -LiteralPath (Join-Path $Work $name) -ErrorAction SilentlyContinue
    }
}

# Every member must be a clip this corpus committed, by exactly its path, and
# nothing else, checked before anything is extracted. Then each is extracted into
# a staging directory, checked against its committed digest, and only then moved.
$stage = Join-Path $Work 'staging'
function Remove-Stage {
    if (Test-Path -LiteralPath $stage) { Remove-Item -Recurse -Force -LiteralPath $stage }
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($joined)
try {
    $members = @($archive.Entries | ForEach-Object { $_.FullName } | Sort-Object)
    $wanted = @($clipSums.Keys | Sort-Object)
    if (($members -join "`n") -cne ($wanted -join "`n")) {
        $archive.Dispose(); $archive = $null
        Remove-Item -Force -LiteralPath $joined
        Fail "the volumes hold files that are not this corpus's clips; nothing was extracted"
    }
    Remove-Stage
    foreach ($entry in $archive.Entries) {
        $target = Join-Path $stage $entry.FullName
        $dir = Split-Path -Parent $target
        if (-not (Test-Path -LiteralPath $dir)) {
            New-Item -ItemType Directory -Force -Path $dir | Out-Null
        }
        [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
    }
} finally { if ($archive) { $archive.Dispose() } }
Remove-Item -Force -LiteralPath $joined

foreach ($path in $clipSums.Keys) {
    $staged = Join-Path $stage $path
    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $staged).Hash.ToLowerInvariant()
    if ($actual -ne $clipSums[$path]) {
        Remove-Stage
        Fail "$path is not the clip this corpus packed; nothing was placed"
    }
    $placed = Join-Path $Root $path
    $taken = (Test-Path -LiteralPath $placed) -and
        -not (Test-Path -LiteralPath $placed -PathType Leaf)
    if ($taken) {
        Remove-Stage
        Fail "$path is taken by something that is not a clip; nothing was placed"
    }
}
foreach ($path in $clipSums.Keys) {
    $placed = Join-Path $Root $path
    $dir = Split-Path -Parent $placed
    if (-not (Test-Path -LiteralPath $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    Move-Item -Force -LiteralPath (Join-Path $stage $path) -Destination $placed
    (Get-Item -LiteralPath $placed).LastWriteTime = Get-Date
}
Remove-Stage
Write-Host "narration: restored into this corpus's audio directories"

# Removed only when empty: anything this script did not put there survives.
if ((Test-Path -LiteralPath $Work) -and -not (Get-ChildItem -Force -LiteralPath $Work)) {
    Remove-Item -Force -LiteralPath $Work
} elseif (Test-Path -LiteralPath $Work) {
    Write-Host "narration: volumes kept in $Work; remove it when you are done"
}

# The pages ask their first clip themselves, so nothing else is written.
Write-Host 'narration: the pages will play the clips from their next load'
exit 0
