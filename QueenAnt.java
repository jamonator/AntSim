import java.util.Random;

public class QueenAnt extends Ant {
    private static final int EGG_COST = 100;
    private static final long HATCH_DELAY = 5 * 60 * 1000; // 2 minutes
    private long nextMoveTime = 0;
    private static final Random random = new Random();
    public static final int WIDTH = 2;  // tiles wide
    public static final int HEIGHT = 3; // tiles tall
    private String queenStatus = "Idle";  // Add this line
    private long statusExpireTime = 0;
    private Direction facingDirection = Direction.DOWN; // Default facing down
    private Nest nest;  // Reference to the Nest instance

    public String getQueenThoughts() {
        return "[ " + queenStatus + " ]"; 
    }


    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public QueenAnt(int x, int y, AntPathfinder pathfinder, Nest nest) {
        super(x, y, pathfinder);
        this.nest = nest; // Initialize the nest
        System.out.println("[+] Queen ant created!");
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
        long now = System.currentTimeMillis();

        // Don't change status if it's still within the display duration
        if (now < statusExpireTime) {
            return;
        }

        // Priority 1: Lay egg if possible
        if (random.nextDouble() < 0.3 && GamePanel.totalFoodCollected >= EGG_COST) {
            GamePanel.totalFoodCollected -= EGG_COST;
            GamePanel.eggsList.add(new Egg(x, y, HATCH_DELAY));
            queenStatus = "Laying an egg";
            statusExpireTime = now + 2000; // Show status for 2+ seconds
            return;
        }

        // Priority 2: Move occasionally
        if (now >= nextMoveTime) {
            tryMove(world);
            nextMoveTime = now + 1000 + random.nextInt(2000);  // 1-3 sec delay
            queenStatus = "Wandering the nest";
            statusExpireTime = now + 2000; // Show status for 2+ seconds
            return;
        }

        // Priority 3: Idle thought
        String[] idleStatuses = {
            "Resting",
            "Grooming",
            "Thinking...",
            "Observing workers",
            "Sensing the colony",
            "Listening for danger",
            "Dreaming of sugar"
        };
        queenStatus = idleStatuses[random.nextInt(idleStatuses.length)];
        statusExpireTime = now + 2000; // Idle thoughts last at least 2 seconds
    }

    private void tryMove(Tile[][] world) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        // Try up to 10 times to find a valid move
        for (int attempts = 0; attempts < 10; attempts++) {
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
