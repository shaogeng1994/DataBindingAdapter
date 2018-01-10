package com.shao.databindingadapter.library

import com.shao.databindingadapter.library.base.BaseAdapter
import com.shao.databindingadapter.library.base.BaseMultiItemAdapter

/**
 * Created by Administrator on 2018/1/6.
 */
class SimpleMultiItemAdapter<T: BaseMultiItemAdapter.MultiItemEntity>
    : BaseMultiItemAdapter<T, BaseAdapter.ViewHolder> (ArrayList()) {


    override fun onBindContent(holder: ViewHolder?, t: T?) {
        holder?.binding?.setVariable(getVariableIdByType(t?.itemType), t)
    }
}