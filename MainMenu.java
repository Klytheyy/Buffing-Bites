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
        playButton.addActionListener(e -> showLevelSelection());

        howButton.addActionListener(e -> showHowToPlay());

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

    

private void openGame(LevelManager.Difficulty difficulty) {
    GamePanel gamePanel = new GamePanel(difficulty);

    gameWindow = new JFrame("BuffingBites");
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


private void showHowToPlay() {
    String[] titles = {
        "MOVEMENT",
        "COLLECT FOOD",
        "SPECIAL FRUITS",
        "PAUSE AND SURVIVE"
    };

    String[] descriptions = {
        "Control your snake using the keyboard.\n\n"
        + "Arrow Keys or W, A, S, D\n"
        + "Move up, down, left, and right.\n"
        + "The snake cannot immediately reverse direction.",

        "Collect regular food to grow your snake.\n\n"
        + "Each regular food gives 10 points.\n"
        + "Your running score is displayed above the game board.\n"
        + "Avoid the walls and your own body.",

        "SPECIAL FRUITS AND EFFECTS\n\n"
        + "GRAPE: Fresh = +15% speed for 7 seconds. Rotten = -15% speed for 7 seconds.\n\n"
        + "GREEN APPLE: Fresh = 2x points for the next 5 regular foods. Rotten = 0.5x points for the next 5 regular foods.\n\n"
        + "BLUEBERRY: Fresh = protects against one collision for 10 seconds. Rotten = removes active protection.\n\n"
        + "GOLDEN PEAR: Fresh = +30 points. Rotten = -15 points.\n\n"
        + "FRUIT ROTTING TIMER: Easy 20 seconds, Medium 15 seconds, Hard 10 seconds. Rotten fruits remain until collected.",

        "PAUSE AND SURVIVE\n\n"
        + "Press ESC to pause the game. Press ESC again to resume.\n"
        + "Fruit countdowns and active effect timers also pause while the game is paused.\n\n"
        + "Avoid hitting the walls or your own body. Use special fruits wisely and try to achieve your highest score."
    };

    JDialog dialog = createStyledDialog(
        "HOW TO PLAY", 700, 500
    );

    JPanel content = new JPanel(new BorderLayout(0, 16));
    content.setBackground(MIDNIGHT);
    content.setBorder(BorderFactory.createEmptyBorder(
        24, 28, 22, 28
    ));

    // Tutorial step indicator
    JLabel stepLabel = new JLabel();
    stepLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    stepLabel.setForeground(TEAL);

    // Tutorial page title
    JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
    titleLabel.setForeground(MINT);

    // Description area
    JTextArea descriptionArea = new JTextArea();
    descriptionArea.setEditable(false);
    descriptionArea.setFocusable(false);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setOpaque(false);
    descriptionArea.setForeground(TEXT);
    descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 15));
    descriptionArea.setBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5)
    );

    // Scroll bar allows longer descriptions to fit without being cut off.
    JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
    descriptionScroll.setBorder(BorderFactory.createEmptyBorder());
    descriptionScroll.setOpaque(false);
    descriptionScroll.getViewport().setOpaque(false);

    descriptionScroll.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
    );

    descriptionScroll.setHorizontalScrollBarPolicy(
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );

    descriptionScroll.getVerticalScrollBar().setUnitIncrement(16);

    // Main instruction card
    JPanel instructionPanel = new JPanel(
        new BorderLayout(0, 18)
    );

    instructionPanel.setBackground(DEEP_VIOLET);
    instructionPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ELECTRIC, 2),
        BorderFactory.createEmptyBorder(24, 22, 24, 22)
    ));

    instructionPanel.add(titleLabel, BorderLayout.NORTH);
    instructionPanel.add(descriptionScroll, BorderLayout.CENTER);

    // Progress indicators
    JPanel progressPanel = new JPanel(
        new GridLayout(1, 4, 8, 0)
    );

    progressPanel.setOpaque(false);

    JPanel[] progressBars = new JPanel[4];

    for (int i = 0; i < progressBars.length; i++) {
        progressBars[i] = new JPanel();
        progressBars[i].setBackground(DEEP_VIOLET);
        progressBars[i].setPreferredSize(new Dimension(20, 5));
        progressPanel.add(progressBars[i]);
    }

    // Navigation buttons
    JButton backButton = createButton("BACK", false);
    JButton nextButton = createButton("NEXT", true);

    JPanel navigation = new JPanel(
        new BorderLayout(12, 0)
    );

    navigation.setOpaque(false);
    navigation.add(backButton, BorderLayout.WEST);
    navigation.add(nextButton, BorderLayout.EAST);

    JPanel bottom = new JPanel(
        new BorderLayout(0, 14)
    );

    bottom.setOpaque(false);
    bottom.add(progressPanel, BorderLayout.NORTH);
    bottom.add(navigation, BorderLayout.SOUTH);

    final int[] currentStep = {0};

    // Updates the displayed tutorial page.
    Runnable updatePage = () -> {
        int step = currentStep[0];

        stepLabel.setText(
            "TUTORIAL  " + (step + 1) + " / " + titles.length
        );

        titleLabel.setText(titles[step]);

        descriptionArea.setText(descriptions[step]);

        // Reset the scrollbar to the top whenever the page changes.
        descriptionArea.setCaretPosition(0);

        backButton.setEnabled(step > 0);

        nextButton.setText(
            step == titles.length - 1 ? "FINISH" : "NEXT"
        );

        // Update progress indicators.
        for (int i = 0; i < progressBars.length; i++) {
            progressBars[i].setBackground(
                i <= step ? TEAL : DEEP_VIOLET
            );
        }
    };

    // Go to the previous tutorial page.
    backButton.addActionListener(e -> {
        if (currentStep[0] > 0) {
            currentStep[0]--;
            updatePage.run();
        }
    });

    // Go to the next page or close the tutorial.
    nextButton.addActionListener(e -> {
        if (currentStep[0] < titles.length - 1) {
            currentStep[0]++;
            updatePage.run();
        } else {
            dialog.dispose();
        }
    });

    // Top section
    JPanel top = new JPanel(new BorderLayout());
    top.setOpaque(false);
    top.add(stepLabel, BorderLayout.WEST);

    // Assemble the tutorial dialog.
    content.add(top, BorderLayout.NORTH);
    content.add(instructionPanel, BorderLayout.CENTER);
    content.add(bottom, BorderLayout.SOUTH);

    dialog.add(content, BorderLayout.CENTER);

    updatePage.run();
    dialog.setVisible(true);
}

