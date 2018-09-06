package io.cryptoblk.networkmap.domain

import net.corda.core.crypto.SecureHash

interface NetworkMapService {

    fun publishNodeInfo(nodeInfo: Pair<SecureHash, ByteArray>)

    fun handleAckParam(bytes: ByteArray)

    fun generateNetworkMap(): ByteArray

    fun getNodeInfo(hash: SecureHash): ByteArray?

    fun getNetworkParameters(hash: SecureHash): ByteArray?
}