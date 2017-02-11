package com.originalcontentsoftware.lfb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;
import android.os.Environment;
import android.util.Log;

public class GraphicsUtil {
  public GraphicsUtil (LFB context) {
    // interior wall
    //textureNumbers.add(R.drawable.wall2);
    //textureNumbers.add(R.drawable.wall_new);
    //textureNumbers.add(R.drawable.wall5);
    textureNumbers.add(R.drawable.wall3);
    //textureNumbers.add(R.drawable.goal2);
    textureNumbers.add(R.drawable.goal_new);
    // gate
    //textureNumbers.add(R.drawable.gate);
    //textureNumbers.add(R.drawable.gate_new);
    //textureNumbers.add(R.drawable.gate2);
    textureNumbers.add(R.drawable.gate3);
    textureNumbers.add(-1);
    textureNumbers.add(-1);
    textureNumbers.add(R.drawable.question_go);
    textureNumbers.add(R.drawable.question_leave);
    textureNumbers.add(R.drawable.toggle_aerial);
    textureNumbers.add(R.drawable.toggle_3d);
    textureNumbers.add(R.drawable.arrow_up);
    textureNumbers.add(R.drawable.arrow_down);
    textureNumbers.add(R.drawable.arrow_left);
    textureNumbers.add(R.drawable.arrow_right);
    textureNumbers.add(-1);
    textureNumbers.add(-1);
    //textureNumbers.add(R.drawable.wall);
    textureNumbers.add(R.drawable.wall4);
    textureNumbers.add(R.drawable.arrow_left_wall);
    textureNumbers.add(R.drawable.arrow_right_wall);
    textureNumbers.add(R.drawable.ground);
    //textureNumbers.add(R.drawable.ground_new);
    textureNumbers.add(R.drawable.ground_visited);
    textureNumbers.add(R.drawable.arrow);
    textureNumbers.add(-1); // used to be answer_a
    textureNumbers.add(-1); // used to be answer_b
    textureNumbers.add(-1); // used to be answer_c
    textureNumbers.add(-1); // used to be answer_d
    textureNumbers.add(R.drawable.finish_button);
    textureNumbers.add(R.drawable.debug_button);
    textureNumbers.add(R.drawable.wall4);
    numTextures = textureNumbers.size();

    texture = new int[numTextures];

    this.context = context;
    this.gl = null;
  }

  public void setGL(GL10 gl) {
    this.gl = gl;
  }

