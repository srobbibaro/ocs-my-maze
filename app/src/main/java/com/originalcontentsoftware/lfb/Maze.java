package com.originalcontentsoftware.lfb;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Maze {
  private List<MazeQuestionGate> questionGates = new ArrayList<MazeQuestionGate>();

  private int maze[][];

  // TODO: temporary hack...
  private int maze_backup[][];

  private MazeBlock mazeData[][];

  private int length;
  private int width;

  private GraphicsUtil graphicsUtil;

  private Vector playerStartPosition;

  private boolean isComplete = false;

  private String createdBy = "Original Content Software";
  private String mazeName = "Demo Maze";

  private final String fileType = "ocm";
  private String version = "0.1.3";

  private int numBitmaps = 0;

  public int getNumBitmaps() {
    return numBitmaps;
  }

  public void calcNumBitmaps() {
    numBitmaps = 0;
    for (MazeQuestionGate mqg : questionGates) {
      if (mqg.isPicture()) {
        numBitmaps++;
      }
    }
  }

  public void setNumBitmaps(int numBitmaps) {
    this.numBitmaps = numBitmaps;
  }

  public int getNextBitmapNumber() {
    int nextBitmapNumber = 0;
    for (MazeQuestionGate mqg : questionGates) {
      if (mqg.isPicture()) {
        String patternStr="^pic(\\d+)\\.png$";
        Pattern p = Pattern.compile(patternStr);
        Matcher m = p.matcher(mqg.getPicturePath());
        if(m.find()){
          int foundInt = Integer.parseInt(m.group(1));
          Log.v("LFB", "Found=" + m.group(1));
          if ( foundInt > nextBitmapNumber) {
            nextBitmapNumber = Integer.parseInt(m.group(1));
          }
        }
      }
    }

    nextBitmapNumber += 1;
    nextBitmapNumber %= 256;
    Log.v("LFB", "Next bitmap number = " + nextBitmapNumber);
    return nextBitmapNumber;
  }

  public int getBitmapNumber(int i) {
    MazeQuestionGate mqg = questionGates.get(i);
    if (mqg.isPicture()) {
      String patternStr="^pic(\\d+)\\.png$";
      Pattern p = Pattern.compile(patternStr);
      Matcher m = p.matcher(mqg.getPicturePath());
      if(m.find()){
        Log.v("LFB", "Found=" + m.group(1));
        return Integer.parseInt(m.group(1));
      }
    }
    return 0;
  }

  private double gateLat = 40.4299;
  private double gateLon = -79.7951;

  private double lastLon = 0.0;
  private double lastLat = 0.0;

  final private Random random = new Random();

  public String getCreatedBy() {
    return createdBy;
  }

  public String getMazeName() {
    return mazeName;
  }

  public boolean buildMazeFromInts(int maze[][], int length, int width) {
    mazeData = new MazeBlock[length][width];
    for (int x = 0; x < width; x++) {
      for (int i = 0; i < length; i++) {
        int data = maze[x][i];

        switch (data) {
        case 1:
        case 2:
          //mazeData[x][i] = new MazeWall();
          //break;
        case 7:
          // TODO: Questions are not >= 20
          // TODO: Need to specify real questions here in some way!
          //mazeData[x][i] = new MazeQuestionGate("blah", new String[] {"hey", "what", "where","who"}, 1, 0);
          //break;
        case 0:
        case 3:
        case 4:
        case 5:
        case 6:
        default:
          mazeData[x][i] = new MazeBlock();
        }
      }
    }
    return true;
  }

  public Maze(GraphicsUtil gu) {
    this.graphicsUtil = gu;
    mazeName = "New Maze";
  }

  public Maze() {
    graphicsUtil = null;
    mazeName = "New Maze";
  }

  public boolean loadMaze(String fileName, Context context, String location, Uri mazeUri) {
    boolean retValue = false;

    Log.v("LFB", "Location = " + location);

    if (location.equals("internal")) {
      retValue = importMazeInternal(fileName, context);
    }
    else if (location.equals("external")) {
      retValue = importMazeExternal(fileName, context);
    }
    else if (location.equals("uri")) {
      retValue = importMazeUri(mazeUri, context);
    }
    else {
      retValue = importMazeResource(fileName, context);
    }

    playerStartPosition = new Vector();

    if (retValue) {
      findPlayerStartPosition();
      maze_backup = copyMaze(maze);
    }

    return retValue;
  }

  public Maze(GraphicsUtil gu, String fileName, Context context, String location, Uri mazeUri) {
    this.graphicsUtil = gu;
    loadMaze(fileName, context, location, mazeUri);
  }

  public boolean importMazeExternal(String fileName, Context context) {
    boolean retVal = false;

    try {
      /*final File file = new File(Environment.getExternalStorageDirectory()
          .getAbsolutePath(), fileName);
       */
      final File file = new File(fileName);
      FileInputStream fstream = new FileInputStream(file);

      retVal = loadFromFile(fstream, context);
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open file!");
    }

    return retVal;
  }

  public boolean importMazeInternal(String fileName, Context context) {
    boolean retVal = false;

    try {
      InputStream fstream = context.openFileInput(fileName);
      retVal = loadFromFile(fstream, context);
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open file!");
    }

    return retVal;
  }

  public boolean importMazeUri(Uri mazeUri, Context context) {
    boolean retVal = false;

    try {
      Log.v("LFB", "Loading from Uri=" + mazeUri);
      InputStream fstream = context.getContentResolver().openInputStream(mazeUri);
      retVal = loadFromFile(fstream, context);
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open file! (" + e + ")");
    }

    return retVal;
  }

  public boolean importMazeResource(String fileName, Context context) {
    boolean retVal = false;

    try {
      InputStream fstream = context.getResources().getAssets().open(fileName);
      retVal = loadFromFile(fstream, context);
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open file!");
    }

    return retVal;
  }

  public boolean loadFromFile(InputStream fstream, Context context) {
    boolean retVal = false;

    try {
      byte data[] = new byte[128];

      ByteArrayInputStream stream = new ByteArrayInputStream(data);
      DataInputStream in = new DataInputStream(stream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));

      char[] buffer = new char[3];
      fstream.read(data, 0, 3);
      br.read(buffer, 0, 3);
      String readFileType = String.valueOf(buffer);
      Log.v("LFB", "File Type=" + readFileType);

      if (!readFileType.equals(fileType)) {
        throw new Exception("Wrong file type!");
      }

      fstream.read(data, 0, 1);
      int versionLength = data[0];
      Log.v("LFB", "Version Length=" + versionLength);

      stream = new ByteArrayInputStream(data);
      in = new DataInputStream(stream);
      br = new BufferedReader(new InputStreamReader(in));

      buffer = new char[versionLength];
      fstream.read(data, 0, versionLength);
      br.read(buffer, 0, versionLength);
      String readVersion = String.valueOf(buffer);
      Log.v("LFB", "Version=" + readVersion);

      if (!readVersion.equals(version)) {
        throw new Exception("Cannot load this version (" + readVersion + ")");
      }

      fstream.read(data, 0, 1);
      int nameLength = data[0];
      Log.v("LFB", "Name Length=" + nameLength);

      stream = new ByteArrayInputStream(data);
      in = new DataInputStream(stream);
      br = new BufferedReader(new InputStreamReader(in));

      buffer = new char[nameLength];
      fstream.read(data, 0, nameLength);
      br.read(buffer, 0, nameLength);
      mazeName = String.valueOf(buffer);
      Log.v("LFB", "Name=" + mazeName);

      fstream.read(data, 0, 1);
      int createdByLength = data[0];
      Log.v("LFB", "Created By Length=" + createdByLength);

      stream = new ByteArrayInputStream(data);
      in = new DataInputStream(stream);
      br = new BufferedReader(new InputStreamReader(in));

      buffer = new char[createdByLength];
      fstream.read(data, 0, createdByLength);
      br.read(buffer, 0, createdByLength);
      createdBy = String.valueOf(buffer);
      Log.v("LFB", "Created By=" + createdBy);

      fstream.read(data, 0, 2);
      length = data[0];
      width = data[1];
      Log.v("LFB", "Length=" + length);
      Log.v("LFB", "Width=" + width);

      maze = new int[length][width];

      for (int x = 0; x < width; x++) {
        for (int i = 0; i < length; i++) {
          fstream.read(data, 0, 1);
          maze[x][i] = data[0];
        }
      }

      fstream.read(data, 0, 1);
      int numQuestions = data[0];
      Log.v("LFB", "Num Questions = " + numQuestions);

      String questionData[] = new String[6];

      for (int i = 0; i < numQuestions; i++) {
        Log.v("LFB", "Reading question (" + i + ")");
        for (int j = 0; j < 6; j++) {
          fstream.read(data, 0, 1);
          int questionLength = data[0];
          Log.v("LFB", "Question Length (" + j + ")=" + questionLength);

          stream = new ByteArrayInputStream(data);
          in = new DataInputStream(stream);
          br = new BufferedReader(new InputStreamReader(in));

          buffer = new char[questionLength];
          fstream.read(data, 0, questionLength);
          br.read(buffer, 0, questionLength);
          questionData[j] = String.valueOf(buffer);
          Log.v("LFB", "Question=" + questionData[j]);
        }

        Log.v("LFB", "Done with strings");

        fstream.read(data, 0, 2);
        int correctAnswer = data[0];
        int incorrectAnswerBehavior = data[1];
        Log.v("LFB", "Correct Answer=" + correctAnswer);
        Log.v("LFB", "Incorrect Answer Behavior=" + incorrectAnswerBehavior);

        MazeQuestionGate questionGate = new MazeQuestionGate(questionData[0], new String[] {questionData[1], questionData[2], questionData[3], questionData[4]}, correctAnswer, incorrectAnswerBehavior, questionData[5]);
        questionGates.add(questionGate);
        Log.v("LFB", "Show Picture=" + questionGate.isPicture());
      }

      fstream.read(data, 0, 1);
      numBitmaps = data[0];
      Log.v("LFB", "Num Bitmaps = " + numBitmaps);

      for (int i = 0; i < numBitmaps; i++) {
        fstream.read(data, 0, 1);
        int lengthLength = data[0];
        Log.v("LFB", "Length Length=" + lengthLength);

        stream = new ByteArrayInputStream(data);
        in = new DataInputStream(stream);
        br = new BufferedReader(new InputStreamReader(in));

        buffer = new char[lengthLength];
        fstream.read(data, 0, lengthLength);
        br.read(buffer, 0, lengthLength);
        String lengthStr = String.valueOf(buffer);
        int dataLength = Integer.parseInt(lengthStr);
        Log.v("LFB", "Data length=" + dataLength);

        data = new byte[dataLength];
        fstream.read(data, 0, dataLength);

        int bitmapNumber = fstream.read();

        Log.v("LFB", "Bitmap Number" + bitmapNumber);

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        FileOutputStream fout = context.openFileOutput("pic" + bitmapNumber + ".png", context.MODE_PRIVATE);
        if (bitmap != null) {
          bitmap.compress(Bitmap.CompressFormat.PNG, 90, fout);
        }
        else {
          Log.v("LFB", "Bitmap should not be null here...");
        }
        fout.close();
      }

      in.close();

      retVal = true;
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open file! " + e.getMessage());
    }

    buildMazeFromInts(maze, length, width);

    return retVal;
  }

  public String generateHeader(Context context) {
    // TODO: hack -- we'd want to store the difficulty in the maze file!
    String message = context.getResources().getString(R.string.load_maze_file_name) + "\n" + // + " " + fileName + "\n" +
        context.getResources().getString(R.string.load_maze_name) + " " + mazeName + "\n" +
        context.getResources().getString(R.string.load_maze_created_by) + " " + getCreatedBy() + "\n" +
        context.getResources().getString(R.string.load_maze_difficulty) + " " + buildDifficultyString(context) + "\n" +
        context.getResources().getString(R.string.load_maze_number_gates) + " " + getNumberOfQuestions();

    return message;
  }

  public String buildMazeNameWithDimensions() {
    return mazeName + " (" + Integer.toString(length) + "x" + Integer.toString(width) + ")";
  }

  public String buildDifficultyString(Context context) {
    if (length >= 11 && length < 21) {
      return context.getResources().getString(R.string.make_maze_size_small);
    }
    else if (length >= 41) {
      return context.getResources().getString(R.string.make_maze_size_large);
    }
    else {
      return context.getResources().getString(R.string.make_maze_size_medium);
    }
  }

  public boolean exportMazeExternal(String fileName, Context context) {
    boolean retVal = false;

    final File file = new File(Environment.getExternalStorageDirectory()
        .getAbsolutePath(), fileName);

    try {
      FileOutputStream fstream = new FileOutputStream(file);

      retVal = writeToFile(fstream, context);
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open file '" + fileName + "' for write!" + e.getMessage());
    }

    return retVal;
  }

  public boolean exportMazeInternal(String fileName, Context context) {
    boolean retVal = false;

    try {
      FileOutputStream fstream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

      retVal = writeToFile(fstream, context);
    }
    catch (Exception e) {
      Log.v("LFB", "Could not open file '" + fileName + "' for write!" + e.getMessage());
    }

    return retVal;
  }

  public boolean removeMazeExternal(String fileName) {
    final File fileToDelete = new File(Environment.getExternalStorageDirectory()
        .getAbsolutePath(), fileName);
    fileToDelete.delete();

    return true;
  }

  public boolean writeToFile(FileOutputStream fstream, Context context) {
    boolean retVal = false;

    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      DataOutputStream in = new DataOutputStream(stream);
      BufferedWriter br = new BufferedWriter(new OutputStreamWriter(in));

      mazeName = mazeName.replaceAll("\n",  "");
      createdBy = createdBy.replaceAll("\n", "");

      br.write(fileType);

      br.write(version.length());
      br.write(version);

      br.write(mazeName.length());
      br.write(mazeName);

      br.write(createdBy.length());
      br.write(createdBy);

      br.write(length);
      br.write(width);

      for (int x = 0; x < width; x++) {
        for (int i = 0; i < length; i++) {
          int val = maze[x][i];
          br.write(val);
        }
      }

      br.write(questionGates.size());

      List<Integer> bitmapNumbers =  new ArrayList<Integer>();

      for (int i = 0; i < questionGates.size(); i++) {
        String question = questionGates.get(i).getQuestion();
        question = question.replaceAll("\n", "");
        String answers[] = questionGates.get(i).getAnswers();
        for (int u = 0; u < answers.length; u++) {
          answers[u] = answers[u].replaceAll("\n", "");
        }
        br.write(question.length());
        br.write(question);

        for (int j = 0; j < 4; j++) {
          br.write(answers[j].length());
          br.write(answers[j]);
        }

        String picturePath = questionGates.get(i).isPicture() ? questionGates.get(i).getPicturePath() : "no_picture";

        if (questionGates.get(i).isPicture()) {
          Log.v("LFB", "adding picture number" + getBitmapNumber(i));
          bitmapNumbers.add(getBitmapNumber(i));
        }

        br.write(picturePath.length());
        br.write(picturePath);

        br.write(questionGates.get(i).getCorrectAnswerNumber());
        br.write(questionGates.get(i).getIncorrectBehavior());
      }

      calcNumBitmaps();

      br.write(numBitmaps);
      br.flush();

      byte data[] = stream.toByteArray();
      Log.v("LFB", "Writing " + data.length + " bytes to file!");
      fstream.write(data, 0, data.length);

      for (int i = 0; i < numBitmaps; i++) {
        int bitmapNumber = bitmapNumbers.get(i);
        String fileName = "pic" + bitmapNumber + ".png";
        FileInputStream fstream2 = context.openFileInput(fileName);
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fstream2.getFD());
        stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,  90, stream);
        data = stream.toByteArray();

        String lengthStr = String.valueOf(data.length);

        stream = new ByteArrayOutputStream();
        in = new DataOutputStream(stream);
        br = new BufferedWriter(new OutputStreamWriter(in));

        br.write(lengthStr.length());
        br.write(lengthStr);
        br.flush();

        byte data2[] = stream.toByteArray();
        fstream.write(data2, 0, data2.length);

        fstream.write(data, 0, data.length);
        Log.v("LFB", "Writing " + data.length + "bytes to file!");

        Log.v("LFB", "Bitmap Number" + bitmapNumber);
        fstream.write(bitmapNumber);
      }

      fstream.flush();
      fstream.close();

      in.close();

      retVal = true;
    }
    catch (Exception e) {
      Log.v("LFB", "Error writing maze to file: " + e);
    }

    return retVal;
  }

  public void setComplete() {
    isComplete = true;
  }

  public boolean isComplete() {
    return isComplete;
  }

  public int[][] copyMaze(int maze2[][]) {
    int length = maze2.length;
    int width = maze2.length;

    int maze1[][] = new int[length][width];
    for (int z = 0; z < length; z++) {
      for (int x = 0; x < width; x++) {
        maze1[z][x] = maze2[z][x];
      }
    }

    return maze1;
  }

  public boolean createBareMaze(int tlength, int twidth) {
    this.length = random.nextInt(50) + 10;
    this.width = length;

    maze = new int[length][width];

    for (int z = 0; z < length; z++) {
      for (int x = 0; x < width; x++) {
        maze[z][x] = 0;
      }
    }

    int plrStartX = random.nextInt(length);
    int plrStartZ = random.nextInt(width);

    playerStartPosition = new Vector();
    playerStartPosition.setVector(plrStartX, 0.0f, plrStartZ);

    maze[plrStartX][plrStartZ] = 3;

    int goalX = random.nextInt(length);
    int goalZ = random.nextInt(width);

    maze[goalX][goalZ] = 4;

    buildMazeFromInts(maze, length, width);
    maze_backup = copyMaze(maze);

    return true;
  }

  Vector getPlayerStartPosition() {
    return playerStartPosition;
  }

  void findPlayerStartPosition() {
    for (int z = 0; z < length; z++) {
      for (int x = 0; x < width; x++) {
        if (maze[z][x] == 3) {
          Log.v("LFB", "Found x=" + x + " z=" +z);
          playerStartPosition.setVector(x, 0.0f, z);
        }
      }
    }
  }

  void resetMaze() {
    maze = copyMaze(maze_backup);
    resetQuestionGates();
    isComplete = false;
  }

  public int numGates() {
    return getNumberOfQuestions();
  }

  public int calcNumberGatesMissed() {
    int numQuestions = questionGates.size();
    int numMissed = 0;
    for (int i = 0; i < numQuestions; i++) {
      numMissed += questionGates.get(i).numWrongAnswers;
    }

    return numMissed;
  }

  public int calcNumberGatesCleared() {
    int numQuestions = questionGates.size();
    int numCleared = 0;
    for (int i = 0; i < numQuestions; i++) {
      if (questionGates.get(i).isCleared()) {
        numCleared++;
      }
    }

    return numCleared;
  }

  public float visitPercentage() {
    int numSpaces = 0;
    int numVisited = 0;
    for (int z = 0; z < length; z++) {
      for (int x = 0; x < width; x++) {
        if (maze[z][x] == 6) {
          numVisited++;
          numSpaces++;
        }
        else if (maze[z][x] == 0 || maze[z][x] >= 20 || maze[z][x] == 3) {
          numSpaces++;
        }
      }
    }

    return (float)(numVisited) / (float)(numSpaces);
  }

  class store {
    int x;
    int z;
    int path_len;

    store(int x, int z, int path_len) {
      this.x = x;
      this.z = z;
      this.path_len = path_len;
    }
  }

  public boolean generateMaze(int tlength, int twidth) {
    this.length = random.nextInt(6) + tlength;

    // we need odd numbers for maze sizes
    if (this.length % 2 == 0) {
      this.length -= 1;
    }

    this.width = length;

    maze = new int[length][width];

    // fill the maze with walls to start
    for (int z = 0; z < length; z++) {
      for (int x = 0; x < width; x++) {
        if (z == 0 || z == length - 1 || x == 0 || x == width - 1) {
          maze[z][x] = 2;
        }
        else {
          maze[z][x] = 1;
        }
      }
    }

    // the player should not start on a border
    int plrStartX = random.nextInt(length - 2) + 1;
    int plrStartZ = random.nextInt(width - 2) + 1;

    // the player's start coordinates should be even
    if (plrStartX % 2 == 0) {
      if (plrStartX - 1 > 0) {
        plrStartX -= 1;
      }
      else {
        plrStartX += 1;
      }
    }

    if (plrStartZ % 2 == 0) {
      if (plrStartZ - 1 > 0) {
        plrStartZ -= 1;
      }
      else {
        plrStartZ += 1;
      }
    }

    playerStartPosition = new Vector();
    playerStartPosition.setVector(plrStartX, 0.0f, plrStartZ);

    maze[plrStartZ][plrStartX] = 3;

    int curX = plrStartX;
    int curZ = plrStartZ;

    boolean quit = false;
    int[] directions = new int[4];
    for (int i = 0; i < 4; i++) {
      directions[i] = 0;
    }

    boolean addGate = false;
    int len = 0;

    store longest = new store(curX, curZ, len);

    List<store> prev = new ArrayList<store>();
    int direction = random.nextInt(4);

    prev.add(new store(curX, curZ, len));

    int add = 2;

    int numQuestionsToAdd = (length - 2) * (width - 2) - 2;
    int questionNumber = 20;

    if (questionGates.size() < numQuestionsToAdd) {
      numQuestionsToAdd = questionGates.size();
    }

    if (numQuestionsToAdd > 30) {
      numQuestionsToAdd = 30;
    }

    List<Integer> skippedQuestionNums = new ArrayList<Integer>();

    Log.v("LFB", "Num Questions to Add = " + numQuestionsToAdd);

    while (!quit) {
      directions[direction] = 1;
      switch (direction) {
      case 0:
        if (curX + add < length - 1 && this.isPositionWall(curX + add, curZ - 1) && this.isPositionWall(curX + add, curZ) && this.isPositionWall(curX + add, curZ + 1)) {
          maze[curZ][curX + 1] = 0;
          maze[curZ][curX + add] = 0;
          if (maze[curZ][curX] == 0 && addGate && numQuestionsToAdd > 0) {
            maze[curZ][curX] = questionNumber;
            questionNumber++;
            addGate = false;
            numQuestionsToAdd--;
          }
          curX = curX + add;
          len = len + 2;
          prev.add(new store(curX, curZ, len));
          //Log.v("LFB", "Direction=" + direction + " curX = " + curX + " curZ" + curZ);
          direction = random.nextInt(4);
          for (int i = 0; i < 4; i++) {
            directions[i] = 0;
          }
        }
        else if (curX + 1 == length - 2 && this.isPositionWall(curX + 1, curZ - 1) && this.isPositionWall(curX + 1, curZ) && this.isPositionWall(curX + 1, curZ + 1)) {
          maze[curZ][curX + 1] = 0;
        }
        break;
      case 1:
        if (curX - add > 0 && this.isPositionWall(curX - add, curZ - 1) && this.isPositionWall(curX - add, curZ) && this.isPositionWall(curX - add, curZ + 1)) {
          maze[curZ][curX - 1] = 0;
          maze[curZ][curX - add] = 0;
          if (maze[curZ][curX] == 0 && addGate && numQuestionsToAdd > 0) {
            maze[curZ][curX] = questionNumber;
            questionNumber++;
            addGate = false;
            numQuestionsToAdd--;
          }
          curX = curX - add;
          len = len + 2;
          prev.add(new store(curX, curZ, len));
          direction = random.nextInt(4);
          //Log.v("LFB", "Direction=" + direction + " curX = " + curX + " curZ" + curZ);
          for (int i = 0; i < 4; i++) {
            directions[i] = 0;
          }
        }
        else if (curX - 1 == 1 && this.isPositionWall(curX - 1, curZ - 1) && this.isPositionWall(curX - 1, curZ) && this.isPositionWall(curX - 1, curZ + 1)) {
          maze[curZ][curX - 1] = 0;
        }
        break;
      case 2:
        if (curZ + add < length - 1 && this.isPositionWall(curX - 1, curZ + add) && this.isPositionWall(curX + 1, curZ + add) && this.isPositionWall(curX, curZ + add)) {
          maze[curZ + 1][curX] = 0;
          maze[curZ + add][curX] = 0;
          if (maze[curZ][curX] == 0 && addGate && numQuestionsToAdd > 0) {
            maze[curZ][curX] = questionNumber;
            questionNumber++;
            addGate = false;
            numQuestionsToAdd--;
          }
          curZ = curZ + add;
          len = len + 2;
          prev.add(new store(curX, curZ, len));
          //Log.v("LFB", "Direction=" + direction + " curX = " + curX + " curZ" + curZ);
          direction = random.nextInt(4);
          for (int i = 0; i < 4; i++) {
            directions[i] = 0;
          }
        }
        else if (curZ + 1 == length - 2 && this.isPositionWall(curX - 1, curZ + 1) && this.isPositionWall(curX + 1, curZ + 1) && this.isPositionWall(curX, curZ + 1)) {
          maze[curZ + 1][curX] = 0;
        }
        break;
      case 3:
        if (curZ - add > 0 && this.isPositionWall(curX - 1, curZ - add) && this.isPositionWall(curX + 1, curZ - add) && this.isPositionWall(curX, curZ - add)) {
          maze[curZ - 1][curX] = 0;
          maze[curZ - add][curX] = 0;
          if (maze[curZ][curX] == 0 && addGate && numQuestionsToAdd > 0) {
            maze[curZ][curX] = questionNumber;
            questionNumber++;
            addGate = false;
            numQuestionsToAdd--;
          }
          curZ = curZ - add;
          len = len + 2;
          prev.add(new store(curX, curZ, len));
          direction = random.nextInt(4);
          //Log.v("LFB", "Direction=" + direction + " curX = " + curX + " curZ" + curZ);
          for (int i = 0; i < 4; i++) {
            directions[i] = 0;
          }
        }
        else if (curZ - 1 == 1 && this.isPositionWall(curX - 1, curZ - 1) && this.isPositionWall(curX + 1, curZ - 1) && this.isPositionWall(curX, curZ - 1)) {
          maze[curZ - 1][curX] = 0;
          addGate = false;
        }
        break;
      default:
      }

      int misses = 0;

      for (int i = 0; i < 4; i++) {
        if (directions[i] == 1) {
          misses++;
        }
      }

      if (misses >= 4) {
        if (prev.size() == 0) {
          quit = true;
        }
        else if ( prev.size() > 0 ) {
          //Log.v("LFB", "size = " + prev.size());
          addGate = true;
          if (len > longest.path_len) {
            longest = new store(curX, curZ, len);
          }

          while (numQuestionsToAdd > 0) {
            MazeQuestionGate temp = questionGates.get(questionNumber - 20);
            if (temp.getIncorrectBehavior() == 2) {
              Log.v("LFB", "Skipping question number (" + questionNumber + "...");
              skippedQuestionNums.add(questionNumber);
              questionNumber++;
              numQuestionsToAdd--;
            }
            else {
              break;
            }
          }

          store temp = prev.get(prev.size() - 1);
          curX = temp.x;
          curZ = temp.z;
          len = temp.path_len;
          prev.remove(prev.size() - 1);
          for (int i = 0; i < 4; i++) {
            directions[i] = 0;
          }
        }
      }
      else if (misses > 0) {
        direction = random.nextInt(4);
      }
    }

    maze[longest.z][longest.x] = 4;

    while (numQuestionsToAdd > 0) {
      skippedQuestionNums.add(questionNumber);
      questionNumber++;
      numQuestionsToAdd--;
    }

    int i = 0;
    while (i < skippedQuestionNums.size()) {
      Log.v("LFB", "Adding extra question..." + numQuestionsToAdd);
      int testX = random.nextInt(length - 2) + 1;
      int testZ = random.nextInt(width - 2) + 1;
      if (maze[testZ][testX] == 1 &&
          ((maze[testZ + 1][testX] == 1 && maze[testZ - 1][testX] == 1) ||
              (maze[testZ][testX + 1] == 1 && maze[testZ][testX - 1] == 1))
          ) {
        maze[testZ][testX] = skippedQuestionNums.get(i);
        i++;
      }
    }

    buildMazeFromInts(maze, length, width);
    maze_backup = copyMaze(maze);

    return true;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public void setMazeName(String mazeName) {
    this.mazeName = mazeName;
  }

  public boolean isPositionInBounds(int x, int z) {
    return x >= 0 && x < width && z >= 0 && z < length;
  }

  public boolean isPositionFree(int x, int z) {
    return isPositionInBounds(z,x) && (maze[z][x] == 0 || maze[z][x] == 6 || maze[z][x] == 3);
  }

  public boolean isPositionWall(int x, int z) {
    return isPositionInBounds(z,x) && (maze[z][x] == 1 || maze[z][x] == 2);
  }

  public boolean isPositionVisited(int x, int z) {
    return isPositionInBounds(z, x) && maze[z][x] == 6;
  }

  public boolean isPositionGate(int x, int z) {
    return isPositionInBounds(z,x) && maze[z][x] >= 20;
  }

  public boolean isPositionGoal(int x, int z) {
    return isPositionInBounds(z,x) && maze[z][x] == 4;
  }

  public void clearGate(Vector playerPosition) {
    if (isPositionInBounds((int)playerPosition.x, (int)playerPosition.z)) {
      maze[(int)playerPosition.z][(int)playerPosition.x] = 0;
    }
  }

  public void draw(Vector playerDir, Vector playerPos, MazePlayMode mode) {
    // draw the level cell by cell -- this can certainly be done in a smarter way. We don't want to process and
    // draw objects which are not in view.
    if (graphicsUtil == null) {
      return;
    }

    int startI = 0;
    int startU = 0;
    int endI = width;
    int endU = length;

    if ( mode == MazePlayMode.NORMAL ) {
      if (playerDir.x > 0) {
        startU = (int)playerPos.x;
      }
      else if (playerDir.x < 0) {
        endU  = (int)playerPos.x + 1;
      }
      if (playerDir.z > 0) {
        startI = (int)playerPos.z;
      }
      else if (playerDir.z < 0) {
        endI = (int)playerPos.z + 1;
      }
    }

    /*
    Log.v("LFB", "start i = " + startI + " endI = " + endI);
    Log.v("LFB", "start u = " + startU + " endU = " + endU);
     */

    for (int i = startI; i < endI; i++) {
      for (int u = startU; u < endU; u++) {
        if (maze[i][u] == 6) {
          graphicsUtil.drawGround((float)u, (float)i, true, mazeData[i][u].getDirectionEntered());
        }
        else if (maze[i][u] == 0) {
          graphicsUtil.drawGround((float)u, (float)i, false, playerDir);
        }
        else if (maze[i][u] == 3) {
          graphicsUtil.drawGround((float)u, (float)i, false, playerDir);
        }
        else if (maze[i][u] != 5) {
          if (graphicsUtil != null)
            graphicsUtil.drawBox((float)u, (float)i, maze[i][u], playerDir, mode);
        }
      }
    }
  }

  public void setPosition(int x, int z, int value) {
    maze[z][x] = value;
  }

  public void setVisited(int x, int z, Vector directionEntered) {
    mazeData[z][x].setDirectionEntered(directionEntered);
    mazeData[z][x].setVisited(true);
    maze[z][x] = 6;
  }

  public MazeQuestionGate getRandomQuestionGate()
  {
    int numQuestions = questionGates.size();

    for (int i = 0; i < numQuestions; i++) {
      MazeQuestionGate temp = questionGates.get(i);
      if (!questionGates.get(i).isCleared()) {
        return temp;
      }
    }

    return questionGates.get(random.nextInt(numQuestions));
  }

  public MazeQuestionGate getQuestionGate(int x, int z)
  {
    return questionGates.get(maze[z][x] - 20);
  }

  public MazeQuestionGate getQuestionGate(int index)
  {
    return questionGates.get(index);
  }

  public void resetQuestionGates() {
    int numQuestions = questionGates.size();

    for (int i = 0; i < numQuestions; i++) {
      questionGates.get(i).isCleared = false;
    }
  }

  public List<MazeQuestionGate> getQuestionGates() {
    return questionGates;
  }

  public int calcGateClearedPercentage() {
    int numQuestions = questionGates.size();
    int numCleared = 0;
    for (int i = 0; i < numQuestions; i++) {
      if (questionGates.get(i).isCleared()) {
        numCleared++;
      }
    }

    return numCleared;
  }

  public void clearQuestions() {
    questionGates.clear();
  }

  public void addQuestion(MazeQuestionGate mazeQuestionGate) {
    questionGates.add(mazeQuestionGate);
  }

  public int getNumberOfQuestions() {
    return questionGates.size();
  }

  public Vector handleIncorrectAnswer(int incorrectBehavior) {
    Vector playerPosition = null;
    if (incorrectBehavior == 1) {
      playerPosition = new Vector();
      playerPosition.x = playerStartPosition.x;
      playerPosition.z = playerStartPosition.z;
    }

    return playerPosition;
  }

  public void addMathQuestions() {
    clearQuestions();
    questionGates.add( new MazeQuestionGate("5 x 5 = ?", new String[] {"5", "1", "0", "25"}, 3, 2));
    questionGates.add( new MazeQuestionGate("2 x 3 = ?", new String[] {"5", "9", "6", "25"}, 2, 0));
    questionGates.add( new MazeQuestionGate("15 - 7 = ?", new String[] {"7", "8", "9", "10"}, 1, 1));
    questionGates.add( new MazeQuestionGate("26 / 2 = ?", new String[] {"12", "11", "0", "13"}, 3, 3));
    questionGates.add( new MazeQuestionGate("0 x 5 = ?", new String[] {"5", "1", "0", "25"}, 2, 2));
    questionGates.add( new MazeQuestionGate("5 x 0 = ?", new String[] {"5", "1", "0", "25"}, 2, 1));
    questionGates.add( new MazeQuestionGate("5 + 5 = ?", new String[] {"5", "10", "0", "25"}, 1, 3));
    questionGates.add( new MazeQuestionGate("5 - 5 = ?", new String[] {"5", "1", "0", "25"}, 2, 0));
    questionGates.add( new MazeQuestionGate("5 / 5 = ?", new String[] {"5", "1", "0", "25"}, 1, 1));
    questionGates.add( new MazeQuestionGate("7 x 5 = ?", new String[] {"35", "30", "0", "25"}, 0, 3));
    questionGates.add( new MazeQuestionGate("7 - 5 = ?", new String[] {"35", "1", "2", "15"}, 2, 0));
    questionGates.add( new MazeQuestionGate("100 x 5 = ?", new String[] {"5", "100", "500", "250"}, 2, 3));
    questionGates.add( new MazeQuestionGate("100 / 5 = ?", new String[] {"5", "20", "500", "250"}, 1, 3));
    questionGates.add( new MazeQuestionGate("100 + 5 = ?", new String[] {"5", "105", "500", "250"}, 1, 3));
    questionGates.add( new MazeQuestionGate("100 - 5 = ?", new String[] {"95", "100", "500", "250"}, 0, 3));
  }
}