private void showLevelSelection() {
    JDialog dialog = createStyledDialog(
        "SELECT DIFFICULTY", 520, 400
    );

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBackground(Color.decode("#1A0B2E"));
    content.setBorder(
        BorderFactory.createEmptyBorder(25, 35, 25, 35)
    );

    JLabel heading = new JLabel("CHOOSE YOUR CHALLENGE");
    heading.setFont(new Font("SansSerif", Font.BOLD, 21));
    heading.setForeground(Color.decode("#F5EBFF"));
    heading.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel description = new JLabel("Select a difficulty to begin");
    description.setFont(new Font("SansSerif", Font.PLAIN, 13));
    description.setForeground(Color.decode("#C9A7FF"));
    description.setAlignmentX(Component.CENTER_ALIGNMENT);

    content.add(heading);
    content.add(Box.createVerticalStrut(8));
    content.add(description);
    content.add(Box.createVerticalStrut(25));

    JButton easyButton = createButton("EASY", false);
    JButton mediumButton = createButton("MEDIUM", true);
    JButton hardButton = createButton("HARD", false);

    Dimension buttonSize = new Dimension(300, 42);

    JButton[] buttons = {
        easyButton, mediumButton, hardButton
    };

    for (JButton button : buttons) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(buttonSize);
        button.setPreferredSize(buttonSize);
        content.add(button);
        content.add(Box.createVerticalStrut(12));
    }

    easyButton.addActionListener(e -> {
        dialog.dispose();
        openGame(LevelManager.Difficulty.EASY);
    });

    mediumButton.addActionListener(e -> {
        dialog.dispose();
        openGame(LevelManager.Difficulty.MEDIUM);
    });

    hardButton.addActionListener(e -> {
        dialog.dispose();
        openGame(LevelManager.Difficulty.HARD);
    });

    dialog.add(content, BorderLayout.CENTER);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}



