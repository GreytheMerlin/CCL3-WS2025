package com.example.snorly.feature.alarm.ToneGenerator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sign
import kotlin.math.abs

enum class Instrument {
    SINE, SQUARE, SAWTOOTH, TRIANGLE
}

object ToneGenerator {
    private const val SAMPLE_RATE = 44100

    // 1. ORDERED LIST (For UI - Preserves C -> C Scale order)
    val SCALE_NOTES = listOf(
        "C4" to 261.63,
        "D4" to 293.66,
        "E4" to 329.63,
        "G4" to 392.00,
        "A4" to 440.00,
        "C5" to 523.25
    )

    // 2. LOOKUP MAP (For ViewModel - Fast access by name)
    // This fixes the "Unresolved reference: NOTES" error
    val NOTES = SCALE_NOTES.toMap()

    private fun generateWave(freq: Double, durationMs: Int, instrument: Instrument): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE / 1000)
        val sample = ShortArray(numSamples)
        val angularFrequency = 2.0 * PI * freq

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val phase = (freq * t) - (freq * t).toInt()

            val signal = when (instrument) {
                Instrument.SINE -> sin(angularFrequency * t)
                Instrument.SQUARE -> sign(sin(angularFrequency * t)) * 0.5
                Instrument.SAWTOOTH -> 2.0 * (phase - 0.5)
                Instrument.TRIANGLE -> 4.0 * abs(phase - 0.5) - 1.0
            }

            val progress = i.toDouble() / numSamples
            val envelope = when {
                progress < 0.05 -> progress / 0.05
                else -> Math.pow(1.0 - progress, 2.0)
            }

            sample[i] = (signal * envelope * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        return sample
    }

    suspend fun playNote(frequency: Double, instrument: Instrument, durationMs: Int = 500) = withContext(Dispatchers.Default) {
        val buffer = generateWave(frequency, durationMs, instrument)
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            delay(durationMs.toLong())
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}