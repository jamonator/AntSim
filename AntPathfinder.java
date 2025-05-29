import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.awt.Point;

public class AntPathfinder {
    private final int nestX, nestY;

    public AntPathfinder(int nestX, int nestY) {
        this.nestX = nestX;
        this.nestY = nestY;
    }

    public int getNestX() { return nestX; }
    public int getNestY() { return nestY; }

    public List<int[]> findPathToNest(Tile[][] world, int startX, int startY) {
        return bfs(world, startX, startY, nestX, nestY, true, -1); // Pass -1 for trailID as it’s not used for the nest path
    }

    public List<int[]> findTrailToFood(Tile[][] world, int startX, int startY, int trailID) {
        return bfs(world, startX, startY, -1, -1, false, trailID);
    }

    private List<int[]> bfs(Tile[][] world, int startX, int startY, int goalX, int goalY, boolean toNest, int trailID) {
        int w = world.length, h = world[0].length;
        boolean[][] visited = new boolean[w][h];
        int[][][] parent = new int[w][h][2];
        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        while (!q.isEmpty()) {
            int[] pos = q.poll();
            int x = pos[0], y = pos[1];

            // Check if we've reached the goal
            if (toNest && x == goalX && y == goalY) {
                return reconstructPath(parent, startX, startY, x, y);
            }
            if (!toNest && world[x][y].type == Tile.Type.FOOD && world[x][y].foodAmount > 0) {
                return reconstructPath(parent, startX, startY, x, y);
            }

            // Explore neighboring tiles
            for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int nx = x + dir[0], ny = y + dir[1];

                // Make sure the tile is within bounds
                if (nx >= 0 && ny >= 0 && nx < w && ny < h && !visited[nx][ny]) {
                    Tile t = world[nx][ny];
                    boolean validStep = false;

                    // Check if the step is valid based on the context (to nest or food)
                    if (toNest) {
                        // Only allow movement on TUNNEL tiles when going back to the nest
                        if (t.type == Tile.Type.TUNNEL) {
                            validStep = true;
                        }
                    } else if (t.type == Tile.Type.FOOD && t.foodAmount > 0) {
                        // Allow movement if the tile is food
                        validStep = true;
                    } else if (t.hasTrail()) {
                        // If there's a pheromone trail, follow it
                        if (trailID == -1 || t.getPheromoneTrailIDs().contains(trailID)) {
                            validStep = true;
                            if (trailID == -1) {
                                t.addPheromoneTrailID(trailID);  // Set the ant's trailID to the one it joined
                            }
                        }
                    }

                    // Proceed if the step is valid
                    if (validStep) {
                        visited[nx][ny] = true;
                        parent[nx][ny][0] = x;
                        parent[nx][ny][1] = y;
                        q.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return null; // No path found
    }

    // The reconstructPath method, which reconstructs the path from the parent array.
    private List<int[]> reconstructPath(int[][][] parent, int sx, int sy, int ex, int ey) {
        List<int[]> path = new ArrayList<>();
        while (ex != sx || ey != sy) {
            path.add(0, new int[]{ex, ey});
            int px = parent[ex][ey][0];
            int py = parent[ex][ey][1];
            ex = px;
            ey = py;
        }
        path.add(0, new int[]{sx, sy}); // Add the start point at the beginning of the path
        return path;
    }

    public List<Point> findPath(int startX, int startY, int goalX, int goalY, Tile[][] world) {
        List<int[]> rawPath = bfs(world, startX, startY, goalX, goalY, true, -1);
        if (rawPath == null) return null;

        List<Point> path = new ArrayList<>();
        for (int[] step : rawPath) {
            path.add(new Point(step[0], step[1]));
        }
        return path;
    }

}
