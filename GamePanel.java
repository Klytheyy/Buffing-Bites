package com.mycompany.buffingbites;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
    private long foodSpawnTime;     // when the current regular apple appeared
    private boolean foodRotten;     // true once the apple has rotted
    private static final int ROTTEN_FOOD_PENALTY = -5;
    private String direction = "RIGHT";

    // Up to two queued turns, so quick key combos (UP then LEFT) are not lost.
    private final ArrayDeque<String> inputQueue = new ArrayDeque<>();

    // Remembers where the tail was before moving, so the shield can undo a move.
    private int prevTailX, prevTailY;

    private final ScoreManager scoreManager = new ScoreManager();
    // SINGLETON: use the one shared DataManager instead of "new DataManager()".
    private final DataManager dataManager = DataManager.getInstance();
    private final LevelManager levelManager = new LevelManager();

    // SMOOTH MOVEMENT: the game logic still moves the snake one cell per "tick",
    // but this timer runs at about 60 frames per second. Each frame draws the
    // snake part of the way between its previous and current cell.
    private static final int FRAME_MS = 16;
    private static final int MAX_TICK_BACKLOG = 3;
    private final int[] prevSegX = new int[COLUMNS * ROWS];
    private final int[] prevSegY = new int[COLUMNS * ROWS];
    private int tickDelayMs;          // how long one cell step takes
    private long lastFrameNs;
    private long accumulatorNs;       // time collected towards the next step
    private double moveProgress = 1.0; // 0..1 through the current step
    private boolean stationary = true;  // true while the snake is standing still (start, shield stop)

    private Timer gameTimer;
    private boolean gameRunning;
    private boolean paused;
    private boolean gameOver;
    private boolean scoreSaved;

    // FACTORY METHOD: one creator per fruit type. spawnFruitIfNeeded picks one
    // at random and asks it to build the fruit.
    private final SpecialFruitCreator[] fruitCreators = {
        new GrapeCreator(),
        new BananaCreator(),
        new OrangeCreator(),
        new GoldenPearCreator()
    };

    // The special fruit currently on the board (null when there is none).
    private SpecialFruit activeFruit;
    private long nextFruitSpawnTime;

    // Separate timers so a grape and an orange do not overwrite each other.
    private long speedEndTime;
    private long shieldEndTime;
    private int speedChange = 0;
    private int multiplierFoodsLeft = 0;
    private double pointMultiplier = 1.0;
    private boolean shieldActive;
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

        tickDelayMs = levelManager.getTimerDelay();
        gameTimer = new Timer(FRAME_MS, this);
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
                queueDirection("UP");
            }
        });
        actions.put("down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                queueDirection("DOWN");
            }
        });
        actions.put("left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                queueDirection("LEFT");
            }
        });
        actions.put("right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                queueDirection("RIGHT");
            }
        });
        actions.put("pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
    }

    // Adds a turn to the queue unless it repeats or reverses the last queued turn.
    private void queueDirection(String newDirection) {
        if (!gameRunning || paused || gameOver) return;

        String last = inputQueue.isEmpty() ? direction : inputQueue.peekLast();
        if (newDirection.equals(last) || newDirection.equals(opposite(last))) return;
        if (inputQueue.size() < 2) {
            inputQueue.addLast(newDirection);
        }
    }

    private String opposite(String dir) {
        switch (dir) {
            case "UP": return "DOWN";
            case "DOWN": return "UP";
            case "LEFT": return "RIGHT";
            default: return "LEFT";
        }
    }

    private void initializeSnake() {
        snakeLength = 3;
        direction = "RIGHT";
        inputQueue.clear();
        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = COLUMNS / 2 - i;
            snakeY[i] = ROWS / 2;
        }
        syncPreviousPositions();
    }

    // Makes "previous" equal "current" so the snake is drawn standing still.
    private void syncPreviousPositions() {
        stationary = true;
        System.arraycopy(snakeX, 0, prevSegX, 0, snakeLength);
        System.arraycopy(snakeY, 0, prevSegY, 0, snakeLength);
    }

    private void spawnRegularFood() {
        int[] cell = findEmptyCell();
        foodX = cell[0];
        foodY = cell[1];
        foodSpawnTime = System.currentTimeMillis();
        foodRotten = false;
    }

    // Finds a board cell that is not used by the snake, the food or the fruit.
    private int[] findEmptyCell() {
        for (int attempt = 0; attempt < COLUMNS * ROWS * 2; attempt++) {
            int x = random.nextInt(COLUMNS);
            int y = random.nextInt(ROWS);
            if (isCellFree(x, y)) {
                return new int[]{x, y};
            }
        }
        // Almost-full board: scan every cell so we never pick one on the snake.
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (isCellFree(x, y)) {
                    return new int[]{x, y};
                }
            }
        }
        return new int[]{random.nextInt(COLUMNS), random.nextInt(ROWS)};
    }

    private boolean isCellFree(int x, int y) {
        return !isSnakeAt(x, y)
                && !(activeFruit != null && activeFruit.x == x && activeFruit.y == y)
                && !(foodX == x && foodY == y);
    }

    private boolean isSnakeAt(int x, int y) {
        for (int i = 0; i < snakeLength; i++) {
            if (snakeX[i] == x && snakeY[i] == y) return true;
        }
        return false;
    }

    private void moveSnake() {
        if (!inputQueue.isEmpty()) {
            direction = inputQueue.pollFirst();
        }

        stationary = false;

        // Where every segment was before this step (used for smooth drawing).
        System.arraycopy(snakeX, 0, prevSegX, 0, snakeLength);
        System.arraycopy(snakeY, 0, prevSegY, 0, snakeLength);

        // Saved so the Orange shield can undo this move.
        prevTailX = snakeX[snakeLength - 1];
        prevTailY = snakeY[snakeLength - 1];

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
            if (foodRotten) {
                // A rotten apple costs points and does not make the snake grow.
                scoreManager.addBonusPoints(ROTTEN_FOOD_PENALTY);
                statusLabel.setText("Rotten apple! " + ROTTEN_FOOD_PENALTY
                        + " points and no growth.");
            } else {
                if (snakeLength < snakeX.length) {
                    // The new tail piece appears where the old tail was.
                    snakeX[snakeLength] = prevTailX;
                    snakeY[snakeLength] = prevTailY;
                    prevSegX[snakeLength] = prevTailX;
                    prevSegY[snakeLength] = prevTailY;
                    snakeLength++;
                }
                int points = (int) Math.round(10 * pointMultiplier);
                scoreManager.addBonusPoints(points);
                if (multiplierFoodsLeft > 0) {
                    multiplierFoodsLeft--;
                    if (multiplierFoodsLeft == 0) pointMultiplier = 1.0;
                }
            }
            spawnRegularFood();
            updateHUD();
        }
    }

    // The regular apple rots after the same time as the special fruits.
    private void updateFood(long now) {
        if (!foodRotten && now - foodSpawnTime >= fruitLifetime()) {
            foodRotten = true;
            statusLabel.setText("The apple has rotted! Eating it costs points.");
        }
    }

    private void spawnFruitIfNeeded(long now) {
        if (activeFruit == null && now >= nextFruitSpawnTime) {
            int[] cell = findEmptyCell();
            // Factory Method: pick a creator, let it decide which fruit to build.
            SpecialFruitCreator creator = fruitCreators[random.nextInt(fruitCreators.length)];
            activeFruit = creator.createFruit(cell[0], cell[1], now);
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

        // The fruit stays in place after rotting; its effect becomes a penalty.
        if (!activeFruit.rotten && now - activeFruit.spawned >= fruitLifetime()) {
            activeFruit.rotten = true;
            statusLabel.setText("Rotten " + fruitName(activeFruit.type) + "! Collect it carefully.");
        }

        if (snakeX[0] == activeFruit.x && snakeY[0] == activeFruit.y) {
            applyFruitEffect(activeFruit.type, activeFruit.rotten, now);
            activeFruit = null;
            nextFruitSpawnTime = now + 8000 + random.nextInt(7001);
            updateHUD();
        }
    }

    private String fruitName(FruitType fruit) {
        switch (fruit) {
            case GRAPE: return "Grape";
            case BANANA: return "Banana";
            case ORANGE: return "Orange";
            default: return "Golden Pear";
        }
    }

    // Fresh and rotten versions of each fruit apply different effects.
    private void applyFruitEffect(FruitType fruit, boolean rotten, long now) {
        switch (fruit) {
            case GRAPE:
                speedChange = rotten ? -1 : 1;
                speedEndTime = now + 7000;
                updateTimerSpeed();
                statusLabel.setText(rotten
                        ? "Rotten Grape: speed reduced for 7 seconds."
                        : "Grape: speed increased for 7 seconds!");
                break;
            case BANANA:
                pointMultiplier = rotten ? 0.5 : 2.0;
                multiplierFoodsLeft = 5;
                statusLabel.setText(rotten
                        ? "Rotten Banana: half points for 5 foods."
                        : "Banana: double points for 5 foods!");
                break;
            case ORANGE:
                if (rotten) {
                    shieldActive = false;
                    statusLabel.setText("Rotten Orange: collision shield removed.");
                } else {
                    shieldActive = true;
                    shieldEndTime = now + 10_000;
                    statusLabel.setText("Orange: one collision shield for 10 seconds!");
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
        if (speedChange != 0 && now >= speedEndTime) {
            speedChange = 0;
            updateTimerSpeed();
            statusLabel.setText("Grape effect has ended.");
        }
        if (shieldActive && now >= shieldEndTime) {
            shieldActive = false;
            statusLabel.setText("Shield expired.");
        }
    }

    private void updateTimerSpeed() {
        int base = levelManager.getTimerDelay();
        int adjusted = speedChange > 0 ? (int) (base * 0.85)
                : speedChange < 0 ? (int) (base * 1.15) : base;
        tickDelayMs = Math.max(35, adjusted);
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
            // Undo the move: every segment goes back to where it was.
            // With the shield the snake stays put for one step so the player can
            // turn; without it, the snake stays on its last safe cell.
            undoMove();
            if (shieldActive) {
                shieldActive = false;
                statusLabel.setText("Shield used! Turn now!");
            } else {
                endGame();
            }
        }
    }

    private void undoMove() {
        for (int i = 0; i < snakeLength - 1; i++) {
            snakeX[i] = snakeX[i + 1];
            snakeY[i] = snakeY[i + 1];
        }
        snakeX[snakeLength - 1] = prevTailX;
        snakeY[snakeLength - 1] = prevTailY;
        syncPreviousPositions();
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
            if (activeFruit != null) activeFruit.spawned += pausedDuration;
            foodSpawnTime += pausedDuration;
            nextFruitSpawnTime += pausedDuration;
            speedEndTime += pausedDuration;
            shieldEndTime += pausedDuration;
            paused = false;
            lastFrameNs = System.nanoTime();
            gameTimer.start();
            statusLabel.setText("Game resumed. Good luck!");
        }
        boardPanel.repaint();
    }

    // Called about 60 times per second. It collects elapsed time, runs a game
    // step each time one full step-duration has passed, and then draws the
    // snake partway through its current step.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning || paused || gameOver) return;

        long nowNs = System.nanoTime();
        accumulatorNs += nowNs - lastFrameNs;
        lastFrameNs = nowNs;

        long stepNs = tickDelayMs * 1_000_000L;
        // After a long freeze, do not rush through many steps at once.
        if (accumulatorNs > stepNs * MAX_TICK_BACKLOG) {
            accumulatorNs = stepNs;
        }
        while (accumulatorNs >= stepNs && gameRunning && !gameOver) {
            accumulatorNs -= stepNs;
            step();
        }

        moveProgress = gameOver ? 1.0 : Math.min(1.0, accumulatorNs / (double) stepNs);
        boardPanel.repaint();
    }

    // One game step: the snake moves one cell and everything is checked.
    private void step() {
        updateFood(System.currentTimeMillis());
        moveSnake();
        checkRegularFood();
        checkCollisions();
        if (gameOver) return;

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

        // Regular food: the apple picture (orb if the picture is missing)
        BufferedImage foodImage = SpecialFruitAssets.getFoodImage(foodRotten);
        if (foodImage != null) {
            g2.drawImage(foodImage, foodX * TILE_SIZE, foodY * TILE_SIZE,
                    TILE_SIZE, TILE_SIZE, this);
        } else {
            g2.setColor(foodRotten ? Color.decode("#8A8A2B") : HOT_RED);
            g2.fillOval(foodX * TILE_SIZE + 3, foodY * TILE_SIZE + 3,
                    TILE_SIZE - 6, TILE_SIZE - 6);
            if (foodRotten) {
                g2.setColor(Color.decode("#2B1D0E"));
                g2.fillOval(foodX * TILE_SIZE + 6, foodY * TILE_SIZE + 6, 4, 4);
                g2.fillOval(foodX * TILE_SIZE + 11, foodY * TILE_SIZE + 10, 3, 3);
            } else {
                g2.setColor(GOLD);
                g2.fillOval(foodX * TILE_SIZE + 6, foodY * TILE_SIZE + 5, 4, 4);
            }
        }

        drawSpecialFruit(g2);

        drawSnake(g2);

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

    // Space between body pictures along the snake, in pixels. Small steps make
    // curves smooth, because every piece is only turned a little bit.
    private static final double BODY_SPACING = 3.0;

    // How round the corners are (in pixels) once the head has moved away from them.
    private static final double CORNER_RADIUS = TILE_SIZE / 2.0;

    // The route the snake body follows, measured in pixels from the head.
    //
    // It goes through the centers of the cells the snake is in (plus the cell the
    // tail is leaving). The whole snake slides along this route, so every piece
    // follows the piece in front of it. Corners are rounded off with a quarter
    // circle, and a corner starts sharp right behind the head and relaxes into a
    // curve as the head moves away, which keeps the motion free of jumps.
    private final class SnakePath {
        private final double[] px, py;       // route points
        private final double[] along;        // distance from the first point
        private final double[] dirX, dirY;   // direction of each route segment
        private final double[] fillet;       // corner rounding at each point
        private final int count;
        final double headDist;               // where the head is on the route
        final double tailDist;               // where the tail is on the route

        SnakePath(double t, boolean still) {
            double[] rx = new double[snakeLength + 1];
            double[] ry = new double[snakeLength + 1];
            int c = 0;
            for (int i = 0; i < snakeLength; i++) {
                c = addRoutePoint(rx, ry, c, (snakeX[i] + 0.5) * TILE_SIZE, (snakeY[i] + 0.5) * TILE_SIZE);
            }
            if (!still) {
                // The cell the tail is leaving during this step.
                c = addRoutePoint(rx, ry, c,
                        (prevSegX[snakeLength - 1] + 0.5) * TILE_SIZE,
                        (prevSegY[snakeLength - 1] + 0.5) * TILE_SIZE);
            }
            count = c;
            px = rx;
            py = ry;

            along = new double[count];
            dirX = new double[count];
            dirY = new double[count];
            fillet = new double[count];
            for (int i = 1; i < count; i++) {
                double len = Math.hypot(px[i] - px[i - 1], py[i] - py[i - 1]);
                along[i] = along[i - 1] + len;
                dirX[i - 1] = (px[i] - px[i - 1]) / len;
                dirY[i - 1] = (py[i] - py[i - 1]) / len;
            }

            headDist = still ? 0 : (1 - t) * TILE_SIZE;
            tailDist = Math.min(headDist + (snakeLength - 1) * TILE_SIZE, along[count - 1]);

            for (int k = 1; k < count - 1; k++) {
                double dot = dirX[k - 1] * dirX[k] + dirY[k - 1] * dirY[k];
                if (dot < 0.5) { // a real turn
                    double behindHead = (along[k] - headDist) / TILE_SIZE;
                    fillet[k] = CORNER_RADIUS * smoothStep(behindHead);
                }
            }
        }

        // Finds the point "distance" along the route. out = {x, y, angle}, where
        // the angle points the way the snake is heading (0 = right, 90 = down).
        void at(double distance, double[] out) {
            if (count < 2) {
                out[0] = px[0];
                out[1] = py[0];
                out[2] = 0;
                return;
            }
            int k = 0;
            while (k < count - 2 && distance > along[k + 1]) {
                k++;
            }

            // Inside the rounded part of a corner? Follow the quarter circle.
            for (int pass = 0; pass < 2; pass++) {
                int c = pass == 0 ? k + 1 : k;
                if (c >= 1 && c <= count - 2 && fillet[c] > 0.01
                        && Math.abs(distance - along[c]) <= fillet[c]) {
                    double r = fillet[c];
                    double ax = dirX[c - 1], ay = dirY[c - 1];
                    double bx = dirX[c], by = dirY[c];
                    double phi = (distance - along[c] + r) / (2 * r) * (Math.PI / 2);
                    double ox = px[c] - ax * r + bx * r;
                    double oy = py[c] - ay * r + by * r;
                    out[0] = ox - bx * r * Math.cos(phi) + ax * r * Math.sin(phi);
                    out[1] = oy - by * r * Math.cos(phi) + ay * r * Math.sin(phi);
                    double tx = ax * Math.cos(phi) + bx * Math.sin(phi);
                    double ty = ay * Math.cos(phi) + by * Math.sin(phi);
                    out[2] = Math.atan2(-ty, -tx);
                    return;
                }
            }

            // Otherwise it is on a straight part.
            double f = (distance - along[k]) / (along[k + 1] - along[k]);
            out[0] = px[k] + (px[k + 1] - px[k]) * f;
            out[1] = py[k] + (py[k + 1] - py[k]) * f;
            out[2] = Math.atan2(-dirY[k], -dirX[k]);
        }
    }

    // Adds a point to the route unless it is the same spot as the previous one
    // (this happens right after the snake has grown).
    private static int addRoutePoint(double[] xs, double[] ys, int count, double x, double y) {
        if (count > 0 && Math.hypot(x - xs[count - 1], y - ys[count - 1]) < 0.5) {
            return count;
        }
        xs[count] = x;
        ys[count] = y;
        return count + 1;
    }

    private static double smoothStep(double x) {
        double v = Math.max(0, Math.min(1, x));
        return v * v * (3 - 2 * v);
    }

    private void drawSnake(Graphics2D g2) {
        double t = stationary ? 1.0 : moveProgress;
        SnakePath path = new SnakePath(t, stationary);

        SnakeAssets.Layers body = SnakeAssets.getBodyLayers();
        if (body != null) {
            drawSnakeSprites(g2, path, SnakeAssets.getHeadLayers(), body, SnakeAssets.getTailLayers());
        } else {
            drawSnakeShapes(g2, path);
        }

        // Teal ring around the head while the Orange shield is active.
        if (shieldActive) {
            double[] head = new double[3];
            path.at(path.headDist, head);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEAL);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval((int) Math.round(head[0]) - TILE_SIZE / 2 - 2,
                    (int) Math.round(head[1]) - TILE_SIZE / 2 - 2,
                    TILE_SIZE + 4, TILE_SIZE + 4);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    // Used when there are no Piskel pictures: one smooth rounded body.
    private void drawSnakeShapes(Graphics2D g2, SnakePath path) {
        double[] spot = new double[3];
        Path2D.Double body = new Path2D.Double();
        path.at(path.headDist, spot);
        body.moveTo(spot[0], spot[1]);
        for (double d = path.headDist + 2; d < path.tailDist; d += 2) {
            path.at(d, spot);
            body.lineTo(spot[0], spot[1]);
        }
        path.at(path.tailDist, spot);
        body.lineTo(spot[0], spot[1]);

        g2.setStroke(new BasicStroke(TILE_SIZE - 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.decode("#2FBF86"));
        g2.draw(body);
        g2.setStroke(new BasicStroke(TILE_SIZE - 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(MINT);
        g2.draw(body);

        // Head (turns teal while the Orange shield is active)
        path.at(path.headDist, spot);
        double headSize = TILE_SIZE - 1;
        g2.setColor(shieldActive ? TEAL : HEAD);
        g2.fill(new java.awt.geom.Ellipse2D.Double(
                spot[0] - headSize / 2, spot[1] - headSize / 2, headSize, headSize));

        // Eyes look the way the snake is heading.
        double dx = Math.cos(spot[2]);
        double dy = Math.sin(spot[2]);
        double forward = 3.5, side = 3.5, eye = 3.6;
        double ex1 = spot[0] + dx * forward - dy * side;
        double ey1 = spot[1] + dy * forward + dx * side;
        double ex2 = spot[0] + dx * forward + dy * side;
        double ey2 = spot[1] + dy * forward - dx * side;
        g2.setColor(MIDNIGHT);
        g2.fill(new java.awt.geom.Ellipse2D.Double(ex1 - eye / 2, ey1 - eye / 2, eye, eye));
        g2.fill(new java.awt.geom.Ellipse2D.Double(ex2 - eye / 2, ey2 - eye / 2, eye, eye));
        g2.setStroke(new BasicStroke(1f));
    }

    // Draws the snake from the Piskel pictures. All pictures are drawn as if the
    // snake is moving to the RIGHT; the code turns them to follow the route.
    // Outlines of every piece are drawn first, then all the insides on top, so
    // overlapping pieces never leave lines across the body.
    private void drawSnakeSprites(Graphics2D g2, SnakePath path,
            SnakeAssets.Layers head, SnakeAssets.Layers body, SnakeAssets.Layers tail) {

        // Where each body piece goes: {x, y, angle}
        ArrayList<double[]> bodySpots = new ArrayList<>();
        double start = tail != null ? path.tailDist - TILE_SIZE * 0.75 : path.tailDist;
        for (double d = start; d > path.headDist; d -= BODY_SPACING) {
            double[] spot = new double[3];
            path.at(d, spot);
            bodySpots.add(spot);
        }
        double[] tailSpot = new double[3];
        path.at(path.tailDist, tailSpot);
        double[] headSpot = new double[3];
        path.at(path.headDist, headSpot);

        SnakeAssets.Layers headLayers = head != null ? head : body;

        // Keep pixel art crisp: no smoothing while drawing the pictures.
        Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Object oldInterp = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Pass 1: outlines
        for (double[] spot : bodySpots) {
            drawSprite(g2, body.outline, spot[0], spot[1], spot[2]);
        }
        if (tail != null) {
            drawSprite(g2, tail.outline, tailSpot[0], tailSpot[1], tailSpot[2]);
        }
        drawSprite(g2, headLayers.outline, headSpot[0], headSpot[1], headSpot[2]);

        // Pass 2: insides
        for (double[] spot : bodySpots) {
            drawSprite(g2, body.fill, spot[0], spot[1], spot[2]);
        }
        if (tail != null) {
            drawSprite(g2, tail.fill, tailSpot[0], tailSpot[1], tailSpot[2]);
        }
        drawSprite(g2, headLayers.fill, headSpot[0], headSpot[1], headSpot[2]);

        // Put the original drawing settings back.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                oldAA != null ? oldAA : RenderingHints.VALUE_ANTIALIAS_ON);
        if (oldInterp != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
        }
    }

    // Draws one picture centered on (x, y), turned by angle (radians).
    private void drawSprite(Graphics2D g2, BufferedImage image, double x, double y, double angle) {
        AffineTransform saved = g2.getTransform();
        g2.translate(Math.round(x), Math.round(y));
        g2.rotate(angle);
        g2.drawImage(image, -TILE_SIZE / 2, -TILE_SIZE / 2, TILE_SIZE, TILE_SIZE, null);
        g2.setTransform(saved);
    }

    // Draws the fruit's image if it has one; otherwise a colored circle with a letter.
    private void drawSpecialFruit(Graphics2D g2) {
        SpecialFruit fruit = activeFruit; // local copy: the timer may clear the field
        if (fruit == null) return;

        int px = fruit.x * TILE_SIZE;
        int py = fruit.y * TILE_SIZE;

        BufferedImage image = SpecialFruitAssets.getImage(fruit.type, fruit.rotten);
        if (image != null) {
            g2.drawImage(image, px, py, TILE_SIZE, TILE_SIZE, this);
        } else {
            g2.setColor(fruitColor(fruit.type, fruit.rotten));
            g2.fillOval(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4);
            if (fruit.rotten) {
                // Dark decay spots so a rotten fruit looks spoiled.
                g2.setColor(Color.decode("#2B1D0E"));
                g2.fillOval(px + 5, py + 4, 4, 4);
                g2.fillOval(px + 11, py + 8, 4, 4);
                g2.fillOval(px + 6, py + 12, 3, 3);
            }
            g2.setColor(fruit.rotten ? Color.decode("#E8E0B0") : MIDNIGHT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(fruitLetter(fruit.type), px + 6, py + 15);
        }
    }

    private Color fruitColor(FruitType fruit, boolean rotten) {
        if (rotten) return Color.decode("#8A8A2B");
        switch (fruit) {
            case GRAPE: return Color.decode("#9D4EDD");
            case BANANA: return Color.decode("#FFE135");
            case ORANGE: return Color.decode("#FF9F1C");
            default: return GOLD;
        }
    }

    private String fruitLetter(FruitType fruit) {
        switch (fruit) {
            case GRAPE: return "G";
            case BANANA: return "B";
            case ORANGE: return "O";
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
        scoreSaved = false;
        scoreManager.resetScore();
        spawnRegularFood();
        long now = System.currentTimeMillis();
        nextFruitSpawnTime = now + 5000;
        gameOver = false;
        paused = false;
        gameRunning = true;
        tickDelayMs = levelManager.getTimerDelay();
        accumulatorNs = 0;
        moveProgress = 1.0;
        lastFrameNs = System.nanoTime();
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