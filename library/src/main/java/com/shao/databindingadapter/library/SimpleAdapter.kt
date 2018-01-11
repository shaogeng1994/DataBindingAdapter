package com.shao.databindingadapter.library

import android.support.annotation.LayoutRes
import com.shao.databindingadapter.library.base.DataBindingAdapter

/**
 * Created by Administrator on 2018/1/4.
 */
class SimpleAdapter<T> (@LayoutRes var layoutRes: Int, var variableId: Int)
    : DataBindingAdapter<T, DataBindingAdapter.ViewHolder>(layoutRes, variableId, ArrayList<T>()) {


    override fun onBindContent(holder: ViewHolder?, t: T?) {
        holder?.binding?.setVariable(variableId, t)
    }

}