# :core-ssh — API Kotlin sobre SSHJ

Módulo multiplataforma (JVM) que envuelve [SSHJ](https://github.com/hierynomus/sshj) con una API estable para CloudTerm Pro.

## API

| Método | Descripción |
|--------|-------------|
| `connect()` | Conecta y autentica (password o clave) |
| `startShell()` | Shell interactivo con PTY |
| `exec(command)` | Ejecuta un comando y devuelve stdout+stderr |
| `openSftp()` | Canal SFTP |
| `createTunnel(localPort, remoteHost, remotePort)` | Túnel local `-L` |

## Ejemplo

```kotlin
val client = SshClient(
    config = SshConfig(
        host = "192.168.1.10",
        port = 22,
        username = "root",
        password = "secreto"
    )
)

client.connect()
println(client.exec("uname -a"))

val shell = client.startShell()
// leer shell.input / escribir shell.output

val sftp = client.openSftp()
sftp.ls("/").forEach { println(it.name) }

val tunnel = client.createTunnel(
    localPort = 8080,
    remoteHost = "127.0.0.1",
    remotePort = 80
)

client.disconnect()
```

## Dependencia

```gradle
implementation project(':core-ssh')
```