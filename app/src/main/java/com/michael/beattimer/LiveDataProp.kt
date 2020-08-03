/*
 * Copyright 2020 toyota-m2k.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package com.michael.beattimer

import androidx.lifecycle.*
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * LiveDataでプロパティを定義すると、それを読み書きするときに、
 *   val a = prop.value
 *   prop.value = b
 * のように、いちいち、value を書く必要があって面倒くさいなぁ。
 * そこで、Kotlinの委譲プロパティを利用して、
 * LiveData<T> を prop.value のような書式ではなく、普通のT型のプロパティとして読み書きできるようにする
 *
 * クラス定義内で、
 *  val prop:Int by LiveDataProp(MutableLiveData<Int>(), 0)
 * のように定義する。（正確には、これではうまくいかない。。。PropHolderのコメント参照）
 */
class LiveDataProp<R,T>(val data: MutableLiveData<T>, val defValue:T) : ReadWriteProperty<R, T> {
    init { data.value = defValue }
    override fun getValue(thisRef: R, property: KProperty<*>): T {
        return data.value ?: defValue
    }
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        data.value = value
    }
}
class LiveDataNullableProp<R,T>(val data: MutableLiveData<T>, defValue:T?=null) : ReadWriteProperty<R, T?> {
    init { data.value = defValue }
    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        return data.value
    }
    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        data.value = value
    }
}

class ReadOnlyLiveDataProp<R,T>(val data: LiveData<T>, val defValue:T) : ReadOnlyProperty<R, T> {
    override fun getValue(thisRef: R, property: KProperty<*>): T {
        return data.value ?: defValue
    }
}
class ReadOnlyLiveDataNullableProp<R,T>(val data: LiveData<T>) : ReadOnlyProperty<R, T?> {
    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        return data.value
    }
}

/**
 * LiveData.observe()で、ラムダ式を使えるようにする拡張メソッド。
 * （今は、直接ラムダ式を渡せない。そのうち、Kotlinがサポートするようになるかもしれないが。）
 *
 * @param owner Activity or Fragment
 * @param fn: リスナー
 * @return 内部で生成した Observer （removeObserverするなら、どこかに覚えておく。不要なら無視してOK）
 */
fun <T> LiveData<T>.observe(owner: LifecycleOwner, fn:(v:T?)->Unit) : Observer<T?> {
    return Observer(fn).also {
        this.observe(owner, it)
    }
}

/**
 * LiveDataPropを使えば、LiveDataを隠蔽できると考えた。しかし、
 *  val prop:Int by LiveDataProp(MutableLiveData<Int>(), 0)
 * と書いてしまうと、中のMutableLiveDataに直接さわれなくなるので、肝心のObserverが登録できない！！（あたりまえ）
 * つまり、隠蔽したかった、MutableLiveDataも、ちゃんと定義しておく必要があるのだ。
 * 例）
 *  val obs_prop = MutableLiveData<Int>()
 *  val prop:Int by LiveDataProp(obs_prop, 0)
 * う～ん、かっこわるい。。。
 *
 * そこで思案。。。Mapがそのまま委譲プロパティとして使えることを利用して、LiveDataをひとまとめにして扱うときれいなんじゃね？
 * というのが、このPropHolderクラス。
 * 副産物として、Observerをまとめて解除するremoveObserversも実現できたので、アクティビティを回転するときなどに便利。
 *
 * 使い方
 *   // プロパティの定義
 *   class SomeModel {
 *      val propHolder = PropHolder()
 *      inner class Observables {
 *          val:propA:MutableLiveData<Int> by propHolder.propMap
 *          val:propB:MutableLiveData<String> by propHolder.propMap
 *          val:propC:MutableLiveData<Boolean> by propHolder.propMap
 *      }
 *      val observables = Observables()
 *
 *      var:propA:Int = propHolder.addProp("propA", MutableLiveData<Int>(), 0)
 *      var:propB:String = propHolder.addProp("propB", MutableLiveData<Int>(), "")
 *      var:propC:Boolean = propHolder.addProp("propC", MutableLiveData<Boolean>(), 0)
 *      ...
 *   }
 *
 *   // プロパティの参照
 *   class SomeActivity {
 *      val model = SomeModel()
 *      override fun onCreate(savedInstanceState: Bundle?) {
 *          ...
 *          // LiveDataには、model.observable経由でアクセス
 *          model.observables.propB.observe(activity) {
 *              // LiveDataの値は直接読み書き
 *              model.propA = "B=${it}:${model.propC}"
 *          }
 *          ...
 *      }
 *   }
 */
class PropHolder {
    val propMap = mutableMapOf<String,LiveData<*>>()

    /**
     * 読み書き可能な(var型の)プロパティを登録する
     * @return 委譲プロパティとして利用可能なLiveDataProp型インスタンス
     */
    fun <R,T> addProp(name:String, data:MutableLiveData<T>,defValue:T) : LiveDataProp<R,T> {
        propMap[name] = data
        return LiveDataProp(data,defValue)
    }
    fun <R,T> addNullableProp(name:String, data:MutableLiveData<T>,defValue:T?=null) : LiveDataNullableProp<R,T> {
        propMap[name] = data
        return LiveDataNullableProp(data,defValue)
    }
    /**
     * 読み取り専用の(val型の)プロパティを登録する
     * @return 委譲プロパティとして利用可能なReadOnlyLiveDataProp型インスタンス
     */
    fun <R,T> addReadOnlyProp(name:String, data:LiveData<T>,defValue:T) : ReadOnlyLiveDataProp<R,T> {
        propMap[name] = data
        return ReadOnlyLiveDataProp(data,defValue)
    }
    fun <R,T> addReadOnlyNullableProp(name:String, data:LiveData<T>) : ReadOnlyLiveDataNullableProp<R,T> {
        propMap[name] = data
        return ReadOnlyLiveDataNullableProp(data)
    }

    /**
     * すべてのObserverの登録を解除する
     */
    fun removeObservers(owner:LifecycleOwner) {
        for((_,data) in propMap) {
            data.removeObservers(owner)
        }
    }
}

