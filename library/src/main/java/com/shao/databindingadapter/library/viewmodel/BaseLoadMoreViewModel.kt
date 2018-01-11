package com.shao.databindingadapter.library.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.support.annotation.LayoutRes

abstract class BaseLoadMoreViewModel(@LayoutRes val layoutRes: Int, val viewModelId: Int): BaseObservable() {

    companion object {
        val STATUS_DEFAULT = 1
        val STATUS_LOADING = 2
        val STATUS_FAIL = 3
        val STATUS_END = 4
    }

    protected var mLoadMoreStatus = STATUS_DEFAULT


    @Bindable
    abstract fun isStatusDefault(): Boolean

    @Bindable
    abstract fun isStatusLoading(): Boolean

    @Bindable
    abstract fun isStatusFail(): Boolean

    @Bindable
    abstract fun isStatusEnd(): Boolean

    abstract fun setStatusDefault()

    abstract fun setStatusLoading()

    abstract fun setStatusFail()

    abstract fun setStatusEnd()



}