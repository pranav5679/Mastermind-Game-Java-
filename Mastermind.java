import javalib.funworld.*;
import javalib.worldimages.*;
import tester.*;
import java.awt.Color;
import java.util.Random;

// represents a configuration for a game of Mastermind
class Mastermind {
  boolean duplicates;
  int length;
  int guesses;
  ILoColor colors;

  Mastermind(boolean duplicates, int length, int guesses, ILoColor colors) {
    this.duplicates = duplicates;
    this.length = new Utils().checkLessThanOne(length,
        "Invalid Length: " + Integer.toString(length));
    this.guesses = new Utils().checkLessThanOne(guesses,
        "Invalid Guesses: " + Integer.toString(guesses));
    this.colors = new Utils().checkColors(colors, length, duplicates);
  }

  // makes a random sequence of colors
  ILoColor makeSequence() {
    return this.makeSequenceHelper(duplicates, length, colors);
  }

  // makes a random sequence of colors
  ILoColor makeSequenceHelper(boolean dup, int len, ILoColor colors) {
    int colorIndex = new Random().nextInt(colors.length()) + 1;
    if (len == 0) {
      return new MtLoColor();
    }
    else if (dup) {
      return new ConsLoColor(colors.chooseColor(colorIndex, 1),
          this.makeSequenceHelper(dup, len - 1, colors));
    }
    else {
      return new ConsLoColor(colors.chooseColor(colorIndex, 1),
          this.makeSequenceHelper(dup, len - 1, colors.dropIndex(colorIndex, 1)));
    }
  }

  // draws the canvas of appropriate size for this Mastermind
  public WorldScene makeScene(ILoList guesses, ILoPair matches, ILoColor sequence) {
    int boxLength = (this.length * 40);
    int boxHeight = 50;
    WorldScene scene = new WorldScene(boxLength + 90,
        boxHeight + 40 + ((this.guesses * 30) + ((this.guesses + 1) * 10)));
    WorldImage rect = new RectangleImage(boxLength, boxHeight, OutlineMode.SOLID, Color.BLACK)
        .movePinholeTo(new Posn(-1 * boxLength / 2, -1 * boxHeight / 2));
    WorldScene scene2 = scene.placeImageXY(rect, 0, 0);
    int down = (this.guesses * 30) + ((this.guesses + 1) * 10) + 15 + boxHeight;
    WorldScene scene3 = this.colors.drawColors(scene2, 25, down);
    WorldScene scene4 = this.drawGrid(scene3, this.guesses, boxHeight + 25);
    WorldScene scene5 = guesses.drawGuesses(scene4, down - 40);
    return matches.drawMatches(scene5, boxLength + 25, down - 40);
  }

  // draws a grid on a given scene
  public WorldScene drawGrid(WorldScene scene, int guesses, int down) {
    if (guesses == 0) {
      return scene;
    }
    else {
      return this.drawRow(this.drawGrid(scene, guesses - 1, down + 40), 25, down, this.length);
    }
  }

  // draws a row of a grid on a given scene
  public WorldScene drawRow(WorldScene scene, int right, int down, int length) {
    if (length == 0) {
      return scene;
    }
    else {
      return this.drawRow(scene, right + 40, down, length - 1)
          .placeImageXY(new CircleImage(15, OutlineMode.OUTLINE, Color.BLACK), right, down);
    }
  }

  // modifies an empty list of list of colors based on given key
  public ILoList makeGuessesEmpty(String key, ILoList list) {
    if ("123456789".contains(key)
        && (Integer.valueOf(key) >= 1 && (Integer.valueOf(key) <= colors.length()))) {
      return new ConsLoList(new ConsLoColor(this.makeGuess(key), new MtLoColor()), new MtLoList());
    }
    else {
      return list;
    }
  }

  // modifies a non-empty list of list of colors based on given key
  public ILoList makeGuesses(String key, ILoColor first, ILoList rest) {
    if ("123456789".contains(key)
        && (Integer.valueOf(key) >= 1 && (Integer.valueOf(key) <= colors.length()))) {
      return rest.dropLast().append(new ConsLoList(this.makeGuessCons(key, first), new MtLoList()));
    }
    else if (key.equals("backspace")) {
      return rest.dropLast().append(new ConsLoList(this.deleteLast(first), new MtLoList()));
    }
    else {
      return rest;
    }
  }

