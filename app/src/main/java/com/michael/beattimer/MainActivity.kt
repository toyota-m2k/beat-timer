package com.michael.beattimer

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val ticker:TickerViewModel by lazy {
        TickerViewModel.getInstance(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ticker.observables.loopCounter.observe(this) {
            updateCounter()
        }
        ticker.observables.tickCounter.observe(this) {
            updateCounter()
        }
        ticker.observables.paused.observe(this) {
            if(it == true) {
                pauseButton.text = "Resume"
            } else {
                pauseButton.text = "Pause"
            }
        }
        ticker.observables.busy.observe(this) {
            val alive = it==true
            startButton.isEnabled = !alive
            stopButton.isEnabled = alive
            pauseButton.isEnabled = alive
            if(alive) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        ticker.observables.label.observe(this) {
            if(it!=null) {
                trainingName.text = it
            }
        }

        startButton?.setOnClickListener(this::start)
        pauseButton?.setOnClickListener(this::pause)
        stopButton?.setOnClickListener(this::stop)

        selectTraining.adapter = ArrayAdapter(this, R.layout.spinner_item, TrainingStore.trainingTitles).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
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

    @SuppressLint("SetTextI18n")
    fun updateCounter() {
        counterText.text = "${ticker.loopCounter} - ${ticker.tickCounter}"
    }

    fun start(@Suppress("UNUSED_PARAMETER") view: View) {
        //Log.d("BT", selectTraining.selectedItem.toString())
        val t = TrainingStore.trainingOf(selectTraining.selectedItem.toString()) ?: return
        (t.beat as Phrase?)?.dump()
        ticker.start(t.beat)
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
}
