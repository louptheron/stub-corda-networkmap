package com.octo.networkmap.domain

import com.octo.networkmap.infra.NetworkParametersRepository
import com.octo.networkmap.infra.NodeInfoRepository
import kotlinx.coroutines.experimental.runBlocking
import net.corda.core.crypto.SecureHash
import net.corda.core.internal.signWithCert
import net.corda.core.node.NetworkParameters
import net.corda.core.serialization.internal.SerializationEnvironmentImpl
import net.corda.core.serialization.internal.nodeSerializationEnv
import net.corda.core.serialization.serialize
import net.corda.nodeapi.internal.createDevNetworkMapCa
import net.corda.nodeapi.internal.network.NetworkMap
import net.corda.nodeapi.internal.serialization.AMQP_P2P_CONTEXT
import net.corda.nodeapi.internal.serialization.AMQP_STORAGE_CONTEXT
import net.corda.nodeapi.internal.serialization.SerializationFactoryImpl
import net.corda.nodeapi.internal.serialization.amqp.AMQPServerSerializationScheme
import java.security.KeyPair
import java.security.cert.X509Certificate
import java.time.Instant

class NetworkMapHandler(
        private val nodeInfoRepository: NodeInfoRepository,
        private val networkParametersRepository: NetworkParametersRepository) : NetworkMapService {

    private var networkMapCert: X509Certificate? = null
    private var keyPair: KeyPair? = null


    companion object {
        private val stubNetworkParameters = NetworkParameters(3, emptyList(),
                10485760, Int.MAX_VALUE, Instant.now(), 10, emptyMap())
    }

    init {
        // TODO: Handle key pair stored on the filesystem
        if (networkMapCert == null && keyPair == null) {
            val networkMapCa = createDevNetworkMapCa()
            keyPair = networkMapCa.keyPair
            networkMapCert = networkMapCa.certificate
        }

        if (nodeSerializationEnv == null) {
            val classloader = this.javaClass.classLoader
            nodeSerializationEnv = SerializationEnvironmentImpl(
                    SerializationFactoryImpl().apply {
                        registerScheme(AMQPServerSerializationScheme(emptyList()))
                    },
                    p2pContext = AMQP_P2P_CONTEXT.withClassLoader(classloader),
                    rpcServerContext = AMQP_P2P_CONTEXT.withClassLoader(classloader),
                    storageContext = AMQP_STORAGE_CONTEXT.withClassLoader(classloader),
                    checkpointContext = AMQP_P2P_CONTEXT.withClassLoader(classloader)
            )
        }
    }

    override fun publishNodeInfo(nodeInfo: Pair<SecureHash, ByteArray>) = runBlocking {
        nodeInfoRepository.save(nodeInfo)
    }

    override fun handleAckParam(bytes: ByteArray) {
        // For the node operator to acknowledge network map that new parameters were accepted for future update
    }

    override fun generateNetworkMap(): ByteArray = runBlocking {
        // Retrieve the current signed network map object. The entire object is signed with the network map certificate which is also attached.
        val signedNetParams = stubNetworkParameters.
                copy(notaries = emptyList(), epoch = stubNetworkParameters.epoch).
                signWithCert(keyPair!!.private, networkMapCert!!)
        val networkParametersHash = signedNetParams.raw.hash
        networkParametersRepository.save(Pair(networkParametersHash, signedNetParams.serialize().bytes))

        val networkNodeInfosHashes = nodeInfoRepository.findAll()?.map { it.first } ?: emptyList()

        val networkMap = NetworkMap(networkNodeInfosHashes, networkParametersHash, null)
        val signedNetworkMap = networkMap.signWithCert(keyPair!!.private, networkMapCert!!)

        signedNetworkMap.serialize().bytes
    }

    override fun getNodeInfo(hash: SecureHash): ByteArray? = runBlocking {
        nodeInfoRepository.findByHash(hash)?.let {
            it
        }
    }

    override fun getNetworkParameters(hash: SecureHash): ByteArray? = runBlocking {
        nodeInfoRepository.findByHash(hash)?.let {
            it
        }
    }
}