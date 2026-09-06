# Fuentes open source

Así se usan. **No se pega el árbol Git** completo de cada repo salvo cuando la licencia lo exige o se documenta en NOTICE.

| Componente | Repo | En esta app |
|---|---|---|
| Terminal / buffer VT100 | [termux/termux-app](https://github.com/termux/termux-app) | **Sí (GPL-3)** — AAR JitPack `com.termux.termux-app:terminal-emulator:0.118.0` y `terminal-view:0.118.0`. Render vía `TerminalRenderer` + `CloudTerminalView`; I/O SSH desde `ClienteSshj`. Ver [NOTICE](../NOTICE). |
| Motor SSH, SFTP, túneles | [hierynomus/sshj](https://github.com/hierynomus/sshj) | **Sí** — `com.hierynomus:sshj` (shell interactivo + core-ssh) |
| Cifrados OpenSSH | [mwiede/jsch](https://github.com/mwiede/jsch) | **Sí** — `com.github.mwiede:jsch` (SFTP en `:app`) |
| Known hosts / port forward | [connectbot/connectbot](https://github.com/connectbot/connectbot) | Idea + túnel `-L` propio. No se copia el source GPL. |
| Explorador dual | [zhanghai/MaterialFiles](https://github.com/zhanghai/MaterialFiles) | Idea. SFTP panel + cola UI. |
| Suite remota | Termius (propietario) | **Solo referencia de UX**. Cero código/assets. |

Logo: cubo isométrico / escudo CloudTerm (icono propio, no Termius).
