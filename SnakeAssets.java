package com.mycompany.buffingbites;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

// Pictures for the snake. Make them in Piskel and save them in your
// resources folder with these names. Draw every picture as if the snake is
// moving to the RIGHT; the game rotates them as the snake turns.
//
//   snake_head.png  - the head, nose pointing right (optional)
//   snake_body.png  - one body piece, straight and horizontal (required)
//   snake_tail.png  - the tail, tip pointing left, joins body on the right (optional)
//
// If snake_body.png is missing, the game draws the snake with shapes instead.
public class SnakeAssets {

    // Every picture is split into two layers so that turns look clean:
    //   outline = the pixels on the edge of the picture (next to transparent pixels)
    //   fill    = everything inside
    // The game draws all outlines first and all fills on top of them, so the
    // outline of one piece never cuts across the inside of its neighbor.
    public static final class Layers {
        public final BufferedImage outline;
        public final BufferedImage fill;

        Layers(BufferedImage outline, BufferedImage fill) {
            this.outline = outline;
            this.fill = fill;
        }
    }

    private static final Map<String, Layers> CACHE = new HashMap<>();

    public static Layers getHeadLayers() {
        return layers("snake_head.png");
    }

    public static Layers getBodyLayers() {
        return layers("snake_body.png");
    }

    public static Layers getTailLayers() {
        return layers("snake_tail.png");
    }

    private static Layers layers(String fileName) {
        if (CACHE.containsKey(fileName)) {
            return CACHE.get(fileName);
        }
        BufferedImage image = SpecialFruitAssets.load(fileName);
        Layers result = image == null ? null : split(image);
        CACHE.put(fileName, result);
        return result;
    }

    private static Layers split(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage outline = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage fill = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        int[][] around = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = source.getRGB(x, y);
                if (((argb >>> 24) & 0xFF) == 0) {
                    continue;
                }
                boolean edge = false;
                for (int[] step : around) {
                    int nx = x + step[0];
                    int ny = y + step[1];
                    if (nx < 0 || nx >= w) {
                        continue; // left/right picture edges join the next piece
                    }
                    if (ny < 0 || ny >= h || ((source.getRGB(nx, ny) >>> 24) & 0xFF) == 0) {
                        edge = true;
                        break;
                    }
                }
                (edge ? outline : fill).setRGB(x, y, argb);
            }
        }
        return new Layers(outline, fill);
    }
}