  public void drawPlayer(float x, float z, Vector direction)
  {
    gl.glPushMatrix();

    if (direction.x > 0) {
      gl.glTranslatef(x * 10.0f + 10.0f, -5.0f, z * 10.0f);
      gl.glRotatef(270.0f, 0.0f, 1.0f, 0.0f);
    }
    else if (direction.x < 0) {
      gl.glTranslatef(x * 10.0f, -5.0f, z * 10.0f + 10.0f);
      gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);

    }
    else if (direction.z > 0 ) {
      gl.glTranslatef(x * 10.0f + 10.0f, -5.0f, z * 10.0f + 10.0f);
      gl.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);

    }
    else {
      gl.glTranslatef(x * 10.0f, -5.0f, z * 10.0f);
    }

    gl.glScalef(10.0f, 10.0f, 10.0f);

    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[20]);

    boxVertexBuffer[4].position(0);
    textureBuffer.position(0);

    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, boxVertexBuffer[4]);
    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    gl.glPopMatrix();
  }

  public void drawGround(float x, float z, boolean isVisited, Vector direction)
  {
    gl.glPushMatrix();
    gl.glTranslatef(x * 10.0f, -10.0f, z * 10.0f);
    gl.glScalef(10.0f, 10.0f, 10.0f);

    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[18]);
    boxVertexBuffer[4].position(0);
    textureBuffer.position(0);
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, boxVertexBuffer[4]);
    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

    gl.glPopMatrix();
    if (isVisited && direction != null) {
      gl.glPushMatrix();
      if (direction.x > 0) {
        gl.glTranslatef(x * 10.0f + 10.0f, -10.0f, z * 10.0f);
        gl.glRotatef(270.0f, 0.0f, 1.0f, 0.0f);
      }
      else if (direction.x < 0) {
        gl.glTranslatef(x * 10.0f, -10.0f, z * 10.0f + 10.0f);
        gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
      }
      else if (direction.z > 0 ) {
        gl.glTranslatef(x * 10.0f + 10.0f, -10.0f, z * 10.0f + 10.0f);
        gl.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
      }
      else {
        gl.glTranslatef(x * 10.0f, -10.0f, z * 10.0f);
      }

      gl.glScalef(10.0f, 10.0f, 10.0f);

      //gl.glDepthFunc(GL10.GL_ALWAYS);
      gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[19]);
      boxVertexBuffer[4].position(0);
      textureBuffer.position(0);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, boxVertexBuffer[4]);
      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

      //gl.glDepthFunc(GL10.GL_LEQUAL);

      gl.glPopMatrix();
    }

    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);
  }

  public void drawBoxColor(float x, float z, int data) {
    gl.glPushMatrix();

    gl.glTranslatef(x * 10.0f, 0.0f, z * 10.0f);
    gl.glScalef(10.0f, 15.0f, 10.0f);

    float useColors[] = {
        colors[data][0], colors[data][1], colors[data][2], colors[data][3],
        colors[data][0] - 0.3f, colors[data][1] - 0.3f, colors[data][2] - 0.3f, colors[data][3],
        colors[data][0] - 0.6f, colors[data][1] - 0.6f, colors[data][2] - 0.6f, colors[data][3],
        colors[data][0] - 0.9f, colors[data][1] - 0.9f, colors[data][2] - 0.9f, colors[data][3]
    };

    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    gl.glDisable(GL10.GL_TEXTURE_2D);

    for (int i = 0; i < sides; i++) {
      colorVertexBuffer[i].clear();
      colorVertexBuffer[i].put( useColors);

      boxVertexBuffer[i].position(0);
      colorVertexBuffer[i].position(0);

      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, boxVertexBuffer[i]);
      gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorVertexBuffer[i]);

      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    gl.glPopMatrix();
  }

  public void drawBox(float x, float z, int data, Vector direction, MazePlayMode mode)
  {
    gl.glPushMatrix();

    if (direction.x > 0) {
      if (mode == MazePlayMode.AERIAL) {
        gl.glTranslatef(x * 10.0f + 10.0f, -10.0f, z * 10.0f);
      }
      else {
        gl.glTranslatef(x * 10.0f + 10.0f, 0.0f, z * 10.0f);
      }
      gl.glRotatef(270.0f, 0.0f, 1.0f, 0.0f);
    }
    else if (direction.x < 0) {
      if (mode == MazePlayMode.AERIAL) {
        gl.glTranslatef(x * 10.0f, -10.0f, z * 10.0f + 10.0f);
      }
      else {
        gl.glTranslatef(x * 10.0f, 0.0f, z * 10.0f + 10.0f);
      }
      gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);

    }
    else if (direction.z > 0 ) {
      if (mode == MazePlayMode.AERIAL) {
        gl.glTranslatef(x * 10.0f + 10.0f, -10.0f, z * 10.0f + 10.0f);
      }
      else {
        gl.glTranslatef(x * 10.0f + 10.0f, 0.0f, z * 10.0f + 10.0f);
      }
      gl.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);

    }
    else {
      if (mode == MazePlayMode.AERIAL) {
        gl.glTranslatef(x * 10.0f, -10.0f, z * 10.0f);
      }
      else {
        gl.glTranslatef(x * 10.0f, 0.0f, z * 10.0f);
      }
    }

    if (mode == MazePlayMode.AERIAL) {
      gl.glScalef(10.0f, 10.0f, 10.0f);
    }
    else {
      gl.glScalef(10.0f, 15.0f, 10.0f);
    }

    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    // wall 1
    if (data == 1) {
      //if (x % 2 + z % 2 == 0 ) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
      //}
      //else {
      //  gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[27]);
      //}
    }
    // wall 2
    else if (data == 2) {
      gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[15]);
    }
    // start
    else if (data == 3) {
      gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
    }
    // goal
    else if (data == 4) {
      gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[1]);
    }
    // gate
    else if (data >= 20) {
      gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[2]);
    }
    // gps gate?
    else if (data == 8) {
      gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[3]);
    }

    for (int i = 0; i < sides; i++) {
      if (mode != MazePlayMode.AERIAL && (i == 0 || i == 4)) {
        continue;
      }
      else if (mode == MazePlayMode.AERIAL && i < 4) {
        continue;
      }

      boxVertexBuffer[i].position(0);
      textureBuffer.position(0);

      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, boxVertexBuffer[i]);
      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    gl.glPopMatrix();
  }

  public void generateTextures() {
    gl.glGenTextures(numTextures, texture, 0);

    for (int i = 0; i < numTextures; i++) {
      loadGLTexture(textureNumbers.get(i), i);
    }

    for (int i = 0; i < sides; i++) {
      boxVertexBuffer[i] = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      colorVertexBuffer[i] = ByteBuffer.allocateDirect(4 * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    // far
    boxVertexBuffer[0].put(new float[] {0.0f, 1.0f, 0.0f});
    boxVertexBuffer[0].put(new float[] {0.0f, 0.0f, 0.0f});
    boxVertexBuffer[0].put(new float[] {1.0f, 1.0f, 0.0f});
    boxVertexBuffer[0].put(new float[] {1.0f, 0.0f, 0.0f});

    // left
    boxVertexBuffer[1].put(new float[] {0.0f, 1.0f, 1.0f});
    boxVertexBuffer[1].put(new float[] {0.0f, 0.0f, 1.0f});
    boxVertexBuffer[1].put(new float[] {0.0f, 1.0f, 0.0f});
    boxVertexBuffer[1].put(new float[] {0.0f, 0.0f, 0.0f});

    // right
    boxVertexBuffer[2].put(new float[] {1.0f, 1.0f, 0.0f});
    boxVertexBuffer[2].put(new float[] {1.0f, 0.0f, 0.0f});
    boxVertexBuffer[2].put(new float[] {1.0f, 1.0f, 1.0f});
    boxVertexBuffer[2].put(new float[] {1.0f, 0.0f, 1.0f});

    // near
    boxVertexBuffer[3].put(new float[] {1.0f, 1.0f, 1.0f});
    boxVertexBuffer[3].put(new float[] {1.0f, 0.0f, 1.0f});
    boxVertexBuffer[3].put(new float[] {0.0f, 1.0f, 1.0f});
    boxVertexBuffer[3].put(new float[] {0.0f, 0.0f, 1.0f});

    // top
    boxVertexBuffer[4].put(new float[] {1.0f, 1.0f, 0.0f});
    boxVertexBuffer[4].put(new float[] {1.0f, 1.0f, 1.0f});
    boxVertexBuffer[4].put(new float[] {0.0f, 1.0f, 0.0f});
    boxVertexBuffer[4].put(new float[] {0.0f, 1.0f, 1.0f});

    triangleVertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    colorTriangleVertexBuffer = ByteBuffer.allocateDirect(4 * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    sqVertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    triangleVertexBuffer.put(new float[] {0.0f, 1.0f, 10.0f});
    colorTriangleVertexBuffer.put(new float[] {0.0f, 1.0f, 0.0f, 0.4f});
    triangleVertexBuffer.put(new float[] {1.0f, 0.0f, 10.0f});
    colorTriangleVertexBuffer.put(new float[] {0.0f, 1.0f, 0.0f, 0.4f});
    triangleVertexBuffer.put(new float[] {-1.0f, 0.0f, 10.0f});
    colorTriangleVertexBuffer.put(new float[] {0.0f, 1.0f, 0.0f, 0.4f});

    sqVertexBuffer.put(new float[] {1.0f, 1.0f, 0.0f});
    sqVertexBuffer.put(new float[] {1.0f, 0.0f, 0.0f});
    sqVertexBuffer.put(new float[] {0.0f, 1.0f, 0.0f});
    sqVertexBuffer.put(new float[] {0.0f, 0.0f, 0.0f});
    float textures[] = {
        // Mapping coordinates for the vertices
        1.0f, 0.0f,      // bottom right (V3)
        1.0f, 1.0f,     // top right    (V4)
        0.0f, 0.0f,     // bottom left  (V1)
        0.0f, 1.0f,     // top left     (V2)
    };

    float texturesRev[] = {
        // Mapping coordinates for the vertices
        0.0f, 0.0f,     // bottom left  (V1)
        0.0f, 1.0f,     // top left     (V2)
        1.0f, 0.0f,      // bottom right (V3)
        1.0f, 1.0f,     // top right    (V4)
    };

    textureBuffer = ByteBuffer.allocateDirect(4 * 3 * 4 ).order(ByteOrder.nativeOrder()).asFloatBuffer();
    textureBuffer.put(textures);
    textureBuffer.position(0);

    textureBufferRev = ByteBuffer.allocateDirect(4 * 3 * 4 ).order(ByteOrder.nativeOrder()).asFloatBuffer();
    textureBufferRev.put(texturesRev);
    textureBufferRev.position(0);
  }

  public void drawDebugControls(float x, float z, boolean isDebugModeEnabled, boolean debug)
  {
    if (isDebugModeEnabled == true) {
      // up and down
      gl.glPushMatrix();

      gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
      gl.glEnable(GL10.GL_TEXTURE_2D);

      gl.glTranslatef(buttonX + x, -8.0f, z);
      gl.glScalef(2.0f, 2.0f, 2.0f);
      if (!debug) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[26]);
      }
      else {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[6]);
      }

      sqVertexBuffer.position(0);
      textureBuffer.position(0);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);
      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

      gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

      // TODO: investigate this call for mapping controls to screen coordinates
      //GLU.gluProject(objX, objY, objZ, model, modelOffset, project, projectOffset, view, viewOffset, win, winOffset)

      gl.glPopMatrix();
    }
  }

  public void drawControlsArrow(float x, float z, int data,
      boolean drawForward, boolean drawBackward, boolean drawLeft, boolean drawRight,
      boolean gateAhead, boolean goalAhead, MazePlayMode state,
      boolean isDebugModeEnabled, boolean aerialControl, boolean debug)
  {
    if (state == MazePlayMode.NORMAL || isDebugModeEnabled == true) {
      // up and down
      for (int i = 0; i < 2; i++) {
        // TODO: Do this a smarter way...
        if (state == MazePlayMode.NORMAL || (state == MazePlayMode.AERIAL && isDebugModeEnabled && !aerialControl)) {
          if ((i == 0 && !drawForward)) {
            continue;
          }
          else if (i == 1 && !drawBackward) {
            continue;
          }
        }
        gl.glPushMatrix();

        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        if (i == 0) {
          //gl.glRotatef(180 * i, 0.0f, 0.0f, 1.0f);
          if (gateAhead  && (state == MazePlayMode.NORMAL || (state == MazePlayMode.AERIAL && isDebugModeEnabled && !aerialControl)) ) {
            gl.glTranslatef(x, -1.0f, z);
            gl.glBindTexture(GL10.GL_TEXTURE_2D,texture[5]);
          }
          else if (goalAhead && (state == MazePlayMode.NORMAL || (state == MazePlayMode.AERIAL && DebugInformation.isDebugEnabled && !aerialControl))) {
            gl.glTranslatef(x, -1.0f, z);
            //gl.glTranslatef(x, 6.5f, z);
            gl.glBindTexture(GL10.GL_TEXTURE_2D,texture[25]);
          }
          else {
            gl.glTranslatef(x, 6.5f, z);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[9]);
          }
          gl.glScalef(2.0f, 2.0f, 2.0f);
        }
        else {
          //gl.glRotatef(180 * i, 0.0f, 0.0f, 1.0f);
          gl.glTranslatef(x, -8.0f, z);
          gl.glScalef(2.0f, 2.0f, 2.0f);
          //gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[5]);
          gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[10]);
        }

        sqVertexBuffer.position(0);
        textureBuffer.position(0);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);


        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // TODO: investigate this call for mapping controls to screen coordinates
        //GLU.gluProject(objX, objY, objZ, model, modelOffset, project, projectOffset, view, viewOffset, win, winOffset)

        gl.glPopMatrix();
      }
      // left and right rotate
      if (state == MazePlayMode.NORMAL || (state == MazePlayMode.AERIAL && DebugInformation.isDebugEnabled && !aerialControl)) {
        for (int i = 0; i < 2; i++) {
          gl.glPushMatrix();

          //gl.glRotatef(180 * i + 90, 0.0f, 0.0f, 1.0f);
          //gl.glTranslatef(x, 1.5f, z);
          //gl.glTranslatef(x, 4.3f, z);

          gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

          if (i == 0) {
            gl.glTranslatef(-buttonX + x, -1.0f, z);
            gl.glScalef(2.0f, 2.0f, 2.0f);
            //gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[6]);
            if (drawLeft) {
              gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[11]);
            }
            else {
              gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[16]);
            }

          }
          else {
            gl.glTranslatef(buttonX + x, -1.0f, z);
            gl.glScalef(2.0f, 2.0f, 2.0f);
            if (drawRight) {
              gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[12]);
            }
            else {
              gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[17]);
            }
          }

          sqVertexBuffer.position(0);

          if (i == 0) {
            textureBuffer.position(0);
          }
          else {
            textureBufferRev.position(0);
          }

          gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);

          if (i==0) {
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
          }
          else {
            //gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBufferRev);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
          }

          gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

          gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

          // TODO: investigate this call for mapping controls to screen coordinates
          //GLU.gluProject(objX, objY, objZ, model, modelOffset, project, projectOffset, view, viewOffset, win, winOffset)

          gl.glPopMatrix();
        }
      }
      else if (state == MazePlayMode.AERIAL && DebugInformation.isDebugEnabled && aerialControl) {
        for (int i = 0; i < 2; i++) {
          gl.glPushMatrix();

          //gl.glRotatef(180 * i + 90, 0.0f, 0.0f, 1.0f);
          //gl.glTranslatef(x, 1.5f, z);
          //gl.glTranslatef(x, 4.3f, z);

          gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

          if (i == 0) {
            gl.glTranslatef(-buttonX + x, 1.0f, z);
            gl.glRotatef(180 * i - 90, 0.0f, 0.0f, 1.0f);
            gl.glScalef(2.0f, 2.0f, 2.0f);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[10]);
          }
          else {
            gl.glTranslatef(buttonX + x, 1.0f, z);
            gl.glRotatef(180 * i + 90, 0.0f, 0.0f, 1.0f);
            gl.glScalef(2.0f, 2.0f, 2.0f);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[9]);
          }

          sqVertexBuffer.position(0);

          if (i == 0) {
            textureBuffer.position(0);
          }
          else {
            textureBufferRev.position(0);
          }

          gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);

          if (i==0) {
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
          }
          else {
            //gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBufferRev);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
          }

          gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

          gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

          // TODO: investigate this call for mapping controls to screen coordinates
          //GLU.gluProject(objX, objY, objZ, model, modelOffset, project, projectOffset, view, viewOffset, win, winOffset)

          gl.glPopMatrix();
        }

      }

      drawDebugControls(x, z, isDebugModeEnabled, debug);
    }


    // map/3D buttons
    if (state == MazePlayMode.NORMAL || state == MazePlayMode.AERIAL ) {
      gl.glPushMatrix();

      //gl.glTranslatef(x, 1.5f, z);
      gl.glTranslatef(-buttonX + x, -8.0f, z);
      gl.glScalef(2.0f, 2.0f, 2.0f);

      gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);


      if (state == MazePlayMode.NORMAL) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[7]);
      }
      else if (state == MazePlayMode.AERIAL) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[8]);
      }

      sqVertexBuffer.position(0);
      textureBuffer.position(0);

      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);

      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

      gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

      // TODO: investigate this call for mapping controls to screen coordinates
      //GLU.gluProject(objX, objY, objZ, model, modelOffset, project, projectOffset, view, viewOffset, win, winOffset)

      gl.glPopMatrix();

      if (state == MazePlayMode.AERIAL && isDebugModeEnabled == true) {
        // up and down
        for (int i = 0; i < 2; i++) {
          gl.glPushMatrix();

          gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
          gl.glEnable(GL10.GL_TEXTURE_2D);

          if (i == 0) {
            //gl.glRotatef(180 * i, 0.0f, 0.0f, 1.0f);
            gl.glTranslatef(-buttonX + x, 6.5f, z);
            gl.glScalef(2.0f, 2.0f, 2.0f);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[10]);
          }
          else {
            gl.glTranslatef(buttonX + x, 6.5f, z);
            gl.glScalef(2.0f, 2.0f, 2.0f);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[9]);
          }

          sqVertexBuffer.position(0);
          textureBuffer.position(0);
          gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);
          gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
          gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

          gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

          // TODO: investigate this call for mapping controls to screen coordinates
          //GLU.gluProject(objX, objY, objZ, model, modelOffset, project, projectOffset, view, viewOffset, win, winOffset)

          gl.glPopMatrix();
        }
      }
    }
  }

  public void drawDebug() {
    gl.glPushMatrix();
    gl.glScalef(1.0f, 2.0f, 1.0f);
    gl.glTranslatef(-0.5f, -0.5f, -0.4f);

    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[14]);

    sqVertexBuffer.position(0);
    textureBuffer.position(0);
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);
    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glPopMatrix();
  }

  void updateMessage(String message) {
    //Create an empty, mutable bitmap
    Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
    //get a canvas to paint over the bitmap
    Canvas canvas = new Canvas(bitmap);
    bitmap.eraseColor(0);

    //get a background image from resources
    //note the image format must match the bitmap format
    Drawable background = context.getResources().getDrawable(R.drawable.debug);
    background.setBounds(0, 0, 256, 256);
    background.draw(canvas); // draw the background to our bitmap

    //Draw the text
    Paint textPaint = new Paint();
    textPaint.setTextSize(10);
    textPaint.setTypeface(Typeface.SANS_SERIF);
    textPaint.setAntiAlias(true);
    textPaint.setARGB(0xff, 0x00, 0x00, 0x00);
    //draw the text centered

    String lines[] = message.split("\\n");

    for (int i = 0; i < lines.length; i++) {
      canvas.drawText(lines[i], 16,40 + (i * 15), textPaint);
    }

    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    //...and bind it to our array
    gl.glBindTexture(GL10.GL_TEXTURE_2D, getTexture(13));

    //Create Nearest Filtered Texture
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

    //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

    //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

    //Clean up
    bitmap.recycle();
  }

  public void drawMessage() {
    gl.glPushMatrix();
    gl.glScalef(1.0f, 2.0f, 1.0f);
    gl.glTranslatef(-0.5f, -0.5f, -0.4f);

    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);

    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[13]);

    sqVertexBuffer.position(0);
    textureBuffer.position(0);
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sqVertexBuffer);
    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glPopMatrix();
  }


  public int getTexture(int textureNumber) {
    return textureNumber >= 0 && textureNumber < numTextures ? texture[textureNumber] : texture[0];
  }

  public void loadGLTexture(int resourceNumber, int textureNumber) {
    if (resourceNumber == -1)
      return;
    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceNumber);
    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[textureNumber]);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

    // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

    // Clean up
    bitmap.recycle();
  }

  public void outputTextureToFile(int resourceNumber, String fileName) {
    if (resourceNumber == -1)
      return;
    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceNumber);

    try {
      FileOutputStream fstream = context.openFileOutput(fileName, context.MODE_PRIVATE);
      bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
      bitmap.compress(Bitmap.CompressFormat.PNG, 90, fstream);

      fstream.close();
    }
    catch (Exception e) {
      Log.v("LFB", "Error saving picture!" + resourceNumber);
    }
    // Clean up
    bitmap.recycle();
  }

  public void outputPictureToFile(String path, String fileName) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 2;
    //Bitmap bitmap = BitmapFactory.decodeFile("20130113_205006.jpg", options);

    Bitmap bitmap1 = null;
    try {
      final File file = new File(Environment.getExternalStorageDirectory()
          .getAbsolutePath(), "20130113_205006.jpg");
      FileInputStream fis = new FileInputStream(file);
      bitmap1 = BitmapFactory.decodeFileDescriptor(fis.getFD());
      fis.close();
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open picture");
    }

    if (bitmap1 == null) {
      Log.v("LFB", "No go!");
      return;
    }

    try {
      FileOutputStream fstream = context.openFileOutput(fileName, context.MODE_PRIVATE);
      bitmap1.compress(Bitmap.CompressFormat.PNG, 90, fstream);
      fstream.close();
    }
    catch (Exception e) {
      Log.v("LFB", "Error saving picture!" + path);
    }
    // Clean up
    bitmap1.recycle();
  }

  void drawSky(float bgcolor_[][])
  {
    FloatBuffer skyVertexBuffer[] = new FloatBuffer[10];
    FloatBuffer colorBuffer[] = new FloatBuffer[10];

    for (int i = 0; i < 10; i++) {
      skyVertexBuffer[i] = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      colorBuffer[i] = ByteBuffer.allocateDirect(4 * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    gl.glDisable(GL10.GL_TEXTURE_2D);
    gl.glDepthMask(false);
    gl.glFrontFace(GL10.GL_CCW);

    //glPushMatrix();
    // -front sky //
    colorBuffer[0].put( bgcolor_[0]);
    skyVertexBuffer[0].put(new float[] {-1.0f, 1.0f, -1.0f});
    colorBuffer[0].put( bgcolor_[1]);
    skyVertexBuffer[0].put(new float[] {-1.0f, 0.0f, -1.0f});
    colorBuffer[0].put(bgcolor_[0]);
    skyVertexBuffer[0].put(new float[]{1.0f, 1.0f, -1.0f});
    colorBuffer[0].put( bgcolor_[1]);
    skyVertexBuffer[0].put(new float[]{1.0f, 0.0f, -1.0f});

    colorBuffer[1].put( bgcolor_[1]);
    skyVertexBuffer[1].put(new float[] {-1.0f, 0.0f, -1.0f});
    colorBuffer[1].put( bgcolor_[2]);
    skyVertexBuffer[1].put(new float[] {-1.0f, -1.0f, -1.0f});
    colorBuffer[1].put( bgcolor_[1]);
    skyVertexBuffer[1].put(new float[] {1.0f, 0.0f, -1.0f});
    colorBuffer[1].put( bgcolor_[2]);
    skyVertexBuffer[1].put(new float[] {1.0f, -1.0f, -1.0f});

    // left sky //
    colorBuffer[2].put( bgcolor_[0]);
    skyVertexBuffer[2].put(new float[] {-1.0f, 1.0f, 1.0f});
    colorBuffer[2].put( bgcolor_[1]);
    skyVertexBuffer[2].put(new float[] {-1.0f, 0.0f, 1.0f});
    colorBuffer[2].put( bgcolor_[0]);
    skyVertexBuffer[2].put(new float[] {-1.0f, 1.0f, -1.0f});
    colorBuffer[2].put( bgcolor_[1]);
    skyVertexBuffer[2].put(new float[] {-1.0f, 0.0f, -1.0f});

    colorBuffer[3].put( bgcolor_[1]);
    skyVertexBuffer[3].put(new float[] {-1.0f, 0.0f, 1.0f});
    colorBuffer[3].put( bgcolor_[2]);
    skyVertexBuffer[3].put(new float[] {-1.0f, -1.0f, 1.0f});
    colorBuffer[3].put( bgcolor_[1]);
    skyVertexBuffer[3].put(new float[] {-1.0f, 0.0f, -1.0f});
    colorBuffer[3].put( bgcolor_[2]);
    skyVertexBuffer[3].put(new float[] {-1.0f, -1.0f, -1.0f});

    // right sky //
    colorBuffer[4].put( bgcolor_[0]);
    skyVertexBuffer[4].put(new float[] {1.0f, 1.0f, -1.0f});
    colorBuffer[4].put( bgcolor_[1]);
    skyVertexBuffer[4].put(new float[] {1.0f, 0.0f, -1.0f});
    colorBuffer[4].put( bgcolor_[0]);
    skyVertexBuffer[4].put(new float[] {1.0f, 1.0f, 1.0f});
    colorBuffer[4].put( bgcolor_[1]);
    skyVertexBuffer[4].put(new float[] {1.0f, 0.0f, 1.0f});

    colorBuffer[5].put( bgcolor_[1]);
    skyVertexBuffer[5].put(new float[] {1.0f, 0.0f, -1.0f});
    colorBuffer[5].put( bgcolor_[2]);
    skyVertexBuffer[5].put(new float[] {1.0f, -1.0f, -1.0f});
    colorBuffer[5].put( bgcolor_[1]);
    skyVertexBuffer[5].put(new float[] {1.0f, 0.0f, 1.0f});
    colorBuffer[5].put( bgcolor_[2]);
    skyVertexBuffer[5].put(new float[] {1.0f, -1.0f, 1.0f});

    // -back sky //
    colorBuffer[6].put( bgcolor_[0]);
    skyVertexBuffer[6].put(new float[] {1.0f, 1.0f, 1.0f});
    colorBuffer[6].put( bgcolor_[1]);
    skyVertexBuffer[6].put(new float[] {1.0f, 0.0f, 1.0f});
    colorBuffer[6].put( bgcolor_[0]);
    skyVertexBuffer[6].put(new float[] {-1.0f, 1.0f, 1.0f});
    colorBuffer[6].put( bgcolor_[1]);
    skyVertexBuffer[6].put(new float[] {-1.0f, 0.0f, 1.0f});

    colorBuffer[7].put( bgcolor_[1]);
    skyVertexBuffer[7].put(new float[] {1.0f, 0.0f, 1.0f});
    colorBuffer[7].put( bgcolor_[2]);
    skyVertexBuffer[7].put(new float[] {1.0f, -1.0f, 1.0f});
    colorBuffer[7].put( bgcolor_[1]);
    skyVertexBuffer[7].put(new float[] {-1.0f, 0.0f, 1.0f});
    colorBuffer[7].put( bgcolor_[2]);
    skyVertexBuffer[7].put(new float[] {-1.0f, -1.0f, 1.0f});

    colorBuffer[8].put( bgcolor_[0]);
    skyVertexBuffer[8].put(new float[] {-1.0f, 1.0f, -1.0f});
    colorBuffer[8].put( bgcolor_[0]);
    skyVertexBuffer[8].put(new float[] {1.0f, 1.0f, -1.0f});
    colorBuffer[8].put( bgcolor_[0]);
    skyVertexBuffer[8].put(new float[] {-1.0f, 1.0f, 1.0f});
    colorBuffer[8].put( bgcolor_[0]);
    skyVertexBuffer[8].put(new float[] {1.0f, 1.0f, 1.0f});

    // bottom sky //
    colorBuffer[9].put( bgcolor_[2]);
    skyVertexBuffer[9].put(new float[] {1.0f, -1.0f, -1.0f});
    colorBuffer[9].put( bgcolor_[2]);
    skyVertexBuffer[9].put(new float[] {-1.0f, -1.0f, -1.0f});
    colorBuffer[9].put( bgcolor_[2]);
    skyVertexBuffer[9].put(new float[] {1.0f, -1.0f, 1.0f});
    colorBuffer[9].put( bgcolor_[2]);
    skyVertexBuffer[9].put(new float[] {-1.0f, -1.0f, 1.0f});

    for (int i = 0; i < 10; i++) {
      skyVertexBuffer[i].position(0);
      colorBuffer[i].position(0);

      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, skyVertexBuffer[i]);
      gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer[i]);

      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    gl.glEnable(GL10.GL_TEXTURE_2D);
    gl.glFrontFace(GL10.GL_CW);
    gl.glDepthMask(true);
  }

  // why 5? we don't care about the bottom
  final private int sides = 5;
  private FloatBuffer boxVertexBuffer[] = new FloatBuffer[sides];
  private FloatBuffer colorVertexBuffer[] = new FloatBuffer[sides];
  private FloatBuffer sqVertexBuffer;
  private FloatBuffer triangleVertexBuffer;
  private FloatBuffer colorTriangleVertexBuffer;

  private FloatBuffer textureBuffer;  // buffer holding the texture coordinates
  private FloatBuffer textureBufferRev;  // buffer holding the texture coordinates

  private LFB context;
  private GL10 gl;

  private final float buttonX = 3.8f;

  private float colors[][] = {
      {0.0f, 0.0f, 0.0f, 1.0f}, // n/a
      {1.0f, 0.0f, 0.0f, 1.0f}, // wall 1
      {0.0f, 0.0f, 1.0f, 1.0f}, // wall 2
      {0.0f, 1.0f, 0.0f, 0.4f}, // player start
      {0.0f, 1.0f, 0.0f, 0.4f}, // end of maze
      {0.5f, 0.8f, 0.5f, 1.0f}, // player
      {0.0f, 1.0f, 1.0f, 0.4f}, // already visited
      {0.0f, 0.0f, 0.0f, 1.0f}, // picture gate
      {0.0f, 0.0f, 0.0f, 1.0f}, // text gate
  };

  private List<Integer> textureNumbers = new ArrayList<Integer>();

  // texture mapping
  private int numTextures = 0;
  private int texture[];
}
