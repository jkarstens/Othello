import java.applet.Applet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.event.*;
import java.awt.*;

public class Othello extends Applet implements MouseListener, ActionListener, ItemListener, Runnable {
  private Thread thread;
  private Graphics dbGraphics;
  private Image dbImage;
  private int WIDTH, HEIGHT;
  private double SCALE; // originally developed on ~100 ppi. looks way too small on 196 ppi.

  private Board board;
  private int currentPlayer;
  private int player1, player2, aiPlayer;
  private boolean playing;
  private Image player1Image, player2Image;
  private boolean player1Won, player2Won, nooneWon;
  private Image player1Win, player2Win, nooneWin;
  private Choice[] playerChoices;
  private Image[] images;
  private Button newGameButton, undoMoveButton;
  private Checkbox showAvailableMovesCheckbox;
  private String os;

  public void init() {
    os = System.getProperty("os.name");
    SCALE = Toolkit.getDefaultToolkit().getScreenResolution() / 100.0;
    WIDTH = (int)(900 * SCALE);
    HEIGHT = (int)(700 * SCALE);
    setSize(WIDTH, HEIGHT);
    setBackground(new Color(230, 255, 230));

    if (os.equals("Windows") && SCALE > 1.1) {
      UIManager.put("OptionPane.buttonFont", new Font("System", Font.PLAIN, (int)(12 * SCALE)));
      UIManager.put("OptionPane.messageFont", new Font("System", Font.BOLD, (int)(20 * SCALE)));
      UIManager.put("ComboBox.font", new Font("System", Font.PLAIN, (int)(20 * SCALE)));
      try {
        UIManager.setLookAndFeel(UIManager.getInstalledLookAndFeels()[3].getClassName());
      } catch (Exception e) {
      }
    }

    addMouseListener(this);
    board = new Board();
    playerChoices = new Choice[2];
    Color playerChoiceColor = new Color(176, 196, 222);
    for (int i = 0; i < playerChoices.length; i++) {
      playerChoices[i] = new Choice();
      playerChoices[i].setFont(new Font("Times New Roman", Font.BOLD, (int)(12 * SCALE)));
      playerChoices[i].add("Black");
      playerChoices[i].add("Gray");
      playerChoices[i].add("Blue");
      playerChoices[i].add("Yellow");
      playerChoices[i].add("Acai");
      playerChoices[i].add("Apple");
      playerChoices[i].add("Banana");
      playerChoices[i].add("Pineapple");
      playerChoices[i].add("Strawberry");
      playerChoices[i].addItemListener(this);
      add(playerChoices[i]);
    }
    playerChoices[0].select(0);
    playerChoices[1].select(1);
    player1 = Board.BLACK;
    player2 = Board.WHITE;
		aiPlayer = Board.WHITE;
    currentPlayer = player1;
    playing = false;
    setImages();
    newGameButton = new Button("New Game");
    newGameButton.setFont(new Font("Comic Sans MS", Font.BOLD, (int)(20 * SCALE)));
    newGameButton.addActionListener(this);
    add(newGameButton);
    undoMoveButton = new Button("Undo Move");
    undoMoveButton.setFont(new Font("Comic Sans MS", Font.BOLD, (int)(16 * SCALE)));
    undoMoveButton.setEnabled(false);
    undoMoveButton.addActionListener(this);
    add(undoMoveButton);
    showAvailableMovesCheckbox = new Checkbox("Show Available Moves");
    showAvailableMovesCheckbox.setFont(new Font("Comic Sans MS", Font.PLAIN, (int)(12 * SCALE)));
    add(showAvailableMovesCheckbox);
    showAvailableMovesCheckbox.setEnabled(false);
    if (!(os.equals("Mac OS X") || os.equals("Mac"))) {
      newGameButton.setBackground(new Color(86, 150, 150));
      undoMoveButton.setBackground(new Color(225, 190, 225));
      playerChoices[0].setBackground(playerChoiceColor);
      playerChoices[1].setBackground(playerChoiceColor);
    }
  }

