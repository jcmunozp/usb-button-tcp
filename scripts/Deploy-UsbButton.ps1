#Requires -RunAsAdministrator
$ErrorActionPreference = 'Stop'

# Rutas
$progData = 'C:\ProgramData\UsbButton'
$svcDir   = 'C:\Program Files\UsbButtonService'
$cliDir   = 'C:\Program Files\UsbButtonClient'
$javaExe  = 'C:\Program Files\Java\jdk-17\bin\java.exe'   # ajusta si usas otra JDK
$port     = 50515

# 1) Crear carpetas
New-Item -ItemType Directory -Path $progData -ErrorAction SilentlyContinue | Out-Null
New-Item -ItemType Directory -Path $svcDir   -ErrorAction SilentlyContinue | Out-Null
New-Item -ItemType Directory -Path $cliDir   -ErrorAction SilentlyContinue | Out-Null

# 2) Generar token seguro 256-bit (Base64)
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$token = [Convert]::ToBase64String($bytes)

# 3) Escribir config.properties con ACL (SYSTEM + Administrators)
$config = @"
port=$port
token=$token
"@
$configPath = Join-Path $progData 'config.properties'
$config | Out-File -FilePath $configPath -Encoding ascii -Force

# ACL: Solo SYSTEM y Administrators
$acl = New-Object System.Security.AccessControl.FileSecurity
$rule1 = New-Object System.Security.AccessControl.FileSystemAccessRule("SYSTEM","FullControl","Allow")
$rule2 = New-Object System.Security.AccessControl.FileSystemAccessRule("Administrators","FullControl","Allow")
$acl.SetOwner([System.Security.Principal.NTAccount]"Administrators")
$acl.SetAccessRuleProtection($true,$false)
$acl.ResetAccessRule($rule1)
$acl.AddAccessRule($rule2)
Set-Acl -Path $configPath -AclObject $acl

Write-Host "Token generado y guardado en $configPath" -ForegroundColor Green

# 4) Copia tus JARs compilados (ajusta nombres)
#   (Asume que ya ejecutaste 'mvn -q -DskipTests package' en ambos módulos.)
Copy-Item ".\service\target\usb-button-service-1.0.0-shaded.jar" "$svcDir\usb-service.jar" -Force
Copy-Item ".\desktop\target\usb-button-desktop-1.0.0-shaded.jar" "$cliDir\usb-client.jar" -Force

# 5) Instalar servicio con WinSW (descarga manual previa o especifica ruta)
$winSw = Join-Path $svcDir 'winsw.exe'
if (-not (Test-Path $winSw)) {
  Write-Warning "Copia WinSW en $svcDir como winsw.exe (https://github.com/winsw/winsw/releases)"
}

$xml = @"
<service>
  <id>UsbButtonService</id>
  <name>USB Button Java Service</name>
  <description>Escucha botón HID y emite eventos por TCP (127.0.0.1:$port)</description>
  <executable>$javaExe</executable>
  <arguments>-jar "$svcDir\usb-service.jar"</arguments>
  <logpath>$progData\logs</logpath>
  <log mode="roll-by-size"/>
  <startmode>Automatic</startmode>
</service>
"@
$xmlPath = Join-Path $svcDir 'UsbButtonService.xml'
$xml | Out-File -FilePath $xmlPath -Encoding utf8 -Force

if (Test-Path $winSw) {
  & $winSw uninstall 2>$null
  & $winSw install
  & $winSw start
  Write-Host "Servicio instalado y arrancado." -ForegroundColor Green
} else {
  Write-Warning "No se instaló el servicio (falta winsw.exe)."
}

# 6) Programar la app de escritorio al iniciar sesión (tarea programada)
$exe = $javaExe
$args = '-jar "' + $cliDir + '\\usb-client.jar"'
$action  = New-ScheduledTaskAction -Execute $exe -Argument $args
$trigger = New-ScheduledTaskTrigger -AtLogOn
$principal = New-ScheduledTaskPrincipal -UserId "$env:USERDOMAIN\$env:USERNAME" -RunLevel Highest
Register-ScheduledTask -TaskName "UsbButtonClient" -Action $action -Trigger $trigger -Principal $principal -Force `
  -Description "Cliente de notificaciones USB (conecta a 127.0.0.1:$port)"

Write-Host "Tarea programada creada: UsbButtonClient" -ForegroundColor Green
