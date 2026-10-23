/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */


package com.mycompany.buffingbites;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class BuffingBites {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
                );
            } catch (Exception e) {
                System.out.println("Could not set the application theme.");
            }

            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}

