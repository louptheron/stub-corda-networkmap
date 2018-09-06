package io.cryptoblk.networkmap.domain

import net.corda.core.crypto.SecureHash

interface NetworkMapService {

    fun handlePublish(nodeInfo: NodeInfoByte)

    fun handleAckParam(bytes: ByteArray)

    fun generateNetworkMap(): ByteArray

    fun handleNodeInfo(hash: SecureHash): ByteArray?

    fun handleNetworkParam(hash: SecureHash): ByteArray?
}