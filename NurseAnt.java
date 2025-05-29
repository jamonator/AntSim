import java.awt.*;
import java.util.List;
import java.util.Random;

public class NurseAnt extends Ant {
    private final Nest nest;
    private final GamePanel gamePanel;
    private boolean carryingEgg = false;
    private Egg carriedEgg = null;
    private static final Random random = new Random();
    private List<Point> path = null; // Path to follow
    private int[] target = null;     // Current target

    public NurseAnt(int x, int y, AntPathfinder pathfinder, Nest nest, GamePanel panel) {
        super(x, y, pathfinder);
        this.nest = nest;
        this.gamePanel = panel;
        this.color = new Color(255, 165, 0); // Light pink-orange
    }

    @Override
    public void update(Tile[][] world) {
        // Change color based on carryingEgg state
        if (carryingEgg) {
            this.color = Color.YELLOW;
        } else {
            this.color = new Color(255, 165, 0); // Light pink-orange
        }
        if (!carryingEgg) {
            // Find the nearest unplaced egg
            Egg nearestEgg = null;
            int minDist = Integer.MAX_VALUE;
            for (Egg egg : GamePanel.eggsList) {
                if (!egg.isPlaced() && world[egg.getX()][egg.getY()].type == Tile.Type.TUNNEL) {
                    int dist = Math.abs(x - egg.getX()) + Math.abs(y - egg.getY());
                    if (dist < minDist) {
                        minDist = dist;
                        nearestEgg = egg;
                    }
                }
            }
            if (nearestEgg != null) {
                // If not at egg, walk to it
                if (x != nearestEgg.getX() || y != nearestEgg.getY()) {
                    if (target == null || target[0] != nearestEgg.getX() || target[1] != nearestEgg.getY() || path == null || path.isEmpty()) {
                        target = new int[]{nearestEgg.getX(), nearestEgg.getY()};
                        path = pathfinder.findPath(x, y, target[0], target[1], world);
                    }
                    walkPath();
                } else {
                    // Pick up the egg
                    carriedEgg = nearestEgg;
                    GamePanel.eggsList.remove(nearestEgg);
                    carryingEgg = true;
                    path = null;
                    target = null;
                }
            } else {
                randomWalkNearNest(world);
            }
        } else {
            // Find best nursery tile for clustering
            List<Point> nurseryTiles = gamePanel.getNurseryTiles();
            Point clusterPoint = findClusteredNurseryTile(world, nurseryTiles);
            Point dropPoint = clusterPoint;
            if (dropPoint == null && !nurseryTiles.isEmpty()) {
                dropPoint = nurseryTiles.get(0);
            }
            if (dropPoint != null) {
                // If not at drop point, walk to it
                if (x != dropPoint.x || y != dropPoint.y) {
                    if (target == null || target[0] != dropPoint.x || target[1] != dropPoint.y || path == null || path.isEmpty()) {
                        target = new int[]{dropPoint.x, dropPoint.y};
                        path = pathfinder.findPath(x, y, target[0], target[1], world);
                    }
                    walkPath();
                } else {
                    // Drop the egg
                    Egg newEgg = new Egg(dropPoint.x, dropPoint.y, carriedEgg.getHatchDelay());
                    newEgg.setPlaced(true);
                    GamePanel.eggsList.add(newEgg);
                    carryingEgg = false;
                    carriedEgg = null;
                    path = null;
                    target = null;
                }
            }
        }
    }

    // Move one step along the path if possible
    private void walkPath() {
        if (path != null && !path.isEmpty()) {
            Point next = path.remove(0);
            x = next.x;
            y = next.y;
        }
    }

    // Prefer tiles adjacent to other eggs for clustering
    private Point findClusteredNurseryTile(Tile[][] world, List<Point> nurseryTiles) {
        for (Point p : nurseryTiles) {
            if (isValidMove(world, p.x, p.y) && !gamePanel.tileHasEgg(p.x, p.y)) {
                // Check for adjacent eggs
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = p.x + dx, ny = p.y + dy;
                        if (nx >= 0 && ny >= 0 && nx < world.length && ny < world[0].length) {
                            if (gamePanel.tileHasEgg(nx, ny)) {
                                return p;
                            }
                        }
                    }
                }
            }
        }
        // If no cluster found, return null
        return null;
    }

    // Simple random walk near the nest
    private void randomWalkNearNest(Tile[][] world) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int dir = random.nextInt(4);
        int nx = x + dx[dir];
        int ny = y + dy[dir];
        if (isValidMove(world, nx, ny)) {
            x = nx;
            y = ny;
        }
    }

    // Use Queen's movement logic if available, otherwise keep this method
    protected boolean isValidMove(Tile[][] world, int newX, int newY) {
        return newX >= 0 && newY >= 0 &&
               newX < world.length && newY < world[0].length &&
               world[newX][newY].type == Tile.Type.TUNNEL;
    }
}
