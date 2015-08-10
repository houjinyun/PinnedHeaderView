/**
 * Copyright © 2012-2013 Hangzhou Enniu Tech Ltd. All right reserved.
 */
package com.hjy.pinnedheaderlistview;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

/**
 * @author houjinyun
 */
public class PinnedHeaderExpandableListView extends ExpandableListView {

	private static final String TAG = "PinnedHeaderExpandableListView";
	
	private static class PinnedView {
		public View view;
		public int position;
	}
	
	private ExpandableListAdapter mAdapter;
	
	/** 
	 * 设置单独的滑动监听器，可以为空
	 */
	private OnScrollListener mDelegateOnScrollListener;
	
	private OnGroupExpandListener mDelegateOnGroupExpandListener;
	private OnGroupCollapseListener mDelegateOnGroupCollapseListener;
	
	/**
	 * 当前显示pinned header view
	 */
	private PinnedView mCurrPinnedView = null;
	/**
	 * 回收的pinned header view
	 */
	private PinnedView mRecyclePinnedView = null;
	/**
	 * 在新旧2个pinned header交替的时候，会有一个过渡，记录这个偏移量
	 */
	private int mTranslateY;
	
	/**
	 * 填充pinned header view，主要是为了实现pinned header的点击事件，可以为空
	 */
	private FrameLayout mPinnedHeaderContainer;
	
	private Handler mHandler = new Handler();
	
	/**
	 * @param context
	 */
	public PinnedHeaderExpandableListView(Context context) {
		super(context);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public PinnedHeaderExpandableListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setOnScrollListener(mOnScrollListener);
		setOnGroupExpandListener(mOnGroupExpandListener);
		setOnGroupCollapseListener(mOnGroupCollapseListener);
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener l) {
		//title置顶的主要逻辑已实现在mOnScrollListener里，此外还可以设置另外的listener
		if(l == mOnScrollListener) {
			super.setOnScrollListener(l);
		} else {
			mDelegateOnScrollListener = l;			
		}
	}
	
	@Override
	public void setOnGroupExpandListener(
			OnGroupExpandListener onGroupExpandListener) {
		if(onGroupExpandListener == mOnGroupExpandListener)
			super.setOnGroupExpandListener(onGroupExpandListener);
		else {
			mDelegateOnGroupExpandListener = onGroupExpandListener;
		}
	}
	
	@Override
	public void setOnGroupCollapseListener(
			OnGroupCollapseListener onGroupCollapseListener) {
		if(onGroupCollapseListener == mOnGroupCollapseListener)
			super.setOnGroupCollapseListener(onGroupCollapseListener);
		else {
			mDelegateOnGroupCollapseListener = onGroupCollapseListener;
		}
	}
	
	private OnGroupExpandListener mOnGroupExpandListener = new OnGroupExpandListener() {

		@Override
		public void onGroupExpand(int groupPosition) {
			if(mDelegateOnGroupExpandListener != null)
				mDelegateOnGroupExpandListener.onGroupExpand(groupPosition);
			Log.d(TAG, "onGroupExpand()....");
			update();
			mHandler.postDelayed(mRefreshRunnable, 300);
		}		
	};
	
	private OnGroupCollapseListener mOnGroupCollapseListener = new OnGroupCollapseListener() {
		@Override
		public void onGroupCollapse(int groupPosition) {
			if(mDelegateOnGroupCollapseListener != null)
				mDelegateOnGroupCollapseListener.onGroupCollapse(groupPosition);
			update();
			mHandler.postDelayed(mRefreshRunnable, 300);
		}
	}; 
	
	private void update() {
		int firstVisibleItem = getFirstVisiblePosition();
		int visibleItemCount = getChildCount();
		int totalItemCount = getAdapter().getCount();
		updatePinnedHeaderListView(PinnedHeaderExpandableListView.this, firstVisibleItem, visibleItemCount, totalItemCount);
		updatePinnedHeaderContainer();
	}
	
	private Runnable mRefreshRunnable = new Runnable() {
		@Override
		public void run() {
			update();
		}
	};
	
