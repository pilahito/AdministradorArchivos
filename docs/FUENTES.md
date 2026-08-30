# Fuentes open source

Así se usan. **No se pega el árbol Git** de cada repo: se usa JAR/AAR o código propio inspirado en la idea.

| Componente | Repo | En esta app |
|---|---|---|
| Terminal / buffer VT100 | [termux/termux-app](https://github.com/termux/termux-app) | GPL-3. AAR `terminal-view` se puede añadir por JitPack. Hoy la Terminal es Compose propia (comandos + teclas). Pegar el source obliga a GPL de **toda** la app (ya licenciada GPL-3). |
| Motor SSH, SFTP, túneles | [hierynomus/sshj](https://github.com/hierynomus/sshj) | **Sí** — `com.hierynomus:sshj` |
| Cifrados OpenSSH | [mwiede/jsch](https://github.com/mwiede/jsch) | **Sí** — `com.github.mwiede:jsch` |
| Known hosts / port forward | [connectbot/connectbot](https://github.com/connectbot/connectbot) | Idea + túnel `-L` propio. No se copia el source GPL. |
| Explorador dual | [zhanghai/MaterialFiles](https://github.com/zhanghai/MaterialFiles) | Idea. SFTP de un panel ahora. Dual-pane en el siguiente bloque. |
| S3 / MinIO | [minio/minio-java](https://github.com/minio/minio-java) | Pendiente (SDK pesado). |
| Suite remota | [GlassHaven/Haven](https://github.com/GlassHaven/Haven) | Referencia de sesiones. No hay artefacto Maven. |

Logo: cubo isométrico verde con prompt `>` (icono propio, no Termius).