private void showHighScores() {
    DataManager dataManager = new DataManager();
    JDialog dialog = createStyledDialog("HIGH SCORES", 650, 540);

    JPanel content = new JPanel(new BorderLayout(0, 14));
    content.setBackground(MIDNIGHT);
    content.setBorder(BorderFactory.createEmptyBorder(14, 24, 20, 24));

    JLabel subtitle = new JLabel("HALL OF FAME");
    subtitle.setFont(new Font("SansSerif", Font.BOLD, 18));
    subtitle.setForeground(TEAL);
    JLabel description = new JLabel("Select a saved score if you want to remove it.");
    description.setFont(new Font("SansSerif", Font.PLAIN, 13));
    description.setForeground(TEXT);

    JPanel heading = new JPanel();
    heading.setOpaque(false);
    heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
    heading.add(subtitle);
    heading.add(Box.createVerticalStrut(5));
    heading.add(description);
    content.add(heading, BorderLayout.NORTH);

    DefaultListModel<String> scoreModel = new DefaultListModel<>();
    JList<String> scoreList = new JList<>(scoreModel);
    scoreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    scoreList.setBackground(DEEP_VIOLET);
    scoreList.setForeground(TEXT);
    scoreList.setSelectionBackground(ELECTRIC);
    scoreList.setSelectionForeground(TEXT);
    scoreList.setFont(new Font("Monospaced", Font.BOLD, 13));
    scoreList.setFixedCellHeight(36);
    scoreList.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

    Runnable refreshScores = () -> {
        scoreModel.clear();
        List<String> allScores = dataManager.loadAllHighScores();
        for (String score : allScores) {
            scoreModel.addElement(score);
        }
        if (scoreModel.isEmpty()) {
            scoreModel.addElement("No saved scores yet. Play a game to get started!");
        }
    };
    refreshScores.run();

    JScrollPane scrollPane = new JScrollPane(scoreList);
    scrollPane.setBorder(BorderFactory.createLineBorder(ELECTRIC, 1));
    scrollPane.getViewport().setBackground(DEEP_VIOLET);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    content.add(scrollPane, BorderLayout.CENTER);

    JButton deleteButton = createButton("DELETE SELECTED SCORE", false);
    JButton closeButton = createButton("BACK TO MENU", true);

    deleteButton.addActionListener(e -> {
        int selectedIndex = scoreList.getSelectedIndex();
        if (selectedIndex < 0 || (scoreModel.size() == 1
                && scoreModel.get(0).startsWith("No saved scores"))) {
            JOptionPane.showMessageDialog(dialog,
                    "Please select a saved score first.",
                    "No Score Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedScore = scoreModel.get(selectedIndex);
        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Delete this saved score?\n\n" + selectedScore,
                "Confirm Deletion", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dataManager.deleteHighScoreAt(selectedIndex)) {
                refreshScores.run();
                JOptionPane.showMessageDialog(dialog,
                        "The selected score has been deleted.",
                        "Score Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "The score could not be deleted. Please check the data folder.",
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

    closeButton.addActionListener(e -> dialog.dispose());
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
    bottom.setOpaque(false);
    bottom.add(deleteButton);
    bottom.add(closeButton);
    content.add(bottom, BorderLayout.SOUTH);

    dialog.add(content, BorderLayout.CENTER);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}

private JPanel createInfoCard(
        String heading, String title, String description) {

    JPanel card = new JPanel();
    card.setBackground(DEEP_VIOLET);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ELECTRIC, 1),
        BorderFactory.createEmptyBorder(12, 14, 12, 14)
    ));
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setAlignmentX(Component.LEFT_ALIGNMENT);
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

    JLabel headingLabel = new JLabel(heading);
    headingLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
    headingLabel.setForeground(TEAL);

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLabel.setForeground(MINT);

    JLabel descriptionLabel = new JLabel(description);
    descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    descriptionLabel.setForeground(TEXT);

    card.add(headingLabel);
    card.add(Box.createVerticalStrut(4));
    card.add(titleLabel);
    card.add(Box.createVerticalStrut(3));
    card.add(descriptionLabel);

    return card;
}

private JDialog createStyledDialog(String title, int width, int height) {
    JDialog dialog = new JDialog(this, false);
    dialog.setTitle(title);
    dialog.setSize(width, height);
    dialog.setResizable(false);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(MIDNIGHT);
    root.setBorder(BorderFactory.createLineBorder(ELECTRIC, 2));

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(DEEP_VIOLET);
    header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 14));

    JLabel titleLabel = new JLabel("BUFFINGBITES  /  " + title);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
    titleLabel.setForeground(TEAL);

    JButton closeButton = new JButton("X");
    closeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
    closeButton.setForeground(TEXT);
    closeButton.setBackground(DEEP_VIOLET);
    closeButton.setBorderPainted(false);
    closeButton.setFocusPainted(false);
    closeButton.setContentAreaFilled(false);
    closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    closeButton.addActionListener(e -> dialog.dispose());

    header.add(titleLabel, BorderLayout.CENTER);
    header.add(closeButton, BorderLayout.EAST);

    root.add(header, BorderLayout.NORTH);
    dialog.setContentPane(root);
    dialog.setLocationRelativeTo(this);

    return dialog;
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
                button.setForeground(primary ? MIDNIGHT : TEXT);
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
