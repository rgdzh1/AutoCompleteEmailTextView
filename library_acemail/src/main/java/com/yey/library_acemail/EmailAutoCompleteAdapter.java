package com.yey.library_acemail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class EmailAutoCompleteAdapter extends ArrayAdapter<String> {
    AutoCompleteEmailTextView autoCompleteEmailTextView;

    public EmailAutoCompleteAdapter(Context context, int textViewResourceId, String[] es, AutoCompleteEmailTextView acetv) {
        super(context, textViewResourceId, es);
        autoCompleteEmailTextView = acetv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mConvertView = convertView;
        if (mConvertView == null) {
            mConvertView = LayoutInflater.from(getContext()).inflate(autoCompleteEmailTextView.getmItemResourecID(), null);
        }
        TextView mShowTV = (TextView) mConvertView.findViewById(R.id.email);
        String mInputContent = autoCompleteEmailTextView.getText().toString();
        int index = mInputContent.indexOf("@");
        //如果用户输入了@字符
        if (index != -1) {
            //那么就截取@字符之前的内容,将@之后的内容去除,也就是将邮箱尾缀去掉
            mInputContent = mInputContent.substring(0, index);
        }
        //将邮箱前缀的内容和尾缀内容拼接一起,展示到下拉框中的TextView中
        mShowTV.setText(mInputContent + getItem(position));
        mShowTV.setGravity(autoCompleteEmailTextView.getGravity());
        int mTextSp = ACETDensityUtil.px2dip(autoCompleteEmailTextView.getContext(), autoCompleteEmailTextView.getTextSize());
        mShowTV.setTextSize(mTextSp);
        return mConvertView;
    }
}
