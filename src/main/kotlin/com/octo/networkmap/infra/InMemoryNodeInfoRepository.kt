package com.octo.networkmap.infra

import net.corda.core.crypto.SecureHash
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object InMemoryNodeInfoRepository : NodeInfoRepository {
    private val nodeInfos: ConcurrentMap<SecureHash, ByteArray> = ConcurrentHashMap()

    override suspend fun findByHash(hash: SecureHash): ByteArray? {
        nodeInfos[hash]?.let {
            return it
        } ?: return null
    }

    override suspend fun findAll(): List<Pair<SecureHash, ByteArray>>? {
        return nodeInfos.toList()
    }

    override suspend fun save(nodeInfo: Pair<SecureHash, ByteArray>) {
        nodeInfos[nodeInfo.first] = nodeInfo.second
    }
}