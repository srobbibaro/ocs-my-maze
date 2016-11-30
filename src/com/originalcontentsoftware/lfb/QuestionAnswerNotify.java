package com.originalcontentsoftware.lfb;

public interface QuestionAnswerNotify {
  void onChoice(String message);
  void onComplete(String message, String emailMessage);
  void onLoaded(String message);
  void onLoadFailure(String message);
  void onDebugChanged();
}
