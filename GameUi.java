package com.mycompany.buffingbites;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;

// Small reusable pieces of the in-game screens (buttons and the leaderboard).
// Both use the BuffingBites palette.

// A rounded button in the game's style. "filled" buttons are solid, the others
// are an outline that fills softly when the mouse is over them.
final class UiButton extends JButton {

    private static final Color MIDNIGHT = Color.decode("#1A0B2E");
    private static final Color GRID = Color.decode("#3E2766");

    private final Color base;
    private final boolean filled;
    private boolean hover;

    UiButton(String text, Color base, boolean filled) {
        super(text);
        this.base = base;
        this.filled = filled;
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setPreferredSize(new Dimension(230, 38));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        boolean on = isEnabled();
        Color color = on ? base : new Color(0x6B, 0x5A, 0x8A);
        Color textColor;

        if (filled) {
            Color fill;
            if (!on) {
                fill = GRID;
            } else if (hover) {
                fill = new Color(Math.min(255, color.getRed() + 25),
                        Math.min(255, color.getGreen() + 25),
                        Math.min(255, color.getBlue() + 25));
            } else {
                fill = color;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            textColor = on ? MIDNIGHT : new Color(0x9A, 0x88, 0xB8);
        } else {
            if (hover && on) {
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 45));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            }
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);
            textColor = color;
        }

        g2.setFont(getFont());
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}

// The top-scores list shown on the game over screen.
final class LeaderboardPanel extends JPanel {

    private static final Color MIDNIGHT = Color.decode("#1A0B2E");
    private static final Color CARD = Color.decode("#2D1B4E");
    private static final Color GRID = Color.decode("#3E2766");
    private static final Color LILAC = Color.decode("#C9A7FF");
    private static final Color TEXT = Color.decode("#F5EBFF");
    private static final Color TEAL = Color.decode("#2EE6D6");
    private static final Color GOLD = Color.decode("#FFD166");
    private static final Color SILVER = Color.decode("#C9CCE0");
    private static final Color BRONZE = Color.decode("#CD7F32");

    // One line of the list.
    static final class Row {
        final int rank;
        final String name;
        final int score;
        final boolean highlight;

        Row(int rank, String name, int score, boolean highlight) {
            this.rank = rank;
            this.name = name;
            this.score = score;
            this.highlight = highlight;
        }
    }

    private ArrayList<Row> rows = new ArrayList<>();

    LeaderboardPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(250, 240));
    }

    void setRows(ArrayList<Row> newRows) {
        rows = newRows;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(LILAC);
        g2.drawString("LEADERBOARD", 0, 14);

        if (rows.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.setColor(TEXT);
            drawCentered(g2, "No scores yet.", w / 2, 100);
            drawCentered(g2, "Be the first on the board!", w / 2, 120);
            g2.dispose();
            return;
        }

        int rowHeight = 34;
        int gap = 6;
        int y = 26;
        for (Row row : rows) {
            // Row background
            if (row.highlight) {
                g2.setColor(new Color(0x2E, 0xE6, 0xD6, 45));
                g2.fillRoundRect(0, y, w - 1, rowHeight, 12, 12);
                g2.setColor(TEAL);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, y + 1, w - 3, rowHeight - 2, 12, 12);
            } else {
                g2.setColor(CARD);
                g2.fillRoundRect(0, y, w - 1, rowHeight, 12, 12);
            }

            // Rank badge: gold, silver and bronze for the top three
            Color badge = row.rank == 1 ? GOLD : row.rank == 2 ? SILVER : row.rank == 3 ? BRONZE : GRID;
            g2.setColor(badge);
            g2.fillOval(8, y + 6, 22, 22);
            g2.setColor(row.rank <= 3 ? MIDNIGHT : TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            drawCentered(g2, String.valueOf(row.rank), 19, y + 22);

            // Score on the right
            String scoreText = String.valueOf(row.score);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            FontMetrics scoreMetrics = g2.getFontMetrics();
            int scoreWidth = scoreMetrics.stringWidth(scoreText);
            g2.setColor(row.highlight ? GOLD : TEAL);
            g2.drawString(scoreText, w - 12 - scoreWidth, y + 23);

            // Name on the left, shortened with "..." if it does not fit
            g2.setFont(new Font("SansSerif", row.highlight ? Font.BOLD : Font.PLAIN, 14));
            g2.setColor(TEXT);
            FontMetrics nameMetrics = g2.getFontMetrics();
            int room = w - 40 - 12 - scoreWidth - 10;
            String name = row.name;
            if (nameMetrics.stringWidth(name) > room) {
                while (name.length() > 1 && nameMetrics.stringWidth(name + "...") > room) {
                    name = name.substring(0, name.length() - 1);
                }
                name = name + "...";
            }
            g2.drawString(name, 40, y + 23);

            y += rowHeight + gap;
        }
        g2.dispose();
    }

    private static void drawCentered(Graphics2D g2, String text, int centerX, int baseline) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, centerX - fm.stringWidth(text) / 2, baseline);
    }
}