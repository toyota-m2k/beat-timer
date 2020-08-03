package com.michael.beattimer

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TickerViewModel : ViewModel(), IPlayer {
    companion object {
        val cs = ClockSound()
        fun getInstance(owner: FragmentActivity): TickerViewModel {
            return ViewModelProvider(owner, ViewModelProvider.NewInstanceFactory()).get(TickerViewModel::class.java)
        }
    }

    private var alive:Boolean = false

    val propHolder = PropHolder()
    inner class Observables {
        val tickCounter:MutableLiveData<Int> by propHolder.propMap
        val loopCounter:MutableLiveData<Int> by propHolder.propMap
        val busy:MutableLiveData<Boolean> by propHolder.propMap
        val pausing:MutableLiveData<Boolean> by propHolder.propMap
        val paused:MediatorLiveData<Boolean> by propHolder.propMap
        val label:MutableLiveData<String> by propHolder.propMap

        fun removeObservers(owner:LifecycleOwner) {
            propHolder.removeObservers(owner)
        }
    }
    val observables = Observables()

    var tickCounter:Int by propHolder.addProp("tickCounter", MutableLiveData(),1)
    var loopCounter:Int by propHolder.addProp("loopCounter", MutableLiveData(),1)
    private var label:String? by propHolder.addNullableProp("label", MutableLiveData())
    private var busy:Boolean by propHolder.addProp("busy", MutableLiveData(),false)
    private var pausing:Boolean by propHolder.addProp("pausing", MutableLiveData(),false)
    @Suppress("unused")
    private var paused:Boolean by propHolder.addProp("paused", MediatorLiveData<Boolean>().apply {
        val cb:(Boolean)->Unit = {_:Boolean-> pausing && alive}
        addSource(observables.busy,cb)
        addSource(observables.pausing,cb)
    },false)

    fun pause() {
        pausing = true
    }
    fun resume() {
        pausing = false
    }


    fun stop() {
        alive = false
    }

    fun start(target:IBeat) {
        if(busy) {
            return
        }

        busy = true
        alive = true
        pausing = false
        setLoopCount(1)

        CoroutineScope(Dispatchers.Main).launch {
            target.start(this@TickerViewModel)
            label = "Let's begin!"
            busy = false
            setLoopCount(1)
        }
    }

    override fun setLoopCount(count: Int) {
        loopCounter = count
        tickCounter = 0
    }

    override fun resetTickCount() {
        tickCounter = 0
    }

    override fun putLabel(label:String) {
        this.label = label
    }

    override suspend fun play(tick: TickType, nextTick:Boolean): Boolean {
        if(!alive) {
            return false
        }
        while(pausing) {
            delay(100)
            if(!alive) {
                return false
            }
        }
        if(nextTick) {
            this.tickCounter++
        }
        if(tick.toneType>=0) {
            cs.beep(tick)
        }
        for(c in 1..10) {
            if(!alive) {
                return false
            }
            delay(100)
        }
        return alive
    }
}