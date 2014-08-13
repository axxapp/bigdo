package com.bigdo.controls;

public interface IXListViewListener {
	public void onRefresh(XListView xListView, int tag, int requestCode);

	public void onLoadMore(XListView xListView, int tag, int requestCode);
}
