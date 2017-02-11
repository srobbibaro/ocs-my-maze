package com.originalcontentsoftware.lfb;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupDialog {
  public PopupDialog(Activity activity, String message, String okayButton, String secondButton, boolean showSecond, ButtonCallback callback) {
    this.callback = callback;
    init(activity, message, okayButton, secondButton, showSecond);
  }

  public interface ButtonCallback {
    void okayButtonCallback();
    void secondButtonCallback();
  }

  private ButtonCallback callback;

  public void init(Activity activity, String message, String okayButton, String secondButton, boolean showSecond) {
    LayoutInflater layoutInflater
    = (LayoutInflater)activity.getBaseContext()
    .getSystemService(activity.LAYOUT_INFLATER_SERVICE);
    View popupView = layoutInflater.inflate(R.layout.popup_dialog, null);

    final PopupWindow popupWindow = new PopupWindow(
        popupView,
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);

    TextView messageTextView = (TextView)popupView.findViewById(R.id.message);
    messageTextView.setText(message);

    Button btnOkay = (Button)popupView.findViewById(R.id.okay);
    btnOkay.setText(okayButton);
    btnOkay.setOnClickListener(new Button.OnClickListener(){

      @Override
      public void onClick(View v) {
        popupWindow.dismiss();
        callback.okayButtonCallback();
        return;
      }});

    Button btnSecond = (Button)popupView.findViewById(R.id.second);
    btnSecond.setText(secondButton);
    if (showSecond) {
      btnSecond.setVisibility(View.VISIBLE);
    }
    else {
      btnSecond.setVisibility(View.GONE);
    }

    btnSecond.setOnClickListener(new Button.OnClickListener(){

      @Override
      public void onClick(View v) {
        popupWindow.dismiss();
        callback.secondButtonCallback();
      }});

    // TODO: This "new LinearLayout" here can't be the best way to do this
    popupWindow.showAtLocation(new LinearLayout(activity), Gravity.CENTER, 0, 0);
  }
}

