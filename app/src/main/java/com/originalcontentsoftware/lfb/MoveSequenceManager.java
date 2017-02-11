package com.originalcontentsoftware.lfb;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class MoveSequenceManager {
  public enum Sequence {
    TOGGLE_DEBUG(0),
    DIZZY_LEFT(1),
    DIZZY_RIGHT(2);

    Sequence(int sequenceNumber) {
      this.sequenceNumber = sequenceNumber;
    }

    public int sequenceNumber;
  }

  public class MoveSequence {
    private List<String> moveSequence = new ArrayList<String>();
    private int moveSequenceNumber = 0;
    private Sequence sequence;

    public MoveSequence(Sequence sequence) {
      this.sequence = sequence;
    }

    public void add(String command) {
      moveSequence.add(command);
    }

    public void resetSequenceNumber() {
      moveSequenceNumber = 0;
    }

    public boolean testMove(String command) {
      if (command.equals(moveSequence.get(moveSequenceNumber))) {
        moveSequenceNumber++;
        //Log.v("LFB", "Match in sequence (" + moveSequenceNumber + ")");

        if (moveSequenceNumber == moveSequence.size()) {
          Log.v("LFB", "Sequence complete (" + sequence + ")");
          moveSequenceNumber = 0;
          return true;
        }
      }
      else if (command.equals(moveSequence.get(0))){
        moveSequenceNumber = 1;
        //Log.v("LFB", "Match in sequence (" + moveSequenceNumber + ")");
      }
      else {
        moveSequenceNumber = 0;
        //Log.v("LFB", "No Match in sequence (" + moveSequenceNumber + ")");
      }

      return false;
    }
  }

  public List <MoveSequence> moveSequences = new ArrayList<MoveSequence>();

  public void resetSequenceNumbers() {
    for (MoveSequence ms : moveSequences) {
      ms.resetSequenceNumber();
    }
  }

  public List<Sequence> findMatches(String command) {
    List <Sequence> matchSequences = new ArrayList<Sequence>();

    for (MoveSequence ms : moveSequences) {
      if (ms.testMove(command)) {
        matchSequences.add(ms.sequence);
      }
    }

    return matchSequences;
  }

  void addToggleDebugSequence() {
    // used right now for toggle of debug mode
    MoveSequence moveSequence = new MoveSequence(Sequence.TOGGLE_DEBUG);
    moveSequence.add("MoveForward");
    moveSequence.add("Up");
    moveSequence.add("MoveForward");
    moveSequence.add("Up");
    moveSequence.add("MoveBackward");
    moveSequence.add("Up");
    moveSequence.add("MoveBackward");
    moveSequence.add("Up");
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequence.add("ChangeView");
    moveSequence.add("Up");
    moveSequence.add("ChangeView");
    moveSequence.add("Up");
    moveSequences.add(moveSequence);
  }

  void addDizzyLeftDequence() {
    MoveSequence moveSequence = new MoveSequence(Sequence.DIZZY_LEFT);
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequence.add("RotLeft");
    moveSequence.add("Up");
    moveSequences.add(moveSequence);
  }

  void addDizzyRightDequence() {
    MoveSequence moveSequence = new MoveSequence(Sequence.DIZZY_RIGHT);
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequence.add("RotRight");
    moveSequence.add("Up");
    moveSequences.add(moveSequence);
  }
}
