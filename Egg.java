import java.awt.Color;

public class Egg {
    private int x, y;
    private boolean placed = false;
    private long hatchDelay;
    private long hatchTime;

    public Egg(int x, int y, long hatchDelay) {
        this.x = x;
        this.y = y;
        this.hatchDelay = hatchDelay;
        this.hatchTime = System.currentTimeMillis() + hatchDelay; // Set the hatch time
    }

    // Only one setPosition method is needed
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Setters and getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
}
    public long getHatchDelay() {
        return hatchDelay;
    }

    // Add the isReadyToHatch() method
    public boolean isReadyToHatch() {
        return System.currentTimeMillis() >= hatchTime; // Check if the egg's hatch time has passed
    }

    // Add the getColor() method
    public Color getColor() {
        return Color.WHITE; // Use white for the egg's color
    }
}