  public Color makeGuess(String key) {
    return this.colors.chooseColor(Integer.valueOf(key), 1);
  }

  public ILoColor makeGuessCons(String key, ILoColor list) {
    if (list.length() == length) {
      return list;
    }
    else {
      return list.append(
          new ConsLoColor(this.colors.chooseColor(Integer.valueOf(key), 1), new MtLoColor()));
    }
  }

  public ILoColor deleteLast(ILoColor first) {
    return first.deleteLast();
  }

  public World submitGuess(ILoList guesses, ILoPair matches, ILoColor sequence, Game game) {
    return guesses.submitGuess(this.length, sequence, game, matches, guesses, this, this.colors);
  }
}

// to contain utility methods
class Utils {
  // to check if length or guesses arguments of Mastermind constructor is less
  // than one
  int checkLessThanOne(int field, String msg) {
    if (field < 1) {
      throw new IllegalArgumentException(msg);
    }
    else {
      return field;
    }
  }

  // to check whether the length of the list of colors is valid
  ILoColor checkColors(ILoColor colors, int length, boolean duplicates) { // TESTS NEEDED
    return colors.checkLength(length, duplicates);
  }
}

// to represent a list of colors
interface ILoColor {
  // to check if the length of this ILoColor is less than the given integer
  ILoColor checkLength(int length, boolean duplicates);

  // draws this list of colors onto a given scene
  WorldScene drawColors(WorldScene scene, int right, int down);

  // to determine the length of this list
  int length();

  // chooses the color at the given index
  Color chooseColor(int index, int start);

  // outputs a new list of colors for makeSequence() recursion
  ILoColor newColors(int colorIndex, boolean duplicates, int start);

  // deletes the last element of this list of Colors
  ILoColor deleteLast();

  // appends that ILoColor to this ILoColor
  ILoColor append(ILoColor that);

  // updates list of exact-inexact match pairs to contain most exact
  // and inexact matches for this list of colors
  ILoPair compareWith(ILoColor sequence, ILoPair matches, ILoColor colors);

  // computes number of exact matches this list of Colors has with the given one
  int countExact(ILoColor that);

  // computes number of exact matches this list of Colors has with the given one
  int countExactHelper(Color first, ILoColor rest);

  int countInexact(ILoColor that, ILoColor colors);

  // removes color at given Index from this list
  ILoColor dropIndex(int i, int start);

  // determines number of inexactMatches by counting each color in both lists
  int countEachColor(ILoColor list1, ILoColor list2);

  // drops exact matches in this list with the given one
  ILoColor dropExact(ILoColor that);

  // drops exact matches in this list with the given one
  ILoColor dropExactHelper(Color first, ILoColor rest);

  int countColor(Color first, ILoColor list);

  int countColorHelp(Color color);
}

// to represent an empty list of colors
class MtLoColor implements ILoColor {
  MtLoColor() {
  }

  // checks if the length of this list is valid
  public ILoColor checkLength(int length, boolean duplicates) {
    throw new IllegalArgumentException("Invalid list length");
  }

  // to determine the length of this empty list
  public int length() {
    return 0;
  }

  // chooses the color at the given index
  public Color chooseColor(int index, int start) {
    return null;
  }

  // outputs a new list of colors for makeSequence() recursion
  public ILoColor newColors(int colorIndex, boolean duplicates, int start) {
    return null;
  }

  // draws this empty list of colors onto a given scene
  public WorldScene drawColors(WorldScene scene, int right, int down) {
    return scene;
  }

  // deletes the last elements of this empty list of Colors
  public ILoColor deleteLast() {
    return this;
  }

  // append that ILoColor to this empty ILoColor
  public ILoColor append(ILoColor that) {
    return that;
  }

  //
  public ILoPair compareWith(ILoColor sequence, ILoPair matches, ILoColor colors) {
    return new ConsLoPair(new PairOfNumbers(1, 2), matches);
  }

  // counts the number of exact matches this empty list of Colors has with the
  // given one
  public int countExact(ILoColor that) {
    return 0;
  }

  // computes number of exact matches this list of Colors has with the given one
  public int countExactHelper(Color first, ILoColor rest) {
    return 0;
  }

  // removes Color at given index from this list
  public ILoColor dropIndex(int i, int start) {
    return this;
  }

