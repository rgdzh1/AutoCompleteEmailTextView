> [项目地址](https://github.com/rgdzh1/AutoCompleteEmailTextView)
##### 初始化
- 获取Item布局文件id
    ```java
    @SuppressLint("ResourceAsColor")
    private void initParame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoCompleteEmailTextView, defStyleAttr, 0);
        //下拉框(ListPopupWindow)中Item条目的布局文件
        mItemResourecID = typedArray.getResourceId(R.styleable.AutoCompleteEmailTextView_acetv_adapter_ietm, R.layout.library_acet);
        typedArray.recycle();
    }    
    ```
- 控件的一些设置
    ```java
    private void init(Context context) {
        //触摸模式可以获取焦点
        this.setFocusableInTouchMode(true);
        //为AutoCompleteEmailTextView设置Adapter
        this.setAdapter(new EmailAutoCompleteAdapter(context, R.layout.library_acet, emailSufixs, this));
        //设置输入一个字符后下拉框会弹出
        this.setThreshold(1);
    }
    ```
- 为控件设置监听
    ```java
    private void initListener() {
        //为AutoCompleteEmailTextView设置获取焦点监听
        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //AutoCompleteEmailTextView获取焦点后
                if (hasFocus) {
                    String mInputContetn = AutoCompleteEmailTextView.this.getText().toString().trim();
                    if (!TextUtils.isEmpty(mInputContetn)) {
                        //用户输入的内容不为空,则执行performFiltering()过滤
                        performFiltering(mInputContetn, 0);
                    }
                }
            }
        });
    }
    ```
##### 过滤器定义
- 定义好过滤器之后, 会根据返回的字符进行匹配,如果符合匹配则可以通过 `AutoCompleteEmailTextView` 的Adapter里的 `getItem()`方法获取到相匹配的字符
    ```java
    /**
     * 自定义过滤器
     *
     * @param text
     * @param keyCode
     */
    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        String mInputContent = text.toString();
        if (!mInputContent.equals("@")) {
            //输入的内容3种情况
            //1.@
            //2,@+非@字符
            //3,非@字符
            int indexOf = mInputContent.indexOf("@");
            if (indexOf != -1) {
                //这种是情况1和2
                //因为含有@字符,只需要将@字符以后的字符串作为过滤器传入performFiltering()中
                //之后会将过滤器与ArrayAdapter中的数据源匹配,最后会show出ListPopupWindow,然后在里面展示匹配的数据.
                //看源码,该方法最终会触发`ListPopupWindow`的显示  
                super.performFiltering(mInputContent.substring(indexOf), keyCode);
            } else {
                //这种是情况3
                //首先判断内容是否包含特殊字符
                if (mInputContent.matches("^[a-zA-Z0-9_]+$")) {
                    //正常字符,那么将@字符作为过滤器,
                    //过滤的时,如果ArrayAdapter数据源中的数据以@字符开头,那么该数据就符合条件,
                    //最后会在ListPopupWindow里展示出来
                    super.performFiltering("@", keyCode);
                } else {
                    //包含特殊字符,隐藏弹窗
                    this.dismissDropDown();
                }
            }
        }
    }
    ```
##### 内容显示
- `ListPopupWindow`中条目被选中后, 会返回该条目索引对应的邮箱后缀到 `replaceText()`中, 再将拼接好的邮箱展示到控件上面就达到最终目的了.
    ```java
    /**
     * @param text 该值为emailSufixs中的某个值,也就是邮箱的后缀了
     */
    @Override
    protected void replaceText(CharSequence text) {
        //获取用户输入的内容
        String mInputContent = this.getText().toString();
        int indexOf = mInputContent.indexOf("@");
        if (indexOf != -1) {
            //如果内容中间有@字符,则将@字符前的内容与邮箱后缀链接起来
            mInputContent = mInputContent.substring(0, indexOf);
        }
        //将内容交由父类中的replaceText()方法,
        //父类中的该方法最终会将拼接好的邮箱展示在控件里面,然后对光标进行了处理.
        super.replaceText(mInputContent + text);
    }
    ```
##### 其他方法
- 他方法
    ```java
    /**
     * 设置邮箱后缀,该数据会交由AutoCompleteEmailTextView的Adapter处理
     *
     * @param es
     */
    public void setEmailSufixs(String[] es) {
        if (es != null && es.length > 0)
            this.emailSufixs = es;
    }
    /**
     * 界面销毁处理
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.dismissDropDown();
    }
    ```          