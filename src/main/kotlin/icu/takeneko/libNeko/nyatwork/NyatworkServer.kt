package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryData
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryPacketSender
import icu.takeneko.libNeko.registry.Identifier

abstract class NyatworkServer(
    val serviceId: Identifier,
    inPipelineConfigurator: PipelineBuilder<FriendlyByteBuf, FriendlyByteBuf, Unit>.() -> Unit,
    outPipelineConfigurator: PipelineBuilder<Packet, Packet, FriendlyByteBuf>.() -> Unit
) : NyatworkService<DiscoveryPacketSender>(inPipelineConfigurator, outPipelineConfigurator) {

    lateinit var discovery: DiscoveryPacketSender

    override fun configureDiscovery(configurator: () -> DiscoveryPacketSender) {
        discovery = configurator()
        discovery.dataProvider(::getDiscoveryData)
    }

    abstract fun getDiscoveryData(): DiscoveryData

}