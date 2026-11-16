package com.mycompany.buffingbites;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

public class SpecialFruitAssets {

    // Folders to search, in order. Whichever one holds your pictures will work.
    private static final String[] FOLDERS = {"/resources/", "/", "/png/", "/assets/"};

    // Each image is loaded or generated once and then reused. A missing image
    // is also remembered (as null) so we do not retry every frame.
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    // Returns the picture for a fruit, or null when there is none at all.
    // GamePanel draws a colored circle with a letter when this returns null.
    //
    // Fresh:  the fruit's own picture (bright and healthy).
    // Rotten: its own rotten picture if you have one (rotten_banana.png);
    //         otherwise the fresh picture is turned into a spoiled-looking
    //         version (discolored with dark spots).
    public static BufferedImage getImage(FruitType type, boolean rotten) {
        if (!rotten) {
            return loadFirst(freshFiles(type));
        }

        String rottenFile = rottenFile(type);
        if (rottenFile != null) {
            BufferedImage own = load(rottenFile);
            if (own != null) {
                return own;
            }
        }

        String key = "generated-rotten:" + type;
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        BufferedImage fresh = loadFirst(freshFiles(type));
        BufferedImage generated = fresh == null ? null : makeRotten(fresh);
        CACHE.put(key, generated);
        return generated;
    }

    // The regular food that the snake eats is the apple. A rotten apple uses
    // rotten_apple.png if you have it, otherwise a spoiled version is generated.
    public static BufferedImage getFoodImage(boolean rotten) {
        if (!rotten) {
            return load("apple.png");
        }
        BufferedImage own = load("rotten_apple.png");
        if (own != null) {
            return own;
        }
        String key = "generated-rotten:apple";
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        BufferedImage fresh = load("apple.png");
        BufferedImage generated = fresh == null ? null : makeRotten(fresh);
        CACHE.put(key, generated);
        return generated;
    }

    // Accepted file names for each fruit's fresh picture, best name first.
    private static String[] freshFiles(FruitType type) {
        switch (type) {
            case GRAPE: return new String[]{"grapes.png", "grape.png"};
            case BANANA: return new String[]{"banana.png"};
            case ORANGE: return new String[]{"orange.png"};
            default: return new String[]{"golden_pear.png", "pear.png"};
        }
    }

    // Tries each file name in turn and returns the first picture that exists.
    private static BufferedImage loadFirst(String[] fileNames) {
        for (String name : fileNames) {
            BufferedImage image = load(name);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    // Optional hand-made rotten picture for the banana (otherwise one is generated).
    private static String rottenFile(FruitType type) {
        return type == FruitType.BANANA ? "rotten_banana.png" : null;
    }

    // Makes a spoiled version of a picture: colors fade toward a dull brownish
    // olive, and dark decay spots are added on top of the fruit only.
    private static BufferedImage makeRotten(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        final int rotR = 0x6B, rotG = 0x5E, rotB = 0x2A; // dull brown-olive

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = source.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    result.setRGB(x, y, 0);
                    continue;
                }
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                // Blend 55% toward the rot color, then darken a little.
                r = (int) ((r * 0.45 + rotR * 0.55) * 0.85);
                g = (int) ((g * 0.45 + rotG * 0.55) * 0.85);
                b = (int) ((b * 0.45 + rotB * 0.55) * 0.85);
                result.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        // Dark spots. A fixed seed makes every rotten fruit look the same.
        Random spotRandom = new Random(42);
        int spots = 6;
        int radius = Math.max(1, Math.min(w, h) / 9);
        for (int i = 0; i < spots; i++) {
            int cx = spotRandom.nextInt(w);
            int cy = spotRandom.nextInt(h);
            if (((source.getRGB(cx, cy) >>> 24) & 0xFF) == 0) {
                continue; // the spot center must land on the fruit
            }
            for (int y = cy - radius; y <= cy + radius; y++) {
                for (int x = cx - radius; x <= cx + radius; x++) {
                    if (x < 0 || y < 0 || x >= w || y >= h) continue;
                    if ((x - cx) * (x - cx) + (y - cy) * (y - cy) > radius * radius) continue;
                    int a = (source.getRGB(x, y) >>> 24) & 0xFF;
                    if (a == 0) continue; // never paint outside the fruit
                    result.setRGB(x, y, (a << 24) | 0x2B1D0E);
                }
            }
        }
        return result;
    }

    static BufferedImage load(String fileName) {
        if (CACHE.containsKey(fileName)) {
            return CACHE.get(fileName);
        }

        BufferedImage image = null;

        for (String folder : FOLDERS) {
            String path = folder + fileName;
            try (InputStream input = SpecialFruitAssets.class.getResourceAsStream(path)) {
                if (input != null) {
                    image = ImageIO.read(input);
                    System.out.println("Loaded: " + path);
                    break;
                }
            } catch (Exception e) {
                System.out.println("Failed to load: " + path);
                e.printStackTrace();
            }
        }

        if (image == null) {
            System.out.println("Image not found in any folder: " + fileName);
        }

        CACHE.put(fileName, image);
        return image;
    }
}
