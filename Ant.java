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

    public Ant(int x, int y, AntPathfinder pathfinder) {
        this.x = x;
        this.y = y;
        this.pathfinder = pathfinder;
    }

    public void update(Tile[][] world) {
        int newX = x, newY = y;

        if (carryingFood) {
            if (path == null || path.isEmpty()) {
                path = pathfinder.findPathToNest(world, x, y);
                return;
            }

            int[] step = path.remove(0);
            newX = step[0];
            newY = step[1];

            // Lay trail with ID
            Tile current = world[x][y];
            current.pheromoneStrength = 100;
            current.addPheromoneTrailID(myTrailID);  // Add trail ID to the list of trails on the tile

            if (newX == pathfinder.getNestX() && newY == pathfinder.getNestY()) {
                carryingFood = false;
                path = null;
            }

        } else {
            if (path == null || path.isEmpty()) {
                // Find first pheromone trail and lock to its ID
                path = pathfinder.findTrailToFood(world, x, y, myTrailID);
                if (path == null) {
                    int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    int[] dir = dirs[(int) (Math.random() * dirs.length)];
                    newX = x + dir[0];
                    newY = y + dir[1];

                    if (isValid(world, newX, newY) && world[newX][newY].type == Tile.Type.DIRT) {
                        world[newX][newY].type = Tile.Type.TUNNEL;
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

                // Check if there is an existing pheromone trail attached to the food
                if (tile.getPheromoneTrailIDs().contains(myTrailID)) {
                    // If there is a trail, join that trail
                    // Don't change myTrailID since it's already part of the trail
                } else {
                    // If no trail exists, create a new one
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
