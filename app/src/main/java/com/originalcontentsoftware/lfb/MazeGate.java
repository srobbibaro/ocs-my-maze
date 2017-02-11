package com.originalcontentsoftware.lfb;

public class MazeGate extends MazeBlock {
  protected boolean isCleared = false;
  protected int numWrongAnswers = 0;

  public MazeGate() {
    isCleared = false;
    numWrongAnswers = 0;
  }

  public int getNumWrongAnswers() {
    return numWrongAnswers;
  }

  public void setNumWrongAnswers(int numWrongAnswers) {
    this.numWrongAnswers = numWrongAnswers;
  }

  public boolean isCleared() {
    return isCleared;
  }

  public void setCleared(boolean isCleared) {
    this.isCleared = isCleared;
  }

  public void incNumWrongAnswers() {
    numWrongAnswers++;
  }
}
