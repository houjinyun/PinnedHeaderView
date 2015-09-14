package com.hjy.pinnedheaderlistview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * 索引条
 * 
 * @author houjinyun
 */
public class IndexScroller {

	private ListView mListView;
	
	private SectionIndexer mSectionIndexer;
	private String[] mSections;
	
	private float mDensity;
	private float mScaledDensity;
	
	private int mListViewWidth;
	private int mListViewHeight;
	
	/** 索引条宽度 */
	private float mIndexBarWidth;
	private float mIndexBarMarginRight;
	private float mIndexBarMarginTop;
	private float mIndexBarMarginBottom;
	private float mIndexBarPaddingTop;
	private float mIndexBarPaddingBottom;
	private RectF mIndexbarRect;
	
	private RectF mPreviewRect;
	private float mPreviewWidth;
	private float mPreviewHeight;
	
	private Paint mIndexbarPaint;
	private Paint mIndexPaint;
	private Paint mPreviewPaint;
	private Paint mPreviewTextPaint;
	
	/** 当前选中的section */
	private int mCurrentSection = -1;
	private boolean mIndexing;

    /**
     * 是否显示预览
     */
    private boolean mShowPreview = true;
	
	public IndexScroller(Context context, ListView listView) {
		mListView = listView;
		
		Resources res = context.getResources();
		mDensity = res.getDisplayMetrics().density;
		mScaledDensity = res.getDisplayMetrics().scaledDensity;
		
		//设置默认初始值
		mIndexBarWidth = 30 * mDensity;
		mIndexBarMarginRight = mIndexBarMarginTop = mIndexBarMarginBottom = 10 * mDensity;
		mIndexBarPaddingTop = mIndexBarPaddingBottom = 10 * mDensity;
		mIndexbarRect = new RectF();
		
		mPreviewWidth = 100 * mDensity;
		mPreviewHeight = 100 * mDensity;
		mPreviewRect = new RectF();
		
		mIndexbarPaint = new Paint();
		mIndexPaint = new Paint();
		mPreviewPaint = new Paint();
		mPreviewTextPaint = new Paint();
		//默认索引条为黑色
		mIndexbarPaint.setColor(Color.BLACK);
		mIndexbarPaint.setAntiAlias(true);
		mIndexbarPaint.setAlpha(96);
		//默认索引文本为白色，字体大小为12sp
		mIndexPaint.setColor(Color.WHITE);
		mIndexPaint.setAntiAlias(true);
		mIndexPaint.setTextSize(12 * mScaledDensity);
		//索引预览区域背景
		mPreviewPaint.setColor(Color.BLACK);
		mPreviewPaint.setAlpha(96);
		mPreviewPaint.setAntiAlias(true);
		mPreviewPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0, 0));
		//索引预览文本
		mPreviewTextPaint.setColor(Color.WHITE);
		mPreviewTextPaint.setAntiAlias(true);
		mPreviewTextPaint.setTextSize(50 * mScaledDensity);
	}

	/**
	 * 初始化indexbar rect
	 */
	private void initIndexbarRect() {
		mIndexbarRect.set(mListViewWidth - mIndexBarMarginRight - mIndexBarWidth, mIndexBarMarginTop, mListViewWidth - mIndexBarMarginRight, mListViewHeight - mIndexBarMarginBottom);
	}
	
	public void setAdapter(ListAdapter adapter) {
		if(adapter instanceof SectionIndexer) {
			mSectionIndexer = (SectionIndexer) adapter;
			mSections = (String[]) mSectionIndexer.getSections();
		} else {
			throw new IllegalArgumentException("adapter must implement SectionIndexer interface.");
		}
	}
	
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		mListViewWidth = w;
		mListViewHeight = h;		
		initIndexbarRect();
		initPreviewRect();
	}


	public void setIndexBarBackgroundColor(int bgColor) {
		mIndexbarPaint.setColor(bgColor);
	}

	public void setIndexBarBackgroundColorAlpha(int alpha) {
		mIndexbarPaint.setAlpha(alpha);
	}

	public void setIndexBarTextColor(int textColor) {
		mIndexPaint.setColor(textColor);
	}

	public void setIndexBarTextSize(float textSize) {
		mIndexPaint.setTextSize(textSize);
	}

	public void setPreviewBackgroundColor(int bgColor) {
		mPreviewPaint.setColor(bgColor);
	}

	public void setPreviewBackgroundColorAlpha(int alpha) {
		mPreviewPaint.setAlpha(alpha);
	}

	public void setPreviewTextColor(int bgColor) {
		mPreviewTextPaint.setColor(bgColor);
	}

	public void setPreviewTextSize(float textSize) {
		mPreviewTextPaint.setTextSize(textSize);
	}

    public void setShowPreiview(boolean isShow) {
        mShowPreview = isShow;
    }

	/**
	 * 设置索引条的宽度
	 * 
	 * @param indexBarWidth 索引条的宽度
	 */
	public void setIndexBarWidth(float indexBarWidth) {
		mIndexBarWidth = indexBarWidth;
		initIndexbarRect();
	}
	
	/**
	 * 设置索引条的margin值
	 * 
	 * @param marginTop margin
	 * @param marginRight margin
	 * @param marginBottom margin
	 */
	public void setIndexbarMargin(float marginTop, float marginRight, float marginBottom) {
		mIndexBarMarginTop = marginTop;
		mIndexBarMarginRight = marginRight;
		mIndexBarMarginBottom = marginBottom;
		initIndexbarRect();
	}
	
	/**
	 * 设置索引条的paddingTop, paddingBottom值
	 * 
	 * @param paddingTop padding
	 * @param paddingBottom padding
	 */
	public void setIndexbarPadding(float paddingTop, float paddingBottom) {
		mIndexBarPaddingTop = paddingTop;
		mIndexBarPaddingBottom = paddingBottom;
	}
	
	/**
	 * 设置预览区域宽度、高度
	 * 
	 * @param previewWidth 预览区域宽度
	 * @param previewHeight 预览区域高度
	 */
	public void setPreviewSize(float previewWidth, float previewHeight) {
		mPreviewWidth = previewWidth;
		mPreviewHeight = previewHeight;
		initPreviewRect();
	}
	
	private void initPreviewRect() {
		float left = (mListViewWidth - mPreviewWidth) / 2;
		float top = (mListViewHeight - mPreviewHeight) / 2;
		mPreviewRect.set(left, top, left + mPreviewWidth, top + mPreviewHeight);
	}
	
	public void draw(Canvas canvas) {
		if(mSections == null || mSections.length == 0)
			return;
		//绘制索引条背景区域
		canvas.drawRoundRect(mIndexbarRect, 5 * mDensity, 5 * mDensity, mIndexbarPaint);
		//绘制索引文本内容
		float sectionHeight = (mIndexbarRect.height() - mIndexBarPaddingTop - mIndexBarPaddingBottom) / mSections.length;
		float paddingTop = (sectionHeight - (mIndexPaint.descent() - mIndexPaint.ascent())) / 2;
		for(int i=0; i<mSections.length; i++) {
			float paddingLeft = (mIndexbarRect.width() - mIndexPaint.measureText(mSections[i])) / 2;
			canvas.drawText(mSections[i], mIndexbarRect.left + paddingLeft, mIndexbarRect.top + mIndexBarPaddingTop + sectionHeight * i + paddingTop - mIndexPaint.ascent(), mIndexPaint);
		}
		
		if(mShowPreview && mCurrentSection >= 0 && mCurrentSection < mSections.length) {
			//绘制预览文本以及背景
			float previewTextWidth = mPreviewTextPaint.measureText(mSections[mCurrentSection]);
			float previewTextHeight = mPreviewTextPaint.descent() - mPreviewTextPaint.ascent();
			float previewPaddingTop = (mPreviewRect.height() - previewTextHeight) / 2;
			canvas.drawRoundRect(mPreviewRect, 5 * mDensity, 5 * mDensity, mPreviewPaint);
			canvas.drawText(mSections[mCurrentSection], 
					mPreviewRect.left + (mPreviewRect.width() - previewTextWidth)/2, 
					mPreviewRect.top + previewPaddingTop - mPreviewTextPaint.ascent(), mPreviewTextPaint);
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			if(contains(event.getX(), event.getY())) {
				mIndexing = true;
				mCurrentSection = findSectionByPoint(event.getY());
				mListView.setSelection(mSectionIndexer.getPositionForSection(mCurrentSection));				
				return true;
			} else {
				mIndexing = false;
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			if(mIndexing) {
/*				if(contains(event.getX(), event.getY())) {
					mCurrentSection = findSectionByPoint(event.getY());
					mListView.setSelection(mSectionIndexer.getPositionForSection(mCurrentSection));
				}*/
                mCurrentSection = findSectionByPoint(event.getY());
                mListView.setSelection(mSectionIndexer.getPositionForSection(mCurrentSection));
				return true;
			}
			break;
		}
		case MotionEvent.ACTION_UP: {
			if(mIndexing) {
				mIndexing = false;
				mCurrentSection = -1;
			}
			break;
		}
		}
		return false;
	}
	
	private boolean contains(float x, float y) {
		return (x >= mIndexbarRect.left && y >= mIndexbarRect.top && y <= mIndexbarRect.bottom);
	}
	
	private int findSectionByPoint(float y) {
		if(mSections == null || mSections.length == 0)
			return 0;
		if(y < mIndexbarRect.top + mIndexBarPaddingTop)
			return 0;
		if(y > mIndexbarRect.bottom - mIndexBarPaddingBottom)
			return mSections.length - 1;
		return (int) ((y - mIndexbarRect.top - mIndexBarPaddingTop)/((mIndexbarRect.height() - mIndexBarPaddingTop - mIndexBarPaddingBottom) / mSections.length));
	}
	
}