import java.util.Stack;

class Board {
  Stack<int[][]> boards;
  static final int EMPTY = 0;
  static final int BLACK = 1;
  static final int WHITE = 2;
  static final Board[][] BOOK = TranscribeBook.getBook();

  Board() {
    boards = new Stack<int[][]>();
    int[][] board = new int[8][8];
    board[3][3] = WHITE;
    board[3][4] = BLACK;
    board[4][3] = BLACK;
    board[4][4] = WHITE;
    boards.push(board);
  }

  int getSpot(int x, int y) {
    return boards.peek()[x][y];
  }

  static int other(int color) {
    switch (color) {
      case BLACK: return WHITE;
      case WHITE: return BLACK;
      default: return EMPTY;
    }
  }

  boolean addMove(int x, int y, int color) {
    boolean valid = false;
    int[][] oldBoard = boards.peek();
    int[][] board = new int[8][8];
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        board[i][j] = oldBoard[i][j];
      }
    }
    if (-1 < x && x < board.length && -1 < y && y < board.length && board[x][y] == EMPTY) {
      int other = other(color);
      for (int dx = -1; dx < 2; dx++) {
        for (int dy = -1; dy < 2; dy++) {
          if ((dx != 0 || dy != 0) && -1 < x + dx && x + dx < board.length && -1 < y + dy && y + dy < board.length && board[x + dx][y + dy] == other) {
            for (int i = x + dx + dx, j = y + dy + dy; -1 < i && i < board.length && -1 < j && j < board.length; i += dx, j += dy) {
              if (board[i][j] == EMPTY) {
                break;
              } else if (board[i][j] == color) {
                for (int k = i - dx, l = j - dy; k != x || l != y; k -= dx, l -= dy) {
                  board[k][l] = color;
                }
                board[x][y] = color;
                valid = true;
                break;
              }
            }
          }
        }
      }
    }
    if (valid) {
      boards.push(board);
    }
    return valid;
  }

  boolean undoMove() {
    if (boards.size() > 1) {
      boards.pop();
      return true;
    } else {
      return false;
    }
  }

  boolean hasMove(int color) {
    int[][] board = boards.peek();
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        if (addMove(i, j, color)) {
          undoMove();
          return true;
        }
      }
    }
    return false;
  }

  int chipCount(int color) {
    int chipCount = 0;
    int[][] board = boards.peek();
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        if (board[i][j] == color) {
          chipCount++;
        }
      }
    }
    return chipCount;
  }

  // AI strategies
  // minimize opponent mobility = minimize (frontier) disks, but not so much that the game ends too early (split game into open, mid, end)
  // place stable discs (eg corners, edges)
  // board spots evaluation
  // opening moves
  // closing moves - most disks, odd parity
  // learn! play games against itself

  static final int HUMAN = BLACK;
  static final int AI = WHITE;
  static final int MAX_SCORE = 1000;
  static final int MIN_SCORE = -1000;
  static final int MAX_DEPTH = 4; // can do more if just loook at book for depth = 0
  static final int[] STAGE = new int[] {16, 32, 64};
  static final int[][] STATIC_EVAL = { {100, -10, 10, 5, 5, 10, -10, 100},
                                       {-10, -20, -5, -3, -3, -5, -20, -10},
                                       {10, -5, 4, 1, 1, 4, -5, 10},
                                       {5, -3, 1, 0, 0, 1, -3, 5},
                                       {5, -3, 1, 0, 0, 1, -3, 5},
                                       {10, -5, 4, 1, 1, 4, -5, 10},
                                       {-10, -20, -5, -3, -3, -5, -20, -10},
                                       {100, -10, 10, 5, 5, 10, -10, 100} };

  Move bestMove(int player, int depth) {
    Move best = new Move();
    best.depth = depth;
    int other = other(player);

    if (!(hasMove(player) || hasMove(other))) { // game is over, return board score
      best.x = -1;
      best.y = -1;
      int aiCount = chipCount(AI);
      int humanCount = chipCount(HUMAN);
      if (aiCount > humanCount) {
        best.score = MAX_SCORE - depth;
      } else if (aiCount < humanCount) {
        best.score = MIN_SCORE - depth;
      } else {
        best.score = 0;
      }
      return best;
    }

    if (player == AI) {
      for (int i = 0; i < BOOK.length; i++) { // check the book first before traversing the game tree
        for (int j = 0; j < 1; j++) { // change to 4 once last 2 variations are implemented
          for (int k = 0; k < 8; k++) {
            for (int l = 0; l < 8; l++) {
              if (addMove(k, l, player)) {
                if (this.equals(BOOK[i][j])) {
                  System.out.println("found a book move: ");
                  System.out.println(BOOK[i][j] + "\n");
                  int score = player == AI ? 900 : -900; // improve book evaluation
                  undoMove();
                  best.x = k;
                  best.y = l;
                  best.score = player == AI ? 900 - (100 * depth) : -900 + (100 * depth);
                  // return new Move(k, l, score, depth);
                } else {
                  undoMove();
                }
              }
            }
          }
        }
      }
    }
    best.score = player == AI ? MIN_SCORE : MAX_SCORE; // set lower bound
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if (addMove(i, j, player)) {
          if (depth > MAX_DEPTH) { // too deep - perform heuristic evaluation of board and return early
            int mobilityScore = 0;
            int aiCount = chipCount(AI), humanCount = chipCount(HUMAN);
            if (aiCount + humanCount < STAGE[1]) {
              mobilityScore = 100 * (humanCount - aiCount);
            }
            undoMove();
            return new Move(i, j, mobilityScore + STATIC_EVAL[i][j] - depth, depth);
          }
          Move reply = bestMove(other, depth + 1);
          undoMove();
          if ((player == AI && reply.score >= best.score) ||
              (player == HUMAN && reply.score <= best.score)) {
            best.x = i;
            best.y = j;
            best.score = reply.score;
          }
        }
      }
    }
    return best;
  }

  public boolean equals(Board b) {
    int[][] board = boards.peek();
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        if (board[i][j] != b.boards.peek()[i][j]) {
          return false;
        }
      }
    }
    return true;
  }

  public String toString() {
    int[][] board = boards.peek();
    String s = "";
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        switch (board[j][i]) {
          case BLACK: s += "B "; break;
          case WHITE: s += "W "; break;
          case EMPTY: s += "_ "; break;
        }
      }
      s += "\n";
    }
    return s;
  }

  class Move {
    int x, y;
    double score;
    int depth;

    Move(int x, int y, int score, int depth) {
      this.x = x;
      this.y = y;
      this.score = score;
      this.depth = depth;
    }

    Move() {
      this(-1, -1, 0, 0);
    }
  }
}
