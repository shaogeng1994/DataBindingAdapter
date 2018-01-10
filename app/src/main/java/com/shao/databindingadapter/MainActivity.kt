package com.shao.databindingadapter

import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.shao.databindingadapter.databinding.ActivityMainBinding
import com.shao.databindingadapter.library.SimpleAdapter
import com.shao.databindingadapter.library.base.BaseAdapter

class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding
    lateinit var mViewModel: MainViewModel
    lateinit var mAdapter: SimpleAdapter<Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        bindViewModel()

        initRecycler()

        mBinding.mainSwipe.setOnRefreshListener { mViewModel.getData() }

//        mViewModel.getData()
    }
    private fun bindViewModel() {
        mViewModel = MainViewModel()

        mViewModel.isRefreshing.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                mBinding.mainSwipe.isRefreshing = (p0 as ObservableBoolean).get()
            }
        })

        mViewModel.isLoadComplete.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                val isLoadComplete = p0 as ObservableBoolean
                if (isLoadComplete.get()) {
                    mAdapter.loadMoreComplete()
                    isLoadComplete.set(false)
                }
            }
        })

        mViewModel.isLoadEnd.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                val isLoadEnd = p0 as ObservableBoolean
                if (isLoadEnd.get()) {
                    mAdapter.loadMoreEnd()
                }
            }
        })


        mBinding.mainViewModel = mViewModel
    }

    private fun initRecycler() {
        mAdapter = SimpleAdapter(R.layout.item_data, BR.dataItemViewModel)
        mBinding.mainRecycler.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = mAdapter
            mAdapter.setOnLoadMoreListener(object : BaseAdapter.OnLoadMoreListener {
                override fun onLoadMore() {
                    if (!mViewModel.isLoadEnd.get()) mViewModel.addData()
                }
            }, this)
        }
        mAdapter.addHeaderView(TextView(this).apply {
            text = "header"
            gravity = Gravity.CENTER
            height = dp2px(60f)
        })

        mAdapter.addFooterView(TextView(this).apply {
            text = "footer"
            gravity = Gravity.CENTER
            height = dp2px(60f)
        })

        mAdapter.setEmptyView(TextView(this).apply {
            text = "no data"
            gravity = Gravity.CENTER
        })
    }


    fun dp2px(dpValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }



    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        @BindingAdapter("bind:base_adapter_items")
        fun <T> setItems(recyclerView: RecyclerView?, data: List<T>?) {
            if (recyclerView == null) return
            if (recyclerView.adapter !is BaseAdapter<*, *>) {
                throw Exception("bind:base_adapter_items recyclerView's adapter must be BaseAdapter")
            }
            (recyclerView.adapter as BaseAdapter<T, *>).setNewData(data)
        }


        @JvmStatic
        @BindingAdapter("bind:background_color")
        fun setBackgroundColor(view: View?, color: Int?) {
            if (view == null) return
            view.setBackgroundColor(color?:0xffffff)
        }
    }
}
