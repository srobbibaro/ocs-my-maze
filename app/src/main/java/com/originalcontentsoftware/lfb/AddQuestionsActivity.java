package com.originalcontentsoftware.lfb;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.content.*;
import android.graphics.*;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class AddQuestionsActivity extends Activity {
  // Question storage
  private int questionNumber = 1;
  private MazeQuestionGate currentQuestionGate;

  // Storage for questions with pictures
  private Bitmap currentBitmap;
  private Bitmap originalBitmap;
  private final String noPicture = "no_picture";
  private final int desiredBitmapWidth = 256;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.add_questions);

    final Button button = (Button) findViewById(R.id.button1);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Next");
      }
    });

    final Button button2 = (Button) findViewById(R.id.button2);
    button2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Done");
      }
    });

    final Button button3 = (Button) findViewById(R.id.button3);
    button3.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "Cancel");
      }
    });

    final Button button4 = (Button) findViewById(R.id.button4);
    button4.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "PreviousQuestion");
      }
    });

    final Button button5 = (Button) findViewById(R.id.button5);
    button5.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "NextQuestion");
      }
    });

    final Button button6 = (Button) findViewById(R.id.button6);
    button6.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "PreviousQuestion");
      }
    });

    final Button button7 = (Button) findViewById(R.id.button7);
    button7.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "NextQuestion");
      }
    });

    final Button button8 = (Button) findViewById(R.id.button8);
    button8.setVisibility(View.GONE);
    button8.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "RemoveQuestion");
      }
    });

    CheckBox cb = (CheckBox)findViewById(R.id.checkBoxAddPicture);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) {
          updatePicture(null);
          currentQuestionGate.setPictureGate(false);
          //cleanupBitmaps();
        }

        if (isChecked) {
          if (!currentQuestionGate.isPictureGate()) {
            choosePicture();
          }
          else {
            try {
              Log.v("LFB", "Loading picture... (" + currentQuestionGate.getPicturePath() + ")");
              FileInputStream fis = getApplicationContext().openFileInput(currentQuestionGate.getPicturePath());
              loadBitmap(fis);
              fis.close();
            }
            catch (Exception e) {
              Log.v("LFB", "Errors loading picture! " + e);
            }
          }
        }
      }
    });
    cb.setChecked(false);
    updatePicture(null);

    ImageView imageView = (ImageView)findViewById(R.id.imageView1);
    imageView.setOnTouchListener(new View.OnTouchListener() {
      private boolean doRotate = false;
      private int rotation = 0;

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (currentBitmap == null) {
          return false;
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
        case MotionEvent.ACTION_DOWN:
          doRotate = true;
          break;
        case MotionEvent.ACTION_UP:
          if (doRotate) {
            rotation += 90;
            if (rotation >= 360) {
              rotation = 0;
            }

            Log.v("LFB", "rotation=" + rotation);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            currentBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
                originalBitmap.getWidth(), originalBitmap.getHeight(),
                matrix, true);

            updatePicture(currentBitmap);
          }
          doRotate = false;
        default:
          doRotate = false;
          break;
        }

        return true;
      }
    });

    currentQuestionGate = new MazeQuestionGate();
    questionNumber = MyMaze.myMaze.getNumberOfQuestions() + 1;

    // we are editing
    if (questionNumber != 1) {
      button.setText(getApplicationContext().getString(R.string.make_maze_edit_add_question));
      button2.setText(getApplicationContext().getString(R.string.make_maze_edit_done));
      Log.v("LFB", "Edit maze");
    }
    else {
      Log.v("LFB", "Create new maze");
    }

    // We want to force portrait screen orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    TextView editText = (TextView)findViewById(R.id.textView1);
    editText.setText("New Question #" + questionNumber);
  }

  public void updatePicture(Bitmap bitmap) {
    ImageView imageView = (ImageView)findViewById(R.id.imageView1);

    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
      imageView.setVisibility(View.VISIBLE);
    }
    else {
      imageView.setImageResource(0);
      imageView.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  public void handleClick(View v, String button) {
    if (button.equals("Next")) {
      addQuestion(false);
    }
    else if (button.equals("Done")) {
      if (addQuestion(true)) {
        handleDone();
      }
    }
    else if (button.equals("PreviousQuestion")) {
      if (questionNumber > 1) {
        questionNumber--;

        updateQuestionFields();
        setFocusOnQuestionLabel();
      }
    }
    else if (button.equals("NextQuestion")) {
      if (questionNumber <= MyMaze.myMaze.getNumberOfQuestions()) {
        questionNumber++;

        updateQuestionFields();
        setFocusOnQuestionLabel();
      }
    }
    else if (button.equals("RemoveQuestion")) {
      if (questionNumber <= MyMaze.myMaze.getNumberOfQuestions()) {
        showRemoveQuestionDialog();
      }
    }
    else {
      handleCancel();
    }
  }

  public void updateQuestionFields() {
    currentQuestionGate = MyMaze.myMaze.getNumberOfQuestions() < questionNumber ?
        new MazeQuestionGate() :
          MyMaze.myMaze.getQuestionGate(questionNumber - 1);

        final Button button8 = (Button) findViewById(R.id.button8);
        if (questionNumber <= MyMaze.myMaze.getNumberOfQuestions()) {
          button8.setVisibility(View.VISIBLE);
        }
        else {
          button8.setVisibility(View.GONE);
        }

        TextView editText =  (TextView)findViewById(R.id.textView1);

        EditText question = (EditText)findViewById(R.id.editText1);
        EditText answerA = (EditText)findViewById(R.id.editText2);
        EditText answerB = (EditText)findViewById(R.id.editText3);
        EditText answerC = (EditText)findViewById(R.id.editText4);
        EditText answerD = (EditText)findViewById(R.id.editText5);

        CheckBox cb = (CheckBox)findViewById(R.id.checkBoxAddPicture);
        boolean isPicture = currentQuestionGate.isPictureGate();
        cb.setChecked(false);
        currentQuestionGate.setPictureGate(isPicture);

        editText.setText("Edit Question #" + questionNumber);
        question.setText(currentQuestionGate.getQuestion());
        answerA.setText(currentQuestionGate.getAnswers()[0]);
        answerB.setText(currentQuestionGate.getAnswers()[1]);
        answerC.setText(currentQuestionGate.getAnswers()[2]);
        answerD.setText(currentQuestionGate.getAnswers()[3]);

        RadioButton rb = (RadioButton)findViewById(R.id.radioButton1);
        switch (currentQuestionGate.getCorrectAnswerNumber()) {
        case 1:
          rb = (RadioButton)findViewById(R.id.radioButton2);
          break;
        case 2:
          rb = (RadioButton)findViewById(R.id.radioButton3);
          break;
        case 3:
          rb = (RadioButton)findViewById(R.id.radioButton4);
          break;
        }

        if (rb != null) {
          rb.setChecked(true);
        }

        rb = (RadioButton)findViewById(R.id.radioButton5);
        switch (currentQuestionGate.getIncorrectBehavior()) {
        case 1:
          rb = (RadioButton)findViewById(R.id.radioButton6);
          break;
        case 2:
          rb = (RadioButton)findViewById(R.id.radioButton7);
          break;
        case 3:
          rb = (RadioButton)findViewById(R.id.radioButton8);
          break;
        }

        if (rb != null) {
          rb.setChecked(true);
        }

        if (currentQuestionGate.isPicture()) {
          cb.setChecked(true);
        }
  }

  public boolean addQuestion(boolean isDone) {
    EditText question = (EditText)findViewById(R.id.editText1);
    EditText answerA = (EditText)findViewById(R.id.editText2);
    EditText answerB = (EditText)findViewById(R.id.editText3);
    EditText answerC = (EditText)findViewById(R.id.editText4);
    EditText answerD = (EditText)findViewById(R.id.editText5);

    RadioGroup rg = (RadioGroup)findViewById(R.id.radioGroup1);
    RadioGroup rg2 = (RadioGroup)findViewById(R.id.radioGroup2);

    String errorMessage = "";
    View focusObject = null;
    int numErrors = 0;

    // simple validation for now...
    if (answerD.getText().toString().equals("")) {
      focusObject = answerD;
      errorMessage = "New Question #" + questionNumber + " (Error: Choice D not entered)";
      numErrors++;
    }
    if (answerC.getText().toString().equals("")) {
      focusObject = answerC;
      errorMessage = "New Question #" + questionNumber + " (Error: Choice C not entered)";
      numErrors++;
    }
    if (answerB.getText().toString().equals("")) {
      focusObject = answerB;
      errorMessage = "New Question #" + questionNumber + " (Error: Choice B not entered)";
      numErrors++;
    }
    if (answerA.getText().toString().equals("")) {
      focusObject = answerA;
      errorMessage = "New Question #" + questionNumber + " (Error: Choice A not entered)";
      numErrors++;
    }
    if (question.getText().toString().equals("")) {
      focusObject = question;
      errorMessage = "New Question #" + questionNumber + " (Error: Question not entered)";
      numErrors++;
    }

    if (isDone &&  numErrors == 5) {
      return true;
    }

    if (!errorMessage.equals("") && focusObject != null) {
      //editText.setText(errorMessage);
      showNotifyValidation(errorMessage);
      focusObject.setFocusableInTouchMode(true);
      focusObject.setFocusable(true);
      focusObject.requestFocus();
      return false;
    }

    try {
      int answer = rg.getCheckedRadioButtonId();
      int actualAnswer = 0;

      if (answer == R.id.radioButton1) {
        actualAnswer = 0;
      }
      else if (answer == R.id.radioButton2) {
        actualAnswer = 1;
      }
      else if (answer == R.id.radioButton3) {
        actualAnswer = 2;
      }
      else if (answer == R.id.radioButton4) {
        actualAnswer = 3;
      }

      int incorrectBehavior = rg2.getCheckedRadioButtonId();
      int actualIncorrectBehavior = 0;

      if (incorrectBehavior == R.id.radioButton5) {
        actualIncorrectBehavior = 0;
      }
      else if (incorrectBehavior == R.id.radioButton6) {
        actualIncorrectBehavior = 1;
      }
      else if (incorrectBehavior == R.id.radioButton7) {
        actualIncorrectBehavior = 2;
      }
      else if (incorrectBehavior == R.id.radioButton8) {
        actualIncorrectBehavior = 3;
      }
      String questionStr = question.getText().toString();
      questionStr.replaceAll("\n", "");

      String[] answers = new String[] {answerA.getText().toString(), answerB.getText().toString(), answerC.getText().toString(), answerD.getText().toString()};
      for (int u = 0; u < answers.length; u++) {
        answers[u] = answers[u].replaceAll("\n", "");
      }

      if (this.currentQuestionGate.isPictureGate() && currentBitmap != null) {
        String picturePath = "pic" + MyMaze.myMaze.getNextBitmapNumber() + ".png";
        currentQuestionGate.setPicturePath(picturePath);

        Log.v("LFB", "Storing bitmap =" + picturePath);

        int picHeight = currentBitmap.getHeight();
        int picWidth = currentBitmap.getWidth();

        float scaleFactor = (float)desiredBitmapWidth / (float)picWidth;
        int newHeight = (int)((float)picHeight * scaleFactor);
        int newWidth = (int)((float)picWidth * scaleFactor);

        Log.v("LFB", "Height=" + picHeight + " width=" + picWidth);
        Log.v("LFB", "new Height=" + newHeight + "new width=" + newWidth);
        Log.v("LFB", "scaleFActor=" + scaleFactor);

        currentBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true);
        storeBitmap(currentBitmap, picturePath);
      }
      else {
        currentQuestionGate.setPicturePath(noPicture);
      }

      currentQuestionGate.setQuestion(questionStr);
      currentQuestionGate.setAnswers(answers);
      currentQuestionGate.setCorrectAnswerNumber(actualAnswer);
      currentQuestionGate.setIncorrectBehavior(actualIncorrectBehavior);

      int nextQuestionNumber = questionNumber;

      // If the user is going to input additional questions, then update display and state to allow it
      if (!isDone) {
        nextQuestionNumber++;
        setFocusOnQuestionLabel();
      }

      // Only add question and move to input next question if there are no errors
      if (questionNumber > MyMaze.myMaze.getNumberOfQuestions()) {
        MyMaze.myMaze.getQuestionGates().add(currentQuestionGate);
      }
      else {
        MyMaze.myMaze.getQuestionGates().set(questionNumber - 1, currentQuestionGate);
      }

      questionNumber = nextQuestionNumber;

      updateQuestionFields();

      return true;
    }
    catch (Exception e) {
      StringWriter writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter( writer );
      e.printStackTrace( printWriter );
      printWriter.flush();
    }

    return false;
  }

  public void setFocusOnQuestionLabel() {
    TextView editText =  (TextView)findViewById(R.id.textView1);

    EditText question = (EditText)findViewById(R.id.editText1);
    question.setFocusable(true);
    question.setFocusableInTouchMode(true);
    question.requestFocus(View.FOCUS_UP);

    int[] location = new int[2];

    editText.getLocationInWindow(location);

    ScrollView sv = (ScrollView)findViewById(R.id.scrollView);
    //sv.scrollTo(location[0], location[1]);
    sv.scrollTo(0,0);
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
            Intent myIntent = new Intent(getApplicationContext(), LFB.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            setResult(RESULT_CANCELED, myIntent);
            CheckBox cb = (CheckBox)findViewById(R.id.checkBoxAddPicture);
            cb.setChecked(false);
            finish();
          }
        }
        );
  }

  public void handleCancel() {
    showCancelDialog();
  }

  public void showDoneDialog() {
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
            Intent myIntent = new Intent(getApplicationContext(), LFB.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            setResult(RESULT_OK, myIntent);
            finish();
          }
        }
        );
  }

  public void showRemoveQuestionDialog() {
    new PopupDialog(
        this,
        getResources().getString(R.string.popup_remove_question_label),
        getResources().getString(R.string.popup_remove_question_yes),
        getResources().getString(R.string.popup_remove_question_no),
        true,
        new PopupDialog.ButtonCallback() {

          @Override
          public void secondButtonCallback() {
          }

          @Override
          public void okayButtonCallback() {
            MyMaze.myMaze.getQuestionGates().remove(questionNumber - 1);

            updateQuestionFields();
            setFocusOnQuestionLabel();
          }
        }
        );
  }

  public void handleDone() {
    showDoneDialog();
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

  public void choosePicture() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    startActivityForResult(intent, 1);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    boolean result = false;

    if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
      try {
        InputStream fis = getContentResolver().openInputStream(
            data.getData());
        result = loadBitmap(fis);
        fis.close();
      }
      catch (Exception e) {
        Log.v("LFB", "Errors loading picture! " + e);
      }
    }

    if (requestCode == 1 && (resultCode != Activity.RESULT_OK || !result)) {
      CheckBox cb = (CheckBox)findViewById(R.id.checkBoxAddPicture);
      cb.setChecked(false);
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  public boolean loadBitmap(InputStream fis) {
    boolean result = false;

    try {
      currentBitmap = BitmapFactory.decodeStream(fis);
      originalBitmap = currentBitmap;
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open picture");
    }

    if (currentBitmap == null) {
      Log.v("LFB", "No go!");
      currentQuestionGate.setPictureGate(false);
    }
    else {
      Log.v("LFB", "Seems to be okay!");

      currentQuestionGate.setPictureGate(true);
      updatePicture(currentBitmap);
      result = true;
    }

    return result;
  }

  public boolean storeBitmap(Bitmap bitmap, String fileName) {
    boolean result = false;
    try {
      FileOutputStream fstream = this.getApplicationContext().openFileOutput(fileName, this.getApplicationContext().MODE_PRIVATE);
      bitmap.compress(Bitmap.CompressFormat.PNG, 90, fstream);
      fstream.close();
      result = true;
      Log.v("LFB", "Seems to be okay!");
    }
    catch (Exception e) {
      Log.v("LFB", "Error saving picture!" + fileName);
    }
    return result;
  }

  public void cleanupBitmaps() {
    if (currentBitmap != null) {
      currentBitmap.recycle();
      currentBitmap = null;
    }

    if (originalBitmap != null) {
      originalBitmap.recycle();
      originalBitmap = null;
    }
  }
}
