package com.originalcontentsoftware.lfb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.bugsnag.android.Bugsnag;

public class MainMenuActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bugsnag.init(this);

    setContentView(R.layout.main_menu);

    // We want to force portrait screen orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    TextView editText =  (TextView)findViewById(R.id.textViewNotice);
    String message = editText.getText().toString() + " - v" + getAppVersion();
    editText.setText(message);

    final Button button = (Button) findViewById(R.id.button1);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Play");
      }
    });

    final Button button2 = (Button) findViewById(R.id.button2);
    button2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Create");
      }
    });

    final Button button8 = (Button) findViewById(R.id.button8);
    button8.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "MyMaze");
      }
    });

    final Button button4 = (Button) findViewById(R.id.button4);
    button4.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Leave");
      }
    });

    // TODO: this could be masking an issue -- why is this value stored even when the app is closed
    DebugInformation.isDebugEnabled = false;
    //DebugInformation.isDebugEnabled = true;

    String[] files = getApplicationContext().fileList();
    for (int i = 0; i < files.length; i++) {
      Log.v("LFB", "file (" + i + ")" + files[i]);
    }

    Intent launchIntent = getIntent();

    if (launchIntent != null) {
      Uri data = launchIntent.getData();

      if (data == null) {
        return;
      }

      Log.v("LFB", "data=" + data);
      Log.v("LFB", "path=" + data.getPath());

      Intent myIntent = new Intent(this, LFB.class);
      myIntent.putExtra("MazeFile", "from_uri");
      myIntent.putExtra("MazeUri", data);
      myIntent.putExtra("Location", "uri");
      startActivityForResult(myIntent, 4);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  public void handleClick(View v, String button) {
    if (button.equals("Play")) {
      Intent myIntent = new Intent(this.getApplicationContext(), LFB.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      myIntent.putExtra("MazeFile", "demo_maze1.ocsmaze");
      myIntent.putExtra("Location", "resource");
      startActivityForResult(myIntent, 4);
    }
    else if (button.equals("Create")) {
      Intent myIntent = new Intent(this.getApplicationContext(), MakeMazeActivity.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      myIntent.putExtra("MazeFile", "no_maze");
      myIntent.putExtra("Location", "internal");
      startActivityForResult(myIntent, 2);
    }
    else if (button.equals("MyMaze")) {
      Intent myIntent = new Intent(this.getApplicationContext(), MyMazeMenu.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivityForResult(myIntent, 6);
    }
    else if (button.equals("Leave")) {
      //handleCancel();
      finish();
    }

    /*
      Intent myIntent = new Intent(v.getContext(), LFB.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      //myIntent.putExtra("MazeFile", newMazeFile);
      setResult(RESULT_OK, myIntent);
      finish();
     */
    else {
    }
  }

  @Override
  public void onBackPressed() {
    //handleCancel();
    finish();
  }

  public void handleCancel() {
    showDialog();
  }

  public void showDialog() {
    new PopupDialog(
        this,
        getResources().getString(R.string.popup_question_label),
        getResources().getString(R.string.popup_question_yes),
        getResources().getString(R.string.popup_question_no),
        true,
        new PopupDialog.ButtonCallback() {

          @Override
          public void secondButtonCallback() {
          }

          @Override
          public void okayButtonCallback() {
            /*
        Intent myIntent = new Intent(v.getContext(), LFB.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(RESULT_CANCELED, myIntent);
             */
            finish();
          }
        }
        );
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Check which request we're responding to
    if (requestCode == 0) {
      // Make sure the request was successful
      if (resultCode == RESULT_OK) {
        Intent myIntent = new Intent(this, LFB.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String fileName = data.getStringExtra("FileName");
        final File file = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath(), fileName);
        myIntent.putExtra("MazeFile", file.getAbsolutePath());
        myIntent.putExtra("Location", "external");
        startActivityForResult(myIntent, 4);
      }
    }
    else if (requestCode == 2) {
      // Make sure the request was successful
      if (resultCode == RESULT_OK) {
        Intent myIntent = new Intent(this, LFB.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        myIntent.putExtra("MazeFile", data.getStringExtra("MazeFile"));
        myIntent.putExtra("Location", "internal");
        startActivityForResult(myIntent, 4);
      }
    }
    else if (requestCode == 10) {
      if (resultCode == RESULT_OK) {
        String FilePath = data.getData().getPath();
        Intent myIntent = new Intent(this, LFB.class);
        myIntent.putExtra("MazeFile", data.getStringExtra(FilePath));
        myIntent.putExtra("Location", "external");
        startActivityForResult(myIntent, 4);
      }
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  private String getAppVersion() {
    String version = "0.0";
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      version = pInfo.versionName;
    }
    catch (Exception e) {
      version = "0.0";
    }

    if (debugEnabled) {
      version += "-debug";
    }

    return version;
  }

  private boolean debugEnabled = false;
}
