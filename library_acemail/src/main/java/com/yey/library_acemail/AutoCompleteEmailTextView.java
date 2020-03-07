package com.yey.library_acemail;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class AutoCompleteEmailTextView extends AppCompatAutoCompleteTextView {

    private String[] emailSufixs = new String[]{"@163.com", "@gmail.com", "@hotmail.com", "@jk.com", "@yuo.com"};
    private int mItemResourecID;

    public AutoCompleteEmailTextView(Context context) {
        this(context, null);
    }

    public AutoCompleteEmailTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoCompleteEmailTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParame(context, attrs, defStyleAttr);
        init(context);
        initListener();
    }


    @SuppressLint("ResourceAsColor")
    private void initParame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoCompleteEmailTextView, defStyleAttr, 0);
        //下拉框(ListPopupWindow)中Item条目的布局文件
        mItemResourecID = typedArray.getResourceId(R.styleable.AutoCompleteEmailTextView_acetv_adapter_ietm, R.layout.library_acet);
        typedArray.recycle();
    }

    private void init(Context context) {
        //触摸模式可以获取焦点
        this.setFocusableInTouchMode(true);
        //为AutoCompleteEmailTextView设置Adapter
        this.setAdapter(new EmailAutoCompleteAdapter(context, R.layout.library_acet, emailSufixs, this));
        //设置输入一个字符后下拉框会弹出
        this.setThreshold(1);
    }

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
                super.performFiltering(mInputContent.substring(indexOf), keyCode);
            } else {
                //这种是情况3
                //首先判断内容是否包含特殊字符
                if (mInputContent.matches("^[a-zA-Z0-9_]+$")) {
                    //正常字符,那么将@字符作为过滤器,
                    //过滤的时,如果ArrayAdapter数据源中的数据以@字符开头,那么该数据就符合条件,
                    //最后会在ListPopupWindow里展示出来
                    //看源码,该方法最终会触发`ListPopupWindow`的显示
                    super.performFiltering("@", keyCode);
                } else {
                    //包含特殊字符,隐藏弹窗
                    this.dismissDropDown();
                }
            }
        }
    }

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

    public int getmItemResourecID() {
        return mItemResourecID;
    }
}
