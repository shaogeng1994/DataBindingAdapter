package com.shao.databindingadapter

import android.databinding.BaseObservable
import android.databinding.Bindable


class DataItemViewModel(val data: Data): BaseObservable() {


    @Bindable
    fun getTitle() = data.title?:""


    @Bindable
    fun getColor() = data.color?:0xffffff
}