package com.shao.databindingadapter.library

import android.support.annotation.LayoutRes
import com.shao.databindingadapter.library.base.BaseAdapter

/**
 * Created by Administrator on 2018/1/4.
 */
class SimpleAdapter<T> (@LayoutRes var layoutRes: Int, var variableId: Int)
    : BaseAdapter<T, BaseAdapter.ViewHolder>(layoutRes, variableId, ArrayList<T>()) {


    override fun onBindContent(holder: ViewHolder?, t: T?) {
        holder?.binding?.setVariable(variableId, t)
    }

}