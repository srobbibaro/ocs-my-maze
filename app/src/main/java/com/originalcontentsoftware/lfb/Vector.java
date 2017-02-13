package com.originalcontentsoftware.lfb;

public class Vector {
  public float x, y, z;

  public Vector() {
    this.x = this.y = this.z = 0.0f;
  }

  public Vector(float x, float y, float z) {
    setVector(x, y, z);
  }

  public void setVector(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
