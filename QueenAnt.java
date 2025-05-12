import java.util.Random;

public class QueenAnt extends Ant {
    private long nextMoveTime = 0;
    private static final Random random = new Random();
    public static final int WIDTH = 2;  // tiles wide
    public static final int HEIGHT = 3; // tiles tall

    private Direction facingDirection = Direction.DOWN; // Default facing down

    private Nest nest;  // Reference to the Nest instance

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public QueenAnt(int x, int y, AntPathfinder pathfinder, Nest nest) {
        super(x, y, pathfinder);
        this.nest = nest; // Initialize the nest
        System.out.println("Queen ant created!");
    }

    private boolean isValid(Tile[][] world, int x, int y) {
        return x >= 0 && y >= 0 && x < world.length && y < world[0].length;
    }

    private boolean isValidQueenMove(Tile[][] world, int x, int y) {
        // Ensure that the queen's new position is within the nest area
        if (!nest.isWithinNest(x, y)) {
            return false;  // Don't allow movement outside the nest zone
        }

        // Check if the queen can move onto the new position (tunnel)
        for (int dx = 0; dx < WIDTH; dx++) {
            for (int dy = 0; dy < HEIGHT; dy++) {
                int checkX = x + dx;
                int checkY = y + dy;
                if (checkX < 0 || checkY < 0 || checkX >= world.length || checkY >= world[0].length) {
                    return false;
                }
                if (world[checkX][checkY].type != Tile.Type.TUNNEL) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isQueen() {
        return true;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    @Override
    public void update(Tile[][] world) {
        if (System.currentTimeMillis() >= nextMoveTime) {
            tryMove(world);
            nextMoveTime = System.currentTimeMillis() + 1000 + random.nextInt(1000);  // 1-2 seconds delay
        }
    }

    private void tryMove(Tile[][] world) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int i = 0; i < 4; i++) {
            int[] dir = directions[random.nextInt(directions.length)];
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (isValidQueenMove(world, newX, newY)) {
                // Set facing direction
                if (dir[0] == 1) facingDirection = Direction.RIGHT;
                else if (dir[0] == -1) facingDirection = Direction.LEFT;
                else if (dir[1] == 1) facingDirection = Direction.DOWN;
                else if (dir[1] == -1) facingDirection = Direction.UP;

                x = newX;
                y = newY;
                break;
            }
        }
    }
}
