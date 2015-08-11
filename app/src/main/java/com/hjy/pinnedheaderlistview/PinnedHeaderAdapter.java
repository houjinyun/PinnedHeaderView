package com.hjy.pinnedheaderlistview;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

/**
 * @author houjinyun
 */
@SuppressLint("UseSparseArrays")
public abstract class PinnedHeaderAdapter extends BaseAdapter implements SectionIndexer {

	private static final String TAG = "PinnedHeaderAdapter";
	
    private SparseArray<Integer> mPositionInSectionForPositionCache;
    private SparseArray<Integer> mSectionForPositionCache;
    private SparseArray<Integer> mSectionCountCache;
    private SparseArray<Integer> mPositionForSectionCache;
    private SparseArray<Boolean> mInSectionHeaderCache;
    
    /** 缓存item count */
    private int mCount;
    /** 缓存section count */
    private int mSectionCount;
	
    public PinnedHeaderAdapter() {
    	mPositionInSectionForPositionCache = new SparseArray<Integer>();
    	mSectionForPositionCache = new SparseArray<Integer>();
    	mSectionCountCache = new SparseArray<Integer>();
    	mPositionForSectionCache = new SparseArray<Integer>();
    	mInSectionHeaderCache = new SparseArray<Boolean>();
    	mCount = -1;
    	mSectionCount = -1;
    }
    
    @Override
    public void notifyDataSetChanged() {
    	mPositionInSectionForPositionCache.clear();
    	mSectionForPositionCache.clear();
    	mSectionCountCache.clear();
    	mPositionForSectionCache.clear();
    	mInSectionHeaderCache.clear();
    	mCount = -1;
    	mSectionCount = -1;
    	super.notifyDataSetChanged();
    }
    
    @Override
    public void notifyDataSetInvalidated() {
    	mPositionInSectionForPositionCache.clear();
    	mSectionForPositionCache.clear();
    	mSectionCountCache.clear();
    	mPositionForSectionCache.clear();
    	mInSectionHeaderCache.clear();
    	mCount = -1;
    	mSectionCount = -1;
    	super.notifyDataSetInvalidated();
    }
    
    public abstract int getSectionCount();

    public abstract int getCountForSection(int section);
   
    public abstract View getSectionItemView(int section, int position, View convertView, ViewGroup parent);

    public abstract View getSectionHeaderView(int section, View convertView, ViewGroup parent);

    @Override
    public Object getItem(int position) {
    	return null;
    }
    
    @Override
    public long getItemId(int position) {
    	return 0;
    }
    
	@Override
	public final int getCount() {
		//缓存数量
		if(mCount >= 0)
			return mCount;
		int count = 0;
		int sectionCount = internalGetSectionCount();
		for(int i=0; i<sectionCount; i++) {
			count += internalGetCountForSection(i);
			count++;
		}
		mCount = count;
		Log.d(TAG, "count = " + count);
		return mCount;
	}

	private int internalGetCountForSection(int section) {
		//优先通过缓存查找
        Integer cachedSectionCount = mSectionCountCache.get(section);
        if (cachedSectionCount != null) {
            return cachedSectionCount;
        }
        int sectionCount = getCountForSection(section);
        mSectionCountCache.put(section, sectionCount); 
        return sectionCount;
    }

    private int internalGetSectionCount() {
    	//优先通过缓存查找
        if (mSectionCount >= 0) {
            return mSectionCount;
        }
        mSectionCount = getSectionCount();
        return mSectionCount;
    }
	
    @Override
    public final int getSectionForPosition(int position) {
        Integer cachedSection = mSectionForPositionCache.get(position);
        if (cachedSection != null) {
            return cachedSection;
        }
        int sectionStart = 0;
        for (int i = 0; i < internalGetSectionCount(); i++) {
            int sectionCount = internalGetCountForSection(i);
            int sectionEnd = sectionStart + sectionCount + 1;
            if (position >= sectionStart && position < sectionEnd) {
                mSectionForPositionCache.put(position, i);
                return i;
            }
            sectionStart = sectionEnd;
        }
        return 0;
    }
    
    @Override
	public final int getPositionForSection(int section) {
    	Integer cachedPosition = mPositionForSectionCache.get(section);
    	if(cachedPosition != null) {
    		return cachedPosition;
    	}
    	int position = 0;
    	for(int i=0; i<section; i++) {
    		int sectionCount = internalGetCountForSection(i);
    		position += sectionCount;
    		position += 1;
    	}
		return position;
	}

    public final int getPositionInSectionForPosition(int position) {
        // first try to retrieve values from cache
        Integer cachedPosition = mPositionInSectionForPositionCache.get(position);
        if (cachedPosition != null) {
            return cachedPosition;
        }
        int sectionStart = 0;
        for (int i = 0; i < internalGetSectionCount(); i++) {
            int sectionCount = internalGetCountForSection(i);
            int sectionEnd = sectionStart + sectionCount + 1;
            if (position >= sectionStart && position < sectionEnd) {
                int positionInSection = position - sectionStart - 1;
                mPositionInSectionForPositionCache.put(position, positionInSection);
                return positionInSection;
            }
            sectionStart = sectionEnd;
        }
        return 0;
    }
    
    public final boolean isSectionHeader(int position) {
    	Boolean isHeader = mInSectionHeaderCache.get(position);
    	if(isHeader != null) {
    		return isHeader;
    	}
        int sectionStart = 0;
        for (int i = 0; i < internalGetSectionCount(); i++) {
            if (position == sectionStart) {
            	mInSectionHeaderCache.put(position, true);
                return true;
            } else if (position < sectionStart) {
            	mInSectionHeaderCache.put(position, false);
                return false;
            }
            sectionStart += internalGetCountForSection(i) + 1;
        }
        mInSectionHeaderCache.put(position, false);
        return false;
    }
    
    public boolean isItemViewTypePinned(int position) {
    	return isSectionHeader(position);
    }
    
    public int getChildItemViewTypeCount() {
		return 1;
	}
	
	public int getSectionHeaderViewTypeCount() {
		return 1;
	}
	
	/**
	 * 注意与getSectionHeaderViewType()分开来，取值范围[0 - getChildItemViewTypeCount());
	 * 
	 * @param section 分组索引值
	 * @param positionInSection 每组里的position
	 *
	 * @return ItemViewType
	 */
	public int getChildItemViewType(int section, int positionInSection) {
		return 0;
	}
	
	/**
	 * 注意与getChildItemViewType()分开来，取值范围要从[0 - getSectionHeaderViewTypeCount()-1);
	 * 
	 * @param section 分组索引值
	 * @return ItemViewType
	 */
	public int getSectionHeaderViewType(int section) {
		return 0;
	}
    
	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		boolean isHeader = isSectionHeader(position);
		if(isHeader) {
			return getSectionHeaderView(getSectionForPosition(position), convertView, parent);
		}
		return getSectionItemView(getSectionForPosition(position), getPositionInSectionForPosition(position), convertView, parent);
	}
	
	@Override
	public final int getItemViewType(int position) {
		if(isSectionHeader(position)) {
			return getSectionHeaderViewType(getSectionForPosition(position)) + getChildItemViewTypeCount();
		} else {
			return getChildItemViewType(getSectionForPosition(position), getPositionInSectionForPosition(position));
		}
	}
	
	@Override
	public final int getViewTypeCount() {
		return getChildItemViewTypeCount() + getSectionHeaderViewTypeCount();
	}
	
	@Override
	public Object[] getSections() {
		return null;
	}
	
}