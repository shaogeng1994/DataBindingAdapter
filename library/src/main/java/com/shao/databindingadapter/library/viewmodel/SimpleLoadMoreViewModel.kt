package com.shao.databindingadapter.library.viewmodel

import com.shao.databindingadapter.library.BR
import com.shao.databindingadapter.library.R

/**
 * Created by Administrator on 2018/1/5.
 */
class SimpleLoadMoreViewModel: BaseLoadMoreViewModel(R.layout.view_load_more, BR.loadMoreViewModel) {
    override fun isStatusDefault() = mLoadMoreStatus == STATUS_DEFAULT

    override fun isStatusLoading() = mLoadMoreStatus == STATUS_LOADING

    override fun isStatusFail() = mLoadMoreStatus == STATUS_FAIL

    override fun isStatusEnd() = mLoadMoreStatus == STATUS_END

    override fun setStatusDefault() {
        mLoadMoreStatus = STATUS_DEFAULT
        notifyChange()
    }

    override fun setStatusLoading() {
        mLoadMoreStatus = STATUS_LOADING
        notifyChange()
    }

    override fun setStatusFail() {
        mLoadMoreStatus = STATUS_FAIL
        notifyChange()
    }

    override fun setStatusEnd() {
        mLoadMoreStatus = STATUS_END
        notifyChange()
    }

}