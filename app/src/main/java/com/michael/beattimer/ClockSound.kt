package com.michael.beattimer

import android.media.AudioManager
import android.media.ToneGenerator
import android.media.ToneGenerator.MAX_VOLUME
import android.util.Log

enum class TickType(val toneType:Int, val duration:Int) {
    SILENT(-1,0),        // 音なし
    TICK(ToneGenerator.TONE_PROP_BEEP, 50),
    SMALL(ToneGenerator.TONE_DTMF_A, 50),
    MIDDLE(ToneGenerator.TONE_DTMF_B, 200),
    PERIOD(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 300),
}

class ClockSound {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, MAX_VOLUME)


    fun beep(type:TickType) {
        toneGenerator.startTone(type.toneType, type.duration)
    }

//    fun beepRaw(type:Int, duration:Int) {
//        toneGenerator.startTone(type, duration)
//    }
//
//    fun stop() {
//        toneGenerator.stopTone()
//    }
}