	private int aiWait = 0;
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    drawStrings(g2);
    drawPieces(g2);
    playerChoices[0].setBounds((int)(620 * SCALE), (int)(210 * SCALE), (int)(119 * SCALE), (int)(20 * SCALE));
    playerChoices[1].setBounds((int)(750 * SCALE), (int)(210 * SCALE), (int)(119 * SCALE), (int)(20 * SCALE));
    showAvailableMovesCheckbox.setBounds((int)(620 * SCALE), (int)(290 * SCALE), (int)(150 * SCALE), (int)(30 * SCALE));
    newGameButton.setBounds((int)(660 * SCALE), (int)(375 * SCALE), (int)(160 * SCALE), (int)(60 * SCALE));
    undoMoveButton.setBounds((int)(660 * SCALE), (int)(328 * SCALE), (int)(160 * SCALE), (int)(40 * SCALE));
    g2.setStroke(new BasicStroke((float)(3.5 * SCALE), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, (float)(10.0 * SCALE)));
    g2.setColor(new Color(120, 145, 120));
    for (int i = 40; i < 670; i += 70) {
      g2.drawLine((int)(40 * SCALE), (int)(i * SCALE), (int)(600 * SCALE), (int)(i * SCALE));
      g2.drawLine((int)(i * SCALE), (int)(40 * SCALE), (int)(i * SCALE), (int)(600 * SCALE));
    }
    g2.setColor(new Color(176, 255, 48));
    if (playing && showAvailableMovesCheckbox.getState()) {
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          if (board.addMove(i, j, currentPlayer)) {
            g2.drawRect((int)((40 + i * 70) * SCALE), (int)((40 + j * 70) * SCALE), (int)(70 * SCALE), (int)(70 * SCALE));
            board.undoMove();
          }
        }
      }
    }
    if (aiWait > 5) {
        Board.Move m = board.bestMove(currentPlayer, 0);
        board.addMove(m.x, m.y, currentPlayer);
        updateMove();
        aiWait = currentPlayer == aiPlayer && playing ? 1 : 0;
    } else if (aiWait > 0) {
        aiWait++;
    }
    g2.setFont(new Font("Comic Sans MS", Font.BOLD, (int)(20 * SCALE)));
    g2.setColor(Color.WHITE);
    if (player1Won) {
      g2.drawImage(player1Win, (int)(640 * SCALE), (int)(475 * SCALE), this);
      g2.drawString("Congrats Othello!", (int)(660 * SCALE), (int)(500 * SCALE));
    } else if (player2Won) {
      g2.drawImage(player2Win, (int)(640 * SCALE), (int)(475 * SCALE), this);
      g2.drawString("Congrats Iago!", (int)(660 * SCALE), (int)(600 * SCALE));
    } else if (nooneWon) {
      g2.drawImage(nooneWin, (int)(640 * SCALE), (int)(475 * SCALE), this);
      g2.drawString("Tie Game!", (int)(690 * SCALE), (int)(590 * SCALE));
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
        g2.fillOval((int)((650 + 120 * i)* SCALE), (int)(120 * SCALE), (int)(70 * SCALE), (int)(70 * SCALE));
        for (int j = 0; j < 8; j++) {
          for (int k = 0; k < 8; k++) {
            if ((i == 0 && board.getSpot(j , k) == player1) || (i == 1 && board.getSpot(j, k) == player2)) {
              g2.fillOval((int)((j * 70 + 44) * SCALE), (int)((k * 70 + 44) * SCALE), (int)(62 * SCALE), (int)(62 * SCALE));
            }
          }
        }
        if (playing && (i == 0 && currentPlayer == player1) || (i == 1 && currentPlayer == player2)) {
          g2.fillOval((int)(770 * SCALE), (int)(235 * SCALE), (int)(70 * SCALE), (int)(70 * SCALE));
        }
      } else {
        Image piece = i == 0 ? player1Image : player2Image;
        g2.drawImage(piece, (int)((650 + 120 * i) * SCALE), (int)(120 * SCALE), this);
        for (int j = 0; j < 8; j++) {
          for (int k = 0; k < 8; k++) {
            if ((i == 0 && board.getSpot(j, k) == player1) || (i == 1 && board.getSpot(j, k) == player2)) {
              g2.drawImage(piece, (int)((j * 70 + 44) * SCALE), (int)((k * 70 + 44) * SCALE), this);
            }
          }
        }
        if (playing && (i == 0 && currentPlayer == player1) || (i == 1 && currentPlayer == player2)) {
          g2.drawImage(piece, (int)(770 * SCALE), (int)(240 * SCALE), this);
        }
      }
    }
  }

  public void drawStrings(Graphics2D g2) {
    g2.setColor(Color.BLACK);
    g2.setFont(new Font("Comic Sans MS", Font.BOLD, (int)(35 * SCALE)));
    g2.drawString("OTHELLO", (int)(660 * SCALE), (int)(60 * SCALE));
    g2.setFont(new Font("Comic Sans MS", Font.ITALIC, (int)(26 * SCALE)));
    g2.drawString("Othello", (int)(645 * SCALE), (int)(105 * SCALE));
    g2.drawString("Iago", (int)(775 * SCALE), (int)(105 * SCALE));
    g2.setFont(new Font("Comic Sans MS", Font.PLAIN, (int)(20 * SCALE)));
    g2.drawString("Current Player:", (int)(620 * SCALE), (int)(280 * SCALE));
  }

	private void updateMove() {
    currentPlayer = board.other(currentPlayer);
    if (!board.hasMove(currentPlayer)) {
      currentPlayer = board.other(currentPlayer);
      if (!board.hasMove(currentPlayer)) {
			  playing = false;
				aiWait = 0;
				showAvailableMovesCheckbox.setState(false);
        showAvailableMovesCheckbox.setEnabled(false);
        undoMoveButton.setEnabled(false);
        int player1Chips = board.chipCount(player1);
        int player2Chips = board.chipCount(player2);
        if (player1Chips < player2Chips) {
          player2Won = true;
          JOptionPane.showMessageDialog(this, "Congratulations, Iago, you win!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
        } else if (player1Chips > player2Chips) {
          player1Won = true;
          JOptionPane.showMessageDialog(this, "Congratulations, Othello, you win!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
        } else {
          nooneWon = true;
          JOptionPane.showMessageDialog(this, "Tie game!", "Good Game!", JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }
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
    if (playing && currentPlayer != aiPlayer) {
      int x = (e.getX() - (int)(40 * SCALE)) / (int)(70 * SCALE);
      int y = (e.getY() - (int)(40 * SCALE)) / (int)(70 * SCALE);
      if (board.addMove(x, y, currentPlayer)) {
        updateMove();
        if (currentPlayer == aiPlayer) {
          aiWait = 1;
				}
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == newGameButton) {
      board = new Board();
			String[] gameModes = new String[] {"Try to beat the computer", "Play against another human"};
			String gameMode = (String)JOptionPane.showInputDialog(this, "", "Game Mode", -1, null, gameModes, gameModes[0]);
      if (gameMode != null) {
        currentPlayer = Board.BLACK;
        aiPlayer = gameMode.equals(gameModes[0]) ? Board.WHITE : 0;
        playing = true;
  			aiWait = 0;
        showAvailableMovesCheckbox.setEnabled(true);
        undoMoveButton.setEnabled(true);
        player1Won = false;
        player2Won = false;
        nooneWon = false;
      }
    } else if (source == undoMoveButton && playing) {
      if (board.undoMove()) {
        currentPlayer = board.other(currentPlayer);
  			if (currentPlayer == aiPlayer) {
  				board.undoMove();
  				currentPlayer = board.other(currentPlayer);
  			}
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
    }
  }

  public void setImages() {
    Toolkit tk = Toolkit.getDefaultToolkit();
    images = new Image[5];
    images[0] = tk.createImage("images/acai.png").getScaledInstance((int)(60 * SCALE), (int)(60 * SCALE), Image.SCALE_SMOOTH);
    images[1] = tk.createImage("images/apple.png").getScaledInstance((int)(60 * SCALE), (int)(60 * SCALE), Image.SCALE_SMOOTH);
    images[2] = tk.createImage("images/banana.png").getScaledInstance((int)(60 * SCALE), (int)(60 * SCALE), Image.SCALE_SMOOTH);
    images[3] = tk.createImage("images/pineapple.png").getScaledInstance((int)(60 * SCALE), (int)(60 * SCALE), Image.SCALE_SMOOTH);
    images[4] = tk.createImage("images/strawberry.png").getScaledInstance((int)(60 * SCALE), (int)(60 * SCALE), Image.SCALE_SMOOTH);
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
}
