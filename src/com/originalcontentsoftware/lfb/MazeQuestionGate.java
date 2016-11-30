package com.originalcontentsoftware.lfb;

public class MazeQuestionGate extends MazeGate {
  private String question;
  private String[] answers;
  private int correctAnswerNumber;
  private int incorrectBehavior;
  private String picturePath;
  private boolean isPictureGate;

  public MazeQuestionGate()
  {
    super();
    question = "";
    answers = new String[]{"", "", "", ""};
    correctAnswerNumber = 0;
    incorrectBehavior = 0;
    picturePath = "no_picture";
    isPictureGate = false;
  }

  public MazeQuestionGate(String question, String[] answers, int correctAnswerNumber, int incorrectBehavior, String picturePath) {
    super();
    this.question = question;
    this.answers = answers;
    this.correctAnswerNumber = correctAnswerNumber;
    this.incorrectBehavior = incorrectBehavior;
    this.picturePath = picturePath;
    isPictureGate = this.isPicture();
  }

  public MazeQuestionGate(String question, String[] answers, int correctAnswerNumber, int incorrectBehavior) {
    super();
    this.question = question;
    this.answers = answers;
    this.correctAnswerNumber = correctAnswerNumber;
    this.incorrectBehavior = incorrectBehavior;
    this.picturePath = "no_picture";
    isPictureGate = false;
  }

  public boolean isPictureGate() {
    return isPictureGate;
  }

  public void setPictureGate(boolean isPictureGate) {
    this.isPictureGate = isPictureGate;
  }

  public int getIncorrectBehavior() {
    return incorrectBehavior;
  }

  public void setIncorrectBehavior(int incorrectBehavior) {
    this.incorrectBehavior = incorrectBehavior;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String[] getAnswers() {
    return answers;
  }

  public void setAnswers(String[] answers) {
    this.answers = answers;
  }

  public int getCorrectAnswerNumber() {
    return correctAnswerNumber;
  }

  public void setCorrectAnswerNumber(int correctAnswerNumber) {
    this.correctAnswerNumber = correctAnswerNumber;
  }

  public boolean isPicture() {
    return (!picturePath.equals("") && !picturePath.equals("no_picture"));
  }

  public String getPicturePath() {
    return picturePath;
  }

  public void setPicturePath(String picturePath) {
    this.picturePath = picturePath;
  }
}