  // counts the number of inexact matches between this list and the given one.
  public int countInexact(ILoColor that, ILoColor colors) {
    return 0;
  }

  @Override
  public int countEachColor(ILoColor list1, ILoColor list2) {
    return 0;
  }

  // drops exact matches in this list with the given one
  public ILoColor dropExact(ILoColor that) {
    return this;
  }

  // drops exact matches in this list with the given one
  public ILoColor dropExactHelper(Color first, ILoColor rest) {
    return this;
  }

  @Override
  public int countColor(Color first, ILoColor list) {
    return 0;
  }

  @Override
  public int countColorHelp(Color color) {
    return 0;
  }
}

// to represent a non-empty list of colors
class ConsLoColor implements ILoColor {
  Color first;
  ILoColor rest;

  ConsLoColor(Color first, ILoColor rest) {
    this.first = first;
    this.rest = rest;
  }

  // checks if the length of this list is valid
  public ILoColor checkLength(int length, boolean duplicates) {
    if (!duplicates && this.length() < length) {
      throw new IllegalArgumentException("Invalid list length");
    }
    else {
      return this;
    }
  }

  // to return the length of this non-empty list of strings
  public int length() {
    return 1 + this.rest.length();
  }

  // chooses the color at the given index
  public Color chooseColor(int index, int start) {
    if (start == index) {
      return this.first;
    }
    else {
      return this.rest.chooseColor(index, start + 1);
    }
  }

  // outputs a new list of colors for makeSequence() recursion
  public ILoColor newColors(int colorIndex, boolean duplicates, int start) {
    if (duplicates) {
      return this;
    }
    else if (start == colorIndex) {
      return this.rest;
    }
    else {
      return new ConsLoColor(this.first, this.rest.newColors(colorIndex, duplicates, start));
    }
  }

  // draws this non-empty list of colors onto a given scene
  public WorldScene drawColors(WorldScene scene, int right, int down) {
    return this.rest.drawColors(scene, right + 40, down)
        .placeImageXY(new CircleImage(15, OutlineMode.SOLID, first), right, down);
  }

  // deletes the last element of this non-empty list of Colors
  public ILoColor deleteLast() {
    if (this.rest.length() == 0) {
      return this.rest;
    }
    else {
      return new ConsLoColor(this.first, this.rest.deleteLast());
    }
  }

  // append that ILoColor to this non-empty ILoColor
  public ILoColor append(ILoColor that) {
    return new ConsLoColor(this.first, this.rest.append(that));
  }

  // counts number of exact and inexact matches between this ILoColor and the
  // given one
  public ILoPair compareWith(ILoColor sequence, ILoPair matches, ILoColor colors) {
    return new ConsLoPair(
        new PairOfNumbers(this.countExact(sequence), this.countInexact(sequence, colors)), matches);
  }

  // counts the number of exact matches this empty list of Colors has with the
  // given one
  public int countExact(ILoColor that) {
    return that.countExactHelper(this.first, this.rest);
  }

  // computes number of exact matches this list of Colors has with the given one
  public int countExactHelper(Color first, ILoColor rest) {
    if (this.first.equals(first)) {
      return 1 + this.rest.countExact(rest);
    }
    else {
      return this.rest.countExact(rest);
    }
  }

  // removes Color at given index from this non-empty list
  public ILoColor dropIndex(int i, int start) {
    if (i == start) {
      return this.rest;
    }
    else {
      return new ConsLoColor(this.first, this.rest.dropIndex(i, start + 1));
    }
  }

  @Override
  public int countInexact(ILoColor that, ILoColor colors) {
    return colors.countEachColor(this.dropExact(that), that.dropExact(this));
  }

  // computes inexact matches by counting each color.
  public int countEachColor(ILoColor list1, ILoColor list2) {
    return list1.countColor(this.first, list2) + this.rest.countEachColor(list1, list2);
  }

  @Override
  public int countEachColor(MtLoColor mtLoColor, ILoColor that) {
    // TODO Auto-generated method stub
    return 0;
  }

  // drops exact matches in this list with the given one
  public ILoColor dropExact(ILoColor that) {
    return that.dropExactHelper(this.first, this.rest);
  }

