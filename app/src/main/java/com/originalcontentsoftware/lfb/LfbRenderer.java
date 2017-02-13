package com.originalcontentsoftware.lfb;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class LfbRenderer implements Renderer {
  public class Player {
    private Vector position = new Vector(0.0f, 0.0f, 0.0f);
    private int direction = 0;

    private int numBacktrack = 0;
    private int numTraveled = 0;

    private int dizzyNumber = 0;

    public void resetValues() {
      numBacktrack = 0;
      numTraveled = 0;
      dizzyNumber = 0;
    }
  }

  // Player information
  private Vector[] directions = {
    new Vector(0.0f, 0.0f, -1.0f),
    new Vector(-1.0f, 0.0f, 0.0f),
    new Vector(0.0f, 0.0f, 1.0f),
    new Vector(1.0f, 0.0f, 0.0f)
  };

  private Player player = new Player();

  private int playerStartDirection = 0;

  private final float cameraHeight = 6.5f;

  // Movement information
  public boolean moveForward = false;
  public boolean moveBackward = false;
  public boolean rotLeft = false;
  public boolean rotRight = false;

  public boolean moveCameraForward = false;
  public boolean moveCameraBackward = false;
  public boolean moveCameraLeft = false;
  public boolean moveCameraRight = false;

  public boolean aerialControl = false;

  public float sky[][] = {{0.5f, 0.5f, 1.0f, 1.0f}, {0.9f, 0.9f, 1.0f, 1.0f}, {0.5f, 0.5f, 1.0f, 1.0f}};

  public Queue<String> commands = new LinkedList<String>();

  // Required objects for game play
  private Maze maze;
  private GameTimer gameTimer_ = new GameTimer(30.0f, 100);

  // Changing values && direction != null
  public float zoomHeight = 200.0f;
  private Vector cameraPosition = new Vector(0.0f, zoomHeight, 0.0f);

  float fps = 0.0f;
  float totalTime = 0.0f;
  float lastMoveTime = 0.0f;
  float nextMoveAllowed = 0.17f;
  float nextMoveAllowedDebug = 0.01f;

  private int gateX = 0;
  private int gateZ = 0;

  private String resolution  = "x";

  private boolean firstTime = false;

  private String mazeFile = "";
  private String location = "resource";
  private Uri mazeUri = null;

  // Shared resources
  public LFB context;
  private GL10 gl;
  private GraphicsUtil graphicsUtil;

  // Game modes and states
  public boolean debug = false;
  public MazePlayMode currentMode = MazePlayMode.NORMAL;

  private MazeQuestionGate questionGate;

  private MoveSequenceManager moveSequenceManager = new MoveSequenceManager();

  QuestionAnswerNotify listener = null;

  public LfbRenderer(LFB c, String mazeFile, String location, Uri mazeUri) {
    Log.v("LFB", "In constructor!");
    this.mazeFile = mazeFile;
    this.location = location;
    this.mazeUri = mazeUri;
    context = c;
    graphicsUtil = new GraphicsUtil(context);

    moveSequenceManager.addDizzyLeftDequence();
    moveSequenceManager.addDizzyRightDequence();
    moveSequenceManager.addToggleDebugSequence();

    WindowManager w = c.getWindowManager();
    Display d = w.getDefaultDisplay();
    int width = d.getWidth();
    int height = d.getHeight();

    resolution = Integer.toString(width) + "x" + Integer.toString(height);
    Log.v("LFB", "Resolution=" + resolution);
  }

  public void restart() {
    player.direction = playerStartDirection;

    moveSequenceManager.resetSequenceNumbers();

    moveForward = false;
    moveBackward = false;
    rotLeft = false;
    rotRight = false;

    moveCameraForward = false;
    moveCameraBackward = false;
    moveCameraLeft = false;
    moveCameraRight = false;

    zoomHeight = 200.0f;
    gameTimer_.init();

    debug = false;

    if (maze == null) {
      return;
    }

    Vector playerStartPosition = maze.getPlayerStartPosition();
    player.position.x = playerStartPosition.x;
    player.position.z = playerStartPosition.z;
    Log.v("LFB", "Plr x=" + player.position.x);
    Log.v("LFB", "Plr z=" + player.position.z);

    determinePlayerStartDirection();

    loadMaze(mazeFile, location);
  }

  public void determinePlayerStartDirection() {
    // ensure that the player is at least always facing a free space and not a wall
    player.direction = 0;

    for (int i = 0; i < 4; i++) {
      int nextX = 0;
      int nextZ = 0;

      if (i == 0) {
        nextX = (int)((player.position.x + (directions[player.direction].x)));
        nextZ = (int)((player.position.z + (directions[player.direction].z)));
      }
      else if (i == 1) {
        nextX = (int)((player.position.x + (directions[player.direction].z)));
        nextZ = (int)((player.position.z + (directions[player.direction].x)));
      }
      else if (i == 2) {
        nextX = (int)((player.position.x - (directions[player.direction].x)));
        nextZ = (int)((player.position.z - (directions[player.direction].z)));
      }
      else if (i == 3) {
        nextX = (int)((player.position.x - (directions[player.direction].z)));
        nextZ = (int)((player.position.z - (directions[player.direction].x)));
      }

      if (maze.isPositionFree(nextX, nextZ) || maze.isPositionGate( nextX, nextZ) || maze.isPositionGoal(nextX, nextZ)) {
        player.direction = i;
        break;
      }
    }

    playerStartDirection = player.direction;
  }

  public void setupGl(GL10 gl) {
    gl.glClearColor(0.7f, 0.9f, 1.0f, 0.5f);
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

    gl.glEnable(GL10.GL_DEPTH_TEST);
    gl.glDepthFunc( GL10.GL_LEQUAL );
    gl.glEnable(GL10.GL_CULL_FACE);
    gl.glFrontFace(GL10.GL_CW);
    gl.glCullFace( GL10.GL_BACK );
    gl.glDepthMask(true);
    gl.glDepthRangef(0.0f,  600.0f);

    gl.glClearDepthf(1.0f);

    gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
    //gl.glBlendFunc(GL10.GL_ONE, GL10.GL_SRC_COLOR);
    //gl_.glFrontFace( GL10.GL_CCW );
    //gl_.glEnable( GL10.GL_CULL_FACE );
    // TODO gl_.glPolygonMode( GL10.GL_FRONT, GL10.GL_FILL );

    gl.glHint( GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST );

    /*
      gl.glEnable( GL10.GL_LIGHTING );
      gl.glEnable(GL10.GL_LIGHT0);

      // Define the ambient component of the first light
      gl.glLightf(GL10.GL_LIGHT0, GL10.GL_AMBIENT, 0.1f);

      // Define the diffuse component of the first light
      gl.glLightf(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, 0.7f);

      // Define the specular component and shininess of the first light
      gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPECULAR, 0.7f);

      // Define the position of the first light
      gl.glLightf(GL10.GL_LIGHT0, GL10.GL_POSITION, 10.0f);

      // Define a direction vector for the light, this one points right down the Z axis
      gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, -1.0f);

      // Define a cutoff angle. This defines a 90 field of vision, since the cutoff
      // is number of degrees to each side of an imaginary line drawn from the light's
      // position along the vector supplied in GL_SPOT_DIRECTION above
      gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 45.0f);

      gl.glEnable(GL10.GL_COLOR_MATERIAL);
     */

    // texture mapping
    gl.glEnable(GL10.GL_TEXTURE_2D);
    gl.glEnable(GL10.GL_BLEND);

    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
  }

  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    Log.v("LFB", "Surface created!");
    setupGl(gl);

    this.gl = gl;

    graphicsUtil.setGL(this.gl);
    graphicsUtil.generateTextures();

    float timeElapsed_ = gameTimer_.getElapsedSeconds();
    totalTime += timeElapsed_;
    lastMoveTime = 0.0f;

    // this is a way to do something on the first time through only...
    if (firstTime == false) {
      boolean success = true;
      if (mazeFile.equals("")) {
        success = false;
      }
      else {
        Log.v("LFB", "Loading maze file...");
        maze = new Maze(graphicsUtil);
        success = maze.loadMaze(mazeFile, context, location, mazeUri);
      }

      if (success && maze != null) {
        restart();
        firstTime = true;
      }
      else {
        String message = "Error loading maze file!";
        maze = null;
        listener.onLoadFailure(message);
      }
    }
  }

  public void onSurfaceChanged(GL10 gl, int width, int height) {
    Log.v("LFB", "Surface resized!");

    float near = 0.001f;
    float fov = 60.0f;

    graphicsUtil.setGL(this.gl);

    gl.glViewport(0, 0, width, height);

    // make adjustments for screen ratio
    gl.glMatrixMode(GL10.GL_PROJECTION);        // set matrix to projection mode
    gl.glLoadIdentity();                        // reset the matrix to its default state

    float fh = (float)Math.tan( fov / 2.0f * (3.14159265f / 180.0f))  * near;
    float aspect = (float)((float)width / (float)height); // 800x600 -> 480x800
    float fw = fh * aspect;

    gl.glFrustumf(-fw, fw, -fh, fh, near, 600.f);
  }

  public void onDrawFrame(GL10 gl) {
    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

    // Set GL_MODELVIEW transformation mode
    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glLoadIdentity();   // reset the matrix to its default state

    float timeElapsed_ = gameTimer_.getElapsedSeconds();
    if (maze != null && !maze.isComplete()) {
      totalTime += timeElapsed_;
      lastMoveTime += timeElapsed_;
    }
    fps = gameTimer_.getFPS();

    //Log.v("LFB", "FPS=" + fps_ + " time elapsed=" + timeElapsed_);

    // Respond to input and adjust the world/view as necessary
    if (DebugInformation.isDebugEnabled && aerialControl && currentMode == MazePlayMode.AERIAL) {
      if (lastMoveTime >= nextMoveAllowedDebug) {
        move();
        lastMoveTime = 0.0f;
        //Log.v("LFB", "Debug delay for move!");
      }
    }
    else {
      if (lastMoveTime >= nextMoveAllowed) {
        move();
        lastMoveTime = 0.0f;
        //Log.v("LFB", "Regular delay for move!");
      }
    }

    graphicsUtil.drawSky(sky);

    if (debug == false) {
      if (currentMode == MazePlayMode.NORMAL || currentMode == MazePlayMode.QUESTION) {
        // 3D view
        GLU.gluLookAt(gl, player.position.x * 10.0f, cameraHeight, player.position.z * 10.0f, (float)((player.position.x * 10.0f)+directions[player.direction].x), cameraHeight, (float)((player.position.z * 10.0f) + directions[player.direction].z), 0.0f, 1.0f, 0.0f);
      }
      else if (currentMode == MazePlayMode.AERIAL){
        // overhead view
        if (!aerialControl) {
          cameraPosition.x = player.position.x;
          cameraPosition.z = player.position.z;
        }
        GLU.gluLookAt(gl, cameraPosition.x * 10.0f, cameraPosition.y, cameraPosition.z * 10.0f, cameraPosition.x * 10.0f, 0.0f, cameraPosition.z * 10.0f,  directions[player.direction].x, 0.0f, directions[player.direction].z);
      }
    }
    else if (debug == true && DebugInformation.isDebugEnabled == true){
      updateDebug(gl);

      GLU.gluLookAt(gl, 0.0f, 0.0f, 1.0f,0.0f, 0.0f, 0.0f, 0f, 1.0f, 0.0f);

      graphicsUtil.drawDebug();
      gl.glClear( GL10.GL_DEPTH_BUFFER_BIT );
      gl.glLoadIdentity();
      gl.glDepthFunc(GL10.GL_ALWAYS);
      graphicsUtil.drawDebugControls(-1.0f, -15.0f, DebugInformation.isDebugEnabled, debug);
      gl.glDepthFunc(GL10.GL_LEQUAL);

      return;
    }

    // place camera where we want it in the maze -- centered in a block
    // adjustment by direction to "push" the camera back 7 units so that walls are not "in your face"
    if (currentMode == MazePlayMode.NORMAL || currentMode == MazePlayMode.QUESTION) {
      gl.glTranslatef((-5.0f + directions[player.direction].x * 7.0f), 0.0f, (-5.0f + directions[player.direction].z * 7.0f));
    }

    if (maze != null) {
      maze.draw(directions[player.direction], player.position, currentMode);
    }

    // In overhead view, draw a cursor for the player.
    if (currentMode == MazePlayMode.AERIAL) {
      gl.glDepthFunc(GL10.GL_ALWAYS);
      graphicsUtil.drawPlayer(player.position.x, player.position.z, directions[player.direction]);
      gl.glDepthFunc(GL10.GL_LESS);
    }

    // Draw the controls onto the screen (directional arrows)
    if (currentMode == MazePlayMode.NORMAL || currentMode == MazePlayMode.AERIAL) {
      /*
    gl.glMatrixMode(GL10.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();   // reset the matrix to its default state
    gl.glOrthof(0.0f, 480.0f, 800.0f, 0.0f, -30.0f, 30.0f);
    gl.glMatrixMode(GL10.GL_MODELVIEW);
       */
      gl.glClear( GL10.GL_DEPTH_BUFFER_BIT );
      gl.glLoadIdentity();
      gl.glDepthFunc(GL10.GL_ALWAYS);
      drawControlsArrow(gl, -1.0f, -15.0f, 1);
      gl.glDepthFunc(GL10.GL_LEQUAL);
      /*
    gl.glMatrixMode(GL10.GL_PROJECTION);
    gl.glPopMatrix();
       */
    }
  }

  public void move() {
    int rl = 0;
    int rr = 0;
    int mf = 0;
    int mb = 0;

    int mcl = 0;
    int mcr = 0;
    int mcf = 0;
    int mcb = 0;

    int answer = -1;

    boolean toggleDebug = false;

    String command = (String)commands.poll();

    while (command != null) {
      Log.v("LFB", "command=" + command);

      List<MoveSequenceManager.Sequence> matchSequences = moveSequenceManager.findMatches(command);

      for (MoveSequenceManager.Sequence s : matchSequences) {
        switch (s) {
        case TOGGLE_DEBUG:
          toggleDebug = true;
          Log.v("LFB", "Sequence complete -- toggle debug");
          break;
        case DIZZY_LEFT:
        {
          String  message = context.getResources().getString(R.string.maze_dizzy_1);
            switch (player.dizzyNumber) {
            case 0:
              player.dizzyNumber++;
            break;
            case 1:
              message = context.getResources().getString(R.string.maze_dizzy_2);
              player.dizzyNumber++;
            break;
            case 2:
              message = context.getResources().getString(R.string.maze_dizzy_3);
            break;
            }
          listener.onLoaded(message);
          Log.v("LFB", "Sequence complete -- dizzy left");
          break;
        }
        case DIZZY_RIGHT:
        {
          String  message = context.getResources().getString(R.string.maze_dizzy_1);
            switch (player.dizzyNumber) {
            case 0:
              player.dizzyNumber++;
            break;
            case 1:
              message = context.getResources().getString(R.string.maze_dizzy_2);
              player.dizzyNumber++;
            break;
            case 2:
              message = context.getResources().getString(R.string.maze_dizzy_3);
            break;
            }
          listener.onLoaded(message);
          Log.v("LFB", "Sequence complete -- dizzy right");
          break;
        }
        default:
          Log.v("LFB", "Unknown sequence" + s);
          break;
        }
      }

      if (command.equals("RotLeft")) {
        rotLeft = true;
        rl++;
      }
      else if (command.equals("RotRight")) {
        rotRight = true;
        rr++;
      }
      else if (command.equals("MoveForward")) {
        moveForward = true;
        mf++;
      }
      else if (command.equals("MoveBackward")) {
        moveBackward = true;
        mb++;
      }
      else if (command.equals("Up")) {
        rotLeft = false;
        rotRight = false;
        moveForward = false;
        moveBackward = false;
        moveCameraForward = false;
        moveCameraBackward = false;
        moveCameraLeft = false;
        moveCameraRight = false;

      }
      else if (command.equals("ChangeView")) {
        if (currentMode == MazePlayMode.NORMAL) {
          currentMode = MazePlayMode.AERIAL;
        }
        else if (currentMode == MazePlayMode.AERIAL) {
          currentMode = MazePlayMode.NORMAL;
        }

        cameraPosition.x = player.position.x;
        cameraPosition.z = player.position.z;
        cameraPosition.y = zoomHeight;
      }
      else if (command.equals("LeaveQuestion")) {
        currentMode = MazePlayMode.NORMAL;
      }
      else if (command.equals("AnswerA")) {
        answer = 0;
      }
      else if (command.equals("AnswerB")) {
        answer = 1;
      }
      else if (command.equals("AnswerC")) {
        answer = 2;
      }
      else if (command.equals("AnswerD")) {
        answer = 3;
      }
      else if (command.equals("MoveCameraLeft")) {
        moveCameraLeft = true;
        mcl++;
      }
      else if (command.equals("MoveCameraRight")) {
        moveCameraRight = true;
        mcr++;
      }
      else if (command.equals("MoveCameraForward")) {
        moveCameraForward = true;
        mcf++;
      }
      else if (command.equals("MoveCameraBackward")) {
        moveCameraBackward = true;
        mcb++;
      }
      else if (command.equals("ZoomIn") && DebugInformation.isDebugEnabled == true) {
        zoomHeight -= 50.0f;
        Log.v("LFB", "ZoomIn");
        if (zoomHeight < 100.0f) {
          zoomHeight = 100.0f;
        }
        cameraPosition.y = zoomHeight;
      }
      else if (command.equals("ZoomOut") && DebugInformation.isDebugEnabled == true) {
        zoomHeight += 50.0f;
        Log.v("LFB", "ZoomOut");
        if (zoomHeight > 600.0f) {
          zoomHeight = 600.0f;
        }
        cameraPosition.y = zoomHeight;
      }
      else if (command.equals("ToggleDebug") && DebugInformation.isDebugEnabled == true) {
        debug = !debug;
      }
      else if (command.equals("ToggleAerialControl") && DebugInformation.isDebugEnabled == true) {
        aerialControl = !aerialControl;
        cameraPosition.x = player.position.x;
        cameraPosition.z = player.position.z;
        cameraPosition.y = zoomHeight;
      }
      else if (command.equals("ExitQuestionMode")) {
        currentMode = MazePlayMode.NORMAL;
      }
      command = (String)commands.poll();
    }

    if (toggleDebug) {
      String message = "Debug mode has been ";
      if (DebugInformation.isDebugEnabled == false) {
        DebugInformation.isDebugEnabled = true;
        message += "enabled.";
      }
      else {
        DebugInformation.isDebugEnabled = false;
        message += "disabled.";
      }

      listener.onDebugChanged();
      listener.onLoaded(message);

      return;
    }

    // handle question/answer input
    if (currentMode == MazePlayMode.QUESTION) {
      Log.v("LFB", "Any correct answers?");
      if (answer == questionGate.getCorrectAnswerNumber()) {
        Log.v("LFB", "Yes!");
        maze.clearGate(new Vector(gateX, 0.0f, gateZ));
        listener.onChoice(context.getResources().getString(R.string.question_mode_correct));
        questionGate.setCleared(true);
      }
      else if (answer >= 0) {
        Log.v("LFB", "No!");
        questionGate.incNumWrongAnswers();

        int incorrectBehavior = questionGate.getIncorrectBehavior();

        if (incorrectBehavior == 2) {
          maze.setPosition(gateX, gateZ, 1);
          listener.onChoice(context.getResources().getString(R.string.question_mode_incorrect_remove_gate));
        }
        else if (incorrectBehavior == 3) {
          float oTotalTime = totalTime;
          totalTime += 10.0f;
          listener.onChoice(context.getResources().getString(R.string.question_mode_incorrect_time_penalty) + "\n" + oTotalTime + " -> " + totalTime);
        }
        else if (incorrectBehavior == 0) {
          listener.onChoice(context.getResources().getString(R.string.question_mode_incorrect_nothing));
        }
        else if (incorrectBehavior == 1) {
          Vector tempPlayerPosition = maze.handleIncorrectAnswer(questionGate.getIncorrectBehavior());
          if (tempPlayerPosition != null) {
            listener.onChoice(context.getResources().getString(R.string.question_mode_incorrect_restart_player));
            player.position.x = tempPlayerPosition.x;
            player.position.z = tempPlayerPosition.z;
            player.direction = playerStartDirection;
          }
        }
      }
    }

    if (currentMode != MazePlayMode.NORMAL && DebugInformation.isDebugEnabled == false) {
      return;
    }

    // Handle rotation
    if (rotLeft || rl > 0) {
      player.direction++;
      if (player.direction > 3)
        player.direction = 0;

      rotLeft = false;
    }
    else if (rotRight || rr > 0) {
      player.direction--;
      if (player.direction < 0)
        player.direction = 3;

      rotRight = false;
    }

    // Handle movement
    if ( moveForward || mf > 0) {
      int nextX = (int)((player.position.x + (directions[player.direction].x)));
      int nextZ = (int)((player.position.z + (directions[player.direction].z)));
      Log.v("LFB", "next x=" + nextX + " z=" + nextZ);
      if (maze.isPositionInBounds(nextX, nextZ) && maze.isPositionFree(nextX, nextZ)) {
        player.numTraveled++;
        if (maze.isPositionVisited(nextX, nextZ)) {
          player.numBacktrack++;
        }

        maze.setVisited(nextX, nextZ, directions[player.direction]);

        player.position.x = nextX;
        player.position.z = nextZ;
        Log.v("LFB", "position x=" + player.position.x + " z=" + player.position.z);

        // don't force player into gate
        int nextNextX = (int)((player.position.x + (directions[player.direction].x)));
        int nextNextZ = (int)((player.position.z + (directions[player.direction].z)));
        if (!maze.isPositionFree(nextNextX, nextNextZ)) {
          moveForward = false;
        }
      }
      else if (maze.isPositionInBounds(nextX, nextZ) && maze.isPositionGate(nextX, nextZ)) {
        Log.v("LFB", "Entering question mode...");
        questionGate = maze.getQuestionGate(nextX, nextZ);

        if (questionGate != null) {
          Intent myIntent = new Intent(context, QuestionActivity.class);
          myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          myIntent.putExtra("Question", questionGate.getQuestion());
          String[] answers = questionGate.getAnswers();
          myIntent.putExtra("AnswerA", answers[0]);
          myIntent.putExtra("AnswerB", answers[1]);
          myIntent.putExtra("AnswerC", answers[2]);
          myIntent.putExtra("AnswerD", answers[3]);
          myIntent.putExtra("ShowPicture", questionGate.getPicturePath());

          context.startActivityForResult(myIntent, 1);

          moveForward = false;

          currentMode = MazePlayMode.QUESTION;
          gateX = nextX;
          gateZ = nextZ;
        }
        else {
          Log.v("LFB", "Could not load question gate!");
        }
      }
      else if (maze.isPositionGoal(nextX, nextZ)) {
        maze.setComplete();
        int numCleared = maze.calcNumberGatesCleared();
        int numMissedQuestions = maze.calcNumberGatesMissed();

        int minutes = (int)(totalTime / 60);
        int seconds = (int)(totalTime % 60);

        float visitPercentage = maze.visitPercentage();

        String message = context.getResources().getString(R.string.maze_complete_message) + "\n\n" +
            context.getResources().getString(R.string.maze_complete_time) + " " + minutes + "min(s), " + seconds + "sec(s)\n" +
            context.getResources().getString(R.string.maze_complete_cleared_gates) + " " + numCleared + " / " + maze.numGates() + "\n" +
            context.getResources().getString(R.string.maze_complete_completion) + " " + MessageFormat.format("{0,number,#.##%}", visitPercentage) + "\n" +
            context.getResources().getString(R.string.maze_complete_missed_questions) + " " + numMissedQuestions + "\n" +
            context.getResources().getString(R.string.maze_complete_number_moves) + " " +  player.numTraveled + "\n" +
            context.getResources().getString(R.string.maze_complete_number_backtrack_moves) + " " + player.numBacktrack;

        String messageEmail =
            context.getResources().getString(R.string.email_maze_complete_message1) + "\n\n" +
                maze.generateHeader(context) + "\n\n" +
                context.getResources().getString(R.string.email_maze_complete_message2) + "\n\n" +
                context.getResources().getString(R.string.email_maze_complete_time) + " " + minutes + "min(s), " + seconds + "sec(s)\n" +
                context.getResources().getString(R.string.email_maze_complete_cleared_gates) + " " + numCleared + " / " + maze.numGates() + "\n" +
                context.getResources().getString(R.string.email_maze_complete_completion) + " " + MessageFormat.format("{0,number,#.##%}", visitPercentage) + "\n" +
                context.getResources().getString(R.string.email_maze_complete_missed_questions) + " " + numMissedQuestions + "\n" +
                context.getResources().getString(R.string.email_maze_complete_number_moves) + " " +  player.numTraveled + "\n" +
                context.getResources().getString(R.string.email_maze_complete_number_backtrack_moves) + " " + player.numBacktrack + "\n\n" +
                context.getResources().getString(R.string.email_maze_complete_legal);

        listener.onComplete(message, messageEmail);
      }
    }
    else if (moveBackward || mb > 0) {
      int nextX = (int)((player.position.x - (directions[player.direction].x)));
      int nextZ = (int)((player.position.z - (directions[player.direction].z)));

      Log.v("LFB", "next x=" + nextX + " z=" + nextZ);
      if (maze.isPositionInBounds(nextX, nextZ) && maze.isPositionFree(nextX, nextZ)) {
        player.numTraveled++;
        if (maze.isPositionVisited(nextX, nextZ)) {
          player.numBacktrack++;
        }

        maze.setVisited(nextX, nextZ, directions[player.direction]);

        player.position.x = nextX;
        player.position.z = nextZ;
        Log.v("LFB", "position x=" + player.position.x + " z=" + player.position.z);
      }
    }
    else if ( moveCameraForward || mcf > 0) {
      cameraPosition.x = (int)((cameraPosition.x + (directions[player.direction].x)));
      cameraPosition.z = (int)((cameraPosition.z + (directions[player.direction].z)));
    }
    else if (moveCameraBackward || mcb > 0) {
      cameraPosition.x = (int)((cameraPosition.x - (directions[player.direction].x)));
      cameraPosition.z = (int)((cameraPosition.z - (directions[player.direction].z)));
    }
    else if ( moveCameraLeft || mcl > 0) {
      if (directions[player.direction].z != 0) {
        cameraPosition.x = (int)((cameraPosition.x + (directions[player.direction].z)));
        cameraPosition.z = (int)((cameraPosition.z + (directions[player.direction].x)));
      }
      else {
        cameraPosition.x = (int)((cameraPosition.x - (directions[player.direction].z)));
        cameraPosition.z = (int)((cameraPosition.z - (directions[player.direction].x)));
      }
    }
    else if ( moveCameraRight || mcr > 0 ) {
      if (directions[player.direction].z != 0) {
        cameraPosition.x = (int)((cameraPosition.x - (directions[player.direction].z)));
        cameraPosition.z = (int)((cameraPosition.z - (directions[player.direction].x)));
      }
      else {
        cameraPosition.x = (int)((cameraPosition.x + (directions[player.direction].z)));
        cameraPosition.z = (int)((cameraPosition.z + (directions[player.direction].x)));
      }
    }
  }

  public void updateDebug(GL10 gl) {
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
    canvas.drawText("Debug Information:", 16,40, textPaint);
    textPaint.setTextSize(10);
    canvas.drawText("Plr x=" + player.position.x, 16,60, textPaint);
    canvas.drawText("Plr z=" + player.position.z, 16,75, textPaint);
    canvas.drawText("fps=" + fps, 16,90, textPaint);
    canvas.drawText("Time=" + totalTime, 16,105, textPaint);
    canvas.drawText("Mode=" + currentMode, 16,120, textPaint);
    canvas.drawText("Total Gates=" + maze.numGates(), 16,135, textPaint);
    canvas.drawText("Missed Questions=" + maze.calcNumberGatesMissed(), 16,150, textPaint);
    canvas.drawText("Number of Moves=" + player.numTraveled, 16,165, textPaint);
    canvas.drawText("Number of Backtrack Moves=" + player.numBacktrack, 16,180, textPaint);
    canvas.drawText("Resolution=" + resolution, 16,195, textPaint);

    //...and bind it to our array
    gl.glBindTexture(GL10.GL_TEXTURE_2D, graphicsUtil.getTexture(14));

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

  public void generateMaze()
  {
    player.numBacktrack = 0;
    totalTime = 0.0f;
    lastMoveTime = 0.0f;
    player.numTraveled = 0;
    mazeFile = "";
    location = "resource";
    maze = new Maze(graphicsUtil);
    maze.addMathQuestions();
    maze.generateMaze(21, 21);
    restart();
  }

  public void generateMathDemoMaze() {
    player.resetValues();
    totalTime = 0.0f;
    lastMoveTime = 0.0f;
    mazeFile = "";
    location = "resource";
    maze = new Maze(graphicsUtil);
    maze.addMathQuestions();
    maze.generateMaze(21, 21);
    maze.setMazeName("Demo Maze #1");
    restart();
    maze.exportMazeExternal("demo_maze1.ocsmaze", context);
  }

  public void generatePresidentsDemoMaze() {
    player.resetValues();
    totalTime = 0.0f;
    lastMoveTime = 0.0f;
    mazeFile = "";
    location = "resource";
    maze = new Maze(graphicsUtil);
    maze.addUsPresidentsQuestions();
    maze.generateMaze(21, 21);
    maze.setMazeName("Demo Maze #2");
    restart();
    maze.exportMazeExternal("demo_maze2.ocsmaze", context);
  }

  public void generateEmptyMaze() {
    player.resetValues();
    totalTime = 0.0f;
    lastMoveTime = 0.0f;
    mazeFile = "";
    location = "resource";
    maze = new Maze(graphicsUtil);
    maze.createBareMaze(21, 21);
    restart();
  }

  public void loadMaze(String fileName, String location) {
    Log.v("LFB", "Load Maze Called!");
    if (fileName.equals("")) {
      fileName = "Demo Maze";
    }
    //mazeFile = fileName;
    player.resetValues();
    totalTime = 0.0f;
    lastMoveTime = 0.0f;
    //restart();

    listener.onLoaded(maze.generateHeader(context));
  }

  public void drawControlsArrow(GL10 gl, float x, float z, int data)
  {
    Vector nextPosition = new Vector(directions[player.direction].x, 0.0f, directions[player.direction].z);
    int nextX = (int)((player.position.x + (nextPosition.x)));
    int nextZ = (int)((player.position.z + (nextPosition.z)));

    boolean drawForward = maze == null ? false : maze.isPositionInBounds(nextX, nextZ) && (maze.isPositionFree(nextX, nextZ) || maze.isPositionGate(nextX, nextZ) || maze.isPositionGoal(nextX, nextZ));

    nextX = (int)((player.position.x - (nextPosition.x)));
    nextZ = (int)((player.position.z - (nextPosition.z)));

    boolean drawBackward = maze == null ? false : maze.isPositionInBounds(nextX, nextZ) && maze.isPositionFree(nextX, nextZ);

    if (directions[player.direction].z != 0) {
      nextX = (int)((player.position.x + (nextPosition.z)));
      nextZ = (int)((player.position.z + (nextPosition.x)));
    }
    else {
      nextX = (int)((player.position.x - (nextPosition.z)));
      nextZ = (int)((player.position.z - (nextPosition.x)));
    }

    boolean drawLeft = maze == null ? false : maze.isPositionInBounds(nextX, nextZ) && (maze.isPositionFree(nextX, nextZ) || maze.isPositionGate(nextX, nextZ) || maze.isPositionGoal(nextX, nextZ));

    if (directions[player.direction].z != 0) {
      nextX = (int)((player.position.x - (nextPosition.z)));
      nextZ = (int)((player.position.z - (nextPosition.x)));
    }
    else {
      nextX = (int)((player.position.x + (nextPosition.z)));
      nextZ = (int)((player.position.z + (nextPosition.x)));
    }

    boolean drawRight = maze == null ? false : maze.isPositionInBounds(nextX, nextZ) && (maze.isPositionFree(nextX, nextZ) || maze.isPositionGate(nextX, nextZ) || maze.isPositionGoal(nextX, nextZ));

    nextX = (int)((player.position.x + (nextPosition.x)));
    nextZ = (int)((player.position.z + (nextPosition.z)));

    boolean gateAhead = maze == null ? false : maze.isPositionGate(nextX, nextZ);
    boolean goalAhead = maze == null ? false : maze.isPositionGoal(nextX, nextZ);

    graphicsUtil.drawControlsArrow(x, z, data, drawForward, drawBackward, drawLeft, drawRight, gateAhead, goalAhead, currentMode, DebugInformation.isDebugEnabled, aerialControl, debug);
  }

  public void registerQuestionAnswerNotify(QuestionAnswerNotify listener) {
    this.listener = listener;
  }

  public void resetTimer() {
    totalTime = 0.0f;
  }

  public void saveMaze() {
    restart();
    if (maze != null) {
      maze.resetMaze();
      maze.exportMazeExternal("saved_maze.ocsmaze", context);
    }
  }

  public void saveMazeInternal() {
    restart();
    if (maze != null) {
      maze.resetMaze();
      maze.exportMazeInternal("new_maze.ocsmaze", context);
    }
  }

  public void addMathQuestions() {
    if (maze != null) {
      maze.addMathQuestions();
    }
  }

  public void determineForwardMove(int buttonNumber) {
    Vector nextPosition = new Vector(directions[player.direction].x, 0.0f, directions[player.direction].z);
    int nextX = (int)((player.position.x + (nextPosition.x)));
    int nextZ = (int)((player.position.z + (nextPosition.z)));

    boolean gateAhead = maze == null ? false : maze.isPositionGate(nextX, nextZ);
    boolean goalAhead = maze == null ? false : maze.isPositionGoal(nextX, nextZ);

    if ((gateAhead || goalAhead) && buttonNumber == 1) {
      commands.add("MoveForward");
    }
    else if ((!gateAhead && !goalAhead) && buttonNumber == 0) {
      commands.add("MoveForward");
    }
  }
}
