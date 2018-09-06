package io.cryptoblk.networkmap.infra

import net.corda.core.crypto.SecureHash

interface NodeInfoRepository {
    suspend fun findByHash(hash: SecureHash): ByteArray?

    suspend fun findAll(): List<Pair<SecureHash, ByteArray>>?

    suspend fun save(nodeInfo: Pair<SecureHash, ByteArray>)
}