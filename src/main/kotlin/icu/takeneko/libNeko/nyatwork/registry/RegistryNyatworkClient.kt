package icu.takeneko.libNeko.nyatwork.registry

import icu.takeneko.libNeko.nyatwork.NyatworkClient
import icu.takeneko.libNeko.nyatwork.packet.PacketHandler
import icu.takeneko.libNeko.nyatwork.packet.RegistryPacketDecoder
import icu.takeneko.libNeko.nyatwork.packet.RegistryPacketEncoder

abstract class RegistryNyatworkClient : NyatworkClient<RegistryNyatworkClient>(
    inPipelineConfigurator = {
        this.start(RegistryPacketDecoder)
            .then(PacketHandler)
            .finish()
    },
    outPipelineConfigurator = {
        this.start(RegistryPacketEncoder)
    }
)