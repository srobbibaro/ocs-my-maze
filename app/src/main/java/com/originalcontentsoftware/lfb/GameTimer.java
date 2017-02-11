package com.originalcontentsoftware.lfb;

public class GameTimer {
  GameTimer() {
    lockFps = defaultLockFps;
    maxTries = defaultMaxTries;
  }

  GameTimer(float lockFps, int maxTries) {
    this.lockFps = lockFps;
    this.maxTries = maxTries;
  }

  public boolean init() {
    m_startTime = System.nanoTime();
    fps = 0.0f;

    return true;
  }

  public float getElapsedSeconds() {
    fps = lockFps + 1.0f;
    float seconds = 0.0f;
    int tries = 0;

    while (fps > lockFps || ++tries > maxTries) {
      long currentTime = System.nanoTime();
      seconds += (float)(currentTime - m_startTime) / 1000000000.0f;
      fps = 1.0f / seconds;
      m_startTime = currentTime;
    }

    /*
    Log.v("GameTimer", "FPS=" + fps);
    Log.v("GameTimer", "m_startTime=" + m_startTime);
    Log.v("GameTimer", "seconds=" + seconds);
     */

    return seconds;
  }

  public float getFPS() {
    return fps;
  }

  private long m_startTime;
  private float fps;
  private float lockFps;
  private int maxTries;

  private final float defaultLockFps = 50.0f;
  private final int defaultMaxTries = 100;
}
