package icu.takeneko.libNeko.nyatwork.registry

import icu.takeneko.libNeko.nyatwork.NyatworkServer
import icu.takeneko.libNeko.nyatwork.packet.PacketHandler
import icu.takeneko.libNeko.nyatwork.packet.RegistryPacketDecoder
import icu.takeneko.libNeko.registry.Identifier

abstract class RegistryNyatworkDedicatedServer(identifier: Identifier) :
    NyatworkServer<RegistryNyatworkDedicatedServer>(
        serviceId = identifier,
        inPipelineConfigurator = {
            this.start(RegistryPacketDecoder)
                .then(PacketHandler)
                .finish()
        },
        outPipelineConfigurator = {
            this.start(DedicatedServerPacketEncoder)
                .then(DedicatedServerPacketSender)
                .finish()
        }
    )
