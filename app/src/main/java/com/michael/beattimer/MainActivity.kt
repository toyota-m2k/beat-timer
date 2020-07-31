package com.michael.beattimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var cs = ClockSound()
    var type = 0
    val ticker:TickerViewModel by lazy {
        TickerViewModel.getInstance(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ticker.observables.repeat.observe(this) { v->
            updateCounter()
        }
        ticker.observables.tick.observe(this) {
            updateCounter()
        }
        ticker.observables.paused.observe(this) {
            if(it == true) {
                pauseButton.text = "Resume"
            } else {
                pauseButton.text = "Pause"
            }
        }
        ticker.observables.alive.observe(this) {
            val alive = it==true
            squatButton.isEnabled = !alive
            hiitButton.isEnabled = !alive
            stopButton.isEnabled = alive
            pauseButton.isEnabled = alive
        }


//        val before = System.currentTimeMillis();
//        Log.d("Ticker", "Before:${before}")
//        cs.beepRaw(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE,5000)
//        val after = System.currentTimeMillis();
//        Log.d("Ticker", "After:${after} (${after-before})")

        squatButton?.setOnClickListener(this::squat)
        hiitButton?.setOnClickListener(this::hiit)
        pauseButton?.setOnClickListener(this::pause)
        stopButton?.setOnClickListener(this::stop)

//        var type = 1
//        var count = 0
//        val h = Handler()
//        val interval = 1000L
//        val runnable:Runnable = object:Runnable {
//            override fun run() {
//                cs.beepRaw(type,200)
//                h.postDelayed(this, interval)
//                count++;
//                if(count==3) {
//                    count = 0
//                    type++
//                    Log.d("Beep", "Type=${type}" )
//                }
//            }
//
//        }
//        runnable.run()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        ticker.observables.removeObservers(this)
        if(isFinishing) {
            ticker.stop()
        }
    }

    fun updateCounter() {
        counterText.text = "${ticker.repeat} - ${ticker.tick}"
    }

    fun squat(@Suppress("UNUSED_PARAMETER") view: View) {
        ticker.start(TickerViewModel.TICKS_SQUAT7, 10)
    }

    fun hiit(@Suppress("UNUSED_PARAMETER") view: View) {
        ticker.start(TickerViewModel.TICKS_HIIT, 8)
    }

    fun pause(@Suppress("UNUSED_PARAMETER") view: View) {
        if(pauseButton.text == "Pause") {
            ticker.pause()
        } else {
            ticker.resume()
        }
    }
    fun stop(@Suppress("UNUSED_PARAMETER") view: View) {
        ticker.stop()
    }


//    fun onNextBeep(@Suppress("UNUSED_PARAMETER") view: View) {
//        type++
//        textView.text = "${type}"
//        cs.stop()
//        cs.beepRaw(type,3000)
//    }
//
//    fun onPrevBeep(@Suppress("UNUSED_PARAMETER") view: View) {
//        if(type>0) {
//            type--
//        }
//        textView.text = "${type}"
//        cs.stop()
//        cs.beepRaw(type,3000)
//    }

}
