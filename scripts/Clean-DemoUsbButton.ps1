# Limpieza de entorno de pruebas UsbButton (demo-button)
# Ejecutar como administrador si se instaló el servicio

$baseDir = 'C:\Users\jcmunozp\Downloads\AAA DIARIO TRABAJO\BOTON ANTIPANICO\CODIGO\COPILOT\demo-button'
$svcDir  = Join-Path $baseDir 'service'
$taskName = 'UsbButtonClient'
$winSw = Join-Path $svcDir 'winsw.exe'

# 1. Desinstalar el servicio si existe
if (Test-Path $winSw) {
    Write-Host "Desinstalando servicio demo..."
    & $winSw uninstall 2>$null
}

# 2. Eliminar tarea programada de pruebas
if (Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue) {
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
    Write-Host "Tarea programada '$taskName' eliminada."
}

# 3. Eliminar carpeta de pruebas
if (Test-Path $baseDir) {
    Remove-Item -Path $baseDir -Recurse -Force
    Write-Host "Directorio de pruebas eliminado: $baseDir"
}
else {
    Write-Host "No existe el directorio de pruebas: $baseDir"
}

Write-Host "Limpieza de entorno de pruebas completada." -ForegroundColor Green
