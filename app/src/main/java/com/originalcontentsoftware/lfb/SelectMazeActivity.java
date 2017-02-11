package com.originalcontentsoftware.lfb;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SelectMazeActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.select_maze);

    // We want to force portrait screen orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    File directory = Environment.getExternalStorageDirectory();
    String[] files = directory.list(new FilenameFilter() {
      public boolean accept(File directory, String fileName) {
        return fileName.endsWith(".ocsmaze");
      }
    });

    String fileString = "";

    for (int i = 0; i < files.length; i++) {
      fileString += files[i] + "\n";
    }

    TextView tv = (TextView)findViewById(R.id.textViewMazeFiles);
    tv.setText(fileString);

    final Button button = (Button) findViewById(R.id.button1);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Update message to include entered name
        EditText editText =  (EditText)findViewById(R.id.editText1);
        String name = editText.getText().toString();

        // Hide keyboard when finished
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        Intent myIntent = new Intent(v.getContext(), LFB.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivityForResult(myIntent, 0);
        myIntent.putExtra("FileName", name);
        setResult(RESULT_OK, myIntent);
        finish();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }
}
