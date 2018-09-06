package io.cryptoblk.networkmap.dao

import net.corda.core.crypto.SecureHash
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object NodeInfoDAO {
    private val nodeInfos: ConcurrentMap<SecureHash, ByteArray> = ConcurrentHashMap()

    suspend fun getNodeInfoByHash(hash: SecureHash):ByteArray? {
        // TODO also use suspend
        return nodeInfos[hash]
    }

    suspend fun getNodeInfos():ConcurrentMap<SecureHash, ByteArray>? {
        return nodeInfos
    }

    suspend fun addNodeInfoByHash(hash: SecureHash, bytes: ByteArray) {
        nodeInfos[hash] = bytes
    }
}