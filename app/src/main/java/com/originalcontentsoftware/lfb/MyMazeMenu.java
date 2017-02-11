package com.originalcontentsoftware.lfb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.originalcontentsoftware.lfb.PopupNotify.ButtonOkayCallback;

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

public class MyMazeMenu extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.my_maze_menu);

    // We want to force portrait screen orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    final Button button7 = (Button) findViewById(R.id.button7);
    button7.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Edit");
      }
    });

    final Button button3 = (Button) findViewById(R.id.button3);
    button3.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Load");
      }
    });

    final Button button5 = (Button) findViewById(R.id.button5);
    button5.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Email");
      }
    });

    final Button button4 = (Button) findViewById(R.id.button4);
    button4.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Leave");
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  public void handleClick(View v, String button) {
    if (button.equals("Edit")) {
      final String newMazeFile = getResources().getString(R.string.load_maze_default);
      Intent myIntent = new Intent(this.getApplicationContext(), MakeMazeActivity.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      myIntent.putExtra("MazeFile", newMazeFile);
      myIntent.putExtra("Location", "internal");
      startActivityForResult(myIntent, 2);
    }
    else if (button.equals("Load")) {
      final String newMazeFile = getResources().getString(R.string.load_maze_default);

      File file = this.getApplicationContext().getFileStreamPath(newMazeFile);
      if (!file.exists()) {
        new PopupNotify(
          this,
          getResources().getString(R.string.my_maze_menu_load_error),
          new PopupNotify.ButtonOkayCallback() {
            @Override
            public void callback() {}
          }
        );
      }
      else {
        Intent myIntent = new Intent(this.getApplicationContext(), LFB.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        myIntent.putExtra("MazeFile", newMazeFile);
        myIntent.putExtra("Location", "internal");
        startActivityForResult(myIntent, 4);
      }
    }
    else if (button.equals("Email")) {
      /*Intent myIntent = new Intent(this.getApplicationContext(), EmailMazeActivity.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivityForResult(myIntent, 3);
       */
      final String newMazeFile = getResources().getString(R.string.load_maze_default);

      File fileTest = this.getApplicationContext().getFileStreamPath(newMazeFile);
      if (!fileTest.exists()) {
        new PopupNotify(
          this,
          getResources().getString(R.string.my_maze_menu_email_error),
          new PopupNotify.ButtonOkayCallback() {
            @Override
            public void callback() {}
          }
        );
      }
      else {
        Maze maze = new Maze();
        maze.importMazeInternal(newMazeFile, this.getApplicationContext());
        maze.exportMazeExternal("temp_maze.ocsmaze", this.getApplicationContext());

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_maze_send_subject));

        String message =
            getResources().getString(R.string.email_maze_send_message1) + "\n\n" +
                maze.generateHeader(getApplicationContext()) + "\n\n" +
                getResources().getString(R.string.email_maze_send_message2) + "\n\n" +
                getResources().getString(R.string.email_maze_send_legal);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        // This won't work form internal storage --
        final File file = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath(), "temp_maze.ocsmaze");
        emailIntent.setType("application/octet-stream");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(emailIntent);
      }
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
    else {}
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
        public void secondButtonCallback() {}

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
}