  // drops exact matches in this list with the given one
  public ILoColor dropExactHelper(Color first, ILoColor rest) {
    if (this.first.equals(first)) {
      return rest.dropExact(this);
    }
    else {
      return new ConsLoColor(first, rest.dropExact(this.rest));
    }
  }

  @Override
  public int countColor(Color first, ILoColor list) {
    return Math.min(this.countColorHelp(first), list.countColorHelp(first));
  }

  @Override
  public int countColorHelp(Color color) {
    if (this.first.equals(color)) {
      return 1 + this.rest.countColorHelp(color);
    }
    else {
      return this.rest.countColorHelp(color);
    }
  }
}

// to represent a list of list of Colors
interface ILoList {
  // draws this list of list of Colors onto a given scene
  WorldScene drawGuesses(WorldScene scene, int down);

  // modifies this list of list of Colors based on given key
  ILoList makeGuesses(String key, Mastermind m);

  // submits the most recent guess in this list
  World submitGuess(int length, ILoColor sequence, Game game, ILoPair matches, ILoList guesses,
      Mastermind m, ILoColor color);

  // appends the given list to this list
  ILoList append(ILoList that);

  // computes the length of this list
  int length();

  // returns the last list of lists in this list
  ILoColor last();

  // drops the last in this list
  ILoList dropLast();
}

// to represent an empty list of list of Colors
class MtLoList implements ILoList {

  // draws this empty list of list of Colors onto a given scene
  public WorldScene drawGuesses(WorldScene scene, int down) {
    return scene;
  }

  // modifies this empty list of list of Colors based on given key
  public ILoList makeGuesses(String key, Mastermind m) {
    return m.makeGuessesEmpty(key, this);
  }

  // submits the most recent guess in this list
  public World submitGuess(int length, ILoColor sequence, Game game, ILoPair matches,
      ILoList guesses, Mastermind m, ILoColor colors) {
    return game;
  }

  // appends the given list to this list
  public ILoList append(ILoList that) {
    return that;
  }

  // computes the length of this empty list
  public int length() {
    return 0;
  }

  // returns the last list of lists in this list
  public ILoColor last() {
    return new MtLoColor();
  }

  // drops the last in this list
  public ILoList dropLast() {
    return this;
  }
}

// to represent a non-empty list of list of Colors
class ConsLoList implements ILoList {
  ILoColor first;
  ILoList rest;

  ConsLoList(ILoColor first, ILoList rest) {
    this.first = first;
    this.rest = rest;
  }

  // draws this non-empty list of list of colors onto a given scene
  public WorldScene drawGuesses(WorldScene scene, int down) {
    return this.first.drawColors(this.rest.drawGuesses(scene, down - 40), 25, down);
  }

  // modifies this non-empty list of list of colors based on given key
  public ILoList makeGuesses(String key, Mastermind m) {
    return m.makeGuesses(key, this.last(), this);
  }

  // submits the most recent guess in this non-empty list
  public World submitGuess(int length, ILoColor sequence, Game game, ILoPair matches,
      ILoList guesses, Mastermind m, ILoColor colors) {
    if (this.first.length() != length) {
      return game;
    }
    else {
      return new Game(m, guesses.append(new ConsLoList(new MtLoColor(), new MtLoList())),
          this.first.compareWith(sequence, matches, colors));
    }
  }

  // appends the given list to this list
  public ILoList append(ILoList that) {
    return new ConsLoList(this.first, this.rest.append(that));
  }

  // computes the length of this non-empty list;
  public int length() {
    return 1 + this.rest.length();
  }

  // returns the last list of lists in this list
  public ILoColor last() {
    if (this.rest.length() == 0) {
      return this.first;
    }
    else {
      return this.rest.last();
    }
  }

  // drop the last in this list
  public ILoList dropLast() {
    if (this.rest.length() == 0) {
      return this.rest;
    }
    else {
      return new ConsLoList(this.first, this.rest.dropLast());
    }
  }
}

// to represent the number of exact and inexact matches for a guess
class PairOfNumbers {
  int first;
  int second;

  PairOfNumbers(int first, int second) {
    this.first = first;
    this.second = second;
  }

  // draws this pair of numbers onto a given scene
  WorldScene drawPair(WorldScene scene, int right, int down) {
    WorldScene drawFirst = scene.placeImageXY(
        new TextImage(Integer.toString(this.first), 30, FontStyle.BOLD, Color.RED), right, down);
    return drawFirst.placeImageXY(
        new TextImage(Integer.toString(this.second), 30, FontStyle.BOLD, Color.RED), right + 40,
        down);
  }
}