	public void setAdapter(ExpandableListAdapter adapter) {
		recyclePinnedHeader();
		updatePinnedHeaderContainer();
		super.setAdapter(adapter);
		mAdapter = adapter;
		try {
			if(mAdapter != null) {
				mAdapter.registerDataSetObserver(mDataSetObserver);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			Log.d("DataSetObserver", "onChanged()");
			int firstVisibleItem = getFirstVisiblePosition();
			int visibleItemCount = getChildCount();
			int totalItemCount = getAdapter().getCount();
			updatePinnedHeaderListView(PinnedHeaderExpandableListView.this, firstVisibleItem, visibleItemCount, totalItemCount);
			updatePinnedHeaderContainer();
		}
		
		public void onInvalidated() {
			Log.d("DataSetObserver", "onInvalidated()");
			int firstVisibleItem = getFirstVisiblePosition();
			int visibleItemCount = getChildCount();
			int totalItemCount = getAdapter().getCount();
			updatePinnedHeaderListView(PinnedHeaderExpandableListView.this, firstVisibleItem, visibleItemCount, totalItemCount);
			updatePinnedHeaderContainer();			
		};
		
	};
	
	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if(mDelegateOnScrollListener != null) {
				mDelegateOnScrollListener.onScrollStateChanged(view, scrollState);
			}
			if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				Log.d("TAG", "onScroll state changed : idle");
				updatePinnedHeaderContainer();
			} else {
				Log.d("TAG", "onScroll state changed : scrolling");
				if(mPinnedHeaderContainer != null && mPinnedHeaderContainer.getVisibility() != View.GONE)
					mPinnedHeaderContainer.setVisibility(View.GONE);
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {		
	//		Log.d(TAG, String.format("onScroll: firstVisibleItem=%d,visibleItemCount=%d,totalItemCount=%d", firstVisibleItem, visibleItemCount, totalItemCount));
			if(mDelegateOnScrollListener != null) {
				mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
			updatePinnedHeaderListView(view, firstVisibleItem, visibleItemCount, totalItemCount);			
		}
		
	};

	private void updatePinnedHeaderContainer() {
		if (mPinnedHeaderContainer != null) {
			if (mCurrPinnedView != null && mAdapter != null) {
				View header = mPinnedHeaderContainer.getChildAt(0);
				if(header != null) {
					Log.d("TAG", "update conatainer....");
					mAdapter.getGroupView(mCurrPinnedView.position, isGroupExpanded(mCurrPinnedView.position), header, null);
					mPinnedHeaderContainer.scrollTo(0, -mTranslateY);
					mPinnedHeaderContainer.setVisibility(View.VISIBLE);
					mPinnedHeaderContainer.invalidate();
				} else {
					mPinnedHeaderContainer.setVisibility(View.GONE);
				}
			} else {
				mPinnedHeaderContainer.setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		Log.d("TAG", "invalidate()...");
	}
	
	private void updatePinnedHeaderListView(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(mAdapter == null || visibleItemCount == 0)
			return;

		if(mDelegateOnScrollListener != null) {
			mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
		
		long packedPosition = getExpandableListPosition(firstVisibleItem); 
		if(packedPosition == PACKED_POSITION_VALUE_NULL) {
			recyclePinnedHeader();
			return;
		}
		int groupPosition = getPackedPositionGroup(packedPosition);
		Log.d(TAG, "firstVisibleItem = " + firstVisibleItem + " group position = " + groupPosition);
		if(mCurrPinnedView == null) {
			createPinnedHeader(groupPosition);
		} else {
			if(mCurrPinnedView.position == groupPosition) {
				int nextGroupPosition = findNextGroupPosition(firstVisibleItem, visibleItemCount);
				Log.d(TAG, "next group = " + nextGroupPosition);
				if(nextGroupPosition != -1) {
					View nextGroupView = getChildAt(nextGroupPosition - firstVisibleItem);
					int top = nextGroupView.getTop();
					mTranslateY = top - mCurrPinnedView.view.getHeight() - getListPaddingTop();
					if(mTranslateY > 0)
						mTranslateY = 0;
				} else {
					mTranslateY = 0;
				}
			} else if(groupPosition < mCurrPinnedView.position){	//手指从上往下滑，临界点
				recyclePinnedHeader();
				createPinnedHeader(groupPosition);
				int nextGroupPosition = findNextGroupPosition(firstVisibleItem, visibleItemCount);
				if(nextGroupPosition != -1) {
					View nextGroupView = getChildAt(nextGroupPosition - firstVisibleItem);
					int top = nextGroupView.getTop();
					if(top < mCurrPinnedView.view.getHeight() + getListPaddingTop()) {
						mTranslateY = top - mCurrPinnedView.view.getHeight() - getListPaddingTop();
					} else {
						mTranslateY = 0;
					}
				} else {
					mTranslateY = 0;
				}
			} else if(groupPosition > mCurrPinnedView.position) { 	//手指从下往上滑，临界点
				recyclePinnedHeader();
				createPinnedHeader(groupPosition);
				View currView = getChildAt(0);
				int top = currView.getTop();
				mTranslateY = top - mCurrPinnedView.view.getHeight() - getListPaddingTop();
				if(mTranslateY > 0)
					mTranslateY = 0;
			}
		}
	}
	
	private int findNextGroupPosition(int firstVisibleItem, int visibleItemCount) {
		for(int i=1; i<visibleItemCount; i++) {
			int position = firstVisibleItem + i;
			long packedPosition = getExpandableListPosition(position);
			int type = getPackedPositionType(packedPosition);
			if(type == PACKED_POSITION_TYPE_GROUP)
				return position;
		}
		return -1;
	}

	/**
	 * 当期的pinned header即将消失，记录上一个pinned header
	 */
	private void recyclePinnedHeader() {
		mRecyclePinnedView = mCurrPinnedView;
		mCurrPinnedView = null;
	}
	
	/**
	 * 创建新的pinned header
	 * 
	 * @param position
	 */
	private void createPinnedHeader(int groupPosition) {
		if(mAdapter == null)
			return;
		//利用recycle view
		PinnedView pinnedView = mRecyclePinnedView;
		View recycleView = pinnedView == null ? null : pinnedView.view;
		mRecyclePinnedView = null;
		
		View pinnedHeader = mAdapter.getGroupView(groupPosition, isGroupExpanded(groupPosition), recycleView, this);
		ViewGroup.LayoutParams params = pinnedHeader.getLayoutParams();
		if(params == null) {
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
		int heightMeasureSpec;
		//确切的大小
		if(params.height > 0) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
		} else {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		pinnedHeader.setLayoutParams(params);
		pinnedHeader.measure(widthMeasureSpec, heightMeasureSpec);
		pinnedHeader.layout(0, 0, pinnedHeader.getMeasuredWidth(), pinnedHeader.getMeasuredHeight());
		mTranslateY = 0;
		
		if(pinnedView == null)
			pinnedView = new PinnedView();
		pinnedView.position = groupPosition;
		pinnedView.view = pinnedHeader;
		mCurrPinnedView = pinnedView; 
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if(mCurrPinnedView != null) {
			canvas.save();
			int paddingLeft = getListPaddingLeft();
			int paddingTop = getListPaddingTop();
			View view = mCurrPinnedView.view;
			canvas.clipRect(paddingLeft, paddingTop, paddingLeft + view.getWidth(), paddingTop + view.getHeight());
			canvas.translate(paddingLeft, paddingTop + mTranslateY);
			drawChild(canvas, view, getDrawingTime());
			canvas.restore();
		}
	}
	
	/**
	 * 设置装载pinned header view的外部ViewGroup，这样pinned header view能响应到touch
	 * event。如果不需要pinned header view响应touch event，仅仅只是置顶显示，可以不设或者设置为null。
	 * 
	 * @param pinnedHeaderContainer
	 *            注意其在view tree中的层次必须在PinnedHeaderListView之上，否则有可能获取不到touch
	 *            event。
	 */
	public void setPinnedHeaderContianer(FrameLayout pinnedHeaderContainer) {
		mPinnedHeaderContainer = pinnedHeaderContainer;
		if(mPinnedHeaderContainer != null)
			mPinnedHeaderContainer.setVisibility(View.GONE);
	}
	
}