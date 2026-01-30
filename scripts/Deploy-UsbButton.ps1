#Requires -RunAsAdministrator
$ErrorActionPreference = 'Stop'

# Rutas
$baseDir = 'C:\Users\jcmunozp\Downloads\AAA DIARIO TRABAJO\BOTON ANTIPANICO\CODIGO\COPILOT\demo-button'
$progData = Join-Path $baseDir 'config'
$svcDir   = Join-Path $baseDir 'service'
$cliDir   = Join-Path $baseDir 'client'
$javaExe  = 'C:\DCS\javasConfigured\openjdk-18.0.1.1_windows-x64_bin\jdk-18.0.1.1\bin\java.exe'   # ajusta si usas otra JDK
$port     = 50515

# 1) Crear carpetas
New-Item -ItemType Directory -Path $baseDir  -ErrorAction SilentlyContinue | Out-Null
New-Item -ItemType Directory -Path $progData -ErrorAction SilentlyContinue | Out-Null
New-Item -ItemType Directory -Path $svcDir   -ErrorAction SilentlyContinue | Out-Null
New-Item -ItemType Directory -Path $cliDir   -ErrorAction SilentlyContinue | Out-Null

# 2) Generar token seguro 256-bit (Base64)
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$token = [Convert]::ToBase64String($bytes)


# 3) Escribir config.properties (sin ACL restrictivo para pruebas)
$config = @"
port=$port
token=$token
"@
$configPath = Join-Path $progData 'config.properties'
$config | Out-File -FilePath $configPath -Encoding ascii -Force

Write-Host "Token generado y guardado en $configPath" -ForegroundColor Green

# 4) Copia tus JARs compilados (ajusta nombres)
#   (Asume que ya ejecutaste 'mvn -q -DskipTests package' en ambos módulos.)
Copy-Item ".\service\target\usb-button-service-1.0.0.jar" "$svcDir\usb-service.jar" -Force
Copy-Item ".\desktop\target\usb-button-desktop-1.0.0.jar" "$cliDir\usb-client.jar" -Force


# Copiar JAR de JNativeHook para pruebas locales
$jnativehookJarM2 = "$env:USERPROFILE\.m2\repository\com\github\kwhat\jnativehook\2.2.2\jnativehook-2.2.2.jar"
$jnativehookJarLocal = ".\service\lib\jnativehook-2.2.2.jar"
if (Test-Path $jnativehookJarM2) {
  Copy-Item $jnativehookJarM2 "$progData\jnativehook-2.2.2.jar" -Force
  Write-Host "JNativeHook copiado a $progData desde Maven local." -ForegroundColor Yellow
} elseif (Test-Path $jnativehookJarLocal) {
  Copy-Item $jnativehookJarLocal "$progData\jnativehook-2.2.2.jar" -Force
  Write-Host "JNativeHook copiado a $progData desde service/lib." -ForegroundColor Yellow
} else {
  Write-Warning "No se encontró jnativehook-2.2.2.jar en Maven ni en service/lib."
}

# Copiar WinSW.exe automáticamente si está en service/lib
$winSw = Join-Path $svcDir 'WinSW.exe'
$winSwLocal = ".\service\lib\WinSW.exe"
if (-not (Test-Path $winSw)) {
  if (Test-Path $winSwLocal) {
    Copy-Item $winSwLocal $winSw -Force
    Write-Host "WinSW.exe copiado a $svcDir desde service/lib." -ForegroundColor Yellow
  } else {
    Write-Warning "Copia WinSW en $svcDir como WinSW.exe (https://github.com/winsw/winsw/releases)"
  }
}
# 5) Instalar servicio con WinSW (descarga manual previa o especifica ruta)
$winSw = Join-Path $svcDir 'WinSW.exe'
if (-not (Test-Path $winSw)) {
  Write-Warning "Copia WinSW en $svcDir como WinSW.exe (https://github.com/winsw/winsw/releases)"
}


# El archivo de configuración debe llamarse igual que el ejecutable pero con extensión .xml
$winSwXml = [System.IO.Path]::ChangeExtension($winSw, '.xml')
$jnativehookAbs = (Resolve-Path "$progData\jnativehook-2.2.2.jar").Path
$usbServiceAbs = (Resolve-Path "$svcDir\usb-service.jar").Path
$xml = @"
<service>
  <id>UsbButtonService</id>
  <name>USB Button Java Service</name>
  <description>Escucha botón HID y emite eventos por TCP (127.0.0.1:$port)</description>
  <executable>$javaExe</executable>
  <arguments>-cp "$jnativehookAbs;$usbServiceAbs" com.jcmp.usbbutton.service.MainService</arguments>
  <logpath>$progData\logs</logpath>
  <log mode="roll-by-size"/>
  <startmode>Automatic</startmode>
</service>
"@
$xml | Out-File -FilePath $winSwXml -Encoding utf8 -Force

if (Test-Path $winSw) {
  & $winSw uninstall 2>$null
  & $winSw install
  & $winSw start
  Write-Host "Servicio instalado y arrancado." -ForegroundColor Green
} else {
  Write-Warning "No se instaló el servicio (falta WinSW.exe)."
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
