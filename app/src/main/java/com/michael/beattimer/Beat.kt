package com.michael.beattimer

import android.util.Log

interface IPlayer {
    suspend fun play(tick:TickType, nextTick:Boolean):Boolean
    fun putLabel(label:String)
    fun setLoopCount(count:Int)
    fun resetTickCount()
}

interface IBeat {
    suspend fun start(player:IPlayer):Boolean

    // forDebug
    val name:String
}

class Tick(override val name:String, private val tick:TickType, private val times:Int=1, private val nextTick:Boolean=true) : IBeat {
    override suspend fun start(player:IPlayer):Boolean {
        Log.d("BEAT", "${name} x ${times} (Tick)")
        for (i in 1..times) {
            if(!player.play(tick,nextTick)) {
                return false
            }
        }
        return true
    }

    companion object {
//        val singleTick = tick(1)
        val singleSmall = Tick("small-1", TickType.SMALL)
//        val singleMiddle = Tick("middle-1", TickType.MIDDLE)
        val period = Tick("pariod", TickType.PERIOD,1,false)

        fun silent(sec:Int) = Tick("silent-${sec} (inc=true)", TickType.SILENT,sec, true)
        fun rest(sec:Int) = Tick("rest-${sec} (inc=false)", TickType.SILENT,sec,false)
        fun tick(sec:Int, nextTick:Boolean=true) = Tick("tick-${sec} (inc=${nextTick})", TickType.TICK,sec,nextTick)
    }
}

object TickCounterReset :IBeat {
    override val name: String = "tickCounterReset"
    override suspend fun start(player: IPlayer): Boolean {
        player.resetTickCount()
        return true
    }
}

class Phrase(override val name:String, private val beats:List<IBeat>, val times:Int=1, private val label:String?=null, private val loopConductor:Boolean=false) : IBeat {
    constructor(name:String, beats:IBeat, times:Int=1, label:String?=null, loopConductor:Boolean=false):this(name, listOf(beats),times,label, loopConductor)
    override suspend fun start(player: IPlayer): Boolean {
        Log.d("BEAT", "${name} x ${times}(Phrase)")
        if(label!=null) {
            player.putLabel(label)
        }
        for(i in 1..times) {
            if(loopConductor) {
                player.setLoopCount(i)
            }
            for (b in beats) {
                if (!b.start(player)) {
                    return false
                }
            }
        }
        return true
    }

    fun dump(prefix:String="") {
        for(b in beats) {
            Log.d("BEAT", "${prefix}${b.name}")
            if(b is Phrase) {
                b.dump("${prefix}  ")
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        val prologue:IBeat = Phrase("prologue", listOf(Tick.tick(3,false), Tick("middle", TickType.MIDDLE, 1, false)))
        val epilogue:IBeat = Tick.period
        val tick5:IBeat = tick5sec()

        fun root(verse:IBeat, label:String?=null):IBeat {
            return Phrase("root", listOf(prologue, verse, epilogue), 1, label)
        }

        fun verse(beats:List<IBeat>, times:Int) : IBeat {
            return Phrase("verse", beats, times,null, true)
        }

        fun tick5sec(times:Int=1, label:String?=null):IBeat {
            return if(times==1) {
                ticksAndAccent(5, label)
            } else {
                val t = ticksAndAccent(5, label)
                Phrase("(${t.name})x${times}", t, times, label)
            }
        }

        fun ticksAndAccent(sec:Int, label:String?=null):IBeat {
            return Phrase("tickAndAccent(${sec})", listOf(Tick.tick(sec-1),Tick.singleSmall), 1, label)
        }
    }
}
