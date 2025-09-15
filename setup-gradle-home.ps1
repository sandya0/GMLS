# Set execution policy for this process
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

# Create directories
$projectDir = Get-Location
$gradleHome = Join-Path $projectDir "gradle-home"
$wrapperDists = Join-Path $gradleHome "wrapper\dists"
$gradleVersion = "gradle-8.9-bin"
$gradleDir = Join-Path $wrapperDists $gradleVersion
$hashDir = Join-Path $gradleDir "90cnw93cvbtalezasaz0blq0a"

Write-Host "Creating directories..."
New-Item -Path $gradleHome -ItemType Directory -Force
New-Item -Path $wrapperDists -ItemType Directory -Force
New-Item -Path $gradleDir -ItemType Directory -Force
New-Item -Path $hashDir -ItemType Directory -Force

# Set full permissions
Write-Host "Setting permissions..."
$acl = Get-Acl $gradleHome
$accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule("sandya","FullControl","ContainerInherit,ObjectInherit","None","Allow")
$acl.SetAccessRule($accessRule)
Set-Acl $gradleHome $acl

# Download Gradle
$url = "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
$output = Join-Path $hashDir "gradle-8.9-bin.zip"
Write-Host "Downloading Gradle 8.9..."
Invoke-WebRequest -Uri $url -OutFile $output

# Create marker files
Write-Host "Creating marker files..."
$markerFile = Join-Path $hashDir "gradle-8.9-bin.zip.ok"
New-Item -Path $markerFile -ItemType File -Force

# Create lock file
$lockFile = Join-Path $hashDir "gradle-8.9-bin.zip.lck"
New-Item -Path $lockFile -ItemType File -Force

Write-Host "Gradle setup complete!" 