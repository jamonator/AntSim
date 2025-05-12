public class Nest {
    private int centerX, centerY;
    private int width, height;

    // Constructor for the Nest that takes center coordinates and dimensions of the nest area
    public Nest(int centerX, int centerY, int width, int height) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }

    // Method to check if the queen's position is within the nest
    public boolean isWithinNest(int x, int y) {
        return x >= centerX - width / 2 && x <= centerX + width / 2 &&
               y >= centerY - height / 2 && y <= centerY + height / 2;
    }

    // Method to get the center of the nest
    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }
}
