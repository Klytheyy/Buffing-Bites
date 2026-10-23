/*
 * BuffingBites main menu.
 * Keeps the existing palette and connects the buttons to the game and score file.
 */
package com.mycompany.buffingbites;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class MainMenu extends JFrame {

    private final Color MIDNIGHT = Color.decode("#1A0B2E");
    private final Color DEEP_VIOLET = Color.decode("#2D1B4E");
    private final Color ELECTRIC = Color.decode("#7B2FF7");
    private final Color TEAL = Color.decode("#2EE6D6");
    private final Color MINT = Color.decode("#5CFFB1");
    private final Color TEXT = Color.decode("#F5EBFF");

    private JFrame gameWindow;

    public MainMenu() {
        setTitle("BuffingBites");
        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundPanel background = new BackgroundPanel();
        background.setLayout(new GridBagLayout());

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("BUFFINGBITES");
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        title.setForeground(TEAL);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playButton = createButton("PLAY", true);
        JButton howButton = createButton("HOW TO PLAY", false);
        JButton scoresButton = createButton("HIGH SCORES", false);
        JButton exitButton = createButton("EXIT", false);

        menu.add(title);
        menu.add(Box.createVerticalStrut(38));
        menu.add(playButton);
        menu.add(Box.createVerticalStrut(12));
        menu.add(howButton);
        menu.add(Box.createVerticalStrut(12));
        menu.add(scoresButton);
        menu.add(Box.createVerticalStrut(12));
        menu.add(exitButton);

        background.add(menu);
        setContentPane(background);

        // Open the actual game screen, then return to this menu when it closes.
        playButton.addActionListener(e -> openGame());

        howButton.addActionListener(e -> JOptionPane.showMessageDialog(
            this,
            "Use the arrow keys or WASD to move.\n"
            + "Eat food to grow and earn points.\n"
            + "Eat special fruits before they rot.\n"
            + "Avoid the walls and your own body.\n\n"
            + "Press ENTER after Game Over to restart.",
            "How to Play",
            JOptionPane.INFORMATION_MESSAGE
        ));

        scoresButton.addActionListener(e -> showHighScores());

        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this, "Exit BuffingBites?", "Exit", JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                if (gameWindow != null) {
                    gameWindow.dispose();
                }
                dispose();
            }
        });
    }

    private void openGame() {
        if (gameWindow != null && gameWindow.isDisplayable()) {
            gameWindow.toFront();
            return;
        }

        GamePanel gamePanel = new GamePanel();
        gameWindow = new JFrame("BuffingBites - New Game");
        gameWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameWindow.setContentPane(gamePanel);
        gameWindow.pack();
        gameWindow.setResizable(false);
        gameWindow.setLocationRelativeTo(this);
        gameWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                gamePanel.startGame();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                gamePanel.stopGame();
                gameWindow = null;
                setVisible(true);
                toFront();
            }
        });

        setVisible(false);
        gameWindow.setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    private void showHighScores() {
        DataManager dataManager = new DataManager();
        List<String> scores = dataManager.loadHighScores();

        String message;
        if (scores.isEmpty()) {
            message = "No saved scores yet. Play a game to create your first record!";
        } else {
            message = String.join("\n", scores);
        }

        JTextArea scoreArea = new JTextArea(message, 12, 36);
        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        scoreArea.setBackground(MIDNIGHT);
        scoreArea.setForeground(TEXT);
        scoreArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(scoreArea);
        JOptionPane.showMessageDialog(
            this, scrollPane, "High Scores", JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setForeground(primary ? MIDNIGHT : TEXT);
        button.setBackground(primary ? TEAL : DEEP_VIOLET);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createLineBorder(primary ? MINT : ELECTRIC, 2));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(250, 48));
        button.setMaximumSize(new Dimension(250, 48));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(primary ? MINT : ELECTRIC);
                button.setForeground(primary ? MIDNIGHT : Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primary ? TEAL : DEEP_VIOLET);
                button.setForeground(primary ? MIDNIGHT : TEXT);
            }
        });
        return button;
    }

    private class BackgroundPanel extends JPanel {
        BackgroundPanel() {
            setBackground(MIDNIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(62, 39, 102, 90));
            int gridSize = 32;
            for (int x = 0; x < getWidth(); x += gridSize) {
                g2.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += gridSize) {
                g2.drawLine(0, y, getWidth(), y);
            }
            g2.dispose();
        }
    }
}
