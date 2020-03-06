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

    private static final String TAG = AutoCompleteEmailTextView.class.getName();

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
        //下拉框中Item条目的资源文件
        mItemResourecID = typedArray.getResourceId(R.styleable.AutoCompleteEmailTextView_acetv_adapter_ietm, R.layout.library_acet);
        typedArray.recycle();
    }

    private void init(Context context) {
        //触摸模式可以获取焦点
        this.setFocusableInTouchMode(true);
        //下拉框所用的Adapter
        this.setAdapter(new EmailAutoCompleteAdapter(context, R.layout.library_acet, emailSufixs, this));
        //输入一个字符后下拉框会弹出
        this.setThreshold(1);
    }

    private void initListener() {
        //为控件设置获取焦点监听
        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //获取焦点后
                if (hasFocus) {
                    String mInputContetn = AutoCompleteEmailTextView.this.getText().toString().trim();
                    if (!TextUtils.isEmpty(mInputContetn)) {
                        //内容不为空,则开始过滤当前内容,设置特定的规则,然后弹出下拉框
                        performFiltering(mInputContetn, 0);
                    }
                }
            }
        });
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        String mInputContent = text.toString();
        if (!mInputContent.equals("@")) {
            //输入的内容不仅是@字符
            //获取输入内容中,@字符的索引, 不存在为-1
            int indexOf = mInputContent.indexOf("@");
            if (indexOf != -1) {
                //如果输入内容为中含有@字符
                //将@字符以后的内容,如@qq.com,作为控件的过滤器
                super.performFiltering(mInputContent.substring(indexOf), keyCode);
            } else {
                //如果没有输入@字符, 那对输入的内容进行正则匹配
                if (mInputContent.matches("^[a-zA-Z0-9_]+$")) {
                    super.performFiltering("@", keyCode);
                } else {
                    this.dismissDropDown();//当用户中途输入非法字符时，关闭下拉提示框
                }
            }
        }
    }

    @Override
    protected void replaceText(CharSequence text) {
        //text代表的是邮箱的后缀,此时需要加上输入的邮箱前缀
        String mInputContent = this.getText().toString();
        int indexOf = mInputContent.indexOf("@");
        if (indexOf != -1) {
            mInputContent = mInputContent.substring(0, indexOf);
        }
        super.replaceText(mInputContent + text);
    }

    /**
     * 设置邮箱后缀
     *
     * @param es
     */
    public void setEmailSufixs(String[] es) {
        if (es != null && es.length > 0)
            this.emailSufixs = es;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.dismissDropDown();
    }

    public int getmItemResourecID() {
        return mItemResourecID;
    }
}
