import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ant {
    public int x, y;
    public boolean carryingFood = false;
    private final AntPathfinder pathfinder;
    private List<int[]> path = null;
    private static int nextTrailID = 1;  // shared across all ants
    private int myTrailID = -1;
    protected Color color = Color.RED;  // Default ant color
    private long stopTime = 0;
    private static final Random random = new Random();

    public Ant(int x, int y, AntPathfinder pathfinder) {
        this.x = x;
        this.y = y;
        this.pathfinder = pathfinder;
    }

    public Color getColor() {
        return color;
    }

    public long getStopTime() {
        return stopTime;
    }

    public boolean isQueen() {
        return false;
    }

    public void update(Tile[][] world) {
        if (System.currentTimeMillis() < stopTime) {
            return;
        }

        int newX = x, newY = y;

        if (carryingFood) {
            if (path == null || path.isEmpty()) {
                path = pathfinder.findPathToNest(world, x, y);
                return;
            }

            int[] step = path.remove(0);
            newX = step[0];
            newY = step[1];

            Tile current = world[x][y];
            current.pheromoneStrength = 100;
            current.addPheromoneTrailID(myTrailID);

            if (newX == pathfinder.getNestX() && newY == pathfinder.getNestY()) {
                carryingFood = false;
                path = null;

                // Increment food counter only when food is dropped at the nest
                GamePanel.totalFoodCollected++;
            }

        } else {
            // Check if the assigned trail has disappeared
            if (myTrailID != -1) {
                boolean trailStillExists = false;

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int checkX = x + dx;
                        int checkY = y + dy;

                        if (isValid(world, checkX, checkY)) {
                            if (world[checkX][checkY].getPheromoneTrailIDs().contains(myTrailID)) {
                                trailStillExists = true;
                                break;
                            }
                        }
                    }
                    if (trailStillExists) break;
                }

                if (!trailStillExists) {
                    myTrailID = -1;  // Trail is gone — allow joining a new one
                }
            }

            if (path == null || path.isEmpty()) {
                path = pathfinder.findTrailToFood(world, x, y, myTrailID);
                if (path == null) {
                    int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    int[] dir = dirs[random.nextInt(dirs.length)];
                    newX = x + dir[0];
                    newY = y + dir[1];

                    if (isValid(world, newX, newY) && world[newX][newY].type == Tile.Type.DIRT) {
                        world[newX][newY].type = Tile.Type.TUNNEL;

                        if (random.nextDouble() < 0.09) {
                            stopTime = System.currentTimeMillis() + (long) (500 + random.nextDouble() * 2500);
                        }
                    }
                }
            } else {
                int[] step = path.remove(0);
                newX = step[0];
                newY = step[1];
            }
        }

        if (isValid(world, newX, newY)) {
            Tile tile = world[newX][newY];

            if (!carryingFood && tile.type == Tile.Type.FOOD && tile.foodAmount > 0) {
                carryingFood = true;
                tile.foodAmount--;

                if (tile.foodAmount <= 0) {
                    tile.type = Tile.Type.TUNNEL;
                }

                if (!tile.getPheromoneTrailIDs().contains(myTrailID)) {
                    if (myTrailID == -1) {
                        myTrailID = nextTrailID++;
                    }
                }

                path = pathfinder.findPathToNest(world, newX, newY);
            }

            if (tile.type == Tile.Type.DIRT) {
                tile.type = Tile.Type.TUNNEL;
            }

            x = newX;
            y = newY;
        }
    }    
    private boolean isValid(Tile[][] world, int x, int y) {
        return x >= 0 && y >= 0 && x < world.length && y < world[0].length;
    }
}
