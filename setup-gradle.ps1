# Create directories
$gradleHome = "C:\Users\sandya\.gradle"
$wrapperDists = "$gradleHome\wrapper\dists"
$gradleVersion = "gradle-8.9-bin"
$gradleDir = "$wrapperDists\$gradleVersion"
$hashDir = "$gradleDir\90cnw93cvbtalezasaz0blq0a"

# Create directories with full permissions
New-Item -Path $wrapperDists -ItemType Directory -Force
New-Item -Path $gradleDir -ItemType Directory -Force
New-Item -Path $hashDir -ItemType Directory -Force

# Set full permissions
$acl = Get-Acl $gradleHome
$accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule("sandya","FullControl","ContainerInherit,ObjectInherit","None","Allow")
$acl.SetAccessRule($accessRule)
Set-Acl $gradleHome $acl

# Download Gradle
$url = "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
$output = "$hashDir\gradle-8.9-bin.zip"
Write-Host "Downloading Gradle 8.9..."
Invoke-WebRequest -Uri $url -OutFile $output

# Create marker files
$markerFile = "$hashDir\gradle-8.9-bin.zip.ok"
New-Item -Path $markerFile -ItemType File -Force

# Create lock file
$lockFile = "$hashDir\gradle-8.9-bin.zip.lck"
New-Item -Path $lockFile -ItemType File -Force

Write-Host "Gradle setup complete!" 