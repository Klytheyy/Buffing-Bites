/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package com.mycompany.buffingbites;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainMenu extends JFrame {

    // BuffingBites color palette
    private final Color MIDNIGHT = Color.decode("#1A0B2E");
    private final Color DEEP_VIOLET = Color.decode("#2D1B4E");
    private final Color ELECTRIC = Color.decode("#7B2FF7");
    private final Color TEAL = Color.decode("#2EE6D6");
    private final Color MINT = Color.decode("#5CFFB1");
    private final Color TEXT = Color.decode("#F5EBFF");

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

        // Simple game title
        JLabel title = new JLabel("BUFFINGBITES");
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        title.setForeground(TEAL);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Menu buttons
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

        // Temporary action until GamePanel is connected
        playButton.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this,
                "The game screen will be connected here.",
                "Play",
                JOptionPane.INFORMATION_MESSAGE
            )
        );

        howButton.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this,
                "Use the arrow keys to move.\n"
                + "Eat food to grow and score points.\n"
                + "Eat special fruits before they rot!\n"
                + "Avoid walls and your own body.",
                "How to Play",
                JOptionPane.INFORMATION_MESSAGE
            )
        );

        scoresButton.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this,
                "The high scores screen will be connected here.",
                "High Scores",
                JOptionPane.INFORMATION_MESSAGE
            )
        );

        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Exit BuffingBites?",
                "Exit",
                JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
    }

    // Creates a consistent style for each button
    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setForeground(primary ? MIDNIGHT : TEXT);
        button.setBackground(primary ? TEAL : DEEP_VIOLET);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setBorder(BorderFactory.createLineBorder(
            primary ? MINT : ELECTRIC, 2
        ));

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

    // Draws a subtle grid for visual interest
    private class BackgroundPanel extends JPanel {

        BackgroundPanel() {
            setBackground(MIDNIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

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
