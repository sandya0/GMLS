# Set execution policy for this process
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

# Define Gradle directories
$gradleHome = "C:\Users\sandya\.gradle"
$wrapperDists = "$gradleHome\wrapper\dists"

# Create directories if they don't exist
if (-not (Test-Path $gradleHome)) {
    New-Item -Path $gradleHome -ItemType Directory -Force
    Write-Host "Created Gradle home directory: $gradleHome"
}

if (-not (Test-Path $wrapperDists)) {
    New-Item -Path $wrapperDists -ItemType Directory -Force
    Write-Host "Created wrapper/dists directory: $wrapperDists"
}

# Set full permissions for the current user
$acl = Get-Acl $gradleHome
$currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
$accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule($currentUser, "FullControl", "ContainerInherit,ObjectInherit", "None", "Allow")
$acl.SetAccessRule($accessRule)
Set-Acl $gradleHome $acl

Write-Host "Successfully set full permissions for $currentUser on $gradleHome"

# Ensure correct version is in gradle-wrapper.properties
$wrapperPropertiesPath = ".\gradle\wrapper\gradle-wrapper.properties"
if (Test-Path $wrapperPropertiesPath) {
    $content = Get-Content $wrapperPropertiesPath -Raw
    $newContent = $content -replace "gradle-8.0-bin.zip", "gradle-8.9-bin.zip"
    Set-Content -Path $wrapperPropertiesPath -Value $newContent
    Write-Host "Updated Gradle wrapper to use version 8.9"
}

Write-Host "Gradle permissions fixed! Try running Android Studio again." 