// to represent a list of pairs
interface ILoPair {
  // draws this list of Pairs onto the given Scene
  WorldScene drawMatches(WorldScene scene, int right, int down);
}

// to represent an empty list of pairs 
class MtLoPair implements ILoPair {

  // draws this empty list of pairs onto the given scene
  public WorldScene drawMatches(WorldScene scene, int right, int down) {
    return scene;
  }
}

// to represent a non-empty list of pairs
class ConsLoPair implements ILoPair {
  PairOfNumbers first;
  ILoPair rest;

  ConsLoPair(PairOfNumbers first, ILoPair rest) {
    this.first = first;
    this.rest = rest;
  }

  // draws this empty list of pairs onto the given scene
  public WorldScene drawMatches(WorldScene scene, int right, int down) {
    return this.first.drawPair(this.rest.drawMatches(scene, right, down - 40), right, down);
  }
}

// to represent a game of Mastermind
class Game extends World {
  Mastermind mastermind;
  ILoList guesses;
  ILoPair matches;
  ILoColor sequence;

  Game(Mastermind mastermind, ILoList guesses, ILoPair matches) {
    this.mastermind = mastermind;
    this.guesses = guesses;
    this.matches = matches;
    this.sequence = mastermind.makeSequence();
  }

  // draws the game
  public WorldScene makeScene() {
    return this.mastermind.makeScene(guesses, matches, sequence);
  }

  public World onTick() {
    return this;
  }

  public World onKeyEvent(String key) {
    if (key.equals("enter")) {
      return this.mastermind.submitGuess(guesses, matches, sequence, this);
    }
    else {
      return new Game(this.mastermind, guesses.makeGuesses(key, this.mastermind), this.matches);
    }
  }
}

// to represent examples and tests for Mastermind
class ExamplesMastermind {
  ILoColor mtColors = new MtLoColor();
  ILoColor colors1 = new ConsLoColor(Color.GREEN,
      new ConsLoColor(Color.RED, new ConsLoColor(Color.BLUE, this.mtColors)));
  Mastermind m1 = new Mastermind(true, 5, 3, this.colors1);
  Mastermind m2 = new Mastermind(false, 3, 9, this.colors1);
  Game g1 = new Game(m1, new MtLoList(), new MtLoPair());

  // tests for Mastermind constructors
  boolean testConstructors(Tester t) {
    return t.checkConstructorException(new IllegalArgumentException("Invalid Length: 0"),
        "Mastermind", true, 0, 3, colors1)
        && t.checkConstructorException(new IllegalArgumentException("Invalid Guesses: 0"),
            "Mastermind", true, 3, 0, colors1)
        && t.checkConstructorException(new IllegalArgumentException("Invalid list length"),
            "Mastermind", false, 5, 3, colors1)
        && t.checkConstructorException(new IllegalArgumentException("Invalid list length"),
            "Mastermind", true, 5, 3, mtColors);
  }

  // tests for checkLessThanOne()
  boolean testCheckLessThanOne(Tester t) {
    return t.checkExpect(new Utils().checkLessThanOne(4, "Error"), 4) && t.checkException(
        new IllegalArgumentException("Error"), new Utils(), "checkLessThanOne", 0, "Error");
  }

  // tests for checkLength()
  boolean testCheckLength(Tester t) {
    return t.checkExpect(colors1.checkLength(3, true), colors1)
        && t.checkExpect(colors1.checkLength(2, true), colors1)
        && t.checkExpect(colors1.checkLength(2, false), colors1)
        && t.checkException(new IllegalArgumentException("Invalid list length"), colors1,
            "checkLength", 5, false)
        && t.checkException(new IllegalArgumentException("Invalid list length"), mtColors,
            "checkLength", 2, true);
  }

  // tests for length()
  boolean testLength(Tester t) {
    return t.checkExpect(mtColors.length(), 0) && t.checkExpect(colors1.length(), 3);
  }

  boolean testBigBang(Tester t) {
    World w = g1;
    int worldWidth = 1000;
    int worldHeight = 1000;
    double tickRate = 1;
    return w.bigBang(worldWidth, worldHeight, tickRate);
  }
}