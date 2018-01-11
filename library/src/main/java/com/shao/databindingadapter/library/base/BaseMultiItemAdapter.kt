package com.shao.databindingadapter.library.base

import android.support.annotation.LayoutRes
import android.view.ViewGroup
import java.lang.IllegalArgumentException

abstract class BaseMultiItemAdapter<T : BaseMultiItemAdapter.MultiItemEntity
        , VH : DataBindingAdapter.ViewHolder>(multiData: List<T>? = null)
    : DataBindingAdapter<T, VH>(-1, -1, multiData) {

    val itemTypeSet: MutableSet<Int> = HashSet()
    val layoutResMap: MutableMap<Int, Int> = HashMap()
    val variableIdMap: MutableMap<Int, Int> = HashMap()


    override fun getItemViewType(position: Int): Int {
        return if (position < mData.size) {
            if (!itemTypeSet.contains(mData[position].itemType)) {
                throw IllegalArgumentException("can not find this itemType")
            }
            mData[position].itemType
        } else {
            super.getItemViewType(position)
        }
    }

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): VH {
        val dataBinding = getItemDataBinding(getLayoutResByType(viewType), parent)
        val viewHolder = createBaseViewHolder(dataBinding.root)
        viewHolder.binding = dataBinding
        return viewHolder
    }


    open fun getLayoutResByType(viewType: Int?) = layoutResMap[viewType]?: -1

    open fun getVariableIdByType(viewType: Int?) = variableIdMap[viewType]?: -1


    open fun addItemType(itemType: Int, @LayoutRes layoutRes: Int, variableId: Int) {
        if (!itemTypeSet.contains(itemType)) {
            itemTypeSet.add(itemType)
        }
        layoutResMap.put(itemType, layoutRes)
        variableIdMap.put(itemType, variableId)
    }



    interface MultiItemEntity {
        val itemType: Int
    }
}