package com.bigdo.controls;


import com.bigdo.app.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.AbsListView.OnScrollListener;

public class XListView extends ListView implements OnScrollListener {

	private float mLastY = 0, mFrist = 0; // save event y
	private Scroller mScroller, mFooterScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;
	private int mTouchSlop = 0;
	// -- header view
	private XListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent, mFooterViewContent;
	private boolean isPostInvali = false;
	private int mHeaderViewHeight, mFooterViewHeight; // header view's height
	private boolean mEnablePullRefresh = true, mPullRefreshing = false;
	private boolean mIsRefreshingOrLoading = false;
	// -- footer view
	private XListViewFooter mFooterView;
	private boolean mEnablePullLoad, oldMEnablePullLoad;
	private boolean mPullLoading;
	// private boolean mIsFooterReady = false;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount, mVisibleItemCount = 0,
			mLastVisibleItemCount = 0;

	// for mScroller, scroll back from header or footer.
	// private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0, SCROLLBACK_FOOTER = 1;

	// scroll back duration
	private final static int SCROLL_DURATION = 400;

	// when pull up >= 50px at bottom, trigger load more.
	// private final static int PULL_LOAD_MORE_DELTA = 50;

	// support iOS like pull feature.
	private final static float OFFSET_RADIO = 1.8f;

	public int tag;

	public final static int user_Hand_Operation = -1000000;

	ListAdapter adapter;

