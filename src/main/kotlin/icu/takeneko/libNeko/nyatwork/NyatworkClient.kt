package icu.takeneko.libNeko.nyatwork

import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryData
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryPacketReceiver
import icu.takeneko.libNeko.nyatwork.discovery.DiscoveryResult

abstract class NyatworkClient(
    inPipelineConfigurator: PipelineBuilder<FriendlyByteBuf, FriendlyByteBuf, Unit>.() -> Unit,
    outPipelineConfigurator: PipelineBuilder<Packet, Packet, FriendlyByteBuf>.() -> Unit
) : NyatworkService<DiscoveryPacketReceiver>(inPipelineConfigurator, outPipelineConfigurator) {

    private lateinit var discovery: DiscoveryPacketReceiver

    override fun configureDiscovery(configurator: () -> DiscoveryPacketReceiver) {
        discovery = configurator()
        discovery.onServerFound(::onServerFound)
    }

    abstract fun onServerFound(dd: DiscoveryData):DiscoveryResult

}