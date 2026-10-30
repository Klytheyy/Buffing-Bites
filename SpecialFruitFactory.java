// The common product used by GamePanel for every kind of special fruit.
enum FruitType {
    GRAPE, GREEN_APPLE, BLUEBERRY, GOLDEN_PEAR
}

class SpecialFruit {
    final int x;
    final int y;
    final FruitType type;
    final long spawned;
    boolean rotten;

    SpecialFruit(int x, int y, FruitType type, long spawned) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.spawned = spawned;
    }
}

// FACTORY METHOD PATTERN:
// This abstract creator declares the method that creates a fruit.
// Each child class chooses the specific fruit type to return.
abstract class SpecialFruitCreator {

    // Factory method: subclasses decide which SpecialFruit is constructed.
    public abstract SpecialFruit createFruit(int x, int y, long spawnTime);
}

// Concrete creator for the Grape fruit.
class GrapeCreator extends SpecialFruitCreator {
    @Override
    public SpecialFruit createFruit(int x, int y, long spawnTime) {
        return new SpecialFruit(x, y, FruitType.GRAPE, spawnTime);
    }
}

// Concrete creator for the Green Apple fruit.
class GreenAppleCreator extends SpecialFruitCreator {
    @Override
    public SpecialFruit createFruit(int x, int y, long spawnTime) {
        return new SpecialFruit(x, y, FruitType.GREEN_APPLE, spawnTime);
    }
}

// Concrete creator for the Blueberry fruit.
class BlueberryCreator extends SpecialFruitCreator {
    @Override
    public SpecialFruit createFruit(int x, int y, long spawnTime) {
        return new SpecialFruit(x, y, FruitType.BLUEBERRY, spawnTime);
    }
}

// Concrete creator for the Golden Pear fruit.
class GoldenPearCreator extends SpecialFruitCreator {
    @Override
    public SpecialFruit createFruit(int x, int y, long spawnTime) {
        return new SpecialFruit(x, y, FruitType.GOLDEN_PEAR, spawnTime);
    }
}
