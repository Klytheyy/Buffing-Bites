import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    private final int TILE_SIZE = 20;
    private final int BOARD_COLUMNS = 30;
    private final int BOARD_ROWS = 20;

    private int[] snakeX = new int[BOARD_COLUMNS * BOARD_ROWS];
    private int[] snakeY = new int[BOARD_COLUMNS * BOARD_ROWS];
    private int snakeLength = 3;
    private int foodX;
    private int foodY;

    private String direction = "RIGHT";
    private String nextDirection = "RIGHT";
    private Timer gameTimer;
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private boolean scoreSaved = false;

    private final ScoreManager scoreManager = new ScoreManager();
    private final DataManager dataManager = new DataManager();

    public GamePanel() {
        setPreferredSize(new Dimension(BOARD_COLUMNS * TILE_SIZE, BOARD_ROWS * TILE_SIZE));
        setBackground(Color.decode("#1A0B2E"));
        setFocusable(true);
        setupKeyboardControls();
        initializeSnake();
        spawnRegularFood();
        gameTimer = new Timer(100, this);
    }

    private void initializeSnake() {
        snakeLength = 3;
        direction = "RIGHT";
        nextDirection = "RIGHT";
        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = BOARD_COLUMNS / 2 - i;
            snakeY[i] = BOARD_ROWS / 2;
        }
    }

    private void setupKeyboardControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && !direction.equals("DOWN")) {
                    nextDirection = "UP";
                } else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && !direction.equals("UP")) {
                    nextDirection = "DOWN";
                } else if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && !direction.equals("RIGHT")) {
                    nextDirection = "LEFT";
                } else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && !direction.equals("LEFT")) {
                    nextDirection = "RIGHT";
                } else if (key == KeyEvent.VK_ENTER && gameOver) {
                    startGame();
                }
            }
        });
    }

    private void spawnRegularFood() {
        boolean onSnake = true;
        while (onSnake) {
            foodX = (int) (Math.random() * BOARD_COLUMNS);
            foodY = (int) (Math.random() * BOARD_ROWS);
            onSnake = false;
            for (int i = 0; i < snakeLength; i++) {
                if (snakeX[i] == foodX && snakeY[i] == foodY) {
                    onSnake = true;
                    break;
                }
            }
        }
    }

    private void moveSnake() {
        direction = nextDirection;
        for (int i = snakeLength - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }
        if (direction.equals("UP")) {
            snakeY[0]--;
        } else if (direction.equals("DOWN")) {
            snakeY[0]++;
        } else if (direction.equals("LEFT")) {
            snakeX[0]--;
        } else if (direction.equals("RIGHT")) {
            snakeX[0]++;
        }
    }

    private void checkRegularFoodCollision() {
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            snakeLength++;
            snakeX[snakeLength - 1] = snakeX[snakeLength - 2];
            snakeY[snakeLength - 1] = snakeY[snakeLength - 2];
            scoreManager.addFoodPoints();
            spawnRegularFood();
        }
    }

    private void checkCollisions() {
        if (snakeX[0] < 0 || snakeX[0] >= BOARD_COLUMNS
                || snakeY[0] < 0 || snakeY[0] >= BOARD_ROWS) {
            endGame();
            return;
        }
        for (int i = 1; i < snakeLength; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                endGame();
                return;
            }
        }
    }

    private void endGame() {
        if (gameOver) {
            return;
        }
        gameOver = true;
        gameRunning = false;
        gameTimer.stop();
        scoreManager.endGame();
        repaint();

        String playerName = JOptionPane.showInputDialog(
            SwingUtilities.getWindowAncestor(this),
            "Game over! Your score is " + scoreManager.getCurrentScore()
            + " points.\nEnter your name to save your score:",
            "Save Score", JOptionPane.PLAIN_MESSAGE
        );
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        boolean saved = dataManager.saveScore(playerName, scoreManager.getCurrentScore());
        if (!saved) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "The score could not be saved. Please check the data folder permissions.",
                "Save Error", JOptionPane.WARNING_MESSAGE
            );
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameRunning) {
            moveSnake();
            checkRegularFoodCollision();
            checkCollisions();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(62, 39, 102, 130));
        for (int x = 0; x <= BOARD_COLUMNS; x++) {
            g2.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, BOARD_ROWS * TILE_SIZE);
        }
        for (int y = 0; y <= BOARD_ROWS; y++) {
            g2.drawLine(0, y * TILE_SIZE, BOARD_COLUMNS * TILE_SIZE, y * TILE_SIZE);
        }

        g2.setColor(Color.decode("#FF3B5C"));
        g2.fillOval(foodX * TILE_SIZE + 2, foodY * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);

        for (int i = 0; i < snakeLength; i++) {
            g2.setColor(i == 0 ? Color.decode("#E0AAFF") : Color.decode("#5CFFB1"));
            g2.fillRoundRect(snakeX[i] * TILE_SIZE + 1, snakeY[i] * TILE_SIZE + 1,
                    TILE_SIZE - 2, TILE_SIZE - 2, 5, 5);
        }

        g2.setColor(Color.decode("#F5EBFF"));
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("SCORE: " + scoreManager.getCurrentScore(), 10, 20);
        g2.drawString("BUFFINGBITES", 10, getHeight() - 10);

        if (!gameRunning && !gameOver) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 26));
            g2.drawString("READY", 270, 180);
        }
        if (gameOver) {
            g2.setColor(new Color(26, 11, 46, 220));
            g2.fillRoundRect(100, 125, 400, 150, 18, 18);
            g2.setColor(Color.decode("#2EE6D6"));
            g2.setFont(new Font("SansSerif", Font.BOLD, 32));
            g2.drawString("GAME OVER", 195, 175);
            g2.setColor(Color.decode("#F5EBFF"));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString("Score: " + scoreManager.getCurrentScore(), 240, 210);
            g2.drawString("Press ENTER to play again", 185, 240);
        }
        g2.dispose();
    }

    public void startGame() {
        initializeSnake();
        spawnRegularFood();
        scoreManager.resetScore();
        gameOver = false;
        scoreSaved = false;
        gameRunning = true;
        gameTimer.start();
        requestFocusInWindow();
        repaint();
    }

    public void stopGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameRunning = false;
    }
}
