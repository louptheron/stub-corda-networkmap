package com.octo.networkmap.api

import com.octo.networkmap.domain.NetworkMapService
import kotlinx.coroutines.experimental.runBlocking
import net.corda.core.crypto.SecureHash
import net.corda.core.serialization.SerializedBytes
import net.corda.core.serialization.serialize
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.serialization.AllWhitelist
import net.corda.nodeapi.internal.serialization.amqp.DeserializationInput
import net.corda.nodeapi.internal.serialization.amqp.SerializerFactory
import java.net.URI
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/network-map")
class NetworkMapAPI(private val networkMap: NetworkMapService) {

    @POST
    @Path("/publish")
    fun publishNodeInfo(input: ByteArray): Response {
        // For the node to upload its signed NodeInfoByte object to the network map.
        val factory = SerializerFactory(AllWhitelist, ClassLoader.getSystemClassLoader())
        val signedNodeInfo = DeserializationInput(factory).deserialize(SerializedBytes<SignedNodeInfo>(input))
        val nodeInfoPair = Pair(signedNodeInfo.raw.hash, signedNodeInfo.serialize().bytes)

        networkMap.publishNodeInfo(nodeInfoPair)

        return Response.created(URI.create("/node-info/" + signedNodeInfo.raw.hash)).build()
    }

    @POST
    @Path("/ack-parameters")
    fun handleAckParam(input: ByteArray) {
        // For the node operator to acknowledge network map that new parameters were accepted for future update
    }

    @GET
    fun generateNetworkMap(): Response {
        val networkMap = networkMap.generateNetworkMap()

        return Response.ok(networkMap.serialize().bytes).build()
    }

    @GET
    @Path("/node-info/{hash}")
    fun getNodeInfo(@PathParam("hash") inputHash: String): Response {
        // Retrieve a signed NodeInfoByte as specified in the network map object.
        val hash = SecureHash.parse(inputHash)

        val nodeInfoByte = networkMap.getNodeInfo(hash)!!

        return nodeInfoByte.let {
            Response.ok(it).build()
        } ?: Response.status(404).build()
    }

    @GET
    @Path("/network-parameters/{hash}")
    fun handleNetworkParam(@PathParam("hash") inputHash: String): Response {
        // Retrieve the signed network parameters (see below). The entire object is signed with the network map certificate which is also attached.
        val hash = SecureHash.parse(inputHash)

        val networkParameters = networkMap.getNetworkParameters(hash)

        return networkParameters?.let {
            Response.ok(it).build()
        } ?: Response.status(404).build()
    }
}