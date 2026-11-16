package com.mycompany.buffingbites;

import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class FruitTest {
    public static void main(String[] args) {

        try {
            File file = new File("assets/grapes.png");

            BufferedImage image = ImageIO.read(file);

            System.out.println("Image loaded: " + (image != null));

            if (image != null) {
                System.out.println("Width: " + image.getWidth());
                System.out.println("Height: " + image.getHeight());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
