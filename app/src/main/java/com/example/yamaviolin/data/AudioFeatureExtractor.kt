package com.example.yamaviolin.data

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.nio.ByteOrder

object AudioFeatureExtractor {
  private const val TAG = "AudioFeatureExtractor"

  // Decodes a compressed audio file and extracts the average amplitude envelope per second.
  // Returns an empty list if decoding fails or is unsupported, signaling fallback is required.
  fun extractAmplitudes(audioPath: String): List<Double> {
    val file = File(audioPath)
    if (!file.exists()) {
      Log.w(TAG, "Audio file does not exist: $audioPath")
      return emptyList()
    }

    val extractor = MediaExtractor()
    var codec: MediaCodec? = null
    val amplitudes = mutableListOf<Double>()

    try {
      extractor.setDataSource(audioPath)
      var audioTrackIndex = -1
      var format: MediaFormat? = null

      for (i in 0 until extractor.trackCount) {
        val trackFormat = extractor.getTrackFormat(i)
        val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
        if (mime.startsWith("audio/")) {
          audioTrackIndex = i
          format = trackFormat
          break
        }
      }

      if (audioTrackIndex == -1 || format == null) {
        Log.w(TAG, "No audio track found in file: $audioPath")
        return emptyList()
      }

      extractor.selectTrack(audioTrackIndex)
      val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
      codec = MediaCodec.createDecoderByType(mime)
      codec.configure(format, null, null, 0)
      codec.start()

      val sampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
        format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
      } else {
        44100
      }
      val channelCount = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
        format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
      } else {
        1
      }

      val samplesPerSecond = sampleRate * channelCount
      var currentSecondSamplesAccumulator = 0L
      var currentSecondSamplesCount = 0

      val info = MediaCodec.BufferInfo()
      var isInputEOS = false
      var isOutputEOS = false
      val timeoutUs = 10000L

      while (!isOutputEOS) {
        // Enforce maximum duration limit to protect resources on extremely large files
        if (amplitudes.size >= 300) {
          Log.i(TAG, "Analysis reached 300 seconds limit. Stopping extractor.")
          break
        }

        if (!isInputEOS) {
          val inputBufferIndex = codec.dequeueInputBuffer(timeoutUs)
          if (inputBufferIndex >= 0) {
            val inputBuffer = codec.getInputBuffer(inputBufferIndex)
            if (inputBuffer != null) {
              val sampleSize = extractor.readSampleData(inputBuffer, 0)
              if (sampleSize < 0) {
                codec.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                isInputEOS = true
              } else {
                val presentationTimeUs = extractor.sampleTime
                codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0)
                extractor.advance()
              }
            }
          }
        }

        val outputBufferIndex = codec.dequeueOutputBuffer(info, timeoutUs)
        if (outputBufferIndex >= 0) {
          val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
          if (outputBuffer != null && info.size > 0) {
            outputBuffer.order(ByteOrder.LITTLE_ENDIAN)
            val shortBuffer = outputBuffer.asShortBuffer()
            val length = info.size / 2

            for (i in 0 until length) {
              if (i < shortBuffer.limit()) {
                val sample = Math.abs(shortBuffer.get(i).toInt())
                currentSecondSamplesAccumulator += sample
                currentSecondSamplesCount++

                if (currentSecondSamplesCount >= samplesPerSecond) {
                  val avg = currentSecondSamplesAccumulator.toDouble() / currentSecondSamplesCount
                  amplitudes.add(avg)
                  currentSecondSamplesAccumulator = 0L
                  currentSecondSamplesCount = 0
                }
              }
            }
          }

          codec.releaseOutputBuffer(outputBufferIndex, false)

          if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            isOutputEOS = true
          }
        }
      }

      if (currentSecondSamplesCount > 0) {
        val avg = currentSecondSamplesAccumulator.toDouble() / currentSecondSamplesCount
        amplitudes.add(avg)
      }

      Log.i(TAG, "Successfully extracted ${amplitudes.size} seconds of amplitudes.")

    } catch (e: Exception) {
      Log.e(TAG, "Error extracting amplitudes: ${e.localizedMessage}", e)
      return emptyList()
    } finally {
      try {
        codec?.stop()
        codec?.release()
      } catch (e: Exception) {
        // ignore
      }
      try {
        extractor.release()
      } catch (e: Exception) {
        // ignore
      }
    }

    return amplitudes
  }

  // Generates simulated amplitudes for fallback or UI preview files.
  // Mimics volume variations with a few distinct spikes and quiet moments.
  fun generateFallbackAmplitudes(durationSeconds: Int): List<Double> {
    Log.d(TAG, "Generating fallback simulated amplitudes for $durationSeconds seconds.")
    val list = mutableListOf<Double>()
    for (i in 0 until durationSeconds) {
      val base = 5000.0
      val noise = Math.sin(i.toDouble() * 0.15) * 1800.0
      var amp = base + noise

      // Quiet sections
      if (durationSeconds >= 15 && i in (durationSeconds * 0.15).toInt()..(durationSeconds * 0.15).toInt() + 2) {
        amp = 150.0
      }
      // Peak sections
      if (durationSeconds >= 25 && i in (durationSeconds * 0.55).toInt()..(durationSeconds * 0.55).toInt() + 1) {
        amp = 26000.0
      }
      list.add(amp)
    }
    return list
  }
}
