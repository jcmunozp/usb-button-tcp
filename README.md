# USB Button TCP (Servicio + Escritorio) - Windows

Servicio de Windows en Java que escucha un botón USB (HID) y emite eventos por TCP (127.0.0.1:50515).
Una app de escritorio (Java) se conecta y muestra notificaciones al usuario.

## Módulos
- `service`: Servicio Windows (sin UI). Provee servidor TCP y emite eventos `BUTTON_PRESS`.
- `desktop`: App de usuario (bandeja del sistema). Se conecta al servicio y muestra notificaciones.

## Protocolo
- Transporte: TCP loopback (`127.0.0.1:50515`)
- Mensaje: JSON enmarcado con 4 bytes (Big Endian) de longitud.
- Autenticación: Token compartido (`AUTH` -> `AUTH_OK`).

## Despliegue rápido
1. `mvn -q -DskipTests package`
2. Ejecutar `scripts/Deploy-UsbButton.ps1` como **Administrador**:
   - Crea `C:\ProgramData\UsbButton\config.properties` con token aleatorio y puerto.
   - Instala el servicio con WinSW (o NSSM si lo adaptas).
   - Programa la App de Escritorio al iniciar sesión.

> **Nota:** `config.properties` contiene el token y **NO** se versiona (se crea en la máquina destino con ACLs).

## Integración HID (puntero)
Integra `hid4java` en el módulo `service` y llama `emitButtonPress("BTN1")` al parsear el input report del dispositivo.
