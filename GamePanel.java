/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.buffingbites;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {
 
    private final int TILE_SIZE = 20;
    private final int BOARD_COLUMNS = 30;
    private final int BOARD_ROWS = 20;
 
    // snake positions are in grid tiles, not pixels
    private int[] snakeX = new int[BOARD_COLUMNS * BOARD_ROWS];
    private int[] snakeY = new int[BOARD_COLUMNS * BOARD_ROWS];
    private int snakeLength = 3;
 
    // regular food position (grid tiles)
    private int foodX;
    private int foodY;
 
    // "UP", "DOWN", "LEFT", "RIGHT"
    private String direction = "RIGHT";
    private String nextDirection = "RIGHT";
 
    private Timer gameTimer;
 
    private boolean gameRunning = false;
    private boolean gameOver = false;
 
    // temporary score for the HUD
    // TODO: replace with ScoreManager once it's ready
    private int score = 0;
 
    public GamePanel() {
        setPreferredSize(new Dimension(
            BOARD_COLUMNS * TILE_SIZE,
            BOARD_ROWS * TILE_SIZE
        ));
 
        setBackground(Color.decode("#1A0B2E"));
        setFocusable(true);
 
        setupKeyboardControls();
        initializeSnake();
        spawnRegularFood();
 
        // update every 100 milliseconds
        gameTimer = new Timer(100, this);
    }
 
    // put the snake in the middle of the board, facing right
    private void initializeSnake() {
        snakeLength = 3;
        direction = "RIGHT";
        nextDirection = "RIGHT";
 
        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = BOARD_COLUMNS / 2 - i;  // body goes to the left of the head
            snakeY[i] = BOARD_ROWS / 2;
        }
    }
 
    // arrow keys + WASD, can't reverse directly
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
                    startGame();  // press enter to restart
                }
            }
        });
    }
 
    // random spot that isn't on the snake
    private void spawnRegularFood() {
        boolean onSnake = true;
 
        while (onSnake) {
            foodX = (int) (Math.random() * BOARD_COLUMNS);
            foodY = (int) (Math.random() * BOARD_ROWS);
 
            onSnake = false;
            for (int i = 0; i < snakeLength; i++) {
                if (snakeX[i] == foodX && snakeY[i] == foodY) {
                    onSnake = true;
                }
            }
        }
    }
 
    private void moveSnake() {
        direction = nextDirection;
 
        // body follows the segment in front of it
        for (int i = snakeLength - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }
 
        // move the head
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
 
            // new tail segment copies the one before it so it doesn't sit at a random spot
            snakeX[snakeLength - 1] = snakeX[snakeLength - 2];
            snakeY[snakeLength - 1] = snakeY[snakeLength - 2];
 
            score += 10;  // TODO: ask ScoreManager to add the points instead
            spawnRegularFood();
        }
    }
 
    private void checkCollisions() {
        // wall
        if (snakeX[0] < 0 || snakeX[0] >= BOARD_COLUMNS
                || snakeY[0] < 0 || snakeY[0] >= BOARD_ROWS) {
            endGame();
        }
 
        // own body (start at 1 so the head doesn't hit itself)
        for (int i = 1; i < snakeLength; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                endGame();
            }
        }
    }
 
    // TODO: coordinate the real game-over behavior with Estrada and Ken
    private void endGame() {
        gameOver = true;
        stopGame();
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
 
        // grid lines
        g2.setColor(new Color(255, 255, 255, 20));
        for (int x = 0; x <= BOARD_COLUMNS; x++) {
            g2.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, BOARD_ROWS * TILE_SIZE);
        }
        for (int y = 0; y <= BOARD_ROWS; y++) {
            g2.drawLine(0, y * TILE_SIZE, BOARD_COLUMNS * TILE_SIZE, y * TILE_SIZE);
        }
 
        // food
        g2.setColor(Color.RED);
        g2.fillOval(foodX * TILE_SIZE, foodY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
 
        // snake (head is a different color)
        for (int i = 0; i < snakeLength; i++) {
            if (i == 0) {
                g2.setColor(Color.YELLOW);
            } else {
                g2.setColor(Color.GREEN);
            }
            g2.fillRect(snakeX[i] * TILE_SIZE, snakeY[i] * TILE_SIZE, TILE_SIZE - 1, TILE_SIZE - 1);
        }
 
        // score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Score: " + score, 10, 20);
 
        // game over text
        if (gameOver) {
            g2.setFont(new Font("Arial", Font.BOLD, 36));
            g2.drawString("GAME OVER", 190, 190);
            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            g2.drawString("Press ENTER to play again", 205, 220);
        }
 
        g2.dispose();
    }
 
    // reset everything and start
    public void startGame() {
        initializeSnake();
        spawnRegularFood();
        score = 0;
        gameOver = false;
        gameRunning = true;
        gameTimer.start();
        requestFocusInWindow();
    }
 
    public void stopGame() {
        gameTimer.stop();
        gameRunning = false;
    }
}
