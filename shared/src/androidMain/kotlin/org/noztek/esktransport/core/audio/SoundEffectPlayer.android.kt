package org.noztek.esktransport.core.audio

import android.content.Context
import android.media.MediaPlayer
import org.koin.core.context.GlobalContext

private const val ComposeResourceAssetRoot = "composeResources/asktransport_cmp.shared.generated.resources"

actual class SoundEffectPlayer internal constructor(
    private val context: Context,
) {
    actual fun play(effect: SoundEffect) {
        runCatching {
            val assetPath = "$ComposeResourceAssetRoot/${effect.resourcePath}"
            val descriptor = context.assets.openFd(assetPath)
            MediaPlayer().apply {
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                setOnCompletionListener { player -> player.release() }
                setOnErrorListener { player, _, _ ->
                    player.release()
                    true
                }
                prepare()
                start()
            }
        }.onFailure { error ->
            println("SoundEffectPlayer failed to play ${effect.name}: ${error.message}")
        }
    }
}

actual fun createSoundEffectPlayer(): SoundEffectPlayer {
    return SoundEffectPlayer(context = GlobalContext.get().get())
}
