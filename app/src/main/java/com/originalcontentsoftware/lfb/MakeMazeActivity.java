package com.originalcontentsoftware.lfb;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MakeMazeActivity extends Activity {
  // General Maze information
  private String newMazeFile;

  private enum MAZE_SIZE {
    // We need to use odd numbers for the maze generation algorithm to properly generate mazes
    EASY (11),
    NORMAL (21),
    DIFFICULT (41);

    private int size;

    private MAZE_SIZE(int size) {
      this.size = size;
    }

    public int getSize() {
      return size;
    }
  }

  private MAZE_SIZE mazeSize = MAZE_SIZE.NORMAL;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_maze);

    // ensure that all pictures have been cleared before we begin
    clearPictures();

    // Update the hint message when a new maze size is selected
    RadioGroup radioGroupMazeSize = (RadioGroup)findViewById(R.id.radioGroupMazeSize);
    radioGroupMazeSize.setOnCheckedChangeListener(
        new RadioGroup.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(RadioGroup group, int checkedId) {
            TextView tv = (TextView)findViewById(R.id.textViewMazeSizeInfo);

            if (checkedId == R.id.radioButtonMazeSizeSmall) {
              mazeSize = MAZE_SIZE.EASY;
              tv.setText(getResources().getString(R.string.make_maze_size_small_info));
            }
            else if (checkedId == R.id.radioButtonMazeSizeLarge) {
              mazeSize = MAZE_SIZE.DIFFICULT;
              tv.setText(getResources().getString(R.string.make_maze_size_large_info));
            }
            else {
              mazeSize = MAZE_SIZE.NORMAL;
              tv.setText(getResources().getString(R.string.make_maze_size_medium_info));
            }
          }
        }
        );

    final Button button2 = (Button) findViewById(R.id.button2);
    button2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "RegenerateMaze");
      }
    });

    final Button button3 = (Button) findViewById(R.id.button3);
    button3.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "RegenerateMazeEditQuestions");
      }
    });

    final Button button4 = (Button) findViewById(R.id.button4);
    button4.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Cancel");
      }
    });

    Intent sender = getIntent();

    MyMaze.myMaze = new Maze(null);

    String mazeFile = sender.getExtras().getString("MazeFile");
    if (mazeFile != null && !mazeFile.equals("no_maze")) {
      Log.v("LFB", "Edit maze file=" + mazeFile);

      String location = sender.getExtras().getString("Location");
      if (location == null) {
        location = "resource";
      }

      Log.v("LFB", "Location=" + location);

      Uri mazeUri = sender.getExtras().getParcelable("MazeUri");
      if (mazeUri == null) {
        Log.v("LFB", "Maze Uri is null");
      }
      else {
        Log.v("LFB", "Maze Uri=" + mazeUri);
      }

      Log.v("LFB", "Loading maze file...");
      boolean success = MyMaze.myMaze.loadMaze(mazeFile, getApplicationContext(), location, mazeUri);
      Log.v("LFB", "Maze load was " + success);
    }
    else {
      Log.v("LFB", "Create new maze");
      button2.setVisibility(View.GONE);
      button3.setText(getResources().getString(R.string.make_maze_generate_maze_add_questions));
    }

    EditText editTextCreatedBy = (EditText)findViewById(R.id.editTextCreatedBy);
    EditText editTextMazeName = (EditText)findViewById(R.id.editTextMazeName);
    editTextMazeName.setText(MyMaze.myMaze.getMazeName());
    editTextCreatedBy.setText(MyMaze.myMaze.getCreatedBy());

    // the default maze size is medium
    String strMazeDifficulty = MyMaze.myMaze.buildDifficultyString(getApplicationContext());
    RadioButton rb = (RadioButton)findViewById(R.id.radioButtonMazeSizeMedium);
    rb.setChecked(true);

    if (strMazeDifficulty.equals(getApplicationContext().getString(R.string.make_maze_size_small))) {
      rb = (RadioButton)findViewById(R.id.radioButtonMazeSizeSmall);
    }
    else if (strMazeDifficulty.equals(getApplicationContext().getString(R.string.make_maze_size_large))) {
      rb = (RadioButton)findViewById(R.id.radioButtonMazeSizeLarge);
    }

    rb.setChecked(true);

    // We want to force portrait screen orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    // Set the maze file name to the default...
    // TODO: We'll probably want to allow for mazes with different names
    newMazeFile = getResources().getString(R.string.load_maze_default);
  }

  public void clearPictures() {
    Log.v("LFB", "Remove pictures...");
    String[] files = getApplicationContext().fileList();
    for (int i = 0; i < files.length; i++) {
      if (files[i].matches("^pic\\d+\\.png$")) {
        Log.v("LFB", "Removing picture (" + i + ")" + files[i]);

        File dir = getFilesDir();
        File file = new File(dir, files[i]);
        file.delete();
      }
    }

    Log.v("LFB", "Files after pictures removed...");
    files = getApplicationContext().fileList();
    for (int i = 0; i < files.length; i++) {
      Log.v("LFB", "file (" + i + ")" + files[i]);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  public void handleClick(View v, String button) {
    if (button.equals("RegenerateMaze")) {
      //handleDone(false);
      Log.v("LFB", "Generating maze...");
      MyMaze.myMaze.generateMaze(mazeSize.getSize(), mazeSize.getSize());
      Log.v("LFB", "Done Generating maze.");

      if (!writeToFile()) {
        showNotifyValidation(getResources().getString(R.string.make_maze_store_fail));
      }

      Intent myIntent = new Intent(getApplicationContext(), LFB.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      myIntent.putExtra("MazeFile", newMazeFile);
      setResult(RESULT_OK, myIntent);
      finish();
    }
    else if (button.equals("RegenerateMazeEditQuestions")) {
      //handleDone(true);
      Intent myIntent = new Intent(getApplicationContext(), AddQuestionsActivity.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      myIntent.putExtra("MazeFile", newMazeFile);
      myIntent.putExtra("Location", "internal");
      startActivityForResult(myIntent, 2);
    }
    else {
      handleCancel();
    }
  }

  public boolean writeToFile() {
    EditText editTextCreatedBy = (EditText)findViewById(R.id.editTextCreatedBy);
    EditText editTextMazeName = (EditText)findViewById(R.id.editTextMazeName);

    String createdBy = editTextCreatedBy.getText().toString();
    createdBy.replaceAll("\n", "");

    String mazeName = editTextMazeName.getText().toString();
    mazeName.replaceAll("\n", "");

    if (mazeName.length() == 0) {
      mazeName = "Untitled Maze";
    }

    if (createdBy.length() == 0) {
      createdBy = "Anonymous";
    }

    MyMaze.myMaze.setCreatedBy(createdBy);
    MyMaze.myMaze.setMazeName(mazeName);

    Log.v("LFB", "Writing maze to file...");
    return MyMaze.myMaze.exportMazeInternal(newMazeFile, this);
  }

  public void hideKeyboard(View v) {
    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (inputManager != null) {
      inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
  }

  @Override
  public void onBackPressed() {
    handleCancel();
  }

  public void showCancelDialog() {
    new PopupDialog(
        this,
        getResources().getString(R.string.popup_cancel_label),
        getResources().getString(R.string.popup_cancel_yes),
        getResources().getString(R.string.popup_cancel_no),
        true,
        new PopupDialog.ButtonCallback() {

          @Override
          public void secondButtonCallback() {
          }

          @Override
          public void okayButtonCallback() {
            MyMaze.myMaze = null;
            Intent myIntent = new Intent(getApplicationContext(), LFB.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            setResult(RESULT_CANCELED, myIntent);
            finish();
          }
        }
        );
  }

  public void handleCancel() {
    showCancelDialog();
  }

  public void showDoneDialog(final boolean editQuestions) {
    new PopupDialog(
        this,
        getResources().getString(R.string.popup_done_label),
        getResources().getString(R.string.popup_done_yes),
        getResources().getString(R.string.popup_done_no),
        true,
        new PopupDialog.ButtonCallback() {

          @Override
          public void secondButtonCallback() {
          }

          @Override
          public void okayButtonCallback() {
            if (!editQuestions) {
              Log.v("LFB", "Generating maze...");
              MyMaze.myMaze.generateMaze(mazeSize.getSize(), mazeSize.getSize());
              Log.v("LFB", "Done Generating maze.");

              if (!writeToFile()) {
                showNotifyValidation(getResources().getString(R.string.make_maze_store_fail));
              }

              Intent myIntent = new Intent(getApplicationContext(), LFB.class);
              myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              myIntent.putExtra("MazeFile", newMazeFile);
              setResult(RESULT_OK, myIntent);
              finish();
            }
            else {
              Intent myIntent = new Intent(getApplicationContext(), AddQuestionsActivity.class);
              myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              myIntent.putExtra("MazeFile", newMazeFile);
              myIntent.putExtra("Location", "internal");
              startActivityForResult(myIntent, 2);
            }
          }
        }
        );
  }

  public void handleDone(boolean editQuestions) {
    showDoneDialog(editQuestions);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  public void showNotifyValidation(String message) {
    new PopupNotify(this, message, new PopupNotify.ButtonOkayCallback() {
      @Override
      public void callback() {
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Check which request we're responding to
    if (requestCode == 2) {
      // Make sure the request was successful
      if (resultCode == RESULT_OK) {
        Log.v("LFB", "Generating maze...");
        MyMaze.myMaze.generateMaze(mazeSize.getSize(), mazeSize.getSize());
        Log.v("LFB", "Done Generating maze.");

        if (!writeToFile()) {
          showNotifyValidation(getResources().getString(R.string.make_maze_store_fail));
          MyMaze.myMaze = null;
        }
        else {
          MyMaze.myMaze = null;
          Intent myIntent = new Intent(getApplicationContext(), LFB.class);
          myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          myIntent.putExtra("MazeFile", newMazeFile);
          setResult(RESULT_OK, myIntent);
          finish();
        }
      }
      else {
        MyMaze.myMaze = null;
        Intent myIntent = new Intent(getApplicationContext(), LFB.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(RESULT_CANCELED, myIntent);
        finish();
      }
    }
  }
}
