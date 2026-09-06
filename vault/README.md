# :vault — Bóveda cifrada AES-256

Stub de `vault.db` con:

- **PBKDF2-HMAC-SHA256** (120k iteraciones) → clave AES-256
- **AES-GCM** para cifrar el blob
- Desbloqueo por passphrase
- Stubs biométricos documentados (BiometricPrompt / Windows Hello / Touch ID)
- Access logs en memoria

```kotlin
val vault = EncryptedVault(File("vault.db"))
vault.unlock("mi-passphrase".toCharArray())
vault.writeBytes("""{"hosts":[]}""".toByteArray())
vault.lock()
```