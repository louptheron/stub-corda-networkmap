package io.cryptoblk.networkmap.handler

import io.cryptoblk.networkmap.dao.NodeInfoDAO
import io.restassured.builder.ResponseBuilder
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.startCoroutine
import net.corda.core.crypto.SecureHash
import net.corda.core.internal.signWithCert
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NotaryInfo
import net.corda.core.serialization.SerializedBytes
import net.corda.core.serialization.internal.SerializationEnvironmentImpl
import net.corda.core.serialization.internal.nodeSerializationEnv
import net.corda.core.serialization.serialize
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.createDevNetworkMapCa
import net.corda.nodeapi.internal.network.NetworkMap
import net.corda.nodeapi.internal.serialization.AMQP_P2P_CONTEXT
import net.corda.nodeapi.internal.serialization.AMQP_STORAGE_CONTEXT
import net.corda.nodeapi.internal.serialization.AllWhitelist
import net.corda.nodeapi.internal.serialization.SerializationFactoryImpl
import net.corda.nodeapi.internal.serialization.amqp.AMQPServerSerializationScheme
import net.corda.nodeapi.internal.serialization.amqp.DeserializationInput
import net.corda.nodeapi.internal.serialization.amqp.SerializerFactory
import java.security.KeyPair
import java.security.cert.X509Certificate
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/network-map")
class NetworkMapHandler {

    private var networkMapCert: X509Certificate? = null
    private var keyPair: KeyPair? = null

    private val networkParams: ConcurrentMap<SecureHash, ByteArray> = ConcurrentHashMap()

    companion object {
        private val stubNetworkParameters = NetworkParameters(3, emptyList(),
            10485760, Int.MAX_VALUE, Instant.now(), 10, emptyMap())
    }

    init {
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

    @POST
    @Path("/publish")
    fun handlePublish(input: ByteArray): Response {
        // For the node to upload its signed NodeInfo object to the network map.
        val factory = SerializerFactory(AllWhitelist, ClassLoader.getSystemClassLoader())
        val signedNodeInfo = DeserializationInput(factory).deserialize(SerializedBytes<SignedNodeInfo>(input))

        launch {
            NodeInfoDAO.addNodeInfoByHash(signedNodeInfo.raw.hash, signedNodeInfo.serialize().bytes)
        }

        return Response.ok().build()
    }

    @POST
    @Path("/ack-parameters")
    fun handleAckParam(input: ByteArray) {
        // For the node operator to acknowledge network map that new parameters were accepted for future update
    }

    @GET
    fun generateNetworkMap(): Response {
        // Retrieve the current signed network map object. The entire object is signed with the network map certificate which is also attached.
        val signedNetParams = stubNetworkParameters.
            copy(notaries = emptyList(), epoch = stubNetworkParameters.epoch).
            signWithCert(keyPair!!.private, networkMapCert!!)
        val paramHash = signedNetParams.raw.hash
        networkParams[paramHash] = signedNetParams.serialize().bytes

        var nodeInfos: ConcurrentMap<SecureHash, ByteArray>? = null
        launch {
            nodeInfos = NodeInfoDAO.getNodeInfos()
        }

        val nodeInfosBytes = nodeInfos?.let { it.keys.toList() } ?: emptyList()
        val networkMap = NetworkMap(nodeInfosBytes, paramHash, null)
        val signedNetworkMap = networkMap.signWithCert(keyPair!!.private, networkMapCert!!)

        return Response.ok(signedNetworkMap.serialize().bytes).build()
    }

    @GET
    @Path("/node-info/{hash}")
    fun handleNodeInfo(@PathParam("hash") h: String): Response {
        // Retrieve a signed NodeInfo as specified in the network map object.
        val hash = SecureHash.parse(h)

        var nodeInfo: ByteArray? = null
        launch {
            nodeInfo =  NodeInfoDAO.getNodeInfoByHash(hash)!!
        }

        return nodeInfo?.let {
            return Response.ok(it).build()
        } ?: Response.status(404).build()
    }

    @GET
    @Path("/network-parameters/{hash}")
    fun handleNetworkParam(@PathParam("hash") h: String): Response {
        // Retrieve the signed network parameters (see below). The entire object is signed with the network map certificate which is also attached.
        val hash = SecureHash.parse(h)

        return networkParams[hash]?.let {
            return Response.ok(it).build()
        } ?: Response.status(404).build()
    }
}