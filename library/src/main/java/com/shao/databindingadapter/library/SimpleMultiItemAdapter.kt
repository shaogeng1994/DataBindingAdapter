package com.shao.databindingadapter.library

import com.shao.databindingadapter.library.base.BaseMultiItemAdapter
import com.shao.databindingadapter.library.base.DataBindingAdapter

/**
 * Created by Administrator on 2018/1/6.
 */
class SimpleMultiItemAdapter<T: BaseMultiItemAdapter.MultiItemEntity>
    : BaseMultiItemAdapter<T, DataBindingAdapter.ViewHolder> (ArrayList()) {


    override fun onBindContent(holder: ViewHolder?, t: T?) {
        holder?.binding?.setVariable(getVariableIdByType(t?.itemType), t)
    }
}