package org.openfocus.app.ui.regain

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.random.Random

enum class AmbientSound(val label: String) {
    Off("Off"),
    White("White noise"),
    Brown("Deep brown noise")
}

/**
 * Plays endless focus noise generated in code (no audio files needed).
 * Call play() to start/switch, stop() when the session ends or the screen closes.
 */
class AmbientPlayer {
    private var track: AudioTrack? = null
    private var thread: Thread? = null

    @Volatile
    private var playing = false

    fun play(sound: AmbientSound) {
        stop()
        if (sound == AmbientSound.Off) return

        val sampleRate = 22050
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuffer.coerceAtLeast(4096))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        track = audioTrack
        playing = true
        audioTrack.play()

        thread = Thread {
            val buffer = ShortArray(2048)
            var last = 0f
            while (playing) {
                for (i in buffer.indices) {
                    val white = Random.nextFloat() * 2f - 1f
                    val sample = if (sound == AmbientSound.Brown) {
                        last = (last + 0.02f * white) / 1.02f
                        last * 3.5f
                    } else {
                        white * 0.5f
                    }
                    buffer[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE * 0.35f).toInt().toShort()
                }
                audioTrack.write(buffer, 0, buffer.size)
            }
        }.also { it.start() }
    }

    fun stop() {
        playing = false
        thread?.join(300)
        thread = null
        track?.let {
            try {
                it.stop()
            } catch (_: IllegalStateException) {
            }
            it.release()
        }
        track = null
    }
}
