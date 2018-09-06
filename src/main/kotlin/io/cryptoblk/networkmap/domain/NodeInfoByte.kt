package io.cryptoblk.networkmap.domain

import net.corda.core.crypto.SecureHash

data class NodeInfoByte(val hash: SecureHash, val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean {
        other as NodeInfoByte

        return other.hash == hash
    }
}