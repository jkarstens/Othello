import java.applet.Applet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.Stack;
import java.awt.event.*;
import java.awt.*;

public class Othello extends Applet implements MouseListener, ActionListener, ItemListener, Runnable {
  private Thread thread;
  private Graphics dbGraphics;
  private Image dbImage;
  private int WIDTH, HEIGHT;

  private Board board;
  private int currentPlayer;
  private int player1, player2;
  private boolean playing;
  private Image player1Image, player2Image;
  private boolean player1Won, player2Won, nooneWon;
  private Image player1Win, player2Win, nooneWin;
  private Choice[] playerChoices;
  private Image[] images;
  private Button newGameButton, undoMoveButton;
  private Checkbox showAvailableMovesCheckbox;
  private Choice backgroundChoice;
  private Color[] backgrounds;

  public void init() {
    WIDTH = 900;
    HEIGHT = 700;
    setSize(WIDTH, HEIGHT);
    addMouseListener(this);
    board = new Board();
    playerChoices = new Choice[2];
    Color playerChoiceColor = new Color(176, 196, 222);
    for (int i = 0; i < playerChoices.length; i++) {
      playerChoices[i] = new Choice();
      playerChoices[i].add("Black");
      playerChoices[i].add("Gray");
      playerChoices[i].add("Blue");
      playerChoices[i].add("Yellow");
      playerChoices[i].add("Acai");
      playerChoices[i].add("Apple");
      playerChoices[i].add("Banana");
      playerChoices[i].add("Pineapple");
      playerChoices[i].add("Strawberry");
      playerChoices[i].setBackground(playerChoiceColor);
      playerChoices[i].addItemListener(this);
      add(playerChoices[i]);
    }
    playerChoices[0].select(2);
    playerChoices[1].select(3);
    player1 = Board.BLACK;
    player2 = Board.WHITE;
    currentPlayer = player1;
    playing = false;
    setImages();
    newGameButton = new Button("New Game");
    newGameButton.setBackground(new Color(86, 150, 150));
    newGameButton.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
    newGameButton.addActionListener(this);
    add(newGameButton);
    undoMoveButton = new Button("Undo Move");
    undoMoveButton.setBackground(new Color(225, 190, 225));
    undoMoveButton.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
    undoMoveButton.setEnabled(false);
    undoMoveButton.addActionListener(this);
    add(undoMoveButton);
    showAvailableMovesCheckbox = new Checkbox("Show Available Moves");
    showAvailableMovesCheckbox.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
    add(showAvailableMovesCheckbox);
    showAvailableMovesCheckbox.setEnabled(false);
    backgroundChoice = new Choice();
    backgroundChoice.add("White");
    backgroundChoice.add("Cyan");
    backgroundChoice.add("Light Blue");
    backgroundChoice.add("Green");
    backgroundChoice.add("Purple");
    backgroundChoice.add("Khaki");
    backgroundChoice.addItemListener(this);
    add(backgroundChoice);
    backgroundChoice.select(1);
    backgrounds = new Color[6];
    backgrounds[0] = Color.WHITE;
    backgrounds[1] = new Color(230, 255, 230);
    backgrounds[2] = new Color(173, 216, 230);
    backgrounds[3] = new Color(164, 255, 164);
    backgrounds[4] = new Color(236, 211, 236);
    backgrounds[5] = new Color(255, 244, 164);
    backgroundChoice.setBackground(backgrounds[1]);
    setBackground(backgrounds[1]);
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    drawStrings(g2);
    drawPieces(g2);
    playerChoices[0].setBounds(620, 210, 119, 10);
    playerChoices[1].setBounds(750, 210, 119, 10);
    showAvailableMovesCheckbox.setBounds(620, 290, 150, 30);
    newGameButton.setBounds(660, 375, 160, 60);
    undoMoveButton.setBounds(660, 328, 160, 40);
    backgroundChoice.setBounds(765, 450, 80, 10);
    g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f));
    g2.setColor(new Color(120, 145, 120));
    for (int i = 40; i < 670; i += 70) {
      g2.drawLine(40, i, 600, i);
      g2.drawLine(i, 40, i, 600);
    }
    g2.setColor(new Color(176, 255, 48));
    if (playing && showAvailableMovesCheckbox.getState()) {
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          Board copy = new Board(board);
          if (copy.addMove(i, j, currentPlayer)) {
            g2.drawRect(40 + i * 70, 40 + j * 70, 70, 70);
          }
        }
      }
    }
    g2.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
    g2.setColor(Color.WHITE);
    if (player1Won) {
      g2.drawImage(player1Win, 640, 475, this);
      g2.drawString("Congrats Othello!", 660, 500);
    } else if (player2Won) {
      g2.drawImage(player2Win, 640, 475, this);
      g2.drawString("Congrats Iago!", 660, 600);
    } else if (nooneWon) {
      g2.drawImage(nooneWin, 640, 475, this);
      g2.drawString("Tie Game!", 690, 590);
    }
  }

  public void drawPieces(Graphics2D g2) {
    for (int i = 0; i < playerChoices.length; i++) {
      boolean oval = true;
      switch (playerChoices[i].getSelectedIndex()) {
        case 0: g2.setColor(Color.BLACK); break;
        case 1: g2.setColor(new Color(160, 160, 160)); break;
        case 2: g2.setColor(new Color(0, 58, 98)); break;
        case 3: g2.setColor(new Color(253, 181, 21)); break;
        default: oval = false;
      }
      if (oval) {
        g2.fillOval(650 + 120 * i, 120, 70, 70);
        for (int j = 0; j < 8; j++) {
          for (int k = 0; k < 8; k++) {
            if ((i == 0 && board.getSpot(j , k) == player1) || (i == 1 && board.getSpot(j, k) == player2)) {
              g2.fillOval(j * 70 + 44, k * 70 + 44, 62, 62);
            }
          }
        }
        if (playing && (i == 0 && currentPlayer == player1) || (i == 1 && currentPlayer == player2)) {
          g2.fillOval(770, 235, 70, 70);
        }
      } else {
        Image piece = i == 0 ? player1Image : player2Image;
        g2.drawImage(piece, 650 + 120 * i, 120, this);
        for (int j = 0; j < 8; j++) {
          for (int k = 0; k < 8; k++) {
            if ((i == 0 && board.getSpot(j, k) == player1) || (i == 1 && board.getSpot(j, k) == player2)) {
              g2.drawImage(piece, j * 70 + 44, k * 70 + 44, this);
            }
          }
        }
        if (playing && (i == 0 && currentPlayer == player1) || (i == 1 && currentPlayer == player2)) {
          g2.drawImage(piece, 770, 240, this);
        }
      }
    }
  }

  public void drawStrings(Graphics2D g2) {
    g2.setColor(Color.BLACK);
    g2.setFont(new Font("Comic Sans MS", Font.BOLD, 35));
    g2.drawString("OTHELLO", 660, 60);
    g2.setFont(new Font("Comic Sans MS", Font.ITALIC, 26));
    g2.drawString("Othello", 645, 105);
    g2.drawString("Iago", 765, 105);
    g2.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
    g2.drawString("Current Player:", 620, 280);
    g2.drawString("Background:", 635, 467);
  }

  public void update(Graphics g) {
    if (dbImage == null) {
      dbImage = createImage(WIDTH, HEIGHT);
      dbGraphics = dbImage.getGraphics();
    }
    dbGraphics.setColor(getBackground());
    dbGraphics.fillRect(0, 0, WIDTH, HEIGHT);
    dbGraphics.setColor(getForeground());
    paint(dbGraphics);
    g.drawImage(dbImage, 0, 0, this);
  }

  public void mouseEntered(MouseEvent e) {
  }
  public void mouseExited(MouseEvent e) {
  }
  public void mousePressed(MouseEvent e) {
  }
  public void mouseReleased(MouseEvent e) {
  }
  public void mouseClicked(MouseEvent e) {
    if (playing) {
      int x = (e.getX() - 40) / 70;
      int y = (e.getY() - 40) / 70;
      if (board.addMove(x, y, currentPlayer)) {
        if (board.isFull()) {
          int player1Chips = board.chipCount(player1);
          int player2Chips = board.chipCount(player2);
          if (player1Chips < player2Chips) {
            JOptionPane.showMessageDialog(this, "Congratulations, Iago, you win!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
            player2Won = true;
          } else if (player1Chips > player2Chips) {
            JOptionPane.showMessageDialog(this, "Congratulations, Othello, you win!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
            player1Won = true;
          } else {
            JOptionPane.showMessageDialog(this, "Tie game!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
            nooneWon = true;
          }
          playing = false;
          showAvailableMovesCheckbox.setEnabled(false);
          undoMoveButton.setEnabled(false);
        } else {
          currentPlayer = board.other(currentPlayer);
          if (!board.hasMove(currentPlayer)) {
            currentPlayer = board.other(currentPlayer);
            if (!board.hasMove(currentPlayer)) {
              int player1Chips = board.chipCount(player1);
              int player2Chips = board.chipCount(player2);
              if (player1Chips < player2Chips) {
                JOptionPane.showMessageDialog(this, "Congratulations, Iago, you win!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
                player2Won = true;
              } else if (player1Chips > player2Chips) {
                JOptionPane.showMessageDialog(this, "Congratulations, Othello, you win!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
                player1Won = true;
              } else {
                JOptionPane.showMessageDialog(this, "Tie game!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
                nooneWon = true;
              }
              playing = false;
              showAvailableMovesCheckbox.setEnabled(false);
              undoMoveButton.setEnabled(false);
            }
          }
        }
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == newGameButton) {
      board = new Board();
      currentPlayer = (int)(2 * Math.random()) + 1;
      if (currentPlayer == player1) {
        JOptionPane.showMessageDialog(this, "Othello goes first!", "Random Choice", JOptionPane.INFORMATION_MESSAGE);
        player1 = Board.BLACK;
        player2 = Board.WHITE;
      } else {
        JOptionPane.showMessageDialog(this, "Iago goes first!", "Random Choice", JOptionPane.INFORMATION_MESSAGE);
        player1 = Board.WHITE;
        player2 = Board.BLACK;
      }
      playing = true;
      showAvailableMovesCheckbox.setEnabled(true);
      undoMoveButton.setEnabled(true);
      player1Won = false;
      player2Won = false;
      nooneWon = false;
    } else if (source == undoMoveButton && playing) {
      if (board.undoMove()) {
        currentPlayer = board.other(currentPlayer);
      }
    }
  }

  public void itemStateChanged(ItemEvent e) {
    Object source = e.getSource();
    if (source == playerChoices[0] || source == playerChoices[1]) {
      int choice0 = playerChoices[0].getSelectedIndex(), choice1 = playerChoices[1].getSelectedIndex();
      if (choice0 == choice1) {
        if (source == playerChoices[0]) {
          if (choice0 == 0) {
            playerChoices[1].select(1);
          } else {
            playerChoices[1].select(0);
          }
        } else {
          if (choice1 == 0) {
            playerChoices[0].select(1);
          } else {
            playerChoices[0].select(0);
          }
        }
      }
      if (choice0 > 3) {
        player1Image = images[choice0 - 4];
      } else {
        player1Image = null;
      }
      if (choice1 > 3) {
        player2Image = images[choice1 - 4];
      } else {
        player2Image = null;
      }
    } else if (source == backgroundChoice) {
      Color c = backgrounds[backgroundChoice.getSelectedIndex()];
      backgroundChoice.setBackground(c);
      showAvailableMovesCheckbox.setBackground(c);
      setBackground(c);
    }
  }

  public void setImages() {
    Toolkit tk = Toolkit.getDefaultToolkit();
    images = new Image[5];
    images[0] = tk.createImage("images/acai.png");
    images[1] = tk.createImage("images/apple.png");
    images[2] = tk.createImage("images/banana.png");
    images[3] = tk.createImage("images/pineapple.png");
    images[4] = tk.createImage("images/strawberry.png");
  }

  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.start();
    }
  }

  public void run() {
    while (thread != null) {
      repaint();
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void stop() {
    thread = null;
  }

  public static void main(String[] args) {
    Applet applet = new Othello();
    applet.init();
    applet.start();

    JFrame frame = new JFrame("OTHELLO");
    frame.setSize(applet.getSize());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    frame.setVisible(true);
  }

  class Board {
    private Stack<int[][]> boards;
    static final int EMPTY = 0;
    static final int BLACK = 1;
    static final int WHITE = 2;

    Board() {
      boards = new Stack<int[][]>();
      int[][] board = new int[8][8];
      board[3][3] = BLACK;
      board[3][4] = WHITE;
      board[4][3] = WHITE;
      board[4][4] = BLACK;
      boards.push(board);
    }

    Board(Board b) {
      boards = new Stack<int[][]>();
      for (int i = 0; i < b.boards.size(); i++) {
        int[][] board = b.boards.get(i);
        int[][] newBoard = new int[8][8];
        for (int j = 0; j < board.length; j++) {
          for (int k = 0; k < board.length; k++) {
            newBoard[j][k] = board[j][k];
          }
        }
        boards.push(newBoard);
      }
    }

    int getSpot(int x, int y) {
      return boards.peek()[x][y];
    }

    int other(int color) {
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
      Board copy = new Board(this);
      for (int i = 0; i < boards.peek().length; i++) {
        for (int j = 0; j < boards.peek().length; j++) {
          if (copy.addMove(i, j, color)) {
            return true;
          }
        }
      }
      return false;
    }

    boolean isFull() {
      return boards.size() == 61;
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

    public boolean equals(Board b) {
      int[][] board = boards.peek();
      try {
        for (int i = 0; i < board.length; i++) {
          for (int j = 0; j < board.length; j++) {
            if (board[i][j] != b.boards.peek()[i][j]) {
              return false;
            }
          }
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        return false;
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
  }
}
