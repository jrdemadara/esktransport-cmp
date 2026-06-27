package org.noztek.esktransport.core.audio

import esktransport.shared.generated.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual class SoundEffectPlayer internal constructor() {
    private val activePlayers = mutableListOf<AVAudioPlayer>()

    actual fun play(effect: SoundEffect) {
        runCatching {
            val url = NSURL.URLWithString(Res.getUri(effect.resourcePath)) ?: return
            val player = AVAudioPlayer(contentsOfURL = url, error = null) ?: return
            activePlayers.removeAll { !it.playing }
            activePlayers.add(player)
            player.prepareToPlay()
            player.play()
        }.onFailure { error ->
            println("SoundEffectPlayer failed to play ${effect.name}: ${error.message}")
        }
    }
}

actual fun createSoundEffectPlayer(): SoundEffectPlayer = SoundEffectPlayer()
