import java.io.*;
import java.util.Iterator;

public class TranscribeBook {
  public static Board[][] getBook() {
    try {
      BufferedReader in = new BufferedReader(new FileReader("Reversi Openings.html"));
      String line;
      int lineCount = 1;
      int moveCount = 0;
      Board[][] book = new Board[75][4]; // 4 variations on each board
      while ((line = in.readLine()) != null) { // no heath-chimney, no parallel play
        if ((lineCount == 985 || lineCount == 990) || (lineCount >= 609 && lineCount < 980 && lineCount != 654 && lineCount != 774 && (lineCount - 609) % 5 == 0)) {
          line = line.substring(33, line.length() - 12);
          Board board = new Board();
          int player = Board.BLACK;
          for (int i = 0; i < line.length(); i += 2) {
            int coords = toIntCoordinates(line.substring(i, i + 2));
            board.addMove(coords / 10, coords % 10, player);
            player = Board.other(player);
          }
          book[moveCount][0] = board;
          // System.out.println(line + " @ " + lineCount + ", parsed to ");
          // System.out.println(board);

          // get "rotations" of board
          for (int i = 1; i < 4; i++) {
            book[moveCount][i] = new Board();
          }
          Iterator iter = board.boards.iterator();
          int[][] lastBoard = (int[][])iter.next();
          player = Board.BLACK;
          while (iter.hasNext()) {
            int[][] nextBoard = (int[][])iter.next();
            for (int i = 0; i < 8; i++) {
              for (int j = 0; j < 8; j++) {
                if (lastBoard[i][j] == Board.EMPTY && nextBoard[i][j] != Board.EMPTY) { // most recent move
                  book[moveCount][1].addMove(7 - i, 7 - j, player);
                }
              }
            }
            player = Board.other(player);
          }

          moveCount++;
        }
        lineCount++;
      }
      // System.out.println(moveCount);
      in.close();
      return book;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static int toIntCoordinates(String coord) {
    coord = coord.toLowerCase();
    int x = (int)coord.charAt(0) - 97;
    int y = Integer.parseInt(coord.substring(1)) - 1;
    return x * 10 + y;
  }

  public static void main(String[] args) {
    getBook();
  }
}
