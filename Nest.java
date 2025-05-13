import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

public class Nest {
    private int centerX, centerY;
    private int width, height;
    private int storedFood = 0;

    // Constructor
    public Nest(int centerX, int centerY, int width, int height) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    // Define the nursery area. The nursery will expand as more eggs are placed.
    public List<Point> getNurseryArea(int size) {
        List<Point> area = new ArrayList<>();
        int half = size / 2;

        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                area.add(new Point(centerX + dx, centerY + dy));
            }
        }
        return area;
    }

    // Check if a given point (x, y) is within the boundaries of the nest
    public boolean isWithinNest(int x, int y) {
        return x >= centerX - width / 2 && x <= centerX + width / 2 &&
               y >= centerY - height / 2 && y <= centerY + height / 2;
    }

    // Add food to the nest
    public void addFood(int amount) {
        storedFood += amount;
    }

    // Consume food from the nest, returns true if successful
    public boolean consumeFood(int amount) {
        if (storedFood >= amount) {
            storedFood -= amount;
            return true;
        }
        return false;
    }

    // Get the amount of food stored in the nest
    public int getStoredFood() {
        return storedFood;
    }
}
