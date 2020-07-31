package com.michael.beattimer

import android.os.Handler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

class TickerViewModel : ViewModel() {
    companion object {
        val TICKS_SQUAT7: List<TickType> = listOf(
            TickType.TICK,
            TickType.TICK,
            TickType.TICK,
            TickType.TICK,
            TickType.MIDDLE,
            TickType.TICK,
            TickType.TICK,
            TickType.REST,
            TickType.REST
        )
        val TICKS_HIIT: List<TickType> = listOf(
            TickType.TICK, TickType.TICK, TickType.TICK, TickType.TICK, TickType.SMALL,   // 5
            TickType.TICK, TickType.TICK, TickType.TICK, TickType.TICK, TickType.SMALL,   // 10
            TickType.TICK, TickType.TICK, TickType.TICK, TickType.TICK, TickType.SMALL,   // 15
            TickType.TICK, TickType.TICK, TickType.TICK, TickType.TICK, TickType.MIDDLE,   // 20
            TickType.SILENT, TickType.SILENT, TickType.SILENT, TickType.SILENT, TickType.SILENT,
            TickType.SILENT, TickType.SILENT, TickType.TICK, TickType.TICK, TickType.MIDDLE
        )
        val cs = ClockSound()
        fun getInstance(owner: FragmentActivity): TickerViewModel {
            return ViewModelProvider(owner, ViewModelProvider.NewInstanceFactory()).get(TickerViewModel::class.java)
        }
    }

    private var cancel:Boolean = false

    val propHolder = PropHolder()
    inner class Observables {
        val tick:MutableLiveData<Int> by propHolder.propMap
        val repeat:MutableLiveData<Int> by propHolder.propMap
        val alive:MutableLiveData<Boolean> by propHolder.propMap
        val pausing:MutableLiveData<Boolean> by propHolder.propMap
        val paused:MediatorLiveData<Boolean> by propHolder.propMap

        fun removeObservers(owner:LifecycleOwner) {
            propHolder.removeObservers(owner)
        }
    }
    val observables = Observables()

    var tick:Int by propHolder.addProp("tick", MutableLiveData<Int>(),1)
    var repeat:Int by propHolder.addProp("repeat", MutableLiveData<Int>(),1)
    var alive:Boolean by propHolder.addProp("alive", MutableLiveData<Boolean>(),false)
    var pausing:Boolean by propHolder.addProp("pausing", MutableLiveData<Boolean>(),false)
    var paused:Boolean by propHolder.addProp("paused", MediatorLiveData<Boolean>().apply {
        val cb:(Boolean)->Unit = {_:Boolean-> pausing && alive}
        addSource(observables.alive,cb)
        addSource(observables.pausing,cb)
    },false)
        private set

    fun start(ticks:List<TickType>, loopCount:Int) {
        if(alive) {
            return
        }

        cancel = false
        repeat = 1
        tick = 1
        pausing = false
        alive = true

        var repeated = 0
        var index = 0
        val h = Handler()
        var last = false
        val runnable:Runnable = object:Runnable {
            override fun run() {
                if(!pausing) {
                    val t = ticks[index]
                    if (t.duration > 0) {
                        tick = index+1
                    }
                    if (t.toneType >= 0) {
                        cs.beep(t)
                    }
                    index++
                    if (index >= ticks.count()) {
                        index = 0
                        repeated++
                        if (repeated < loopCount) {
                            repeat = repeated + 1
                        } else {
                            last = true
                        }
                    }
                }
                if(!cancel&&!last) {
                    h.postDelayed(this, 1000)
                } else {
                    if(last) {
                        cs.beep(TickType.PERIOD)
                    }
                    alive = false
                }
            }

        }
        runnable.run()
    }

    fun pause() {
        pausing = true
    }
    fun resume() {
        pausing = false
    }


    fun stop() {
        cancel = true
    }

}