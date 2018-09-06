package io.cryptoblk.networkmap.infra

import net.corda.core.crypto.SecureHash
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object InMemoryNetworkParametersRepository : NetworkParametersRepository{
    private val networkParam: ConcurrentMap<SecureHash, ByteArray> = ConcurrentHashMap()

    override suspend fun findByHash(hash: SecureHash): ByteArray? {
        networkParam[hash]?.let {
            return it
        } ?: return null
    }

    override suspend fun save(networkParameters: Pair<SecureHash, ByteArray>) {
        networkParam[networkParameters.first] = networkParameters.second
    }
}