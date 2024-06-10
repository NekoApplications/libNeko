package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryData
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryPacketSender
import icu.takeneko.libNeko.nyatwork.packet.PacketHandlingContext
import icu.takeneko.libNeko.nyatwork.packet.PacketSendingContext
import icu.takeneko.libNeko.nyatwork.util.FriendlyByteBuf
import icu.takeneko.libNeko.registry.Identifier
import icu.takeneko.libNeko.util.readByteArrayLine
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class NyatworkServer<T: NyatworkServer<T>>(
    val serviceId: Identifier,
    inPipelineConfigurator: PipelineBuilder<PacketHandlingContext, PacketHandlingContext, Unit>.(T) -> Unit,
    outPipelineConfigurator: PipelineBuilder<PacketSendingContext, PacketSendingContext, Unit>.(T) -> Unit
) : NyatworkService<DiscoveryPacketSender, T>(inPipelineConfigurator, outPipelineConfigurator, "NyatworkServer") {

    lateinit var discovery: DiscoveryPacketSender

    override fun serviceThread() {
        runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val serverSocket = aSocket(selectorManager).tcp().bind(targetAddress, targetPort)
            while (true) {
                val socket = serverSocket.accept()
                launch {
                    val receiveChannel = socket.openReadChannel()
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    while (true) {
                        try {
                            val line = receiveChannel.readByteArrayLine()
                            val buf = FriendlyByteBuf.wrap(line)
                            inPipeline.accept(PacketHandlingContext(buf, sendChannel, inPipeline, outPipeline))
                        } catch (e: Throwable) {
                            onServiceException(e)
                        }

                    }
                }
            }
        }
    }

    abstract fun onServiceException(e:Throwable)

    override fun configureDiscovery(configurator: () -> DiscoveryPacketSender) {
        discovery = configurator()
        discovery.dataProvider(::getDiscoveryData)
    }

    abstract fun getDiscoveryData(): DiscoveryData

}