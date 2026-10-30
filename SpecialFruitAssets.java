import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SpecialFruitAssets {

    public static BufferedImage getImage(FruitType type) {

        try {
            switch (type) {

                case GRAPE:
                    return ImageIO.read(
                        new File("assets/grape.png")
                    );

                case GREEN_APPLE:
                    return ImageIO.read(
                        new File("assets/orange.png")
                    );

                case BLUEBERRY:
                    return ImageIO.read(
                        new File("assets/blueberry.png")
                    );

                case GOLDEN_PEAR:
                    return ImageIO.read(
                        new File("assets/golden-pear.png")
                    );

                default:
                    return null;
            }

        } catch (Exception e) {
            System.out.println("Failed to load fruit asset: " + type);
            e.printStackTrace();
            return null;
        }
    }
}