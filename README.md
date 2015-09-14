### PinnedHeaderView
类似iOS上可折叠头部的ListView，效果如下图所示

![Smaller icon](https://github.com/houjinyun/PinnedHeaderView/blob/master/screenshots/img1.png?raw=true)

###Gradle

	compile 'com.hjy.library:PinnedHeaderListView:1.0.3'
	
###使用方法

#####PinnedHeaderListView
仅实现可折叠头部的ListView，右边无索引条

1. 在layout里配置：
	      
	  	<com.hjy.pinnedheaderlistview.PinnedHeaderListView
            android:id="@+id/ListView_Country"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:cacheColorHint="#00000000"
            android:divider="@null"
            />
           
2. Adapter需继承PinnedHeaderAdapter类，实现以下几个主要方法：

		//总的分组数
	    public abstract int getSectionCount();
		
		//每一分组里的总条目数
    	public abstract int getCountForSection(int section);
   		
   		//创建item view   		
    	public abstract View getSectionItemView(int section, int position, View convertView, ViewGroup parent);
		
		//创建每组的header
    	public abstract View getSectionHeaderView(int section, View convertView, ViewGroup parent);
    	
3. 实现ListView的OnItemClickListener事件，必须继承OnPinnedHeaderListViewItemClickListener，实现以下几个方法
	
	   /**
         *         
         * @param adapterView ListView
         * @param section 分组的索引值, 从0开始
         * @param id 数据id
         */
		public abstract void onSectionHeaderClick(AdapterView<?> adapterView, int section, long id);

       /**
         *
         * @param adapterView ListView
         * @param section 分组的索引值
         * @param positionInSection 数据在每组内部的索引值, 从0开始
         * @param id 数据id
         */
		public abstract void onItemClick(AdapterView<?> adapterView, int section, int positionInSection, long id);

	   /**
		 * 点击ListView.addHeader()添加的item
		 *
		 * @param adapterView ListView
		 * @param position header里的索引值, 从0开始
		 */
		public void onHeaderClick(AdapterView<?> adapterView, int position) {
			
		}

	   /**
		 * 点击ListView.addFooter()添加的item
		 *
		 * @param adapterView ListView
		 * @param position footer里的索引值, 从0开始
		 */
		public void onFooterClick(AdapterView<?> adapterView, int position) {
			
		}
		
4. 实现ListView的OnItemLongClickListener事件，必须继承OnPinnedHeaderListViewLongItemClickListener，实现以下几个方法

		public abstract void onSectionHeaderLongClick(AdapterView<?> adapterView, int section, long id);

		public abstract void onItemLongClick(AdapterView<?> adapterView, int section, int positionInSection, long id);

		public void onHeaderLongClick(AdapterView<?> adapterView, int position) {

		}

		public void onFooterLongClick(AdapterView<?> adapterView, int position) {

		}
		
#####IndexablePinnedHeaderListView
实现了PinnedHeaderListView的全部功能，同时能够在右边展示出一个快速索引条

1. 在layout里进行配置
	
	   <com.hjy.pinnedheaderlistview.IndexablePinnedHeaderListView
            android:id="@+id/ListView_Country"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:cacheColorHint="#00000000"
            android:divider="@null"
            android:dividerHeight="0px"
            />
            
 2. 同样Adapter必须继承PinnedHeaderAdapter类
 	
 	为了实现快速索引条，必须实现该方法：
 		
 	    @Override
		public Object[] getSections() {
		    return null;
		}
	返回的必须是一个字符串数组，该数组用来显示快速索引条上的文本内容，该数组的长度必须与adapter的getSectionCount()返回的长度一致，并一一对应。
 
 3. 继承OnPinnedHeaderListViewItemClickListener类来实现itemClick事件
 
 4. 继承OnPinnedHeaderListViewLongItemClickListener类来实现itemLongClick事件
 
 5. 自定义索引条样式
 
 默认的索引条是灰色半透明背景，文字为白色，预览同样为灰色半透明背景，文字为白色，如需修改可采用如下方法：
 
 * 通过IndexablePinnedHeaderListView.getIndexScroller()获取到IndexScroller对象
 * 修改IndexScroller的一些属性，如：
 
 		//设置索引条背景颜色
 		public void setIndexBarBackgroundColor(int bgColor)

		//设置索引条背景的透明度
		public void setIndexBarBackgroundColorAlpha(int alpha)

		//设置索引条文本字体颜色
		public void setIndexBarTextColor(int textColor)

		//设置索引条文本字体大小
		public void setIndexBarTextSize(float textSize)

		//设置预览区域背景颜色
		public void setPreviewBackgroundColor(int bgColor)
		
		//设置预览区域背景的透明度
		public void setPreviewBackgroundColorAlpha(int alpha)

		//设置预览区域字体颜色
		public void setPreviewTextColor(int bgColor)

		//设置预览区域字体大小
		public void setPreviewTextSize(float textSize)
		
		//设置索引条的宽度
		public void setIndexBarWidth(float indexBarWidth)
		
		//设置预览区域的大小
		public void setPreviewSize(float previewWidth, float previewHeight)