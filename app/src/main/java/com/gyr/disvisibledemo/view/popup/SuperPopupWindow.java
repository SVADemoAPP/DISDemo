package com.gyr.disvisibledemo.view.popup;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.gyr.disvisibledemo.framework.activity.BaseActivity;
import com.gyr.disvisibledemo.util.WindowChangeUtils;


/***
 * 自己写来用于好使用各种弹窗的类
 * xhf
 */
public class SuperPopupWindow extends PopupWindow {
    private View mView;
    private BaseActivity context;
    private static float light=1f;
    private static float black=0.6f;
    /**
     * 构造函数
     *
     * @param context 上下文对象
     * @param layout
     */
    public SuperPopupWindow(final Context context, int layout,ViewListener viewListener) {
        this.context = (BaseActivity) context;
        mView = LayoutInflater.from(context).inflate(layout, null, false);
        //设置PopupWindow的View
        this.setContentView(mView);
        //设置PopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置PopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置PopupWindow弹出窗体可点击
        this.setOutsideTouchable(false);//设置外部点击可消失
        this.setFocusable(false);
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowChangeUtils.changeWindowAlfa((BaseActivity) context, light);//改变窗口透明度
            }
        });
        viewListener.getViewOfPop(mView);
    }

    public void setWarpContent() {
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setWidthAndHeight(int width,int height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    /**
     * 设置为true :PopupWindow可以点击周围消失，并且具备焦点
     * 设置为flase:PopupWindow不可以通过点击来取消时间。（一般这时候需要重写物理返回键事件）
     * PS:一般是使用来Popupwindow进行弹框的时候填写EditText的时候可以使用
     *
     * @param flag
     */
    public void setChangFocusable(boolean flag) {
        this.setFocusable(flag);
        this.setOutsideTouchable(flag);
    }

    /***
     * 显示Popupwindow
     *
     */
    public void showPopupWindow() {
        WindowChangeUtils.changeWindowAlfa(context, black);//改变窗口透明度
        this.showAtLocation(mView, Gravity.CENTER, 0, 0);
        this.update();
    }

    /**
     * 显示在某view下方
     * @param view
     */
    public void showPopupWindowAsDropDown(View view) {
//        WindowChangeUtils.changeWindowAlfa(context, black);//改变窗口透明度
        this.showAsDropDown(view);
        this.update();
    }

    /**
     * 隐藏PopupWindow
     */
    public void hidePopupWindow() {
        WindowChangeUtils.changeWindowAlfa((BaseActivity) context, light);//改变窗口透明度
        this.dismiss();
    }

    /**
     * 获取PopupWindow弹出的View类
     *
     * @return
     */
    public View getPopupView() {
        return mView;
    }

    /**
     * 设置动画样式
     */
    public void setAnimotion(int style) {
        this.setAnimationStyle(style);
    }

    public interface ViewListener{
        void getViewOfPop(View view);
    }

}