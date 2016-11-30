package com.originalcontentsoftware.lfb;

import android.util.Log;
//#include <math.h>
//#include <stdio.h>



public class Matrix
{
  public float data[] = new float[16];      // 4 x 4

  Matrix() {
    loadIdentity();
  }

  Matrix( Matrix t )
  {
    for ( int i = 0; i < 16; i++ )
      data[i] = t.data[i];
  }

  Matrix ( float c11, float c21, float c31, float c41,
      float c12, float c22, float c32, float c42,
      float c13, float c23, float c33, float c43,
      float c14, float c24, float c34, float c44 )
      {
    data[0] = c11; data[1] = c12; data[2] = c13; data[3] = c14;
    data[4] = c21; data[5] = c22; data[6] = c23; data[7] = c24;
    data[8] = c31; data[9] = c32; data[10] = c33; data[11] = c34;
    data[12] = c41; data[13] = c42; data[14] = c43; data[15] = c44;

      }

  void loadIdentity() {
    for ( int i = 0; i < 16; i++ )
      data[i] = 0;

    data[0] = 1;
    data[5] = 1;
    data[10] = 1;
    data[15] = 1;
  }

  void loadZero() {
    for ( int i = 0; i < 16; i++ )
      data[i] = 0;
  }

  void assign( Matrix t ) // operator =
    {
    for ( int i = 0; i < 16; i++ )
      data[i] = t.data[i];
    };

  Matrix multiply( Matrix p ) // operator *
  {
    Matrix t = new Matrix();
    int c, r;

    for ( int i = 0; i < 16; i++ )
    {
      t.data[i] = 0.0f;

      c = (int ) i / 4; r = i % 4;
      for ( int j = 0; j < 4; j++ )
        t.data[i] += ( p.data[(c*4)+j] * data[ (j*4) + r ] );
    }

    return (t);
  }

  void transpose(Matrix m)
  {
    for ( int j = 0; j < 3; j++ ) {
      for (int i = 0; i < (4-j); i++) {
        m.data[(i*4)+j]=data[15-(j*4)-i];
        m.data[15-(j*4)-i]=data[(i*4)+j];
      }
    }
  }

  void setRotation( float angle, float x, float y, float z )
  {
    float t = 1 - (float)Math.cos(angle);
    float s = (float)Math.sin(angle);
    float c = (float)Math.cos(angle);

    loadIdentity();

    data[0] = ( t *  x * x ) + c;
    data[4] = ( t * x* y ) + ( s * z);
    data[8] = ( t * x * z ) - ( s * y );

    data[1] = (t*x*y) - (s*z);
    data[5] = (t*y*y) + c;
    data[9] = (t*y*z) + (s*x);

    data[2] = (t*x*z) + (s*y);
    data[6] = (t*y*z) - (s*x);
    data[10] = (t*z*z) + c;
  }

  void setRotation2( float x, float y, float z )
  {
    // simplify math for later //
    float cosX = (float)Math.cos(x/2);
        float cosY = (float)Math.cos(y/2);
        float cosZ = (float)Math.cos(z/2);

        float sinX = (float)Math.sin(x/2);
        float sinY = (float)Math.sin(y/2);
        float sinZ = (float)Math.sin(z/2);

        float cosFinal = cosY * cosZ;
        float sinFinal = sinY * sinZ;

        // build the quaternion //
        float quatW = cosX * cosFinal + sinX * sinFinal;
        float quatX = sinX * cosFinal - cosX * sinFinal;
        float quatY = cosX * sinY * cosZ + sinX * cosY * sinZ;
        float quatZ = cosX * cosY * sinZ - sinX * sinY * cosZ;

        float wx, wy, wz, xx, yy, yz, xy, xz, zz, x2, y2, z2;

        // calculate coefficients
        x2 = quatX + quatX; y2 = quatY + quatY;
        z2 = quatZ + quatZ;
        xx = quatX * x2; xy = quatX * y2; xz = quatX * z2;
        yy = quatY * y2; yz = quatY * z2; zz = quatZ * z2;
        wx = quatW * x2; wy = quatW * y2; wz = quatW * z2;

        // build the matrix //
        data[0] = 1.0f - (yy + zz); data[4] = xy - wz;
        data[8] = xz + wy; data[12] = 0.0f;

        data[1] = xy + wz; data[5] = 1.0f - (xx + zz);
        data[9] = yz - wx; data[13] = 0.0f;


        data[2] = xz - wy; data[6] = yz + wx;
        data[10] = 1.0f - (xx + yy); data[14] = 0.0f;


        data[3] = 0; data[7] = 0;
        data[11] = 0; data[15] = 1;
  }


  void setTranslation( float x, float y, float z )
  {
    loadIdentity();

    data[12] = x;
    data[13] = y;
    data[14] = z;
  }

  void setScale( float x, float y, float z )
  {
    loadIdentity();

    data[0] = x;
    data[5] = y;
    data[10] = z;
  }

  void setShadow( float lightPos[], float plane[] )
  {
    float temp = plane[0] * lightPos[0] + plane[1] * lightPos[1] + plane[1] * lightPos[2] + plane[3] * lightPos[3];

    data[0] = temp - lightPos[0] * plane[0];
    data[4] = 0.0f - lightPos[0] * plane[1];
    data[8] = 0.0f - lightPos[0] * plane[2];
    data[12] = 0.0f - lightPos[0] * plane[3];

    data[1] = 0.0f - lightPos[1] * plane[0];
    data[5] = temp - lightPos[1] * plane[1];
    data[9] = 0.0f - lightPos[1] * plane[2];
    data[13] = 0.0f - lightPos[1] * plane[3];

    data[2] = 0.0f - lightPos[2] * plane[0];
    data[6] = 0.0f - lightPos[2] * plane[1];
    data[10] = temp - lightPos[2] * plane[2];
    data[14] = 0.0f - lightPos[2] * plane[3];

    data[3] = 0.0f - lightPos[3] * plane[0];
    data[7] = 0.0f - lightPos[3] * plane[1];
    data[11] = 0.0f - lightPos[3] * plane[2];
    data[15] = temp - lightPos[3] * plane[3];
  }

  void transformVertex( float v[] , float newVertex[])
  {
    float h, x, y, z;

    x = ( v[0] * data[0] ) + ( v[1] * data[4] ) + ( v[2] * data[8] ) + data[12];
    y = ( v[0] * data[1] ) + ( v[1] * data[5] ) + ( v[2] * data[9] ) + data[13];
    z = ( v[0] * data[2] ) + ( v[1] * data[6] ) + ( v[2] * data[10] ) + data[14];
    h = ( v[0] * data[3] ) + ( v[1] * data[7] ) + ( v[2] * data[11] ) + data[15];

    newVertex[0] = x; newVertex[1] = y; newVertex[2] = z;

    newVertex[0] /= h;
    newVertex[1] /= h;
    newVertex[2] /= h;
  }

  void display()
  {
    for ( int i = 0; i < 16; i++ )
    {
      Log.v("Matrix", "Data[" + i + "]=" + data[i] );
    }
  }

  //void CreateFromAxisAngle(float x, float y, float z, float angle);

  //void CreateFromAxisAngles(float x, float y, float z, float angle);


  //void fix();
}