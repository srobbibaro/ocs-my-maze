package com.originalcontentsoftware.lfb;

public enum MazePlayMode {
  NORMAL(0),
  AERIAL(1),
  QUESTION(2);

  int mode;

  MazePlayMode(int mode) {
    this.mode = mode;
  }
}
