package com.originalcontentsoftware.lfb;

public class DisplayTimedMessage {
  public DisplayTimedMessage(String message, float maxDuration, GraphicsUtil graphicsUtil) {
    super();

    this.message = message;
    this.maxDuration = maxDuration;
    this.curDuration = 0.0f;
    this.isActive = true;
    this.graphicsUtil = graphicsUtil;

    graphicsUtil.updateMessage(message);
  }

  public void refresh() {
    graphicsUtil.updateMessage(message);
  }

  public void draw() {
    if (isActive) {
      graphicsUtil.drawMessage();
    }
  }

  public boolean updateDuration(float inc) {
    curDuration += inc;

    if (curDuration >= maxDuration) {
      isActive = false;
    }

    return isActive;
  }

  private String message;

  private float maxDuration;
  private float curDuration;

  private boolean isActive;

  private GraphicsUtil graphicsUtil;
}
