> 在研究邮箱后缀自动补齐的时候,好奇那几个方法的调用逻辑,就研究了源码是怎么调用的.
##### 内容输入
- 当用户在 `AutoCompleteTextView`控件中输入内容时候,会触发自身的`afterTextChanged()`
    ```java
    public void afterTextChanged(Editable s) {
        ...
        //关注该方法  
        refreshAutoCompleteResults();
    }
  
    public final void refreshAutoCompleteResults() {
        // the drop down is shown only when a minimum number of characters
        // was typed in the text view
        if (enoughToFilter()) {
           ...
            //这个方法很重要,getText()为用户输入的内容   
            performFiltering(getText(), mLastKeyCode);
        } else {
          ...
        }
    }
    protected void performFiltering(CharSequence text, int keyCode) { 
        mFilter.filter(text, this);
    }
    ```
##### 开始过滤  
- 看到 `filter()`方法会好奇 `mFilter`对象是什么.当为 `AutoCompleteTextView`设置 `Adapter`时候获取到的.
    ```java
    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        ...
        if (mAdapter != null) {
            //此处获取到mFilter对象,去ArrayAdapter中看看getFilter()方法做了什么
            mFilter = ((Filterable) mAdapter).getFilter();
            ...
        } else {
            mFilter = null;
        }
        mPopup.setAdapter(mAdapter);
    }
    //直接创建了一个ArrayFilter对象
    @Override
    public @NonNull Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }
    ```
- 看看 `Filter`中的`filter()`,这个方法主要是将用户输入的内容交由`RequestHandler`中的`handlerMessage()`处理
    ```java
     public final void filter(CharSequence constraint, FilterListener listener) { 
             mThreadHandler = new RequestHandler(thread.getLooper());
             ...
             //用户输入的内容,交由 RequestHandler(Filter中的内部类)中的handlerMessage处理
             args.constraint = constraint != null ? constraint.toString() : null;
             ...
             mThreadHandler.sendMessageDelayed(message, delay);
         }
     }
  ```
- 对于`RequestHandler`中的`handlerMessage()`,我们需要分两步来看
    ```java
    public void handleMessage(Message msg) {
        int what = msg.what;
        Message message;
        switch (what) {
            case FILTER_TOKEN:
                RequestArguments args = (RequestArguments) msg.obj;
                try {
                    //这是第一步
                    //这里后看下去是个接口,回想上面的Filter对象是由ArrayAdapter里得方法提供,我们到时候看看ArrayAdapter中有没有什么发现,
                    args.results = performFiltering(args.constraint);
                } catch (Exception e) {
                   ...
                } finally {
                    //这是第二步
                    //从ResultHandler获取一个Message 
                    message = mResultHandler.obtainMessage(what);
                    message.obj = args;
                    //将Message发送到ResultHandler中的 handlerMessage() 
                    message.sendToTarget(); 
                ...
                break;
        }
    }
    ``` 
  - 先看第一步, `ArrayAdapter`中的 `performFiltering()`,该方法接收的参数为用户输入的内容.
    ```java
    protected FilterResults performFiltering(CharSequence prefix) {
        final FilterResults results = new FilterResults();
        if (mOriginalValues == null) {
            synchronized (mLock) {
                //mObjects 看源码可知为创建ArrayAdapter时候传入的数组加工而来.
                //这里又重新复制一份数据.
                mOriginalValues = new ArrayList<>(mObjects);
            }
        }
        if (prefix == null || prefix.length() == 0) {
             ... 
             //我们只分析输入内容不为空的情况
        } else {
            final String prefixString = prefix.toString().toLowerCase();
            final ArrayList<T> values;
            synchronized (mLock) {
                values = new ArrayList<>(mOriginalValues);
            }
            final int count = values.size();
            //遍历创建ArrayAdapter时候传入的数据,用用户输入的内容作为前缀进行比对,相同的则用一个新的集合将这些数据存起来.
            final ArrayList<T> newValues = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final T value = values.get(i);
                final String valueText = value.toString().toLowerCase();
                if (valueText.startsWith(prefixString)) {
                    //新集合存起来  
                    newValues.add(value);
                } else {
                    ...
                }
            }
            results.values = newValues;
            results.count = newValues.size();
        }
        //最后将结果返回, 此时我们再回到上一步RequestHandler中的handlerMessage()
        return results;
    }
    ```

  - 第二步,将过滤好的数据结果发送到`ResultsHandler`中的`handlerMessage()`处理.
    ```java
    @Override
    public void handleMessage(Message msg) {
        RequestArguments args = (RequestArguments) msg.obj;
        //第一步
        publishResults(args.constraint, args.results);
        if (args.listener != null) {
            int count = args.results != null ? args.results.count : -1;
            //第二步
            args.listener.onFilterComplete(count);
        }
    }
    ```
##### 开始展示下拉列表    
- `ResultsHandler`中的`handlerMessage()` 同样也需要分两步来看.  
    - 第一步看看 `publishResults()`,该方法同样是在ArrayAdapter中具体实现
        ```java
         @Override
         protected void publishResults(CharSequence constraint, FilterResults results) {
             //一目了然, mObjects该变量为ArrayAdapter中得数据源,将过滤后的数据源替换原来得数据源.
             mObjects = (List<T>) results.values;
             //刷新数据 
             if (results.count > 0) {
                 notifyDataSetChanged();
             } else {
                 notifyDataSetInvalidated();
             }
         }
        ``` 
    - 第二步 `onFilterComplete()`,最终会将 `ListPopupWindow` 显示出来
        ```java
        public void onFilterComplete(int count) {
            updateDropDownForFilter(count);
        }
        private void updateDropDownForFilter(int count) {
            ...
            if ((count > 0 || dropDownAlwaysVisible) && enoughToFilter) {
                if (hasFocus() && hasWindowFocus() && mPopupCanBeUpdated) {
                    //显示出  
                    showDropDown();
                }
            } 
            ...  
        }
        public void showDropDown() {
            ...
            //最熟悉不过的show()  
            mPopup.show();
            mPopup.getListView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        }
        ```
##### 用户点击选择下拉列表中内容
> 目前,用户向 `AutoCompleteTextView` 输入内容,从过滤,到`ListPopupWindow`(下拉列表)显示都分析过了, 最后就是点击`ListPopupWindow`中的条目,条目中的内容如何展示到 `AutoCompleteTextView`中
- 看到在`AutoCompleteTextView`构造方法中为`ListPopupWindow`对象设置了一个 `DropDownItemClickListener`对象
    ```java
    public AutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes, Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes);
                  