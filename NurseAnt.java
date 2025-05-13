import java.awt.*;
import java.util.List;
import java.util.Random;

public class NurseAnt extends Ant {
    private final Nest nest;
    private final GamePanel gamePanel;
    private boolean carryingEgg = false;
    private Egg carriedEgg = null;
    private static final Random random = new Random();

    public NurseAnt(int x, int y, AntPathfinder pathfinder, Nest nest, GamePanel panel) {
        super(x, y, pathfinder);
        this.nest = nest;
        this.gamePanel = panel;
        this.color = new Color(255, 165, 0); // Light pink-orange
    }

    @Override
    public void update(Tile[][] world) {
        if (!carryingEgg) {
            for (Egg egg : GamePanel.eggsList) {
                if (!egg.isPlaced() && world[egg.getX()][egg.getY()].type == Tile.Type.TUNNEL) {
                    // Move to egg if adjacent
                    if (Math.abs(egg.getX() - x) <= 1 && Math.abs(egg.getY() - y) <= 1) {
                        x = egg.getX();
                        y = egg.getY();
                        carriedEgg = egg;
                        GamePanel.eggsList.remove(egg);
                        carryingEgg = true;
                        return;
                    }
                }
            }
            randomWalkNearNest(world);
        } else {
            List<Point> nurseryTiles = gamePanel.getNurseryTiles();
            boolean placed = false;

            for (Point p : nurseryTiles) {
                if (world[p.x][p.y].type == Tile.Type.TUNNEL && !gamePanel.tileHasEgg(p.x, p.y)) {
                    long hatchTime = System.currentTimeMillis() + carriedEgg.getHatchDelay();
                    Egg newEgg = new Egg(p.x, p.y, carriedEgg.getHatchDelay());
                    newEgg.setPlaced(true);
                    GamePanel.eggsList.add(newEgg);
                    x = p.x;
                    y = p.y;
                    carryingEgg = false;
                    carriedEgg = null;
                    placed = true;
                    break;
                }
            }

            if (!placed && !nurseryTiles.isEmpty()) {
                Point p = nurseryTiles.get(0);
                if (world[p.x][p.y].type == Tile.Type.TUNNEL) {
                    long hatchTime = System.currentTimeMillis() + carriedEgg.getHatchDelay();
                    Egg newEgg = new Egg(p.x, p.y, carriedEgg.getHatchDelay());
                    newEgg.setPlaced(true);
                    GamePanel.eggsList.add(newEgg);
                    x = p.x;
                    y = p.y;
                    carryingEgg = false;
                    carriedEgg = null;
                }
            }
        }
    }

    private void randomWalkNearNest(Tile[][] world) {
        int dx = random.nextInt(3) - 1;
        int dy = random.nextInt(3) - 1;
        int newX = x + dx;
        int newY = y + dy;

        int nestX = nest.getCenterX();
        int nestY = nest.getCenterY();
        int radius = 5;

        if (Math.abs(newX - nestX) <= radius &&
            Math.abs(newY - nestY) <= radius &&
            isValidMove(world, newX, newY)) {
            x = newX;
            y = newY;
        }
    }

    private boolean isValidMove(Tile[][] world, int newX, int newY) {
        return newX >= 0 && newY >= 0 &&
               newX < world.length && newY < world[0].length &&
               world[newX][newY].type == Tile.Type.TUNNEL;
    }
}
