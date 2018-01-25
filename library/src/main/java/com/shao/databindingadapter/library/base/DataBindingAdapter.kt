/**
 * Copyright 2016 陈宇明
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shao.databindingadapter.library.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.IntRange
import android.support.annotation.LayoutRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.shao.databindingadapter.library.viewmodel.BaseLoadMoreViewModel
import com.shao.databindingadapter.library.viewmodel.SimpleLoadMoreViewModel
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 *
 * @param layoutRes layout id of item
 * @param variableId variable id of item
 * @param data
 */
abstract class DataBindingAdapter<T, VH : DataBindingAdapter.ViewHolder>(@LayoutRes private var layoutRes: Int,
                                                                                            private var variableId: Int,
                                                                                            private var data: List<T>? = null)
    : RecyclerView.Adapter<VH>() {

    companion object {
        val TAG = this::class.java.simpleName

        val HEADER_VIEW = 0x0000fff1
        val LOADING_VIEW = 0x0000fff2
        val FOOTER_VIEW = 0x0000fff3
        val EMPTY_VIEW = 0x0000fff4
    }

    var mLoading: Boolean = false
    private var loadMoreEnable = false
    private var nextLoadMore = false
    private var isUseEmptyView = false

    private var mHeaderLayout: LinearLayout? = null
    private var mFooterLayout: LinearLayout? = null
    private var mEmptyLayout: FrameLayout? = null
    private var mLoadMoreViewModel: BaseLoadMoreViewModel = SimpleLoadMoreViewModel()

    private var mRecyclerView: RecyclerView? = null

    private var mLayoutInflater: LayoutInflater? = null

    var mData: List<T> = ArrayList()

    private var mOnLoadMoreListener: OnLoadMoreListener? = null

    init {
        mData = data ?: ArrayList()
    }

    /**
     * check mRecyclerView [mRecyclerView] is null
     */
    private fun checkNotNull() {
        if (mRecyclerView == null) {
            throw RuntimeException("please bind recyclerView first!")
        }
    }

    /**
     * same as recyclerView.setAdapter(), and save the instance of recyclerView
     * @param recyclerView
     */
    fun bindToRecyclerView(recyclerView: RecyclerView) {
        if (mRecyclerView != null) {
            throw RuntimeException("Don't bind twice")
        }
        mRecyclerView = recyclerView
        mRecyclerView?.adapter = this
    }


    /**
     * This method display different data depending on the itemViewType [getItemViewType].
     * headers,footers,empty view and loading view.
     * call onBindContent [onBindContent] when it is not these views.
     *
     * @param holder holder of
     * @param position position of RecyclerView's item
     */
    override fun onBindViewHolder(holder: VH?, position: Int) {
        autoLoadMore(position)

        when (holder?.itemViewType) {
            HEADER_VIEW -> {}
            FOOTER_VIEW -> {}
            EMPTY_VIEW -> {}
            LOADING_VIEW -> holder.binding?.setVariable(mLoadMoreViewModel.viewModelId, mLoadMoreViewModel)
            else -> {
                onBindContent(holder, getItem(position - getHeaderLayoutCount()))
                holder?.binding?.executePendingBindings()
            }
        }

    }

    /**
     *
     * @param position
     */
    override fun getItemViewType(position: Int): Int {
        return if (getEmptyLayoutCount() == 1) EMPTY_VIEW
        else {
            val header = getHeaderLayoutCount()
            return if (position < header) {
                HEADER_VIEW
            } else {
                var adjPosition = position - header
                if (adjPosition < mData.size) {
                    return super.getItemViewType(position)
                } else {
                    adjPosition -= mData.size
                    val footer = getFooterLayoutCount()
                    if (adjPosition < footer) {
                        FOOTER_VIEW
                    } else {
                        LOADING_VIEW
                    }
                }

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        mLayoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            LOADING_VIEW -> getLoadingView(parent)
            HEADER_VIEW -> createBaseViewHolder(mHeaderLayout)
            FOOTER_VIEW -> createBaseViewHolder(mFooterLayout)
            EMPTY_VIEW -> createBaseViewHolder(mEmptyLayout)
            else -> onCreateDefViewHolder(parent, viewType)
        }
    }

    open fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): VH {
        val dataBinding = getItemDataBinding(layoutRes, parent)
//        dataBinding.executePendingBindings()
        val viewHolder = createBaseViewHolder(dataBinding.root)
        viewHolder.binding = dataBinding
        return viewHolder
    }

    /**
     * Get item view by DataBindingUtil
     */
    protected fun getItemDataBinding(@LayoutRes layoutResId: Int, parent: ViewGroup?): ViewDataBinding {
        return DataBindingUtil.inflate<ViewDataBinding>(mLayoutInflater, layoutResId, parent, false)
    }

    /**
     * Get item view by mLayoutInflater [mLayoutInflater]
     */
    protected fun getItemView(@LayoutRes layoutResId: Int, parent: ViewGroup): View? {
        return mLayoutInflater?.inflate(layoutResId, parent, false)
    }


    override fun getItemCount(): Int {
        return if (getEmptyLayoutCount() == 1) 1
        else getHeaderLayoutCount() + mData.size + getFooterLayoutCount() + getLoadMoreViewCount()
    }



    /**
     * data start
     */

    fun setNewData(data: List<T>? = null) {
        mData = data ?: ArrayList()
        if (mOnLoadMoreListener != null) {
            mLoading = false
            nextLoadMore = true
            loadMoreEnable = true
            mLoadMoreViewModel.setStatusDefault()
        }

        notifyDataSetChanged()
    }


    /**
     * add one new data in to certain location
     *
     * @param position the insert position
     * @param data the new data
     */
    fun addData(@IntRange(from = 0) position: Int, data: T) {
        (mData as ArrayList).add(position, data)
        notifyItemInserted(position + getHeaderLayoutCount())
//        notifyItemInserted(position)
        compatibilityDataSizeChanged(1)
    }

    /**
     * add one new data
     */
    fun addData(data: T) {
        (mData as ArrayList).add(data)
        notifyItemInserted(mData.size + getHeaderLayoutCount())
//        notifyItemInserted(mData.size)
        compatibilityDataSizeChanged(1)
    }


    /**
     * add new data in to certain location
     *
     * @param position the insert position
     * @param newData  the new data collection
     */
    fun addData(@IntRange(from = 0) position: Int, newData: Collection<T>) {
        (mData as ArrayList).addAll(position, newData)
//        notifyItemRangeInserted(position, newData.size)
        notifyItemRangeInserted(position + getHeaderLayoutCount(), newData.size)
        compatibilityDataSizeChanged(newData.size)
    }

    /**
     * add new data to the end of mData
     *
     * @param newData the new data collection
     */
    fun addData(newData: Collection<T>) {
        (mData as ArrayList).addAll(newData)
//        notifyItemRangeInserted(mData.size - newData.size, newData.size)
        notifyItemRangeInserted(mData.size - newData.size + getHeaderLayoutCount(), newData.size)
        compatibilityDataSizeChanged(newData.size)
    }


    /**
     * data end
     */


    /**
     * compatible getLoadMoreViewCount and getEmptyViewCount may change
     *
     * @param size Need compatible data size
     */
    private fun compatibilityDataSizeChanged(size: Int) {
        if (mData.size == size) {
            notifyDataSetChanged()
        }
    }


    /**
     * Get the data of list
     *
     * @return 列表数据
     */
    fun getData(): List<T> {
        return mData
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     * data set.
     * @return The data at the specified position.
     */
    fun getItem(@IntRange(from = 0) position: Int): T? {
        return if (position < mData.size)
            mData[position]
        else
            null
    }


    /**
     * loadMore start
     */


    /**
     * Load more view count
     *
     * @return 0 or 1
     */
    fun getLoadMoreViewCount(): Int {
        if (mOnLoadMoreListener == null || !loadMoreEnable) {
            return 0
        }
        if (!nextLoadMore) {
            return 0
        }
        return if (mData.isEmpty()) {
            0
        } else 1
    }


    fun getLoadMoreViewPosition(): Int {
        return getHeaderLayoutCount() + mData.size + getFooterLayoutCount()
    }


    /**
     * create the loadingView viewHolder by mLoadMoreViewModel [mLoadMoreViewModel]
     *
     * @see onCreateViewHolder
     * @return a viewHolder hold loading view
     */
    private fun getLoadingView(parent: ViewGroup): VH {
        val dataBinding = getItemDataBinding(mLoadMoreViewModel.layoutRes, parent)
        val viewHolder = createBaseViewHolder(dataBinding.root)
        viewHolder.binding = dataBinding
        return viewHolder
    }

    /**
     * Set the onLoadMoreListener and get the instance of mRecyclerView [mRecyclerView]
     * also make load more enable
     *
     * @param onLoadMoreListener
     * @param recyclerView
     */
    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener, recyclerView: RecyclerView) {
        mOnLoadMoreListener = onLoadMoreListener
        loadMoreEnable = true
        nextLoadMore = true
        mRecyclerView = recyclerView
    }


    fun setLoadMoreEnable(enable: Boolean) {
        loadMoreEnable = enable
        notifyItemChanged(getLoadMoreViewPosition())
    }

    /**
     * This method can call mOnLoadMoreListener to onLoadMore [mOnLoadMoreListener] when position is
     * last of mData.
     *
     * @see onBindViewHolder
     * @param position
     */
    private fun autoLoadMore(position: Int) {
        if (getLoadMoreViewCount() == 0) {
            return
        }
        if (itemCount == 1) {
            return
        }
        if (position < itemCount - 1) {
            return
        }
        if (loadMoreEnable && nextLoadMore && mLoadMoreViewModel.isStatusDefault()) {

            mLoadMoreViewModel.setStatusLoading()
            if (!mLoading) {
                mLoading = true
                if (mRecyclerView != null) {
                    mRecyclerView?.post({ mOnLoadMoreListener?.onLoadMore() })
                } else {
                    mOnLoadMoreListener?.onLoadMore()
                }
            }
        }

    }

    /**
     * change loadMoreViewModel status to default
     */
    fun loadMoreComplete() {
        if (getLoadMoreViewCount() == 0) {
            return
        }
        mLoadMoreViewModel.setStatusDefault()
        nextLoadMore = true
        mLoading = false
    }

    /**
     * change loadMoreViewModel status to fail
     */
    fun loadMoreFail() {
        if (getLoadMoreViewCount() == 0) {
            return
        }
        mLoadMoreViewModel.setStatusFail()
        nextLoadMore = false
        mLoading = false
        notifyItemChanged(getLoadMoreViewPosition())
    }

    /**
     * change loadMoreViewModel status to end
     */
    fun loadMoreEnd() {
        if (getLoadMoreViewCount() == 0) {
            return
        }
        mLoadMoreViewModel.setStatusEnd()
        loadMoreEnable = false
        mLoading = false
        notifyItemChanged(getLoadMoreViewPosition())
    }


    /**
     * loadMore end
     */


    /**
     * if has header return 1. else 0
     */
    fun getHeaderLayoutCount(): Int {
        return if (mHeaderLayout?.childCount?.compareTo(0) == 1) 1
        else 0
    }

    /**
     * if has footer return 1. else 0
     */
    fun getFooterLayoutCount(): Int {
        return if (mFooterLayout?.childCount?.compareTo(0) == 1) 1
        else 0
    }

    /**
     * if has empty view and mData is empty return 1. else 0
     */
    fun getEmptyLayoutCount(): Int {
        return if (mEmptyLayout == null) 0
        else if (mEmptyLayout?.childCount?.compareTo(0) == 0) 0
        else if (!isUseEmptyView) 0
        else if (!mData.isEmpty()) 0
        else 1
    }


    /**
     * Called when a view created by this adapter has been attached to a window.
     * simple to solve item will layout using all
     * [.setFullSpan]
     *
     * @param holder
     */
    override fun onViewAttachedToWindow(holder: VH?) {
        super.onViewAttachedToWindow(holder)
        val type = holder!!.itemViewType
        if (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW) {
            setFullSpan(holder)
        }
//        else {
//            addAnimation(holder)
//        }
    }

    /**
     * When set to true, the item will layout using all span area. That means, if orientation
     * is vertical, the view will have full width; if orientation is horizontal, the view will
     * have full height.
     * if the hold view use StaggeredGridLayoutManager they should using all span area
     *
     * @param holder True if this item should traverse all spans.
     */
    protected fun setFullSpan(holder: RecyclerView.ViewHolder) {
        if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            val params = holder
                    .itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView?.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = getItemViewType(position)
                    return if (isNeedFixedViewType(type)) manager.spanCount
                    else 1
                }
            }
        }
    }

    protected fun isNeedFixedViewType(type: Int): Boolean {
        return type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW
    }


    @Suppress("UNCHECKED_CAST")
    protected fun createBaseViewHolder(view: View?): VH {
        var temp: Class<*>? = javaClass
        var z: Class<*>? = null
        while (z == null && null != temp) {
            z = getInstancedGenericVHClass(temp)
            temp = temp.superclass
        }
        val vh: VH?
        // 泛型擦除会导致z为null
        vh = if (z == null) {
            ViewHolder(view) as VH
        } else {
            createGenericVHInstance(z, view)
        }
        return vh ?: ViewHolder(view) as VH
    }

    @Suppress("UNCHECKED_CAST")
    private fun createGenericVHInstance(z: Class<*>, view: View?): VH? {
        try {
            val constructor: Constructor<*>
            // inner and unstatic class
            if (z.isMemberClass && !Modifier.isStatic(z.modifiers)) {
                constructor = z.getDeclaredConstructor(javaClass, View::class.java)
                constructor.isAccessible = true
                return constructor.newInstance(this, view) as VH
            } else {
                constructor = z.getDeclaredConstructor(View::class.java)
                constructor.isAccessible = true
                return constructor.newInstance(view) as VH
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return null
    }


    private fun getInstancedGenericVHClass(z: Class<*>): Class<*>? {
        val type = z.genericSuperclass
        if (type is ParameterizedType) {
            val types = type.actualTypeArguments
            for (temp in types) {
                if (temp is Class<*>) {
                    if (ViewHolder::class.java.isAssignableFrom(temp)) {
                        return temp
                    }
                }
            }
        }
        return null
    }


    /**
     * Return root layout of header
     */

    fun getHeaderLayout(): LinearLayout? {
        return mHeaderLayout
    }

    /**
     * Return root layout of footer
     */
    fun getFooterLayout(): LinearLayout? {
        return mFooterLayout
    }

    /**
     * Append header to the rear of the mHeaderLayout.
     *
     * @param header
     */
    fun addHeaderView(header: View): Int {
        return addHeaderView(header, -1)
    }

    /**
     * Add header view to mHeaderLayout and set header view position in mHeaderLayout.
     * When index = -1 or index >= child count in mHeaderLayout,
     * the effect of this method is the same as that of [.addHeaderView].
     *
     * @param header
     * @param index  the position in mHeaderLayout of this header.
     * When index = -1 or index >= child count in mHeaderLayout,
     * the effect of this method is the same as that of [.addHeaderView].
     */
    fun addHeaderView(header: View, index: Int): Int {
        return addHeaderView(header, index, LinearLayout.VERTICAL)
    }

    /**
     * add the header view to mHeaderLayout [mHeaderLayout] depending on index and orientation
     *
     * @param header
     * @param index
     * @param orientation
     */
    fun addHeaderView(header: View, index: Int, orientation: Int): Int {
        var newIndex = index
        if (mHeaderLayout == null) {
            mHeaderLayout = LinearLayout(header.context)
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout?.orientation = LinearLayout.VERTICAL
                mHeaderLayout?.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            } else {
                mHeaderLayout?.orientation = LinearLayout.HORIZONTAL
                mHeaderLayout?.layoutParams = RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            }
        }
        val childCount = mHeaderLayout?.childCount?: 0
        if (index < 0 || index > childCount) {
            newIndex = childCount
        }
        mHeaderLayout?.addView(header, index)
        if (mHeaderLayout?.childCount == 1) {
            val position = getHeaderViewPosition()
            if (position != -1) {
                notifyItemInserted(position)
            }
        }
        return newIndex
    }

    fun setHeaderView(header: View): Int {
        return setHeaderView(header, 0, LinearLayout.VERTICAL)
    }

    fun setHeaderView(header: View, index: Int): Int {
        return setHeaderView(header, index, LinearLayout.VERTICAL)
    }


    fun setHeaderView(header: View, index: Int, orientation: Int): Int {
        return if (mHeaderLayout == null || mHeaderLayout!!.childCount <= index) {
            addHeaderView(header, index, orientation)
        } else {
            mHeaderLayout?.removeViewAt(index)
            mHeaderLayout?.addView(header, index)
            index
        }
    }

    /**
     * Append footer to the rear of the mFooterLayout.
     *
     * @param footer
     */
    fun addFooterView(footer: View): Int {
        return addFooterView(footer, -1, LinearLayout.VERTICAL)
    }

    fun addFooterView(footer: View, index: Int): Int {
        return addFooterView(footer, index, LinearLayout.VERTICAL)
    }

    /**
     * Add footer view to mFooterLayout and set footer view position in mFooterLayout.
     * When index = -1 or index >= child count in mFooterLayout,
     * the effect of this method is the same as that of [.addFooterView].
     *
     * @param footer
     * @param index  the position in mFooterLayout of this footer.
     * When index = -1 or index >= child count in mFooterLayout,
     * the effect of this method is the same as that of [.addFooterView].
     */
    fun addFooterView(footer: View, index: Int, orientation: Int): Int {
        var newIndex = index
        if (mFooterLayout == null) {
            mFooterLayout = LinearLayout(footer.context)
            if (orientation == LinearLayout.VERTICAL) {
                mFooterLayout?.orientation = LinearLayout.VERTICAL
                mFooterLayout?.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            } else {
                mFooterLayout?.orientation = LinearLayout.HORIZONTAL
                mFooterLayout?.layoutParams = RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            }
        }
        val childCount = mFooterLayout?.childCount?: 0
        if (index < 0 || index > childCount) {
            newIndex = childCount
        }
        mFooterLayout?.addView(footer, index)
        if (mFooterLayout?.childCount == 1) {
            val position = getFooterViewPosition()
            if (position != -1) {
                notifyItemInserted(position)
            }
        }
        return newIndex
    }

    fun setFooterView(header: View): Int {
        return setFooterView(header, 0, LinearLayout.VERTICAL)
    }

    fun setFooterView(header: View, index: Int): Int {
        return setFooterView(header, index, LinearLayout.VERTICAL)
    }

    fun setFooterView(header: View, index: Int, orientation: Int): Int {
        return if (mFooterLayout == null || mFooterLayout!!.childCount <= index) {
            addFooterView(header, index, orientation)
        } else {
            mFooterLayout?.removeViewAt(index)
            mFooterLayout?.addView(header, index)
            index
        }
    }

    /**
     * remove header view from mHeaderLayout.
     * When the child count of mHeaderLayout is 0, mHeaderLayout will be set to null.
     *
     * @param header
     */
    fun removeHeaderView(header: View) {
        if (getHeaderLayoutCount() == 0) return

        mHeaderLayout?.removeView(header)
        if (mHeaderLayout?.childCount == 0) {
            val position = getHeaderViewPosition()
            if (position != -1) {
                notifyItemRemoved(position)
            }
        }
    }

    /**
     * remove footer view from mFooterLayout,
     * When the child count of mFooterLayout is 0, mFooterLayout will be set to null.
     *
     * @param footer
     */
    fun removeFooterView(footer: View) {
        if (getFooterLayoutCount() == 0) return

        mFooterLayout?.removeView(footer)
        if (mFooterLayout?.childCount == 0) {
            val position = getFooterViewPosition()
            if (position != -1) {
                notifyItemRemoved(position)
            }
        }
    }

    /**
     * remove all header view from mHeaderLayout and set null to mHeaderLayout
     */
    fun removeAllHeaderView() {
        if (getHeaderLayoutCount() == 0) return

        mHeaderLayout?.removeAllViews()
        val position = getHeaderViewPosition()
        if (position != -1) {
            notifyItemRemoved(position)
        }
    }

    /**
     * remove all footer view from mFooterLayout and set null to mFooterLayout
     */
    fun removeAllFooterView() {
        if (getFooterLayoutCount() == 0) return

        mFooterLayout?.removeAllViews()
        val position = getFooterViewPosition()
        if (position != -1) {
            notifyItemRemoved(position)
        }
    }

    private fun getHeaderViewPosition(): Int {
        //Return to header view notify position
        return if (getEmptyLayoutCount() == 1) {
            -1
        } else {
            0
        }
    }

    private fun getFooterViewPosition(): Int {
        //Return to footer view notify position
        return if (getEmptyLayoutCount() == 1) {
            -1
        } else {
            getHeaderLayoutCount() + mData.size
        }
    }

    fun setEmptyView(layoutResId: Int, viewGroup: ViewGroup) {
        val view = LayoutInflater.from(viewGroup.context).inflate(layoutResId, viewGroup, false)
        setEmptyView(view)
    }

    /**
     * bind recyclerView [.bindToRecyclerView] before use!
     *
     * @see .bindToRecyclerView
     */
    fun setEmptyView(layoutResId: Int) {
        checkNotNull()
        setEmptyView(layoutResId, mRecyclerView!!)
    }

    fun setEmptyView(emptyView: View) {
        var insert = false
        if (mEmptyLayout == null) {
            mEmptyLayout = FrameLayout(emptyView.context)
            val layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
            val lp = emptyView.layoutParams
            if (lp != null) {
                layoutParams.width = lp.width
                layoutParams.height = lp.height
            }
            mEmptyLayout?.layoutParams = layoutParams
            insert = true
        }
        mEmptyLayout?.removeAllViews()
        mEmptyLayout?.addView(emptyView)
        isUseEmptyView = true
        if (insert) {
            if (getEmptyLayoutCount() == 1) {
                val position = 0
                notifyItemInserted(position)
            }
        }
    }

    /**
     * This interface will call onLoadMore() when it need load more.
     *
     * @see setOnLoadMoreListener
     * @see autoLoadMore
     */
    interface OnLoadMoreListener {
        fun onLoadMore()
    }


    /**
     * you can bind data to view.like bind viewModel to viewDataBinding in this abstract method
     */
    abstract fun onBindContent(holder: VH?, t: T?)


    open class ViewHolder(view: View? = null) : RecyclerView.ViewHolder(view) {
        var binding: ViewDataBinding? = null
    }
}