# Política de seguridad — CloudTerm Pro

## Alcance

CloudTerm Pro es un cliente local SSH/SFTP. **No hay backend propio**.

## Reportar vulnerabilidades

Abre un [Security Advisory](https://github.com/pilahito/AdministradorArchivos/security/advisories/new) privado
o un issue sin detalles explotables si no tienes acceso a advisories.

Incluye: versión / commit, impacto, pasos mínimos de reproducción.

## Buenas prácticas del proyecto

- Bóveda: AES-256-GCM + PBKDF2 (ver `:vault`)
- No registrar contraseñas en logs
- Host key verification promisoria solo en LAN/dev — endurecer en producción
- Sync: credenciales OAuth/WebDAV solo en almacenamiento cifrado del dispositivo

## Versiones soportadas

| Versión | Soporte |
|---------|---------|
| 0.4.x-pro | Activo |
| 0.3.x (CyberTerm) | Mejor esfuerzo |