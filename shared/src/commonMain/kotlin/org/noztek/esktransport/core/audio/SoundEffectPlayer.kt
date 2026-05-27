package org.noztek.esktransport.core.audio

enum class SoundEffect(val resourcePath: String) {
    Tap("files/tap.mp3"),
    Online("files/online_1.mp3"),
    Alert("files/alert.mp3"),
    Denied("files/denied.mp3"),
    Close("files/close.mp3"),
}

expect class SoundEffectPlayer {
    fun play(effect: SoundEffect)
}

expect fun createSoundEffectPlayer(): SoundEffectPlayer
