package jp.ikigai.toolbox.services

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.HandlerThread

class SoundPoolManager {
    private val handlerThread = HandlerThread("SoundPoolManager").apply { start() }
    private val handler = Handler(handlerThread.looper)
    private val fileSoundIdMap: MutableMap<String, Int> = mutableMapOf()
    private val soundIdStreamIdMap: MutableMap<Int, Int> = mutableMapOf()
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .build()

    fun playNotification(filePath: String) {
        handler.post {
            val soundId = fileSoundIdMap[filePath]
            if (soundId == null) {
                soundPool.setOnLoadCompleteListener { pool, soundID, status ->
                    if (status == 0) {
                        fileSoundIdMap[filePath] = soundID
                        val result = pool.play(
                            soundID,
                            1f,
                            1f,
                            0,
                            0,
                            1f,
                        )
                        if (result != 0) soundIdStreamIdMap[soundID] = result
                    }
                }
                soundPool.load(filePath, 1)
            } else {
                val streamId = soundIdStreamIdMap[soundId]
                if (streamId != null) {
                    soundPool.stop(streamId)
                }
                val result = soundPool.play(
                    soundId,
                    1f,
                    1f,
                    0,
                    0,
                    1f,
                )
                if (result != 0) soundIdStreamIdMap[soundId] = result
            }
        }
    }

    fun releaseSoundPool() {
        handler.post {
            soundIdStreamIdMap.values.forEach { soundPool.stop(it) }
            fileSoundIdMap.values.forEach { soundPool.unload(it) }
            soundPool.release()
            fileSoundIdMap.clear()
            soundIdStreamIdMap.clear()
            handlerThread.quitSafely()
        }
    }
}
