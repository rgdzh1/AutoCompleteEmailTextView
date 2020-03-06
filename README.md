- 简单捋一捋 `AutoCompleteTextView`这个控件如果展示刷新的
    -  当用户获取输入内容调用 `afterTextChanged()`->`refreshAutoCompleteResults()`-> `performFiltering()`
        - 看看`performFiltering()`方法
            ```java
            protected void performFiltering(CharSequence text, int keyCode) {
                //text 为用户输入的内容
                mFilter.filter(text, this);
            }
            //来看看mFilter对象如何得到,当为AutoCompleteTextView设置Adapter时候获得.
            public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
                ...
                if (mAdapter != null) {
                    //noinspection unchecked
                    mFilter = ((Filterable) mAdapter).getFilter();
                    adapter.registerDataSetObserver(mObserver);
                } else {
                    mFilter = null;
                }
                ...
            }
            //再来看看ArrayAdapter中得getFilter(),最终返回得是一个ArrayFilter对象.
            public @NonNull Filter getFilter() {
                if (mFilter == null) {
                    mFilter = new ArrayFilter();
                }
                return mFilter;
            }
            ```
        - 看看 `Filter`中的 `filter()`方法,只看重要逻辑
            ```java
            //
            public final void filter(CharSequence constraint, FilterListener listener) {
                    //等会儿看 `RequestHandler` 类中的handlerMessage()
                    mThreadHandler = new RequestHandler(thread.getLooper());
                    Message message = mThreadHandler.obtainMessage(FILTER_TOKEN);
                    RequestArguments args = new RequestArguments();
                    args.constraint = constraint != null ? constraint.toString() : null;
                    args.listener = listener;
                    message.obj = args;
                    ...
                    //去看handlerMessage()
                    mThreadHandler.sendMessageDelayed(message, delay);
                }
            }
          
            public void handleMessage(Message msg) {
                int what = msg.what;
                Message message;
                switch (what) {
                    case FILTER_TOKEN:
                        RequestArguments args = (RequestArguments) msg.obj;
                        try {
                            //performFiltering()这个方法得去ArrayAdapter中看了.
                            //args.constraint 为用户输入得内容
                            args.results = performFiltering(args.constraint);
                        } catch (Exception e) {
                            ...
                        } finally {
                            //这里是从 ResultsHandler获取一个Message 
                            message = mResultHandler.obtainMessage(what);
                            message.obj = args;
                            //将 message发送,由ResultsHandler中的handleMessage处理
                            message.sendToTarget();
                        }
                        ...
                        break;
                    case FINISH_TOKEN:
                       ...
                        break;
                }
            }
            //看下performFiltering()方法,只看主要的,
            // prefix为用户输入内容
            protected FilterResults performFiltering(CharSequence prefix) {
                final FilterResults results = new FilterResults();
                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        //mObjects 看源码即可知道,该对象就是传入得数据
                        //复制一份数据
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
                    //对拷贝得数据进行遍历,然后得到前缀相符得数据,以这些相符得数据生成一个新的数据源  
                    final ArrayList<T> newValues = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        final T value = values.get(i);
                        final String valueText = value.toString().toLowerCase();
                        if (valueText.startsWith(prefixString)) {
                            newValues.add(value);
                        } else {
                            ...
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                //将结果返回  
                return results;
            }
            //来看看ResultsHandler中的handleMessage()
            @Override
            public void handleMessage(Message msg) {
                RequestArguments args = (RequestArguments) msg.obj;
                //这个方法得在ArrayAdapter中看  
                publishResults(args.constraint, args.results);
                if (args.listener != null) {
                    int count = args.results != null ? args.results.count : -1;
                    //还记得  performFiltering()传入得this么,就是FilterListener对象
                    //回到AutoCompleteTextView中看看 onFilterComplete()
                    args.listener.onFilterComplete(count);
                }
            }
            public void onFilterComplete(int count) {
                //该方法最终将下拉框弹出来了  
                updateDropDownForFilter(count);
            }
            //弹出下拉框
            private void updateDropDownForFilter(int count) {
                    final boolean dropDownAlwaysVisible = mPopup.isDropDownAlwaysVisible();
                    final boolean enoughToFilter = enoughToFilter();
                    if ((count > 0 || dropDownAlwaysVisible) && enoughToFilter) {
                        if (hasFocus() && hasWindowFocus() && mPopupCanBeUpdated) {
                            showDropDown();
                        }
                    } else if (!dropDownAlwaysVisible && isPopupShowing()) {
                        ...
                    }
                }
            //这个方法主要就是刷新ListPopupWindow中得数据
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //results.values; 其实就是刚刚筛选过的匹配得数据,然后重新赋值给ListPopupWindow后刷新数据.
                mObjects = (List<T>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
            ```