	/**
	 * @param context
	 */
	public XListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Log.w("headerDividersEnabled", headerDividersEnabled + "");
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		try {
			mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
			setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			mScroller = new Scroller(context, new DecelerateInterpolator());

			mFooterScroller = new Scroller(context,
					new DecelerateInterpolator());
			// XListView need the scroll event, and it will dispatch the event
			// to
			// user's listener (as a proxy).
			super.setOnScrollListener(this);

			// init header view
			mHeaderView = new XListViewHeader(context);
			mHeaderViewContent = (RelativeLayout) mHeaderView
					.findViewById(R.id.xlistview_header_content);

			addHeaderView(mHeaderView, null, true);

			// init footer view
			mFooterView = new XListViewFooter(context);

			addFooterView(mFooterView, null, true);

			mFooterViewContent = (RelativeLayout) mFooterView
					.findViewById(R.id.xlistview_footer_content);
			// init header height
			mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {
						public void onGlobalLayout() {
							mHeaderViewHeight = mHeaderViewContent.getHeight();
							getViewTreeObserver().removeGlobalOnLayoutListener(
									this);
						}
					});
			// init footer height
			mFooterView.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {
						public void onGlobalLayout() {
							mFooterViewHeight = mFooterViewContent.getHeight();
							getViewTreeObserver().removeGlobalOnLayoutListener(
									this);
						}
					});
		} catch (Exception e) {
		}

	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		this.adapter = adapter;
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) {
			mHeaderViewContent.setVisibility(View.GONE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		oldMEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterViewContent.setVisibility(View.GONE);
		} else {
			mFooterViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing) {
			mPullRefreshing = false;
			mHeaderView.setVisiableHeight(0);
			mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			invalidate();
			this.scrollBy(0, mHeaderView.getWindowTop() - getWindowTop());
			Log.e("stopRefresh", "true");
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading) {
			mPullLoading = false;
			mFooterView.setVisiableHeight(0);
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
			// this.scrollBy(0, -(getWindowTop() - mFooterView.getWindowTop()));
			invalidate();
		}

	}

	public void startLoadMore(int requestCode) {
		if (!mPullLoading) {
			mPullLoading = true;
			mFooterView.setState(XListViewFooter.STATE_LOADING);
			if (mListViewListener != null) {
				mListViewListener.onLoadMore(this, tag, requestCode);
			}
		}
	}

	public void startRefresh(int requestCode) {
		if (!mPullRefreshing) {
			mPullRefreshing = true;
			mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
			if (mListViewListener != null) {
				mListViewListener.onRefresh(this, tag, requestCode);
			}
		}
	}

	public void startAutoHeightRefresh(int requestCode) {
		if (!mPullRefreshing) {
			// 160 / OFFSET_RADIO

			mHeaderView.setVisiableHeight(0);
			this.scrollTo(0, 0);

			isHeaderFooterSelection = true;
			// resetHeaderHeight();
			setSelection(0);
			mHeaderView.setVisiableHeight((int) (dip2px(getContext(), 50)));
			// updateHeaderHeight(dip2px(getContext(), 50) * OFFSET_RADIO);
			mPullRefreshing = true;
			mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
			// resetHeaderHeight();
			if (mListViewListener != null) {
				mListViewListener.onRefresh(this, tag, requestCode);
			}
			// HiddenLoadMore();
		}
	}

	/**
	 * ����ֻ�ķֱ��ʴ� dp �ĵ�λ ת��Ϊ px(����)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * ����ֻ�ķֱ��ʴ� px(����) �ĵ�λ ת��Ϊ dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public void setEmptyView(View emptyView) {
		super.setEmptyView(emptyView);
		// this.getHitRect(outRect)
	}

	public boolean isHeaderHidden() {
		if (mHeaderView.getVisibility() == XListViewHeader.GONE
				|| mHeaderView.getVisibility() == XListViewHeader.INVISIBLE
				|| mHeaderView.getVisiableHeight() <= 0) {
			return true;
		}
		return false;
	}

	public void showLoadMore() {
		if (mFooterView != null) {
			mEnablePullLoad = oldMEnablePullLoad;
			mFooterView.setVisibility(XListViewFooter.VISIBLE);
			requestLayout();
			postInvalidate();
		}
	}

	public void hiddenLoadMore() {
		if (mFooterView != null) {
			mEnablePullLoad = false;
			mFooterView.setVisiableHeight(0);
			mFooterView.setVisibility(XListViewFooter.GONE);
			requestLayout();
			postInvalidate();
		}
	}

	private void hidFooter() {
		if (!isFristFooterSelection && mFooterView != null) {
			// mFooterView.setState(XListViewFooter.STATE_NORMAL);
			isFristFooterSelection = true;
			mFooterView.setVisiableHeight(0);
			requestLayout();
			postInvalidate();
		}
	}

	private void hidHeader() {
		if (!isHeaderFooterSelection && mHeaderView != null) {
			// mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			isHeaderFooterSelection = true;
			mHeaderView.setVisiableHeight(0);
			requestLayout();
			postInvalidate();
		}
	}

	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	boolean isHeaderFooterSelection = true;

	private void updateHeaderHeight(float delta) {
		if (mEnablePullRefresh) {
			if (isHeaderFooterSelection && _getCount() > 0) {
				isHeaderFooterSelection = false;
				setVerticalScrollBarEnabled(false);
				// setSelection(0);
				delta += (mHeaderView.getWindowTop() - getWindowTop());
				mHeaderView.requestFocus();
				mHeaderView.requestFocusFromTouch();
			}
			mHeaderView.setVisiableHeight((int) (delta / OFFSET_RADIO));
			// + mHeaderView.getVisiableHeight());
			if (!mPullRefreshing) {
				if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
					mHeaderView.setState(XListViewHeader.STATE_READY);
				} else {
					mHeaderView.setState(XListViewHeader.STATE_NORMAL);
				}
			}
		}
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		this.scrollBy(0, mHeaderView.getWindowTop() - getWindowTop());
		isHeaderFooterSelection = true;
		int height = mHeaderView.getVisiableHeight();
		if (height < 0) {
			height = 0;
		}
		int finalHeight = 0;
		if (mPullRefreshing) {
			if (height > mHeaderViewHeight) {
				finalHeight = mHeaderViewHeight - height;
			} else if (height < mHeaderViewHeight) {
				finalHeight = mHeaderViewHeight - height;
			} else {
				return;
			}
		} else {
			finalHeight = 0 - height;
		}
		if (finalHeight != 0) {
			// mScrollBack = SCROLLBACK_HEADER;
			mScroller.startScroll(0, height, 0, finalHeight, SCROLL_DURATION);
			//
		}

		postInvalidate();
	}

	boolean isFristFooterSelection = true;

	private void updateFooterHeight(float delta) {
		if (mEnablePullLoad) {
			if (isFristFooterSelection && _getCount() > 0) {
				isFristFooterSelection = false;
				setVerticalScrollBarEnabled(false);
				setSelection(_getCount());
				// delta += (getWindowBottom() - mFooterView.getWindowBottom());
			}
			// Log.e("updateFooterHeight", (int) (delta / OFFSET_RADIO) + "");
			mFooterView.setVisiableHeight((int) (delta / OFFSET_RADIO));
			if (!mPullLoading) {
				if (mFooterView.getVisiableHeight() > mFooterViewHeight) {
					mFooterView.setState(XListViewFooter.STATE_READY);
				} else {
					mFooterView.setState(XListViewFooter.STATE_NORMAL);
				}
			}
		}
	}

	private void resetFooterHeight() {
		isFristFooterSelection = true;
		int height = mFooterView.getVisiableHeight();
		if (height < 0) {
			height = 0;
		}
		int finalHeight = 0;
		if (mPullLoading) {
			if (height > mFooterViewHeight) {
				finalHeight = mFooterViewHeight - height;
			} else if (height < mFooterViewHeight) {
				finalHeight = mFooterViewHeight - height;
			} else {
				return;
			}
		} else {
			finalHeight = 0 - height;
		}
		if (finalHeight != 0) {
			mFooterScroller.startScroll(0, height, 0, finalHeight,
					SCROLL_DURATION);
			//
		}
		postInvalidate();
	}

	/*
	 * int atth = 0;
	 * 
	 * private void setFooterTopMargin() { atth = 0; if (mEnablePullLoad) { //
	 * measure(0, 0); int mh = getHeight(); int[] foorer_location = { 0, 0 };
	 * mFooterView.getLocationInWindow(foorer_location); int[]
	 * xListView_location = { 0, 0 }; getLocationInWindow(xListView_location);
	 * int xh = xListView_location[1] + mh, y = foorer_location[1];
	 * Log.w("XListView" + this.getId(), "h=" + mh + ",y=" + y + ",xh=" + xh);
	 * if (mh > 0 && y > 0 && xh > 0 && y < xh) { atth = xh - y;
	 * Log.w("TopMargin" + this.getId(), "y=" + atth); //
	 * mFooterView.setTopMargin(y); // invokeOnScrolling(); // invalidate(); } }
	 * }
	 */

	@Override
	public void setFooterDividersEnabled(boolean footerDividersEnabled) {
		super.setFooterDividersEnabled(footerDividersEnabled);

	}

	@Override
	public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
		super.setHeaderDividersEnabled(headerDividersEnabled);

	}

	public int getWindowTop() {
		final int[] location = { 0, 0 };
		getLocationInWindow(location);
		return location[1];
	}

	public int getWindowBottom() {
		final int[] location = { 0, 0 };
		getLocationInWindow(location);
		return location[1] + getHeight();
	}

	public int getScreenBottom() {
		final int[] location = { 0, 0 };
		getLocationOnScreen(location);
		return location[1] + this.getHeight();
	}

	// public boolean isRefreshingOrLoading() {
	// return mIsRefreshingOrLoading;
	// }

	/*
	 * OnItemClickListener _listener, __listener = new OnItemClickListener() {
	 * 
	 * @Override public void onItemClick(AdapterView<?> arg0, View arg1, int
	 * arg2, long arg3) { if (!isRefreshingOrLoading() && _listener != null) {
	 * _listener.onItemClick(arg0, arg1, arg2, arg3); } } };
	 * 
	 * @Override public void setOnItemClickListener(OnItemClickListener
	 * listener) { _listener = listener; if (listener == null) {
	 * super.setOnItemClickListener(null); } else {
	 * super.setOnItemClickListener(__listener); } }
	 * 
	 * OnItemLongClickListener _long_listener, __long_listener = new
	 * OnItemLongClickListener() {
	 * 
	 * @Override public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
	 * int arg2, long arg3) { if (!isRefreshingOrLoading() && _long_listener !=
	 * null) { return _long_listener.onItemLongClick(arg0, arg1, arg2, arg3); }
	 * return false; } };
	 * 
	 * @Override public void setOnItemLongClickListener(OnItemLongClickListener
	 * listener) { _long_listener = listener; if (listener == null) {
	 * super.setOnItemLongClickListener(null); } else {
	 * super.setOnItemLongClickListener(__long_listener); } }
	 */

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			resetHeaderAndFooterHeight();
			mIsRefreshingOrLoading = false;
			isLessTouchSlop = true;
			mLastY = ev.getRawY();
			mFrist = mLastY;
			fHeaderHeight = mHeaderView.getVisiableHeight();
			fFooterHeight = mFooterView.getVisiableHeight();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		default:
			resetHeaderAndFooterHeight();
			break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	float deltaY, fHeaderHeight = 0, fFooterHeight = 0;
	boolean isLessTouchSlop = true; // isNotHeaderProcessing = true

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.e("onTouchEvent", ev.getAction() + "," + mLastY + "");
		switch (ev.getAction()) {
		// case MotionEvent.ACTION_DOWN:
		// Log.e("onTouchEvent_ACTION_DOWN", mLastY + "");
		// break;
		case MotionEvent.ACTION_MOVE:
			deltaY = ev.getRawY() - mLastY;
			/*
			 * if (!mIsRefreshingOrLoading) { Log.e("mTouchSlop", mTouchSlop +
			 * ""); if (mTouchSlop >= Math.abs(deltaY)) { break; } else { if
			 * (isLessTouchSlop) { isLessTouchSlop = false; if (deltaY > 0) {
			 * mLastY = mFrist + mTouchSlop; } else { mLastY = mFrist -
			 * mTouchSlop; } // mFrist = mTouchSlop; deltaY = ev.getRawY() -
			 * mLastY; } } }
			 */
			// mLastY = ev.getRawY();
			// Log.e("onTouchEvent", deltaY + "," + _getLastVisiblePosition()
			// + "-" + _getCount() + "," + mEnablePullLoad);
			if (deltaY != 0) {
				// if (deltaY > 0) {
				if (deltaY > 0) {
					Log.e("deltaY", deltaY + "");
					if (mEnablePullRefresh
							&& (!mPullRefreshing && !mPullLoading && _getFirstVisiblePosition() <= 0)) {
						// deltaY += (mHeaderView.getWindowTop() -
						// getWindowTop());
						// deltaY / OFFSET_RADIO
						mIsRefreshingOrLoading = true;
						// Log.e("updateHeaderHeight__", (deltaY +
						// fHeaderHeight)
						// + "," + deltaY);
						updateHeaderHeight((deltaY + fHeaderHeight));
						if (!mPullLoading) {
							hidFooter();
						}
						// return true;
					}
				} else {
					Log.e("deltaY", deltaY + ",,,,,,"
							+ _getLastVisiblePosition() + "--" + _getCount());
					if (mEnablePullLoad && !mPullRefreshing

					&& (_getLastVisiblePosition() >= _getCount())) {
						deltaY = 0 - deltaY;
						// Log.e("mFooterView", (deltaY + fFooterHeight) + ",");
						mIsRefreshingOrLoading = true;
						updateFooterHeight((deltaY + fFooterHeight));
						// mFooterView.requestFocus();
						// mFooterView.requestFocusFromTouch();
						if (!mPullRefreshing) {
							hidHeader();
						}
						// return true;
					}
				}
			}
			break;
		default:
			resetHeaderAndFooterHeight();
			break;
		}
		return super.onTouchEvent(ev);
	}

	private void resetHeaderAndFooterHeight() {
		if (mIsRefreshingOrLoading) {
			mLastY = -1; // reset
			if (mEnablePullRefresh) {
				if (!mPullRefreshing && _getFirstVisiblePosition() <= 0) {
					// invoke refresh
					if (mHeaderView.getVisiableHeight() >= mHeaderViewHeight) {
						if (!mPullLoading) {
							hidFooter();
						}
						startRefresh(user_Hand_Operation);
					}
				}
				resetHeaderHeight();
			}
			// invoke load more.
			if (mEnablePullLoad) {
				if (!mPullLoading && (_getLastVisiblePosition() >= _getCount())) {
					if (mFooterView.getVisiableHeight() >= mFooterViewHeight) {
						if (!mPullRefreshing) {
							hidHeader();
						}
						startLoadMore(user_Hand_Operation);
					}
				}
				resetFooterHeight();
				// setSelection(getCount());
			}
			setVerticalScrollBarEnabled(true);
			mIsRefreshingOrLoading = false;
		}

	}

	private int _getCount() {
		int c = 0;
		if (adapter != null) {
			c = adapter.getCount();
			// c--;
			if (c < 0) {
				c = 0;
			}
		}
		return c;

	}

	private int _getFirstVisiblePosition() {
		int f = getFirstVisiblePosition();
		// f--;
		if (f < 0) {
			f = 0;
		}
		return f;
	}

	private int _getLastVisiblePosition() {
		int f = getLastVisiblePosition();
		f--;
		if (f < 0) {
			f = 0;
		}
		return f;
	}

	@Override
	public void computeScroll() {
		isPostInvali = false;
		if (mScroller.computeScrollOffset()) {
			if (mHeaderView.getVisiableHeight() != 0) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());

				if (!mPullRefreshing && mHeaderView.getVisiableHeight() != 0
						&& !mScroller.computeScrollOffset()) {
					// Log.e("computeScroll", "Header_compute");
					mHeaderView.setVisiableHeight(0);
					// this.requestLayout();
				}
			}
			isPostInvali = true;
		}
		if (mFooterScroller.computeScrollOffset()) {
			if (mFooterView.getVisiableHeight() != 0) {
				mFooterView.setVisiableHeight(mFooterScroller.getCurrY());
				if (!mPullLoading && mFooterView.getVisiableHeight() != 0
						&& !mFooterScroller.computeScrollOffset()) {
					// Log.e("computeScroll", "Footer_compute");
					mFooterView.setVisiableHeight(0);
					// this.requestLayout();
				}
			}
			isPostInvali = true;
		}
		if (isPostInvali) {
			postInvalidate();

		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		mScrollListener = listener;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mVisibleItemCount = visibleItemCount;
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}

	}

	public void setXListViewListener(IXListViewListener listener) {
		mListViewListener = listener;
	}

}