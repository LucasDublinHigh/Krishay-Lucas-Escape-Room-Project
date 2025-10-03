import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;


public class GameGUI extends JComponent {
    private static final long serialVersionUID = 141L;


    public static final int WIDTH = 510;
    public static final int HEIGHT = 360;
    public static final int SPACE_SIZE = 60;
    public static final int GRID_W = 8;
    public static final int GRID_H = 5;
    public static final int START_X = 15;
    public static final int START_Y = 15;


    private JFrame frame;


    private int playerX = START_X;
    private int playerY = START_Y;
    private int playerSteps = 0;


    private static final int TOTAL_WALLS = 20;
    private static final int TOTAL_PRIZES = 3;
    private static final int TOTAL_TRAPS = 5;


    private Rectangle[] walls = new Rectangle[TOTAL_WALLS];
    private Rectangle[] prizes = new Rectangle[TOTAL_PRIZES];
    private Rectangle[] traps = new Rectangle[TOTAL_TRAPS];


    private final int PRIZE_SCORE = 10;
    private final int TRAP_PENALTY = 5;
    private final int END_BONUS = 10;
    private final int OFF_GRID_PENALTY = 5;
    private final int HIT_WALL_PENALTY = 5;


    private Image bgImage;
    private Image playerImage;
    private Image prizeImage;
    private Image trapImage;


    public GameGUI() {
        loadImages();
        initializeBoard();
        setupFrame();
    }


    private void setupFrame() {
        frame = new JFrame("EscapeRoom");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setResizable(false);
        frame.add(this);


        frame.setJMenuBar(createMenuBar());


        // Key bindings instead of KeyListener for better focus handling
        setupKeyBindings();


        frame.setVisible(true);
    }


