package com.originalcontentsoftware.lfb;

import android.util.Log;

public class Matrix {
  public static final int size = 16; // 4 x 4

  public float data[] = new float[size];

  public Matrix() {
    loadIdentity();
  }

  public Matrix(Matrix t) {
    for (int i = 0; i < size; ++i)
      data[i] = t.data[i];
  }

  public Matrix(
      float c11, float c21, float c31, float c41,
      float c12, float c22, float c32, float c42,
      float c13, float c23, float c33, float c43,
      float c14, float c24, float c34, float c44) {
    data[0] = c11; data[1] = c12; data[2] = c13; data[3] = c14;
    data[4] = c21; data[5] = c22; data[6] = c23; data[7] = c24;
    data[8] = c31; data[9] = c32; data[10] = c33; data[11] = c34;
    data[12] = c41; data[13] = c42; data[14] = c43; data[15] = c44;
  }

  void loadIdentity() {
    for (int i = 0; i < size; ++i)
      data[i] = 0.0f;

    data[0]  = 1.0f;
    data[5]  = 1.0f;
    data[10] = 1.0f;
    data[15] = 1.0f;
  }

  void loadZero() {
    for (int i = 0; i < size; ++i)
      data[i] = 0.0f;
  }

  void display() {
    for (int i = 0; i < size; ++i) {
      Log.v("Matrix", "Data[" + i + "]=" + data[i]);
    }
  }
}
