package com.michael.beattimer

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.michael.beattimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val ticker:TickerViewModel by lazy {
        TickerViewModel.getInstance(this)
    }
    lateinit var controls: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controls = ActivityMainBinding.inflate(layoutInflater)
        setContentView(controls.root)

        ticker.observables.loopCounter.observe(this) {
            updateCounter()
        }
        ticker.observables.tickCounter.observe(this) {
            updateCounter()
        }
        ticker.observables.paused.observe(this) {
            if(it == true) {
                controls.pauseButton.text = "Resume"
            } else {
                controls.pauseButton.text = "Pause"
            }
        }
        ticker.observables.busy.observe(this) {
            val alive = it==true
            controls.startButton.isEnabled = !alive
            controls.stopButton.isEnabled = alive
            controls.pauseButton.isEnabled = alive
            if(alive) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        ticker.observables.label.observe(this) {
            if(it!=null) {
                controls.trainingName.text = it
            }
        }

        controls.startButton.setOnClickListener(this::start)
        controls.pauseButton.setOnClickListener(this::pause)
        controls.stopButton.setOnClickListener(this::stop)

        controls.selectTraining.adapter = ArrayAdapter(this, R.layout.spinner_item, TrainingStore.trainingTitles).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        setOrientation(resources.configuration.orientation ==Configuration.ORIENTATION_LANDSCAPE)
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
        controls.counterText.text = "${ticker.loopCounter} - ${ticker.tickCounter}"
    }

    fun start(@Suppress("UNUSED_PARAMETER") view: View) {
        //Log.d("BT", selectTraining.selectedItem.toString())
        val t = TrainingStore.trainingOf(controls.selectTraining.selectedItem.toString()) ?: return
        (t.beat as Phrase?)?.dump()
        ticker.start(t.beat)
    }

    fun pause(@Suppress("UNUSED_PARAMETER") view: View) {
        if(controls.pauseButton.text == "Pause") {
            ticker.pause()
        } else {
            ticker.resume()
        }
    }
    fun stop(@Suppress("UNUSED_PARAMETER") view: View) {
        ticker.stop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setOrientation(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
    }

    private fun setOrientation(landscape:Boolean) {
        if(landscape) {
            controls.rootLayout.orientation = LinearLayout.HORIZONTAL
        } else {
            controls.rootLayout.orientation = LinearLayout.VERTICAL
        }
    }
}
