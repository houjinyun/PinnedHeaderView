package com.hjy.pinnedheaderlistview;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

/**
 * 支持索引滑动
 * 
 * @author houjinyun
 */
public class IndexablePinnedHeaderListView extends PinnedHeaderListView{

	private IndexScroller mIndexScroller;

	/**
	 * @param context Context
	 * @param attrs 属性值
	 */
	public IndexablePinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mIndexScroller = new IndexScroller(getContext(), this);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if(mIndexScroller != null) {
			mIndexScroller.draw(canvas);
		}
	}

	/**
	 * adapter must implement SectionIndexer interface.
	 */
	public final void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if(mIndexScroller != null)
			mIndexScroller.setAdapter(adapter);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(mIndexScroller != null)
			mIndexScroller.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(mIndexScroller != null && mIndexScroller.onTouchEvent(ev))
			return true;
		return super.onTouchEvent(ev);
	}
	
	@Override
	public void setSelection(int position) {
		ListAdapter adapter = getAdapter();
		if(adapter instanceof HeaderViewListAdapter) {
			position += ((HeaderViewListAdapter) adapter).getHeadersCount();
		}
		super.setSelection(position);
	}
	
	public IndexScroller getIndexScroller() {
		return mIndexScroller;
	}



}