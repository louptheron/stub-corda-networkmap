package io.cryptoblk.networkmap.infra

import io.cryptoblk.networkmap.domain.NodeInfoByte
import net.corda.core.crypto.SecureHash

interface NodeInfoRepository {
    suspend fun findByHash(hash: SecureHash): NodeInfoByte?

    suspend fun findAll(): List<NodeInfoByte>?

    suspend fun save(nodeInfo: NodeInfoByte)
}