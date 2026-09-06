package com.cloudterm.core.ssh

/**
 * Túnel local (-L): tráfico a localPort se reenvía a remoteHost:remotePort
 * a través de la sesión SSH.
 */
interface SshTunnel : AutoCloseable {
    val localPort: Int
    val remoteHost: String
    val remotePort: Int
    val isOpen: Boolean
}