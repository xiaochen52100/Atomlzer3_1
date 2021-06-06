package com.example.atomlzer30;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.core.content.ContextCompat;

public class MyNumberPicker extends NumberPicker {

    /**
     * 构造方法 NumberPicker
     * */

    public MyNumberPicker(Context context) {
        super(context);
    }

    public MyNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * addView方法 ViewGroup
     * */

    @Override
    public void addView(View child) {
        super.addView(child);
        setNumberPickerView(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        setNumberPickerView(child);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        setNumberPickerView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        setNumberPickerView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setNumberPickerView(child);
    }

    public void setNumberPickerView(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextWhite)); //字体颜色
            ((EditText) view).setTextSize(25f);//字体大小
        }
    }
}

