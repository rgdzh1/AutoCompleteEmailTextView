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
        //ListPopupWindow中展示Email的TevtView的id必须是email.
        //如果需要在条目上增加图标或者其他内容, 不必关注展示email的控件,方便控制.
        TextView mShowTV = (TextView) mConvertView.findViewById(R.id.email);
        //获取用户输入的内容
        String mInputContent = autoCompleteEmailTextView.getText().toString();
        int index = mInputContent.indexOf("@");
        //如果用户输入的内容中含有@字符
        if (index != -1) {
            //那么就截取@字符之前的内容,将@之后的内容去除
            mInputContent = mInputContent.substring(0, index);
        }
        //最后将不含有@字符的内容与邮箱后缀拼接在一起,最后展示到ListPopupWindow中的条目中
        mShowTV.setText(mInputContent + getItem(position));
        return mConvertView;
    }
}
