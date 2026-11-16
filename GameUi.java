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
import javax.swing.JButton;

// Reusable custom button used by the in-game screens.
// It follows the BuffingBites color palette.

// A rounded button in the game's style. "filled" buttons are solid, the others
// are an outline that fills softly when the mouse is over them.
final class GameUi extends JButton {

    private static final Color MIDNIGHT = Color.decode("#1A0B2E");
    private static final Color GRID = Color.decode("#3E2766");

    private final Color base;
    private final boolean filled;
    private boolean hover;

    GameUi(String text, Color base, boolean filled) {
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

