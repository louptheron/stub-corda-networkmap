package com.octo.networkmap.infra

import net.corda.core.crypto.SecureHash

interface NetworkParametersRepository {
    suspend fun findByHash(hash: SecureHash): ByteArray?

    suspend fun save(networkParameters: Pair<SecureHash, ByteArray>)
}