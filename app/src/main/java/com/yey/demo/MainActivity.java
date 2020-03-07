package com.yey.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.yey.library_acemail.AutoCompleteEmailTextView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoCompleteEmailTextView emailTextView = (AutoCompleteEmailTextView) findViewById(R.id.acet);
        emailTextView.setEmailSufixs(new String[]{"@163.com", "@gmail.com", "@hotmail.com", "@jk.com", "@yuo.com"});
//        emailTextView.setDropDownBackgroundResource(android.R.color.white);
//        emailTextView.setDropDownVerticalOffset(50);//向下偏移
//        emailTextView.setDropDownWidth(100);
//        emailTextView.setDropDownHeight(200);
    }
}
