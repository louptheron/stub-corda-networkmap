package io.cryptoblk.networkmap.infra

import io.cryptoblk.networkmap.domain.NodeInfoByte
import net.corda.core.crypto.SecureHash
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object InMemoryNodeInfoRepository : NodeInfoRepository{
    private val nodeInfos: ConcurrentMap<SecureHash, ByteArray> = ConcurrentHashMap()

    override suspend fun findByHash(hash: SecureHash): NodeInfoByte? {
        nodeInfos[hash]?.let {
            return NodeInfoByte(hash, it)
        } ?: return null
    }

    override suspend fun findAll(): List<NodeInfoByte>? {
        return nodeInfos.map { NodeInfoByte(it.key, it.value) }.toList()
    }

    override suspend fun save(nodeInfo: NodeInfoByte) {
        nodeInfos[nodeInfo.hash] = nodeInfo.bytes
    }
}