    private void setupKeyBindings() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();


        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke("UP"), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke('D'), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke('A'), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke('W'), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke('S'), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "pickupPrize");
        inputMap.put(KeyStroke.getKeyStroke('H'), "springTrap");


        actionMap.put("moveRight", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { handleMove(SPACE_SIZE, 0); }
        });
        actionMap.put("moveLeft", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { handleMove(-SPACE_SIZE, 0); }
        });
        actionMap.put("moveUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { handleMove(0, -SPACE_SIZE); }
        });
        actionMap.put("moveDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { handleMove(0, SPACE_SIZE); }
        });
        actionMap.put("pickupPrize", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int delta = pickupPrize();
                if (delta > 0) JOptionPane.showMessageDialog(frame, "Prize collected!");
                else JOptionPane.showMessageDialog(frame, "No prize here!");
            }
        });
        actionMap.put("springTrap", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (isTrap()) {
                    springTrap();
                    showGameOverScreen();
                } else {
                    JOptionPane.showMessageDialog(frame, "No trap here!");
                }
            }
        });
    }


    private void handleMove(int dx, int dy) {
        int result = movePlayer(dx, dy);
        if (result == -OFF_GRID_PENALTY) {
            JOptionPane.showMessageDialog(frame, "You tried to move off the grid!");
        } else if (result == -HIT_WALL_PENALTY) {
            JOptionPane.showMessageDialog(frame, "There is a wall in the way!");
        }
        if (isTrap()) {
            springTrap();
            showGameOverScreen();
        }
        repaint();
    }


    private void loadImages() {
        bgImage = safeLoad("grid.png");
        playerImage = safeLoad("player.png");
        prizeImage = safeLoad("coin.png");
        trapImage = safeLoad("trap.png");
    }


    private Image safeLoad(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (Exception e) {
            System.err.println("Could not load image: " + filename);
            return null;
        }
    }


    private void initializeBoard() {
        createWalls();
        createPrizes();
        createTraps();
        resetPlayer();
    }


    public void resetPlayer() {
        playerX = START_X;
        playerY = START_Y;
        playerSteps = 0;
        repaint();
    }


    private void createWalls() {
        Random rand = new Random();
        for (int i = 0; i < TOTAL_WALLS; i++) {
            int gridX = rand.nextInt(GRID_W);
            int gridY = rand.nextInt(GRID_H);
            boolean vertical = rand.nextBoolean();


            if (vertical) {
                walls[i] = new Rectangle(gridX * SPACE_SIZE + SPACE_SIZE - 5, gridY * SPACE_SIZE + 10, 5, SPACE_SIZE);
            } else {
                walls[i] = new Rectangle(gridX * SPACE_SIZE + 8, gridY * SPACE_SIZE + SPACE_SIZE - 5, SPACE_SIZE, 5);
            }
        }
    }


    private void createPrizes() {
        Random rand = new Random();
        for (int i = 0; i < TOTAL_PRIZES; i++) {
            prizes[i] = new Rectangle(rand.nextInt(GRID_W) * SPACE_SIZE + 15,
                    rand.nextInt(GRID_H) * SPACE_SIZE + 15, 15, 15);
        }
    }


    private void createTraps() {
        Random rand = new Random();
        for (int i = 0; i < TOTAL_TRAPS; i++) {
            traps[i] = new Rectangle(rand.nextInt(GRID_W) * SPACE_SIZE + 15,
                    rand.nextInt(GRID_H) * SPACE_SIZE + 15, 15, 15);
        }
    }


    public int movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;
        playerSteps++;


        if (newX < 0 || newX > WIDTH - SPACE_SIZE || newY < 0 || newY > HEIGHT - SPACE_SIZE) {
            return -OFF_GRID_PENALTY;
        }


        for (Rectangle wall : walls) {
            int wx1 = wall.x;
            int wy1 = wall.y;
            int wx2 = wx1 + wall.width;
            int wy2 = wy1 + wall.height;


            // Simplified collision check: check if line from old to new crosses wall rectangle
            if (dx > 0 && playerX < wx1 && newX >= wx1 && playerY >= wy1 && playerY <= wy2) return -HIT_WALL_PENALTY;
            if (dx < 0 && playerX > wx2 && newX <= wx2 && playerY >= wy1 && playerY <= wy2) return -HIT_WALL_PENALTY;
            if (dy > 0 && playerY < wy1 && newY >= wy1 && playerX >= wx1 && playerX <= wx2) return -HIT_WALL_PENALTY;
            if (dy < 0 && playerY > wy2 && newY <= wy2 && playerX >= wx1 && playerX <= wx2) return -HIT_WALL_PENALTY;
        }


        playerX = newX;
        playerY = newY;
        return 0;
    }


    public boolean isTrap() {
        Rectangle playerRect = new Rectangle(playerX, playerY, SPACE_SIZE / 2, SPACE_SIZE / 2);
        for (Rectangle trap : traps) {
            if (trap.getWidth() > 0 && trap.intersects(playerRect)) return true;
        }
        return false;
    }


    public int springTrap() {
        Rectangle playerRect = new Rectangle(playerX, playerY, SPACE_SIZE / 2, SPACE_SIZE / 2);
        for (Rectangle trap : traps) {
            if (trap.getWidth() > 0 && trap.intersects(playerRect)) {
                trap.setSize(0, 0);
                repaint();
                return -TRAP_PENALTY;
            }
        }
        return 0;
    }


    public int pickupPrize() {
        Rectangle playerRect = new Rectangle(playerX, playerY, SPACE_SIZE / 2, SPACE_SIZE / 2);
        for (Rectangle prize : prizes) {
            if (prize.getWidth() > 0 && prize.intersects(playerRect)) {
                prize.setSize(0, 0);
                repaint();
                return PRIZE_SCORE;
            }
        }
        return -PRIZE_SCORE;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;


        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, null);
        } else {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }


        g2.setColor(Color.RED);
        for (Rectangle trap : traps) {
            if (trap.getWidth() > 0) {
                if (trapImage != null) g2.drawImage(trapImage, trap.x, trap.y, 20, 20, null);
                else g2.fillRect(trap.x, trap.y, trap.width, trap.height);
            }
        }


        g2.setColor(Color.YELLOW);
        for (Rectangle prize : prizes) {
            if (prize.getWidth() > 0) {
                if (prizeImage != null) g2.drawImage(prizeImage, prize.x, prize.y, 15, 15, null);
                else g2.fillOval(prize.x, prize.y, prize.width, prize.height);
            }
        }


        g2.setColor(Color.BLACK);
        for (Rectangle wall : walls) {
            g2.fill(wall);
        }


        if (playerImage != null) {
            g2.drawImage(playerImage, playerX, playerY, 40, 40, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillOval(playerX, playerY, 40, 40);
        }
    }


    public void showGameOverScreen() {
        JOptionPane.showMessageDialog(frame,
                "GAME OVER! You stepped on a trap!",
                "Game Over",
                JOptionPane.ERROR_MESSAGE);
        frame.dispose();
    }


    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();


        JMenu gameMenu = new JMenu("Game");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> frame.dispose());
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);


        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "EscapeRoom Game\nMove with WASD or arrow keys\nCollect prizes and avoid traps!",
                "About",
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);


        return menuBar;
    }


    public static void main(String[] args) {
        // Run GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new GameGUI());
    }
}


