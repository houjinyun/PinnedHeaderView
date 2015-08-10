/**
 * Copyright © 2012-2013 Hangzhou Enniu Tech Ltd. All right reserved.
 */
package com.hjy.pinnedheaderlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 每组的标题title可以一直置顶的ListView
 * 
 * @author houjinyun
 */
public class PinnedHeaderListView extends ListView {

	private static final String TAG = "PinnedHeaderListView";

	private static class PinnedView {
		public View view;
		public int position;
	}

	/**
	 * 设置单独的滑动监听器，可以为空
	 */
	private OnScrollListener mDelegateOnScrollListener;

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

	/**
	 * @param context
	 */
	public PinnedHeaderListView(Context context) {
		super(context);
		Log.d(TAG, "constructor 1...");
		initView();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public PinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "constructor 2...");
		initView();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG, "constructor 3...");
		initView();
	}

	private void initView() {
		Log.d(TAG, "initView()");
		// 设置滑动监听器，主要的处理逻辑在这里
		setOnScrollListener(mOnScrollListener);
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		// title置顶的主要逻辑已实现在mOnScrollListener里，此外还可以设置另外的listener
		if (l == mOnScrollListener) {
			super.setOnScrollListener(l);
		} else {
			mDelegateOnScrollListener = l;
		}
	}

	private OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mDelegateOnScrollListener != null) {
				mDelegateOnScrollListener.onScrollStateChanged(view, scrollState);
			}
			if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				Log.d(TAG, "onScroll state changed : idle");
				updatePinnedHeaderContainer();
			} else {
				Log.d(TAG, "onScroll state changed : scrolling");
				if(mPinnedHeaderContainer != null && mPinnedHeaderContainer.getVisibility() != View.GONE)
					mPinnedHeaderContainer.setVisibility(View.GONE);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			Log.d(TAG, String.format("onScroll: firstVisibleItem=%d,visibleItemCount=%d,totalItemCount=%d", firstVisibleItem, visibleItemCount, totalItemCount));
			if(mDelegateOnScrollListener != null) {
				mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
			updatePinnedHeaderListView(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	};

	private void updatePinnedHeaderContainer() {
		if (mPinnedHeaderContainer != null) {
			if (mCurrPinnedView != null) {
				View header = mPinnedHeaderContainer.getChildAt(0);
				PinnedHeaderAdapter adapter = getCurrAdapter();
				if(adapter != null && header != null) {
					adapter.getSectionHeaderView(mCurrPinnedView.position, header, null);
					mPinnedHeaderContainer.setVisibility(View.VISIBLE);
					mPinnedHeaderContainer.scrollTo(0, -mTranslateY);
					mPinnedHeaderContainer.invalidate();
				} else {
					mPinnedHeaderContainer.setVisibility(View.GONE);
				}
			} else {
				mPinnedHeaderContainer.setVisibility(View.GONE);
			}
		}
	}
	
	private void updatePinnedHeaderListView(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		PinnedHeaderAdapter adapter = getCurrAdapter();
		if (adapter == null || visibleItemCount == 0)
			return;
		// 如果ListView设置过 header view，要减去header view的个数，因为ListView会把header
		// view也计算进来，但是adapter是不包含header view的
		int actualVisibleItemCount = visibleItemCount;
		if (view.getAdapter() instanceof HeaderViewListAdapter) {
			int headerCount = ((HeaderViewListAdapter) view.getAdapter()).getHeadersCount();
			int footerCount = ((HeaderViewListAdapter) view.getAdapter()).getFootersCount();
			Log.d(TAG, "header count = " + headerCount);
			firstVisibleItem = firstVisibleItem - headerCount;
			visibleItemCount = visibleItemCount - headerCount;
			actualVisibleItemCount = visibleItemCount - footerCount - headerCount;
		}
		if (mDelegateOnScrollListener != null) {
			mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}

		int groupPosition = findCurrentSection(firstVisibleItem);
		Log.d(TAG, "firstVisibleItem = " + firstVisibleItem + " group position = " + groupPosition);
		if (groupPosition == -1) {
			recyclePinnedHeader();
			return;
		}
		if (firstVisibleItem == 0 && groupPosition == 0){
			recyclePinnedHeader();
		}
		if (mCurrPinnedView == null) {
			if (actualVisibleItemCount > 0) {
				createPinnedHeader(groupPosition);
			}
		} else {
			if (mCurrPinnedView.position == groupPosition) {
				int nextGroupPosition = findNextGroupPosition(firstVisibleItem, visibleItemCount);
				Log.d(TAG, "next group = " + nextGroupPosition);
				if (nextGroupPosition != -1 && (nextGroupPosition - firstVisibleItem) < getChildCount()) {
					View nextGroupView = getChildAt(nextGroupPosition - firstVisibleItem);
					int top = nextGroupView.getTop();
					mTranslateY = top - mCurrPinnedView.view.getHeight() - getListPaddingTop();
					if (mTranslateY > 0)
						mTranslateY = 0;
				} else {
					mTranslateY = 0;
				}
			} else if (groupPosition < mCurrPinnedView.position) { // 手指从上往下滑，临界点
				recyclePinnedHeader();
				if (actualVisibleItemCount > 0) {
					createPinnedHeader(groupPosition);
				}
				int nextGroupPosition = findNextGroupPosition(firstVisibleItem, visibleItemCount);
				if (nextGroupPosition != -1) {
					View nextGroupView = getChildAt(nextGroupPosition - firstVisibleItem);
					int top = nextGroupView.getTop();
					if (top < mCurrPinnedView.view.getHeight() + getListPaddingTop()) {
						mTranslateY = top - mCurrPinnedView.view.getHeight() - getListPaddingTop();
					} else {
						mTranslateY = 0;
					}
				} else {
					mTranslateY = 0;
				}
			} else if (groupPosition > mCurrPinnedView.position) { // 手指从下往上滑，临界点
				recyclePinnedHeader();
				if (actualVisibleItemCount > 0) {
					createPinnedHeader(groupPosition);
				}
				if (getChildCount() > 0) {
					View currView = getChildAt(0);
					int top = currView.getTop();
					mTranslateY = top - mCurrPinnedView.view.getHeight() - getListPaddingTop();
					if (mTranslateY > 0)
						mTranslateY = 0;
				}

			}
		}

	}
	
	/**
	 * 得到当前绑定的adapter对象
	 * 
	 * @return
	 */
	private PinnedHeaderAdapter getCurrAdapter() {
		// 如果该ListView有addHeaderView()或addFooterView()时，在ListView.setAdapter()时，会将原本的adapter包装成一个HeaderViewListAdapter对象
		ListAdapter adapter = getAdapter();
		if (adapter == null)
			return null;
		PinnedHeaderAdapter pinnedHeaderAdapter = null;
		if (adapter instanceof HeaderViewListAdapter) {
			if (((HeaderViewListAdapter) adapter).getWrappedAdapter() instanceof PinnedHeaderAdapter)
				pinnedHeaderAdapter = (PinnedHeaderAdapter) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		} else {
			if (adapter instanceof PinnedHeaderAdapter)
				pinnedHeaderAdapter = (PinnedHeaderAdapter) adapter;
		}
		return pinnedHeaderAdapter;
	}

	private int findCurrentSection(int position) {
		if (position < 0)
			return -1;
		PinnedHeaderAdapter adapter = getCurrAdapter();
		int sectionIndex = adapter.getSectionForPosition(position);
		return sectionIndex;
	}

	private int findNextGroupPosition(int firstVisibleItem, int visibleItemCount) {
		PinnedHeaderAdapter adapter = getCurrAdapter();
		for (int i = 1; i < visibleItemCount; i++) {
			int position = firstVisibleItem + i;
			if (adapter.isItemViewTypePinned(position))
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
	private void createPinnedHeader(int position) {
		PinnedHeaderAdapter adapter = getCurrAdapter();
		if (adapter == null) {
            recyclePinnedHeader();
            updatePinnedHeaderContainer();
			return;
		}
		// 利用recycle view
		PinnedView pinnedView = mRecyclePinnedView;
		View recycleView = pinnedView == null ? null : pinnedView.view;
		mRecyclePinnedView = null;

		View pinnedHeader = adapter.getSectionHeaderView(position, recycleView, null);
		ViewGroup.LayoutParams params = pinnedHeader.getLayoutParams();
		if (params == null) {
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
		int heightMeasureSpec;
		// 确切的大小
		if (params.height > 0) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
		} else {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		pinnedHeader.setLayoutParams(params);
		pinnedHeader.measure(widthMeasureSpec, heightMeasureSpec);
		pinnedHeader.layout(0, 0, pinnedHeader.getMeasuredWidth(), pinnedHeader.getMeasuredHeight());
		mTranslateY = 0;

		if (pinnedView == null)
			pinnedView = new PinnedView();
		pinnedView.position = position;
		pinnedView.view = pinnedHeader;
		mCurrPinnedView = pinnedView;

		Log.d("Hou1", "---------------create pin " + mCurrPinnedView.position);
	}

	@Override
	protected void handleDataChanged() {
		super.handleDataChanged();
        PinnedHeaderAdapter adapter = getCurrAdapter();
        if(adapter == null || adapter.getCount() == 0) {
            recyclePinnedHeader();
            updatePinnedHeaderContainer();
        }
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		recyclePinnedHeader();
		updatePinnedHeaderContainer();		
		super.setAdapter(adapter);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mCurrPinnedView != null) {
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

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
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

	public final void setOnItemClickListener(OnPinnedHeaderListViewItemClickListener listener) {
		super.setOnItemClickListener(listener);
	}

	public final void setOnItemLongClickListener(OnPinnedHeaderListViewLongItemClickListener listener) {
		super.setOnItemLongClickListener(listener);
	}

	public static abstract class OnPinnedHeaderListViewItemClickListener implements OnItemClickListener {

		@Override
		public final void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			Log.d(TAG, "onItemClick : position = " + position);

			PinnedHeaderAdapter adapter = null;
			int headerCount = 0;
			int footerCount = 0;
			// 如果设置过header view，注意会有一个包装的adapter返回，并不是原来设置的adapter
			if (adapterView.getAdapter() instanceof HeaderViewListAdapter) {
				HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
				headerCount = wrapperAdapter.getHeadersCount();
				footerCount = wrapperAdapter.getFootersCount();
				if (wrapperAdapter.getWrappedAdapter() instanceof PinnedHeaderAdapter)
					adapter = (PinnedHeaderAdapter) wrapperAdapter.getWrappedAdapter();
			} else {
				if (adapterView.getAdapter() instanceof PinnedHeaderAdapter)
					adapter = (PinnedHeaderAdapter) adapterView.getAdapter();
			}
			if (adapter == null)
				return;
			// 如果点击的是header view，则不响应
			if (position < headerCount) {
				onHeaderClick(adapterView, position);
				return;
			}
			// 如果点击的是footer view，也不响应
			if (footerCount > 0 && position >= adapterView.getAdapter().getCount() - footerCount) {
				onFooterClick(adapterView, position - (adapterView.getAdapter().getCount() - footerCount));
				return;
			}
			int adjustPosition = position - headerCount;
			int section = adapter.getSectionForPosition(adjustPosition);
			if (adapter.isSectionHeader(adjustPosition)) {
				onSectionHeaderClick(adapterView, section, id);
			} else {
				int positionInSection = adapter.getPositionInSectionForPosition(adjustPosition);
				onItemClick(adapterView, section, positionInSection, id);
			}
		}

		public abstract void onSectionHeaderClick(AdapterView<?> adapterView, int section, long id);

		public abstract void onItemClick(AdapterView<?> adapterView, int section, int positionInSection, long id);

		/**
		 * 点击ListView.addHeader()添加的item
		 * 
		 * @param adapterView
		 * @param position
		 */
		public void onHeaderClick(AdapterView<?> adapterView, int position) {
			
		}

		/**
		 * 点击ListView.addFooter()添加的item
		 * 
		 * @param adapterView
		 * @param position 第一个footerView从0开始
		 */
		public void onFooterClick(AdapterView<?> adapterView, int position) {
			
		}
		
	}

	public static abstract class OnPinnedHeaderListViewLongItemClickListener implements OnItemLongClickListener {

		@Override
		public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onLongItemClick : position = " + position);

			PinnedHeaderAdapter adapter = null;
			int headerCount = 0;
			int footerCount = 0;
			// 如果设置过header view，注意会有一个包装的adapter返回，并不是原来设置的adapter
			if (parent.getAdapter() instanceof HeaderViewListAdapter) {
				HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) parent.getAdapter();
				headerCount = wrapperAdapter.getHeadersCount();
				footerCount = wrapperAdapter.getFootersCount();
				if (wrapperAdapter.getWrappedAdapter() instanceof PinnedHeaderAdapter)
					adapter = (PinnedHeaderAdapter) wrapperAdapter.getWrappedAdapter();
			} else {
				if (parent.getAdapter() instanceof PinnedHeaderAdapter)
					adapter = (PinnedHeaderAdapter) parent.getAdapter();
			}
			if (adapter == null)
				return false;
			// 如果点击的是header view，则不响应
			if (position < headerCount) {
				onHeaderLongClick(parent, position);
				return false;
			}
			// 如果点击的是footer vier，也不响应
			if (footerCount > 0 && position >= parent.getAdapter().getCount() - footerCount)
				return false;
			int adjustPosition = position - headerCount;
			int section = adapter.getSectionForPosition(adjustPosition);
			if (adapter.isSectionHeader(adjustPosition)) {
				onSectionHeaderLongClick(parent, section, id);
			} else {
				int positionInSection = adapter.getPositionInSectionForPosition(adjustPosition);
				onItemLongClick(parent, section, positionInSection, id);
			}
			return false;
		}

		public abstract void onSectionHeaderLongClick(AdapterView<?> adapterView, int section, long id);

		public abstract void onItemLongClick(AdapterView<?> adapterView, int section, int positionInSection, long id);

		public abstract void onHeaderLongClick(AdapterView<?> adapterView, int position);
	}

}