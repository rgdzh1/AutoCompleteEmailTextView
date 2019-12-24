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
    }

    @SuppressLint("ResourceAsColor")
    private void initParame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoCompleteEmailTextView, defStyleAttr, 0);
        mItemResourecID = typedArray.getResourceId(R.styleable.AutoCompleteEmailTextView_acetv_adapter_ietm, R.layout.library_acet);
        typedArray.recycle();
    }

    private void init(Context context) {
        this.setFocusableInTouchMode(true);
        this.setAdapter(new EmailAutoCompleteAdapter(context, R.layout.library_acet, emailSufixs, this));
        this.setThreshold(1);//输入1个字符之后立马开启框下拉展示
        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //获取焦点时候
                    String mInputContetn = AutoCompleteEmailTextView.this.getText().toString().trim();
                    if (!TextUtils.isEmpty(mInputContetn)) {
                        //如果输入的内容不为null,则执行过滤
                        performFiltering(mInputContetn, 0);
                    }
                }
            }
        });
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        //********输入的数据与Adapter中的某条数据开始的部分完全匹配,那么Adapter中的这条数据就会出现在下拉提示框中
        String mInputContent = text.toString();
        int indexOf = mInputContent.indexOf("@");
        if (!mInputContent.equals("@")) {
            if (indexOf != -1) {
                //如果输入的内容包含@,将@之后的内容交给AutoCompleteEmailTextView 处理
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
