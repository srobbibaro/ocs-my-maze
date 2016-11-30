package com.originalcontentsoftware.lfb;

public class MazeBlock {
  protected boolean isVisited;
  protected boolean isBlockable;
  protected Vector directionEntered;

  public MazeBlock(boolean isVisited, boolean isBlockable, boolean isDrawable) {
    super();
    this.isVisited = isVisited;
    this.isBlockable = isBlockable;
    this.isDrawable = isDrawable;
    this.directionEntered = null;
  }

  public MazeBlock() {
    isVisited = false;
  }

  public void setDirectionEntered(Vector directionEntered) {
    this.directionEntered = new Vector();

    this.directionEntered.x = directionEntered.x;
    this.directionEntered.y = directionEntered.y;
    this.directionEntered.z = directionEntered.z;
  }

  public Vector getDirectionEntered() {
    return directionEntered;
  }

  public boolean isVisited() {
    return isVisited;
  }
  public void setVisited(boolean isVisited) {
    this.isVisited = isVisited;
  }
  public boolean isBlockable() {
    return isBlockable;
  }
  public void setBlockable(boolean isBlockable) {
    this.isBlockable = isBlockable;
  }
  public boolean isDrawable() {
    return isDrawable;
  }
  public void setDrawable(boolean isDrawable) {
    this.isDrawable = isDrawable;
  }
  protected boolean isDrawable;
}
