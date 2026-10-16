package com.cllarity.app.ui.theme

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Niezawodny silnik audio w stylu Apple (Sensory Sound Engine).
 * Łączy ultra-szybki natywny SoundPool ze wstępnie zsyntetyzowanymi mikro-samplami PCM
 * oraz systemowym AudioManager.playSoundEffect jako natychmiastowym zero-latency fallbackiem.
 */
class SoundFeedbackHelper(
    private val context: Context,
    private val view: View?,
    private val isSoundEnabled: Boolean
) {
    private var soundPool: SoundPool? = null
    private var woodblockSoundId: Int = -1
    private var chordSoundId: Int = -1
    private var tapSoundId: Int = -1

    init {
        if (isSoundEnabled) {
            try {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(attrs)
                    .build()

                // Przygotowujemy pliki wav w pamięci cache i ładujemy do SoundPool
                initSampleFiles()
            } catch (_: Exception) {}
        }
    }

    private fun initSampleFiles() {
        try {
            val sp = soundPool ?: return
            val cacheDir = context.cacheDir

            val woodFile = File(cacheDir, "zen_woodblock.wav")
            if (!woodFile.exists()) {
                writeWav(woodFile, generateTone(520f, 260f, 75, 14f, 0.45f))
            }
            woodblockSoundId = sp.load(woodFile.absolutePath, 1)

            val chordFile = File(cacheDir, "zen_chord.wav")
            if (!chordFile.exists()) {
                writeWav(chordFile, generateChord(523.25f, 659.25f, 130, 0.35f))
            }
            chordSoundId = sp.load(chordFile.absolutePath, 1)

            val tapFile = File(cacheDir, "zen_tap.wav")
            if (!tapFile.exists()) {
                writeWav(tapFile, generateTone(900f, 500f, 22, 28f, 0.25f))
            }
            tapSoundId = sp.load(tapFile.absolutePath, 1)
        } catch (_: Exception) {}
    }

    /**
     * Dyskretny, stłumiony klik mechaniczny (np. stepper +/-, zmiana zakładki).
     */
    fun tap() {
        if (!isSoundEnabled) return
        val sp = soundPool
        if (sp != null && tapSoundId > 0) {
            val streamId = sp.play(tapSoundId, 0.7f, 0.7f, 1, 0, 1.0f)
            if (streamId == 0) view?.playSoundEffect(SoundEffectConstants.CLICK)
        } else {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    /**
     * Dźwięk uderzenia w gładkie drewno (Woodblock Pop – zaliczenie nawyku).
     */
    fun woodblock() {
        if (!isSoundEnabled) return
        val sp = soundPool
        if (sp != null && woodblockSoundId > 0) {
            val streamId = sp.play(woodblockSoundId, 0.9f, 0.9f, 1, 0, 1.0f)
            if (streamId == 0) view?.playSoundEffect(SoundEffectConstants.CLICK)
        } else {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    /**
     * Aksamitny dwuton harmonijny (Apple Success Chord – podwójny pop sukcesu).
     */
    fun chord() {
        if (!isSoundEnabled) return
        val sp = soundPool
        if (sp != null && chordSoundId > 0) {
            val streamId = sp.play(chordSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
            if (streamId == 0) view?.playSoundEffect(SoundEffectConstants.CLICK)
        } else {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    /**
     * Kojący, głęboki ton fali oddechowej (Misa tybetańska / Zen Bell).
     */
    fun zenBreath() {
        if (!isSoundEnabled) return
        chord()
    }

    private fun generateTone(freqStart: Float, freqEnd: Float, durationMs: Int, decay: Float, vol: Float): ShortArray {
        val sampleRate = 22050
        val count = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val data = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = freqStart + (freqEnd - freqStart) * t
            val phase = 2.0 * PI * freq * (i.toDouble() / sampleRate)
            val env = exp(-decay * t) * (1.0 - exp(-35.0 * t))
            val sample = (sin(phase) * env * vol * Short.MAX_VALUE).toInt()
            data[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateChord(f1: Float, f2: Float, durationMs: Int, vol: Float): ShortArray {
        val sampleRate = 22050
        val count = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val data = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val p1 = 2.0 * PI * f1 * (i.toDouble() / sampleRate)
            val p2 = 2.0 * PI * f2 * (i.toDouble() / sampleRate)
            val env = exp(-9.0 * t) * (1.0 - exp(-25.0 * t))
            val sample = ((sin(p1) + sin(p2)) * 0.5 * env * vol * Short.MAX_VALUE).toInt()
            data[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun writeWav(file: File, pcmData: ShortArray) {
        val sampleRate = 22050
        val channels = 1
        val byteRate = sampleRate * channels * 2
        val dataSize = pcmData.size * 2
        val totalSize = 36 + dataSize

        FileOutputStream(file).use { out ->
            // RIFF header
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(totalSize))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16)) // subchunk1size (16 for PCM)
            out.write(shortToByteArray(1)) // audio format (1 = PCM)
            out.write(shortToByteArray(channels.toShort()))
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray((channels * 2).toShort())) // block align
            out.write(shortToByteArray(16)) // bits per sample
            out.write("data".toByteArray())
            out.write(intToByteArray(dataSize))

            val bytes = ByteArray(dataSize)
            var byteIdx = 0
            for (sample in pcmData) {
                bytes[byteIdx++] = (sample.toInt() and 0xFF).toByte()
                bytes[byteIdx++] = ((sample.toInt() shr 8) and 0xFF).toByte()
            }
            out.write(bytes)
        }
    }

    private fun intToByteArray(v: Int): ByteArray = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v shr 8) and 0xFF).toByte(),
        ((v shr 16) and 0xFF).toByte(),
        ((v shr 24) and 0xFF).toByte()
    )

    private fun shortToByteArray(v: Short): ByteArray = byteArrayOf(
        (v.toInt() and 0xFF).toByte(),
        ((v.toInt() shr 8) and 0xFF).toByte()
    )
}

@Composable
fun rememberSound(isSoundEnabled: Boolean = true): SoundFeedbackHelper {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(isSoundEnabled) {
        SoundFeedbackHelper(context.applicationContext, view, isSoundEnabled)
    }
}
