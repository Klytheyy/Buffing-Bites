package com.mycompany.buffingbites;

import java.awt.*;
import javax.swing.*;

class UiButton extends JButton {
    private final Color accent;

    UiButton(String text, Color accent, boolean filled) {
        super(text);
        this.accent = accent;
        setFont(new Font("SansSerif", Font.BOLD, 11));
        setForeground(filled ? Color.decode("#1A0B2E") : Color.decode("#F5EBFF"));
        setBackground(filled ? accent : Color.decode("#2D1B4E"));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? getBackground() : new Color(70, 60, 85));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(accent);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
