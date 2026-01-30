# Prueba local de KeyListenerService

Para probar la detección de Ctrl+A y la escritura en Activacion.txt sin instalar como servicio, ejecuta en consola (ajusta la ruta de jnativehook-2.2.2.jar si es necesario):

```
java -cp "target/usb-button-service-1.0.0-shaded.jar;C:/Users/jcmunozp/.m2/repository/com/github/kwhat/jnativehook/2.2.2/jnativehook-2.2.2.jar" com.jcmp.usbbutton.service.KeyListenerService
```

Esto creará el archivo Activacion.txt en la carpeta config del demo-button al pulsar Ctrl+A.

Si usas Linux/Mac, cambia el separador de classpath a ':' en vez de ';'.
# usb-button-service

## Detección de combinación de teclas (Ctrl+A)
Este servicio utiliza JNativeHook para detectar la combinación global Ctrl+A y escribe una línea en `C:/ProgramData/UsbButton/Activacion.txt` cada vez que se detecta.

### Limitación importante: pantalla bloqueada
**Java (y JNativeHook) no pueden detectar combinaciones de teclas cuando la pantalla de Windows está bloqueada.**

Para lograr la detección con pantalla bloqueada, se requiere un driver o hook nativo a nivel de sistema (C/C++), lo cual no es posible solo con Java. Si necesitas esta funcionalidad, deberás desarrollar un componente nativo adicional.

---

- El servicio debe ejecutarse con permisos elevados (SYSTEM) para funcionar correctamente.
- El archivo Activacion.txt se crea automáticamente si no existe.
