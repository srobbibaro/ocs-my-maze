package com.originalcontentsoftware.lfb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class LFB extends Activity implements SurfaceHolder.Callback, QuestionAnswerNotify {
  public class DisplayInformation {
    final float working_width = 480.0f;
    final float working_height = 800.0f;

    float width;
    float height;
    float ratioWidth;
    float ratioHeight;

    float midX;
    float midY;

    DisplayInformation(Activity context) {
      WindowManager w = context.getWindowManager();
      Display d = w.getDefaultDisplay();

      width = (float)(d.getWidth());
      height = (float)(d.getHeight());

      ratioWidth = working_width / (float)(width);
      ratioHeight = working_height / (float)(height);

      midX = (int)(working_width / 2.0f);
      midY = (int)(working_height / 2.0f);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    glSurfaceView = new GLSurfaceView(this);
    //glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    Intent sender = getIntent();

    String mazeFile = sender.getExtras().getString("MazeFile");
    if (mazeFile == null)
      mazeFile = "demo_maze1.ocsmaze";

    Log.v("LFB", "maze file=" + mazeFile);

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

    // We want to force portrait screen orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    // this has something to do with fixing depth buffer for android
    glSurfaceView.setEGLConfigChooser(
        5, 6, 5, 0, 24, 0 );
    //glSurfaceView.setEGLConfigChooser(false);
    //glSurfaceView.setEGLConfigChooser(true);

    //renderer = new LfbRenderer(getApplicationContext());
    renderer = new LfbRenderer(this, mazeFile, location, mazeUri);
    glSurfaceView.setRenderer(renderer);
    setContentView(glSurfaceView);
    //glSurfaceView.setFixedSize(100, 100);

    glSurfaceView.getHolder().addCallback(this);

    renderer.registerQuestionAnswerNotify(this);

    displayInformation = new DisplayInformation(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    createMenu(menu);
    return true;
  }

  public void createMenu(Menu menu) {
    this.menu = menu;
    getMenuInflater().inflate(R.menu.activity_main, menu);
    menu.add(1,3,0, "Toggle Aerial Control");
    menu.add(1,5,0,getResources().getString(R.string.menu_restart));
    menu.add(1,7,0,getResources().getString(R.string.menu_quit));
    menu.add(1,12,0,"New Empty Maze");
    menu.add(1,13,0,"New Generated Maze");
    menu.add(1,14,0,"Save Maze to File");
    menu.add(1,15,0,"Generate Math Demo Maze");
    menu.add(1,16,0,"Generate Presidents Demo Maze");
    menu.add(1,17,0,"Save maze as internal");

    MenuItem toggleAerial = menu.findItem(3);
    toggleAerial.setVisible(DebugInformation.isDebugEnabled);
    MenuItem newMaze = menu.findItem(12);
    newMaze.setVisible(DebugInformation.isDebugEnabled);
    MenuItem generateMaze = menu.findItem(13);
    generateMaze.setVisible(DebugInformation.isDebugEnabled);
    MenuItem saveMaze = menu.findItem(14);
    saveMaze.setVisible(DebugInformation.isDebugEnabled);
    MenuItem mathQuestions = menu.findItem(15);
    mathQuestions.setVisible(DebugInformation.isDebugEnabled);
    MenuItem saveMazeInternal = menu.findItem(17);
    saveMazeInternal.setVisible(DebugInformation.isDebugEnabled);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
    case 12:
      renderer.generateEmptyMaze();
      return true;
    case 13:
      renderer.generateMaze();
      return true;
    case 14:
      renderer.saveMaze();
      return true;
    case 15:
      renderer.generateMathDemoMaze();
      return true;
    case 17:
      renderer.saveMazeInternal();
      return true;
    case 3:
      renderer.commands.add("ToggleAerialControl");
      return true;
    case 5:
      renderer.restart();
      return true;
    case 7:
      handleCancel();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    glSurfaceView.onResume();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    glSurfaceView.onPause();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // Don't allow user input when a popup is being displayed
    if (lockControls) {
      return true;
    }

    switch (event.getAction()) {
    case MotionEvent.ACTION_DOWN:
      float xDelta = (float)(event.getX()) * displayInformation.ratioWidth;
      float yDelta = (float)(event.getY()) * displayInformation.ratioHeight;

      Log.v("LFB", "position x=" +xDelta + "/" + displayInformation.working_width + "  y=" + yDelta + "/" + displayInformation.working_height);

      if (renderer.currentMode == MazePlayMode.AERIAL) {
        lastX = (int) event.getX();
        lastY = (int) event.getY();
      }
      else {
        lastX = 0;
        lastY = 0;
      }

      if (renderer.currentMode == MazePlayMode.NORMAL || (renderer.currentMode == MazePlayMode.AERIAL && !renderer.aerialControl)) {
        if (xDelta < 150.0f  && yDelta > displayInformation.midY - 75 && yDelta < displayInformation.midY + 75) {
          renderer.commands.add("RotLeft");
        }
        else if (xDelta > 330 && yDelta > displayInformation.midY - 75 && yDelta < displayInformation.midY + 75) {
          renderer.commands.add("RotRight");
        }
        else if ( yDelta < 150 && xDelta > displayInformation.midX - 75.0f && xDelta < displayInformation.midX + 75.0f) {
          renderer.determineForwardMove(0);
        }
        if (xDelta < displayInformation.midX + 75 && xDelta > displayInformation.midX - 75.0  && yDelta > displayInformation.midY - 75 && yDelta < displayInformation.midY + 75) {
          renderer.determineForwardMove(1);
        }
        else if ( yDelta > 650 && xDelta > displayInformation.midX - 75.0f && xDelta < displayInformation.midX + 75.0f) {
          renderer.commands.add("MoveBackward");
        }
        else if ( yDelta > 650.0f && xDelta < 150.0f) {
          renderer.commands.add("ChangeView");
        }
        else if ( xDelta < 150.0f && yDelta < 150.0f) {
          renderer.commands.add("ZoomIn");
        }
        else if ( xDelta > 330 && yDelta < 150.0f) {
          renderer.commands.add("ZoomOut");
        }
        else if ( yDelta > 650.0f && xDelta > 330.0f) {
          renderer.commands.add("ToggleDebug");
        }
      }
      else if (renderer.currentMode == MazePlayMode.AERIAL) {
        if (xDelta < 150.0f  && yDelta > displayInformation.midY - 75 && yDelta < displayInformation.midY + 75) {
          renderer.commands.add("MoveCameraLeft");
        }
        else if (xDelta > 330 && yDelta > displayInformation.midY - 75 && yDelta < displayInformation.midY + 75) {
          renderer.commands.add("MoveCameraRight");
        }
        else if ( yDelta < 150 && xDelta > displayInformation.midX - 75.0f && xDelta < displayInformation.midX + 75.0f) {
          renderer.commands.add("MoveCameraForward");
        }
        else if ( yDelta > 650 && xDelta > displayInformation.midX - 60.0f && xDelta < displayInformation.midX + 60.0f) {
          renderer.commands.add("MoveCameraBackward");
        }
        else if ( yDelta > 650.0f && xDelta < 150.0f) {
          renderer.commands.add("ChangeView");
        }
        else if ( xDelta < 150.0f && yDelta < 150.0f) {
          renderer.commands.add("ZoomIn");
        }
        else if ( xDelta > 330 && yDelta < 150.0f) {
          renderer.commands.add("ZoomOut");
        }
        else if ( yDelta > 650.0f && xDelta > 330.0f) {
          renderer.commands.add("ToggleDebug");
        }
      }

      return true;

    case MotionEvent.ACTION_UP:
      renderer.commands.add("Up");
      return true;

    case MotionEvent.ACTION_MOVE:
      if (renderer.currentMode == MazePlayMode.AERIAL && (lastX > 0 || lastY > 0)) {
        // Notify the user that swiping is not allowed in aerial view
        int latestX = (int)event.getX();
        int latestY = (int)event.getY();

        int xDistance = latestX - lastX;
        int yDistance = latestY - lastY;

        if (xDistance > 150 || xDistance < -150 || yDistance > 150 || yDistance < -150) {
          lastX = 0;
          lastY = 0;

          String message;
          if (numAerialSwipes < 3) {
            message = getResources().getString(R.string.aerial_no_swipe_1);
            numAerialSwipes++;
          }
          else if (numAerialSwipes >= 3 && numAerialSwipes < 6) {
            message = getResources().getString(R.string.aerial_no_swipe_2);
            numAerialSwipes++;
          }
          else if (numAerialSwipes >= 6 && numAerialSwipes < 9) {
            message = getResources().getString(R.string.aerial_no_swipe_3);
            numAerialSwipes++;
          }
          else if (numAerialSwipes >= 9 && numAerialSwipes < 12) {
            message = getResources().getString(R.string.aerial_no_swipe_4);
            numAerialSwipes++;
          }
          else if (numAerialSwipes >= 12 && numAerialSwipes < 15) {
            message = getResources().getString(R.string.aerial_no_swipe_5);
            numAerialSwipes++;
          }
          else {
            message = getResources().getString(R.string.aerial_no_swipe_6);
          }


          this.showNotify(message);
        }
      }

    default:
      return super.onTouchEvent(event);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
    default:
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }


  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {}

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {}

  @Override
  public void onBackPressed() {
    handleCancel();
  }

  public void handleCancel() {
    showDialog();
  }

  @Override
  public void onChoice(final String message) {
    runOnUiThread(
      new Runnable() {
        @Override
        public void run() {
          showNotifyQuestionComplete(message);
        }
      }
    );
  }

  @Override
  public void onComplete(final String message, final String emailMessage) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        showDialogMazeComplete(message, emailMessage, true);
      }
    });
  }

  @Override
  public void onLoaded(final String message) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        showNotify(message);
      }
    });
  }

  @Override
  public void onLoadFailure(final String message) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        showNotifyMazeLoadFail(message);
      }
    });
  }

  @Override
  public void onDebugChanged() {
    if (menu != null) {
      MenuItem toggleAerial = menu.findItem(3);
      toggleAerial.setVisible(DebugInformation.isDebugEnabled);
      MenuItem newMaze = menu.findItem(12);
      newMaze.setVisible(DebugInformation.isDebugEnabled);
      MenuItem generateMaze = menu.findItem(13);
      generateMaze.setVisible(DebugInformation.isDebugEnabled);
      MenuItem saveMaze = menu.findItem(14);
      saveMaze.setVisible(DebugInformation.isDebugEnabled);
      MenuItem mathQuestions = menu.findItem(15);
      mathQuestions.setVisible(DebugInformation.isDebugEnabled);
      MenuItem saveMazeInternal = menu.findItem(17);
      saveMazeInternal.setVisible(DebugInformation.isDebugEnabled);
    }
  }

  public void showDialog() {
    lockControls = true;
    new PopupDialog(
        this,
        getResources().getString(R.string.popup_question_label),
        getResources().getString(R.string.popup_question_yes),
        getResources().getString(R.string.popup_question_no),
        true,
        new PopupDialog.ButtonCallback() {

          @Override
          public void secondButtonCallback() {
            lockControls = false;
          }

          @Override
          public void okayButtonCallback() {
            renderer = null;
            lockControls = false;
            finish();
          }
        }
        );
  }

  public void showNotifyQuestionComplete(String message) {
    lockControls = true;
    new PopupNotify(this, message, new PopupNotify.ButtonOkayCallback() {
      @Override
      public void callback() {
        renderer.commands.add("ExitQuestionMode");
        lockControls = false;
      }
    });
  }

  public void showNotify(String message) {
    lockControls = true;
    new PopupNotify(this, message, new PopupNotify.ButtonOkayCallback() {
      @Override
      public void callback() {
        lockControls = false;
      }
    });
  }

  public void showNotifyMazeLoadFail(String message) {
    lockControls = true;
    new PopupNotify(this, message, new PopupNotify.ButtonOkayCallback() {
      @Override
      public void callback() {
        renderer = null;
        lockControls = false;
        finish();
      }
    });
  }

  public void showDialogMazeComplete(final String message, final String emailMessage, boolean showSendEmail) {
    lockControls = true;
    new PopupDialog(
        this,
        message,
        getResources().getString(R.string.maze_complete_return),
        getResources().getString(R.string.maze_complete_send_email),
        showSendEmail,
        new PopupDialog.ButtonCallback() {

          @Override
          public void secondButtonCallback() {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_maze_complete_subject));
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailMessage);
            startActivity(Intent.createChooser(emailIntent, "Email:"));

            lockControls = false;
            renderer = null;
            finish();
          }

          @Override
          public void okayButtonCallback() {
            renderer = null;
            lockControls = false;
            finish();
          }
        }
        );
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Check which request we're responding to
    if (requestCode == 1) {
      // Make sure the request was successful
      if (resultCode == RESULT_OK) {
        // The user picked a contact.
        // The Intent's data Uri identifies which contact was selected.

        // Do something with the contact here (bigger example below)
        String result = data.getStringExtra("ButtonPressed");

        renderer.commands.add(result);
      }
    }
  }

  private int lastX = 0;
  private int lastY = 0;

  private int numAerialSwipes = 0;

  private DisplayInformation displayInformation;

  private GLSurfaceView glSurfaceView;
  private LfbRenderer renderer;

  private Menu menu;

  private boolean lockControls = false;
}
