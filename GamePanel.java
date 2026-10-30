import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    // BuffingBites palette
    private static final Color MIDNIGHT = Color.decode("#1A0B2E");
    private static final Color GRID = Color.decode("#3E2766");
    private static final Color HOT_RED = Color.decode("#FF3B5C");
    private static final Color GOLD = Color.decode("#FFD166");
    private static final Color HEAD = Color.decode("#E0AAFF");
    private static final Color MINT = Color.decode("#5CFFB1");
    private static final Color TEXT = Color.decode("#F5EBFF");
    private static final Color TEAL = Color.decode("#2EE6D6");
    private static final Color PURPLE = Color.decode("#7B2FF7");

    private static final int TILE_SIZE = 20;
    private static final int COLUMNS = 30;
    private static final int ROWS = 20;
    private static final int BOARD_WIDTH = COLUMNS * TILE_SIZE;
    private static final int BOARD_HEIGHT = ROWS * TILE_SIZE;

    private final int[] snakeX = new int[COLUMNS * ROWS];
    private final int[] snakeY = new int[COLUMNS * ROWS];
    private final Random random = new Random();

    private int snakeLength = 3;
    private int foodX, foodY;
    private String direction = "RIGHT";
    private String nextDirection = "RIGHT";

    private final ScoreManager scoreManager = new ScoreManager();
    private final DataManager dataManager = new DataManager();
    private final LevelManager levelManager = new LevelManager();

    private Timer gameTimer;
    private boolean gameRunning;
    private boolean paused;
    private boolean gameOver;
    private boolean scoreSaved;

    // Fruit details are kept together so each fruit has a clear type and timer.
    private enum FruitType {
        GRAPE, GREEN_APPLE, BLUEBERRY, GOLDEN_PEAR
    }

    private FruitType activeFruit;
    private int fruitX, fruitY;
    private long fruitSpawnTime;
    private long nextFruitSpawnTime;
    private long effectEndTime;
    private int speedChange = 0;
    private int multiplierFoodsLeft = 0;
    private double pointMultiplier = 1.0;
    private boolean shieldActive;
    private boolean shieldUsed;
    private long pausedAt;

    private JLabel scoreLabel;
    private JLabel statusLabel;
    private final JPanel boardPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard((Graphics2D) g);
        }
    };

    public GamePanel() {
        this(LevelManager.Difficulty.MEDIUM);
    }

    public GamePanel(LevelManager.Difficulty difficulty) {
        levelManager.setDifficulty(difficulty);
        setLayout(new BorderLayout());
        setBackground(MIDNIGHT);
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + 86));

        // HUD is outside the board, so the score never covers the snake or food.
        add(createTopHUD(), BorderLayout.NORTH);

        boardPanel.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        boardPanel.setBackground(MIDNIGHT);
        add(boardPanel, BorderLayout.CENTER);

        add(createBottomBar(), BorderLayout.SOUTH);

        initializeSnake();
        spawnRegularFood();
        nextFruitSpawnTime = System.currentTimeMillis() + 5000;

        gameTimer = new Timer(levelManager.getTimerDelay(), this);
        setupKeyboardControls();
    }

    private JPanel createTopHUD() {
        JPanel hud = new JPanel(new BorderLayout());
        hud.setBackground(MIDNIGHT);
        hud.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        scoreLabel = new JLabel("SCORE: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        scoreLabel.setForeground(TEAL);

        JLabel title = new JLabel("BUFFINGBITES");
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(TEXT);

        JLabel difficulty = new JLabel("MODE: " + levelManager.getDifficulty());
        difficulty.setFont(new Font("SansSerif", Font.BOLD, 13));
        difficulty.setForeground(Color.decode("#C9A7FF"));

        hud.add(scoreLabel, BorderLayout.WEST);
        hud.add(title, BorderLayout.CENTER);
        hud.add(difficulty, BorderLayout.EAST);
        return hud;
    }

    private JPanel createBottomBar() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.decode("#2D1B4E"));
        bottom.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));

        statusLabel = new JLabel("Collect food and avoid the walls!");
        statusLabel.setForeground(TEXT);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel controls = new JLabel("ARROWS / WASD: MOVE     ESC: PAUSE");
        controls.setForeground(TEAL);
        controls.setFont(new Font("SansSerif", Font.BOLD, 11));

        bottom.add(statusLabel, BorderLayout.WEST);
        bottom.add(controls, BorderLayout.EAST);
        return bottom;
    }

    private void setupKeyboardControls() {
        // Key bindings work even when a child component has focus.
        InputMap input = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();

        input.put(KeyStroke.getKeyStroke("UP"), "up");
        input.put(KeyStroke.getKeyStroke("W"), "up");
        input.put(KeyStroke.getKeyStroke("DOWN"), "down");
        input.put(KeyStroke.getKeyStroke("S"), "down");
        input.put(KeyStroke.getKeyStroke("LEFT"), "left");
        input.put(KeyStroke.getKeyStroke("A"), "left");
        input.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        input.put(KeyStroke.getKeyStroke("D"), "right");
        input.put(KeyStroke.getKeyStroke("ESCAPE"), "pause");

        actions.put("up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!"DOWN".equals(direction)) nextDirection = "UP";
            }
        });
        actions.put("down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!"UP".equals(direction)) nextDirection = "DOWN";
            }
        });
        actions.put("left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!"RIGHT".equals(direction)) nextDirection = "LEFT";
            }
        });
        actions.put("right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!"LEFT".equals(direction)) nextDirection = "RIGHT";
            }
        });
        actions.put("pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
    }

    private void initializeSnake() {
        snakeLength = 3;
        direction = "RIGHT";
        nextDirection = "RIGHT";
        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = COLUMNS / 2 - i;
            snakeY[i] = ROWS / 2;
        }
    }

    private void spawnRegularFood() {
        int[] cell = findEmptyCell();
        foodX = cell[0];
        foodY = cell[1];
    }

    // Searches only valid board cells and avoids the snake and special fruit.
    private int[] findEmptyCell() {
        for (int attempt = 0; attempt < COLUMNS * ROWS * 2; attempt++) {
            int x = random.nextInt(COLUMNS);
            int y = random.nextInt(ROWS);
            if (!isSnakeAt(x, y)
                    && !(activeFruit != null && fruitX == x && fruitY == y)
                    && !(foodX == x && foodY == y)) {
                return new int[]{x, y};
            }
        }
        return new int[]{random.nextInt(COLUMNS), random.nextInt(ROWS)};
    }

    private boolean isSnakeAt(int x, int y) {
        for (int i = 0; i < snakeLength; i++) {
            if (snakeX[i] == x && snakeY[i] == y) return true;
        }
        return false;
    }

    private void moveSnake() {
        direction = nextDirection;
        for (int i = snakeLength - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }
        switch (direction) {
            case "UP": snakeY[0]--; break;
            case "DOWN": snakeY[0]++; break;
            case "LEFT": snakeX[0]--; break;
            case "RIGHT": snakeX[0]++; break;
        }
    }

    private void checkRegularFood() {
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            if (snakeLength < snakeX.length) {
                snakeX[snakeLength] = snakeX[snakeLength - 1];
                snakeY[snakeLength] = snakeY[snakeLength - 1];
                snakeLength++;
            }
            int points = (int) Math.round(10 * pointMultiplier);
            scoreManager.addBonusPoints(points);
            if (multiplierFoodsLeft > 0) {
                multiplierFoodsLeft--;
                if (multiplierFoodsLeft == 0) pointMultiplier = 1.0;
            }
            spawnRegularFood();
            updateHUD();
        }
    }

    private void spawnFruitIfNeeded(long now) {
        if (activeFruit == null && now >= nextFruitSpawnTime) {
            int[] cell = findEmptyCell();
            fruitX = cell[0];
            fruitY = cell[1];
            FruitType[] types = FruitType.values();
            activeFruit = types[random.nextInt(types.length)];
            fruitSpawnTime = now;
            statusLabel.setText("Special fruit appeared! Collect it before it rots.");
        }
    }

    private long fruitLifetime() {
        switch (levelManager.getDifficulty()) {
            case EASY: return 20_000;
            case HARD: return 10_000;
            default: return 15_000;
        }
    }

    private void updateFruit(long now) {
        if (activeFruit == null) return;

        if (now - fruitSpawnTime >= fruitLifetime()) {
            // The fruit stays in place after rotting; its effect changes to a penalty.
            statusLabel.setText("Rotten " + fruitName(activeFruit) + "! Collect it carefully.");
        }

        if (snakeX[0] == fruitX && snakeY[0] == fruitY) {
            boolean rotten = now - fruitSpawnTime >= fruitLifetime();
            applyFruitEffect(activeFruit, rotten, now);
            activeFruit = null;
            nextFruitSpawnTime = now + 8000 + random.nextInt(7001);
            updateHUD();
        }
    }

    private String fruitName(FruitType fruit) {
        switch (fruit) {
            case GRAPE: return "Grape";
            case GREEN_APPLE: return "Green Apple";
            case BLUEBERRY: return "Blueberry";
            default: return "Golden Pear";
        }
    }

    // Fresh and rotten versions of each fruit apply different effects.
    private void applyFruitEffect(FruitType fruit, boolean rotten, long now) {
        switch (fruit) {
            case GRAPE:
                speedChange = rotten ? -1 : 1;
                effectEndTime = now + 7000;
                updateTimerSpeed();
                statusLabel.setText(rotten
                        ? "Rotten Grape: speed reduced for 7 seconds."
                        : "Grape: speed increased for 7 seconds!");
                break;
            case GREEN_APPLE:
                pointMultiplier = rotten ? 0.5 : 2.0;
                multiplierFoodsLeft = 5;
                statusLabel.setText(rotten
                        ? "Rotten Green Apple: half points for 5 foods."
                        : "Green Apple: double points for 5 foods!");
                break;
            case BLUEBERRY:
                if (rotten) {
                    shieldActive = false;
                    statusLabel.setText("Rotten Blueberry: collision shield removed.");
                } else {
                    shieldActive = true;
                    shieldUsed = false;
                    effectEndTime = now + 10_000;
                    statusLabel.setText("Blueberry: one collision shield for 10 seconds!");
                }
                break;
            case GOLDEN_PEAR:
                scoreManager.addBonusPoints(rotten ? -15 : 30);
                statusLabel.setText(rotten
                        ? "Rotten Golden Pear: -15 points."
                        : "Golden Pear: +30 points!");
                break;
        }
        updateHUD();
    }

    private void updateEffectTimers(long now) {
        if (speedChange != 0 && now >= effectEndTime) {
            speedChange = 0;
            updateTimerSpeed();
            statusLabel.setText("Grape effect has ended.");
        }
        if (shieldActive && now >= effectEndTime && speedChange == 0) {
            shieldActive = false;
        }
    }

    private void updateTimerSpeed() {
        int base = levelManager.getTimerDelay();
        int adjusted = speedChange > 0 ? (int) (base * 0.85)
                : speedChange < 0 ? (int) (base * 1.15) : base;
        gameTimer.setDelay(Math.max(35, adjusted));
    }

    private void checkCollisions() {
        boolean hitWall = snakeX[0] < 0 || snakeX[0] >= COLUMNS
                || snakeY[0] < 0 || snakeY[0] >= ROWS;
        boolean hitBody = false;
        for (int i = 1; i < snakeLength; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                hitBody = true;
                break;
            }
        }

        if (hitWall || hitBody) {
            if (shieldActive && !shieldUsed) {
                shieldUsed = true;
                shieldActive = false;
                // Shield consumes one collision and puts the head back in bounds.
                snakeX[0] = Math.max(0, Math.min(COLUMNS - 1, snakeX[0]));
                snakeY[0] = Math.max(0, Math.min(ROWS - 1, snakeY[0]));
                statusLabel.setText("Shield used! You survived one collision.");
            } else {
                endGame();
            }
        }
    }

    private void updateHUD() {
        scoreLabel.setText("SCORE: " + scoreManager.getCurrentScore());
        boardPanel.repaint();
    }

    private void togglePause() {
        if (gameOver || !gameRunning) return;

        if (!paused) {
            paused = true;
            pausedAt = System.currentTimeMillis();
            gameTimer.stop();
            statusLabel.setText("PAUSED - Press ESC again to resume.");
        } else {
            long pausedDuration = System.currentTimeMillis() - pausedAt;
            fruitSpawnTime += pausedDuration;
            nextFruitSpawnTime += pausedDuration;
            effectEndTime += pausedDuration;
            paused = false;
            gameTimer.start();
            statusLabel.setText("Game resumed. Good luck!");
        }
        boardPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning || paused || gameOver) return;

        moveSnake();
        checkRegularFood();
        checkCollisions();

        long now = System.currentTimeMillis();
        spawnFruitIfNeeded(now);
        updateFruit(now);
        updateEffectTimers(now);

        updateHUD();
    }

    private void endGame() {
        if (gameOver) return;
        gameOver = true;
        gameRunning = false;
        gameTimer.stop();
        scoreManager.endGame();
        updateHUD();

        if (!scoreSaved) {
            scoreSaved = true;
            String name = JOptionPane.showInputDialog(
                    this,
                    "Game over! Final score: " + scoreManager.getCurrentScore()
                    + "\nEnter your name to save your score:",
                    "Save Score", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Player";
            if (!dataManager.saveScore(name.trim(), scoreManager.getCurrentScore())) {
                JOptionPane.showMessageDialog(this,
                        "Could not save the score. Check the data folder permissions.",
                        "Save Error", JOptionPane.WARNING_MESSAGE);
            }
        }
        boardPanel.repaint();
    }

    private void drawBoard(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(MIDNIGHT);
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Grid lines are clipped to the exact board dimensions.
        g2.setColor(GRID);
        for (int x = 0; x <= COLUMNS; x++) {
            int px = x * TILE_SIZE;
            g2.drawLine(px, 0, px, BOARD_HEIGHT);
        }
        for (int y = 0; y <= ROWS; y++) {
            int py = y * TILE_SIZE;
            g2.drawLine(0, py, BOARD_WIDTH, py);
        }

        // Regular food orb
        g2.setColor(HOT_RED);
        g2.fillOval(foodX * TILE_SIZE + 3, foodY * TILE_SIZE + 3,
                TILE_SIZE - 6, TILE_SIZE - 6);
        g2.setColor(GOLD);
        g2.fillOval(foodX * TILE_SIZE + 6, foodY * TILE_SIZE + 5, 4, 4);

        // Special fruit uses a distinct color and a letter marker.
        if (activeFruit != null) {
            boolean rotten = System.currentTimeMillis() - fruitSpawnTime >= fruitLifetime();
            Color fruitColor = fruitColor(activeFruit, rotten);
            g2.setColor(fruitColor);
            g2.fillOval(fruitX * TILE_SIZE + 2, fruitY * TILE_SIZE + 2,
                    TILE_SIZE - 4, TILE_SIZE - 4);
            g2.setColor(TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(fruitLetter(activeFruit),
                    fruitX * TILE_SIZE + 6, fruitY * TILE_SIZE + 14);
        }

        for (int i = 0; i < snakeLength; i++) {
            g2.setColor(i == 0 ? HEAD : MINT);
            g2.fillRoundRect(snakeX[i] * TILE_SIZE + 1,
                    snakeY[i] * TILE_SIZE + 1,
                    TILE_SIZE - 2, TILE_SIZE - 2, 5, 5);
        }

        if (paused || gameOver || !gameRunning) {
            g2.setColor(new Color(26, 11, 46, 220));
            int boxW = 300, boxH = 145;
            int boxX = (BOARD_WIDTH - boxW) / 2;
            int boxY = (BOARD_HEIGHT - boxH) / 2;
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
            g2.setColor(PURPLE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);
            g2.setColor(TEAL);
            g2.setFont(new Font("SansSerif", Font.BOLD, 27));
            String message = gameOver ? "GAME OVER" : paused ? "PAUSED" : "READY";
            g2.drawString(message, boxX + 55, boxY + 48);
            g2.setColor(TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String detail = gameOver
                    ? "FINAL SCORE: " + scoreManager.getCurrentScore()
                    : paused ? "Press ESC to resume" : "Get ready!";
            g2.drawString(detail, boxX + 55, boxY + 82);
            if (gameOver) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString("Close this window to return to Main Menu",
                        boxX + 28, boxY + 112);
            }
        }
    }

    private Color fruitColor(FruitType fruit, boolean rotten) {
        if (rotten) return Color.decode("#8A8A2B");
        switch (fruit) {
            case GRAPE: return Color.decode("#9D4EDD");
            case GREEN_APPLE: return MINT;
            case BLUEBERRY: return Color.decode("#2EE6D6");
            default: return GOLD;
        }
    }

    private String fruitLetter(FruitType fruit) {
        switch (fruit) {
            case GRAPE: return "G";
            case GREEN_APPLE: return "A";
            case BLUEBERRY: return "B";
            default: return "P";
        }
    }

    public void startGame() {
        initializeSnake();
        activeFruit = null;
        speedChange = 0;
        multiplierFoodsLeft = 0;
        pointMultiplier = 1.0;
        shieldActive = false;
        shieldUsed = false;
        scoreSaved = false;
        scoreManager.resetScore();
        spawnRegularFood();
        long now = System.currentTimeMillis();
        nextFruitSpawnTime = now + 5000;
        gameOver = false;
        paused = false;
        gameRunning = true;
        gameTimer.setDelay(levelManager.getTimerDelay());
        gameTimer.start();
        updateHUD();
        requestFocusInWindow();
        boardPanel.repaint();
    }

    public void stopGame() {
        if (gameTimer != null) gameTimer.stop();
        gameRunning = false;
    }
}
