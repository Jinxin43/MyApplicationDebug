package com.example.dingtu2.myapplication.BlueTooth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.DingTu.Base.ICallback;
import com.DingTu.Base.PubVar;
import com.DingTu.Base.Tools;
import com.example.dingtu2.myapplication.R;

import static com.example.dingtu2.myapplication.controls.FormTemplate.buttonOnFocusChangeListener;


public class v1_FormTemplate extends Dialog {
    public Context C = null;
    private ICallback _returnCallback = null; // 回调函数

    public void SetCallback(ICallback returnCallback) {
        _returnCallback = returnCallback;
    }

    public void HideSoftInputMode() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 弹出窗体时隐藏输入法
    }



    /**
     * 设置标题文本
     *
     * @param CaptionText
     */
    public void SetCaption(String CaptionText) {
        Button button = (Button) this.findViewById(R.id.formtemp_quit);
        button.setText("" + CaptionText);
    }




    /**
     * 按下这个按钮进行的颜色过滤
     */
    public final static float[] BT_SELECTED = new float[]{1.5f, 0, 0, 0, 0, 1.6f, 1.5f, 0, 0, 0, 1.8f, 0, 1.3f, 0, 0,
            0, 0, 0, 1.3f, 0};


    /**
     * 按钮触碰按下效果
     */
    public final static OnTouchListener buttonOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
                v.invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.getBackground().clearColorFilter();
                v.invalidate();
            }
            return false;
        }
    };

    /**
     * 设置图片按钮获取焦点改变状态
     *
     */
    public final static void setButtonFocusChanged(View inView) {
        inView.setOnTouchListener(buttonOnTouchListener);
        inView.setOnFocusChangeListener(buttonOnFocusChangeListener);
    }

    public v1_FormTemplate(Context context) {
        super(context);
        this.C = context;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCancelable(false); // 不响应回退键
        this.HideSoftInputMode();
        this.setContentView(R.layout.l_dialogtemplate);
        // 绑定退出按钮
        Button v = (Button) this.findViewById(R.id.formtemp_quit);
        v.setOnClickListener(new ViewClick());
        v.setText(Tools.ToLocale(v.getText() + "") + " ");
        setButtonFocusChanged(v);
    }

    class ViewClick implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {
            String Tag = arg0.getTag().toString();
            v1_FormTemplate.this.DoCommand(Tag);
        }
    }

    // 按钮事件
    public void DoCommand(String StrCommand) {
        if (StrCommand.equals(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.exit))) {
            Button v = (Button) v1_FormTemplate.this.findViewById(R.id.formtemp_quit);
            if (v.getText().equals("输入坐标序列")) {
                Tools.ShowYesNoMessage(v1_FormTemplate.this.getContext(), "确定退出？", new ICallback() {

                    @Override
                    public void OnClick(String Str, Object ExtraStr) {
                        if (Str.equals("YES")) {
                            if (v1_FormTemplate.this._returnCallback != null)
                                v1_FormTemplate.this._returnCallback.OnClick("退出", "");
                            v1_FormTemplate.this.dismiss();
                        }

                    }
                });
            } else {
                if (this._returnCallback != null)
                    this._returnCallback.OnClick("退出", "");
                this.dismiss();
            }
        }
    }

    // 设置其它尺寸
    public void SetOtherView(int ViewID) {
        LayoutInflater inflater = (LayoutInflater) this.C.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View vPopupWindow = inflater.inflate(ViewID, null, false);
        this.SetOtherView(vPopupWindow);
    }

    public void SetOtherView(View view) {
        LinearLayout LY = (LinearLayout) this.findViewById(R.id.databindalertdialoglayout);
        LY.addView(view, LY.getLayoutParams());
    }

    // 重新设置尺寸
    public void ReSetSize(float WScale, float HScale) {
        ReSetSize(WScale, HScale, 0, 0);
    }

    public void ReSetSize(float WScale, float HScale, int XOffset, int YOffset) {
        WindowManager.LayoutParams p = this.getWindow().getAttributes();
        WindowManager m = ((Activity) PubVar.m_DoEvent.m_Context).getWindowManager();

        // 获取对话框当前的参数值 ，可根据不同分辨率的设置显示的高与宽
        Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
        p.x = XOffset; // 设置位置 默认为居中
        p.y = YOffset; // 设置位置 默认为居中

        p.width = (int) (d.getWidth() * WScale); // 宽度设置为屏幕
        if (HScale > 0)
            p.height = (int) (d.getHeight() * HScale); // 高度设置为屏幕

        this.getWindow().setAttributes((android.view.WindowManager.LayoutParams) p);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DoCommand("退出");
            return false;

        }
        return super.onKeyDown(keyCode, event);
    }

}
