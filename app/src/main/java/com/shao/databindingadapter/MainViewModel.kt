package com.shao.databindingadapter

import android.databinding.BaseObservable
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.os.Handler

/**
 * Created by Administrator on 2018/1/9.
 */
class MainViewModel: BaseObservable() {

    val dataList = ObservableArrayList<DataItemViewModel>()

    val isRefreshing = ObservableBoolean(false)
    val isLoadComplete = ObservableBoolean(true)
    val isLoadEnd = ObservableBoolean(false)


    val mHandler = Handler()

    fun getData() {
        isLoadEnd.set(false)
        isRefreshing.set(true)
        mHandler.postDelayed({
            dataList.clear()
            dataList.addAll(ArrayList<DataItemViewModel>().apply {
                add(DataItemViewModel(Data("数据1", 0xffffffff.toInt())))
                add(DataItemViewModel(Data("数据2", 0xff00ffff.toInt())))
                add(DataItemViewModel(Data("数据3", 0xffff00ff.toInt())))
                add(DataItemViewModel(Data("数据4", 0xffffff00.toInt())))
                add(DataItemViewModel(Data("数据5", 0xff999999.toInt())))
                add(DataItemViewModel(Data("数据6", 0xffcccccc.toInt())))
            })
            isRefreshing.set(false)
        }, 1000)
    }


    fun addData() {
        mHandler.postDelayed({
            dataList.addAll(ArrayList<DataItemViewModel>().apply {
                add(DataItemViewModel(Data("添加数据1", 0xffffffff.toInt())))
                add(DataItemViewModel(Data("添加数据2", 0xff00ffff.toInt())))
                add(DataItemViewModel(Data("添加数据3", 0xffff00ff.toInt())))
                add(DataItemViewModel(Data("添加数据4", 0xffffff00.toInt())))
                add(DataItemViewModel(Data("添加数据5", 0xff999999.toInt())))
                add(DataItemViewModel(Data("添加数据6", 0xffcccccc.toInt())))
            })
            if (dataList.size > 20) {
                mHandler.postDelayed({isLoadEnd.set(true)}, 300)
            } else {
                isLoadComplete.set(true)
            }

        }, 1000)
    }

}