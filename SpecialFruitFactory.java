package com.mycompany.buffingbites;

// The common product used by GamePanel for every kind of special fruit.
enum FruitType {
    GRAPE, BANANA, ORANGE, GOLDEN_PEAR
}

class SpecialFruit {
    final int x;
    final int y;
    final FruitType type;
    long spawned; // not final: pausing shifts it forward
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

// Concrete creator for the Banana fruit.
class BananaCreator extends SpecialFruitCreator {
    @Override
    public SpecialFruit createFruit(int x, int y, long spawnTime) {
        return new SpecialFruit(x, y, FruitType.BANANA, spawnTime);
    }
}

// Concrete creator for the Orange fruit.
class OrangeCreator extends SpecialFruitCreator {
    @Override
    public SpecialFruit createFruit(int x, int y, long spawnTime) {
        return new SpecialFruit(x, y, FruitType.ORANGE, spawnTime);
    }
}

// Concrete creator for the Golden Pear fruit.
class GoldenPearCreator extends SpecialFruitCreator {
    @Override
    public SpecialFruit createFruit(int x, int y, long spawnTime) {
        return new SpecialFruit(x, y, FruitType.GOLDEN_PEAR, spawnTime);
    }
}