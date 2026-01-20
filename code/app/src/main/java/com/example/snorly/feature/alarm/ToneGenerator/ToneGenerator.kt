package com.example.snorly.feature.alarm.ToneGenerator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

enum class Instrument {
    SINE, SQUARE, SAWTOOTH, TRIANGLE
}

object ToneGenerator {
    private const val SAMPLE_RATE = 44100

    // SAFETY: Limit concurrent sounds to prevent "Cannot create AudioTrack" crash.
    // Android usually allows ~32, but we keep it safe at 10 to allow room for system sounds.
    private const val MAX_POLYPHONY = 10
    private val activeVoices = AtomicInteger(0)

    // CACHE: Store generated sound waves so we don't recalculate them on every tap.
    // Key format: "Frequency|Instrument"
    private val bufferCache = ConcurrentHashMap<String, ShortArray>()

    val SCALE_NOTES = listOf(
        "C4" to 261.63,
        "D4" to 293.66,
        "E4" to 329.63,
        "G4" to 392.00,
        "A4" to 440.00,
        "C5" to 523.25
    )

    /**
     * Retrieves a pre-generated buffer from cache, or generates it if missing.
     */
    private fun getOrGenerateBuffer(freq: Double, instrument: Instrument, durationMs: Int): ShortArray {
        val key = "$freq|${instrument.name}|$durationMs"
        return bufferCache.getOrPut(key) {
            generateWave(freq, durationMs, instrument)
        }
    }

    private fun generateWave(freq: Double, durationMs: Int, instrument: Instrument): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE / 1000)
        val sample = ShortArray(numSamples)
        val angularFrequency = 2.0 * PI * freq

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val phase = (freq * t) - (freq * t).toInt()

            val signal = when (instrument) {
                Instrument.SINE -> sin(angularFrequency * t)
                Instrument.SQUARE -> sign(sin(angularFrequency * t)) * 0.5 // Reduced volume for square
                Instrument.SAWTOOTH -> 2.0 * (phase - 0.5)
                Instrument.TRIANGLE -> 4.0 * abs(phase - 0.5) - 1.0
            }

            // Envelope: Fast Attack, Exponential Decay (Pluck style)
            val progress = i.toDouble() / numSamples
            val envelope = when {
                progress < 0.05 -> progress / 0.05
                else -> (1.0 - progress).pow(2.0)
            }

            sample[i] = (signal * envelope * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        return sample
    }

    suspend fun playNote(frequency: Double, instrument: Instrument, durationMs: Int = 500) = withContext(Dispatchers.Default) {
        // 1. SAFETY CHECK: If too many sounds are playing, ignore this tap.
        if (activeVoices.get() >= MAX_POLYPHONY) {
            Log.w("ToneGenerator", "Polyphony limit reached, skipping note.")
            return@withContext
        }

        activeVoices.incrementAndGet()

        // 2. FAST FETCH: Get audio data from RAM (Cache)
        val buffer = getOrGenerateBuffer(frequency, instrument, durationMs)

        var audioTrack: AudioTrack? = null

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // 3. WAIT: Let the sound play out
            delay(durationMs.toLong())

        } catch (e: Exception) {
            Log.e("ToneGenerator", "AudioTrack creation failed: ${e.message}")
        } finally {
            // 4. CLEANUP: Always release resources, even if stopped/crashed
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {
                // Ignore errors during release
            }
            activeVoices.decrementAndGet()
        }
    }
}