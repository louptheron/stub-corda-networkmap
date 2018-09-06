package io.cryptoblk.networkmap.api

import io.cryptoblk.networkmap.domain.NetworkMapService
import io.cryptoblk.networkmap.domain.NodeInfoByte
import kotlinx.coroutines.experimental.runBlocking
import net.corda.core.crypto.SecureHash
import net.corda.core.serialization.SerializedBytes
import net.corda.core.serialization.serialize
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.serialization.AllWhitelist
import net.corda.nodeapi.internal.serialization.amqp.DeserializationInput
import net.corda.nodeapi.internal.serialization.amqp.SerializerFactory
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/network-map")
class NetworkMapAPI(private val networkMap: NetworkMapService) {

    @POST
    @Path("/publish")
    fun handlePublish(input: ByteArray) = runBlocking {
        // For the node to upload its signed NodeInfoByte object to the network map.
        val factory = SerializerFactory(AllWhitelist, ClassLoader.getSystemClassLoader())
        val signedNodeInfo = DeserializationInput(factory).deserialize(SerializedBytes<SignedNodeInfo>(input))
        val nodeInfoByte = NodeInfoByte(signedNodeInfo.raw.hash, signedNodeInfo.serialize().bytes)

        networkMap.handlePublish(nodeInfoByte)
    }

    @POST
    @Path("/ack-parameters")
    fun handleAckParam(input: ByteArray) {
        // For the node operator to acknowledge network map that new parameters were accepted for future update
    }

    @GET
    fun generateNetworkMap(): Response = runBlocking {
        val networkMap = networkMap.generateNetworkMap()

        Response.ok(networkMap.serialize().bytes).build()
        }

    @GET
    @Path("/node-info/{hash}")
    fun handleNodeInfo(@PathParam("hash") inputHash: String): Response = runBlocking {
        // Retrieve a signed NodeInfoByte as specified in the network map object.
        val hash = SecureHash.parse(inputHash)

        val nodeInfoByte = networkMap.handleNodeInfo(hash)!!

        nodeInfoByte.let {
            Response.ok(it).build()
        } ?: Response.status(404).build()
    }

    @GET
    @Path("/network-parameters/{hash}")
    fun handleNetworkParam(@PathParam("hash") inputHash: String): Response = runBlocking {
        // Retrieve the signed network parameters (see below). The entire object is signed with the network map certificate which is also attached.
        val hash = SecureHash.parse(inputHash)

        networkMap.handleNetworkParam(hash)?.let {
            Response.ok(it).build()
        } ?: Response.status(404).build()
    }
}