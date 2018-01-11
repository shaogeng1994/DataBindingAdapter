# DataBindingAdapter
This is a DataBinding-based RecyclerView Adapter written by Kotlin.

---

## Usage
1.item_data.xml
```
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:bind="http://schemas.android.com/apk/res-auto">
    <data>
        <variable
            name="dataItemViewModel"
            type="com.shao.databindingadapter.DataItemViewModel" />
    </data>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:textSize="16sp"
        android:gravity="center"
        android:textColor="@android:color/black"
        bind:background_color="@{dataItemViewModel.color}"
        android:text="@{dataItemViewModel.title}" />
</layout>
```

2.DataItemViewModel
```
class DataItemViewModel(val data: Data): BaseObservable() {

    @Bindable
    fun getTitle() = data.title?:""
    
    @Bindable
    fun getColor() = data.color?:0xffffff
}
```

3.in activity
```
val mAdapter = SimpleAdapter(R.layout.item_data, BR.dataItemViewModel, mData)
```

After these three steps, you can create a recyclerView adapter so easy.

## Thanks
[CymChad/BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)