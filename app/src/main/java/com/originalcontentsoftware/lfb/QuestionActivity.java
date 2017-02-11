package com.originalcontentsoftware.lfb;

import java.io.FileInputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class QuestionActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.question_mode_rb);

    // We want to force portrait screen orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    // Get question data from the sender...
    Intent sender = getIntent();
    String answerA = sender.getExtras().getString("AnswerA");
    String answerB = sender.getExtras().getString("AnswerB");
    String answerC = sender.getExtras().getString("AnswerC");
    String answerD = sender.getExtras().getString("AnswerD");
    String question = sender.getExtras().getString("Question");
    String picturePath = sender.getExtras().getString("ShowPicture");

    // If there is a picture with this question, load it and update the image on the form
    ImageView imageView = (ImageView)findViewById(R.id.imageView1);

    Bitmap bitmap = null;
    if (!picturePath.equals("") && !picturePath.equals("no_picture")) {
      try {
        Log.v("LFB", "Trying to open '" + picturePath + "'");
        FileInputStream fstream = this.getApplicationContext().openFileInput(picturePath);
        bitmap = BitmapFactory.decodeFileDescriptor(fstream.getFD());
      }
      catch (Exception e) {
        Log.v("LFB", "Could not open picture");
      }

      if (bitmap != null) {
        Log.v("LFB", "Image loaded height=" + bitmap.getHeight() + " width=" + bitmap.getWidth());
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
      }
      else {
        Log.v("LFB", "Bitmap is null...");
      }
    }
    else {
      Log.v("LFB", "No picture specified.");
      imageView.setVisibility(View.GONE);
    }

    // Update the text of the question on the form with the actual question
    TextView editText = (TextView)findViewById(R.id.textView1);
    editText.setText(question);

    // Update the text of each radio button with the possible answers
    RadioButton answer1 = (RadioButton)findViewById(R.id.radioButtonAnswerA);
    answer1.setText(answerA);
    RadioButton answer2 = (RadioButton)findViewById(R.id.radioButtonAnswerB);
    answer2.setText(answerB);
    RadioButton answer3 = (RadioButton)findViewById(R.id.radioButtonAnswerC);
    answer3.setText(answerC);
    RadioButton answer4 = (RadioButton)findViewById(R.id.radioButtonAnswerD);
    answer4.setText(answerD);

    // Setup handlers for the submit and cancel buttons
    final Button buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
    buttonSubmit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "SubmitQuestion");
      }
    });

    final Button buttonLeave = (Button) findViewById(R.id.buttonLeave);
    buttonLeave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v, "LeaveQuestion");
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  public void handleClick(View v, String button) {
    String ret = button;
    if (button.equals("SubmitQuestion")) {
      RadioGroup radioGroupAnswers = (RadioGroup)findViewById(R.id.radioGroupAnswers);
      int answer = radioGroupAnswers.getCheckedRadioButtonId();

      if (answer == R.id.radioButtonAnswerA) {
        ret = "AnswerA";
      }
      else if (answer == R.id.radioButtonAnswerB) {
        ret = "AnswerB";
      }
      else if (answer == R.id.radioButtonAnswerC) {
        ret = "AnswerC";
      }
      else if (answer == R.id.radioButtonAnswerD) {
        ret = "AnswerD";
      }
    }
    Intent myIntent = new Intent(v.getContext(), LFB.class);
    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    myIntent.putExtra("ButtonPressed", ret);
    setResult(RESULT_OK, myIntent);
    finish();
  }

  @Override
  public void onBackPressed() {
    // Currently, we don't do anything on this screen for the back arrow
    return;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }
}
