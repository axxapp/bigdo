package com.bigdo.controls;


import com.bigdo.app.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XListViewFooter extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;
	private int mState = STATE_NORMAL;
	LinearLayout moreView;
	// private Context mContext;
	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;
	private ImageView mArrowImageView;
	private RelativeLayout mContentView;
	private ProgressBar mProgressBar;
	private TextView mHintView;
	private final int ROTATE_ANIM_DURATION = 180;

	public XListViewFooter(Context context) {
		super(context);
		initView(context);
		// this.setBackgroundColor(color)
	}

	public XListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public void setState(int state) {
		if (state == mState) {
			return;
		}
		// mHintView.setVisibility(View.INVISIBLE);
		if (state == STATE_READY) {
			mProgressBar.setVisibility(View.GONE);
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.xlistview_footer_hint_ready);
			mArrowImageView.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.startAnimation(mRotateUpAnim);
		} else if (state == STATE_LOADING) {
			mProgressBar.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.xlistview_header_hint_loading);
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.GONE);
		} else {
			mProgressBar.setVisibility(View.GONE);
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.xlistview_footer_hint_normal);//
			mArrowImageView.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.startAnimation(mRotateDownAnim);
		}
		mState = state;
	}

	public void setBottomMargin(int height) {
		if (height < 0) {
			return;
		}
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}

	public int getBottomMargin() {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentView
				.getLayoutParams();
		return lp.bottomMargin;
	}

	public int getTopMargin() {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentView
				.getLayoutParams();
		return lp.topMargin;
	}

	public void setTopMargin(int topMargin) {
		if (topMargin < 0) {
			topMargin = 0;
		}
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) moreView
				.getLayoutParams();
		lp.topMargin = topMargin;
		moreView.setLayoutParams(lp);
		// moreView.setPadding(0, topMargin, 0, 0);
	}

	/**
	 * normal status
	 */
	public void normal() {
		mHintView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}

	/**
	 * loading status
	 */
	public void loading() {
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}

	/**
	 * show footer
	 */
	public void show() {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}

	private void initView(Context context) {
		setFocusable(true);
		setFocusableInTouchMode(true);
		setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		try {
			// mContext = context;
			moreView = (LinearLayout) LayoutInflater.from(context).inflate(
					R.layout.xlistview_footer, null);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, 0);
			addView(moreView, lp);
			setGravity(Gravity.TOP);
			// moreView.setLayoutParams(new LinearLayout.LayoutParams(
			// LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			mContentView = (RelativeLayout) moreView
					.findViewById(R.id.xlistview_footer_content);
			mProgressBar = (ProgressBar) moreView
					.findViewById(R.id.xlistview_footer_progressbar);
			mHintView = (TextView) moreView
					.findViewById(R.id.xlistview_footer_hint_textview);
			mArrowImageView = (ImageView) findViewById(R.id.xlistview_footer_arrow);

			mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
			mRotateUpAnim.setFillAfter(true);
			mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
			mRotateDownAnim.setFillAfter(true);

		} catch (Exception e) {
		}
	}

	public void setVisiableHeight(int height) {
		if (height < 0) {
			height = 0;
		}
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) moreView
				.getLayoutParams();
		lp.height = height;
		// Log.e("updateFooterHeight---", height + "");
		moreView.setLayoutParams(lp);
		// moreView.requestLayout();
	}

	public int getVisiableHeight() {
		return moreView.getHeight();
	}

	public int getWindowTop() {
		final int[] location = { 0, 0 };
		getLocationInWindow(location);
		return location[1];
	}

	public int getWindowBottom() {
		final int[] location = { 0, 0 };
		getLocationInWindow(location);
		return location[1] + moreView.getHeight();
	}

}