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

public class PopupNotify {
  public PopupNotify(Activity activity, String message, ButtonOkayCallback callback) {
    this.callback = callback;
    init(activity, message);
  }

  public interface ButtonOkayCallback {
    void callback();
  }

  private ButtonOkayCallback callback;

  public void init(Activity activity, String message) {
    LayoutInflater layoutInflater
    = (LayoutInflater)activity
    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View popupView = layoutInflater.inflate(R.layout.popup_notify, null);

    final PopupWindow popupWindow = new PopupWindow(
        popupView,
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);

    TextView messageTextView = (TextView)popupView.findViewById(R.id.message);
    messageTextView.setText(message);

    Button okay = (Button)popupView.findViewById(R.id.okay);
    okay.setOnClickListener(
        new Button.OnClickListener() {
          @Override
          public void onClick(View v) {
            popupWindow.dismiss();
            callback.callback();
            return;
          }
        });

    popupWindow.showAtLocation(new LinearLayout(activity), Gravity.CENTER, 0, 0);
  }
}
