package com.octo.networkmap.domain

import com.octo.networkmap.TestHelper
import com.octo.networkmap.infra.NetworkParametersRepository
import com.octo.networkmap.infra.NodeInfoRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import net.corda.core.internal.SignedDataWithCert
import net.corda.core.serialization.SerializedBytes
import net.corda.core.serialization.internal.SerializationEnvironmentImpl
import net.corda.core.serialization.internal.nodeSerializationEnv
import net.corda.core.serialization.serialize
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.createDevNetworkMapCa
import net.corda.nodeapi.internal.network.NetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkMap
import net.corda.nodeapi.internal.serialization.AMQP_P2P_CONTEXT
import net.corda.nodeapi.internal.serialization.AMQP_STORAGE_CONTEXT
import net.corda.nodeapi.internal.serialization.AllWhitelist
import net.corda.nodeapi.internal.serialization.SerializationFactoryImpl
import net.corda.nodeapi.internal.serialization.amqp.AMQPServerSerializationScheme
import net.corda.nodeapi.internal.serialization.amqp.DeserializationInput
import net.corda.nodeapi.internal.serialization.amqp.SerializerFactory
import org.glassfish.jersey.server.model.Parameterized
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NetworkMapHandlerTest : TestHelper() {

    @MockK
    lateinit var networkParametersRepository: NetworkParametersRepository

    @MockK
    lateinit var nodeInfoRepository: NodeInfoRepository

    @Test
    fun `signed network map is correct`() {
        // Given
        coEvery { networkParametersRepository.save(any()) } just Runs
        coEvery { nodeInfoRepository.findAll() } returns emptyList()

        val networkMapHandler = NetworkMapHandler(nodeInfoRepository, networkParametersRepository)

        // When
        val signedNetworkMapBytes = networkMapHandler.generateNetworkMap()
        println(signedNetworkMapBytes)

        // Then
        val factory = SerializerFactory(AllWhitelist, ClassLoader.getSystemClassLoader())
        DeserializationInput(factory).deserialize(SerializedBytes<SignedNetworkMap>(signedNetworkMapBytes))

    }
}