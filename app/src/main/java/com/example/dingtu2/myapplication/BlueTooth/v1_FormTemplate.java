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
    private ICallback _returnCallback = null; // �ص�����

    public void SetCallback(ICallback returnCallback) {
        _returnCallback = returnCallback;
    }

    public void HideSoftInputMode() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // ��������ʱ�������뷨
    }



    /**
     * ���ñ����ı�
     *
     * @param CaptionText
     */
    public void SetCaption(String CaptionText) {
        Button button = (Button) this.findViewById(R.id.formtemp_quit);
        button.setText("" + CaptionText);
    }




    /**
     * ���������ť���е���ɫ����
     */
    public final static float[] BT_SELECTED = new float[]{1.5f, 0, 0, 0, 0, 1.6f, 1.5f, 0, 0, 0, 1.8f, 0, 1.3f, 0, 0,
            0, 0, 0, 1.3f, 0};


    /**
     * ��ť��������Ч��
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
     * ����ͼƬ��ť��ȡ����ı�״̬
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
        this.setCancelable(false); // ����Ӧ���˼�
        this.HideSoftInputMode();
        this.setContentView(R.layout.l_dialogtemplate);
        // ���˳���ť
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

    // ��ť�¼�
    public void DoCommand(String StrCommand) {
        if (StrCommand.equals(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.exit))) {
            Button v = (Button) v1_FormTemplate.this.findViewById(R.id.formtemp_quit);
            if (v.getText().equals("������������")) {
                Tools.ShowYesNoMessage(v1_FormTemplate.this.getContext(), "ȷ���˳���", new ICallback() {

                    @Override
                    public void OnClick(String Str, Object ExtraStr) {
                        if (Str.equals("YES")) {
                            if (v1_FormTemplate.this._returnCallback != null)
                                v1_FormTemplate.this._returnCallback.OnClick("�˳�", "");
                            v1_FormTemplate.this.dismiss();
                        }

                    }
                });
            } else {
                if (this._returnCallback != null)
                    this._returnCallback.OnClick("�˳�", "");
                this.dismiss();
            }
        }
    }

    // ���������ߴ�
    public void SetOtherView(int ViewID) {
        LayoutInflater inflater = (LayoutInflater) this.C.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View vPopupWindow = inflater.inflate(ViewID, null, false);
        this.SetOtherView(vPopupWindow);
    }

    public void SetOtherView(View view) {
        LinearLayout LY = (LinearLayout) this.findViewById(R.id.databindalertdialoglayout);
        LY.addView(view, LY.getLayoutParams());
    }

    // �������óߴ�
    public void ReSetSize(float WScale, float HScale) {
        ReSetSize(WScale, HScale, 0, 0);
    }

    public void ReSetSize(float WScale, float HScale, int XOffset, int YOffset) {
        WindowManager.LayoutParams p = this.getWindow().getAttributes();
        WindowManager m = ((Activity) PubVar.m_DoEvent.m_Context).getWindowManager();

        // ��ȡ�Ի���ǰ�Ĳ���ֵ ���ɸ��ݲ�ͬ�ֱ��ʵ�������ʾ�ĸ����
        Display d = m.getDefaultDisplay(); // Ϊ��ȡ��Ļ����
        p.x = XOffset; // ����λ�� Ĭ��Ϊ����
        p.y = YOffset; // ����λ�� Ĭ��Ϊ����

        p.width = (int) (d.getWidth() * WScale); // �������Ϊ��Ļ
        if (HScale > 0)
            p.height = (int) (d.getHeight() * HScale); // �߶�����Ϊ��Ļ

        this.getWindow().setAttributes((android.view.WindowManager.LayoutParams) p);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DoCommand("�˳�");
            return false;

        }
        return super.onKeyDown(keyCode, event);
    }

}
