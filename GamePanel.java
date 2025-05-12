import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    final int width = 500, height = 200, tileSize = 3;
    final Tile[][] world = new Tile[width][height];
    final List<Ant> ants = new ArrayList<>();
    final AntPathfinder pathfinder = new AntPathfinder(width / 2, height / 2);

    public GamePanel() {
        setPreferredSize(new Dimension(width * tileSize, height * tileSize));
        setBackground(Color.BLACK);

        // Initialize tiles
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                world[x][y] = new Tile(Tile.Type.DIRT);

        // Nest center
        world[width / 2][height / 2].type = Tile.Type.TUNNEL;

        // Spawn food
        for (int i = 0; i < 60; i++) {
            int fx = (int)(Math.random() * width);
            int fy = (int)(Math.random() * height);
            world[fx][fy].type = Tile.Type.FOOD;
        }

        // Spawn ants
        for (int i = 0; i < 30; i++) {
            ants.add(new Ant(width / 2, height / 2, pathfinder));
        }

        // Main game loop
        new javax.swing.Timer(50, e -> {
            for (Tile[] row : world)
                for (Tile tile : row)
                    tile.decayTrail();

            for (Ant ant : ants)
                ant.update(world);

            repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = world[x][y];

                if (tile.type == Tile.Type.DIRT) g.setColor(new Color(80, 60, 40));
                else if (tile.type == Tile.Type.TUNNEL) g.setColor(Color.LIGHT_GRAY);
                else if (tile.type == Tile.Type.FOOD) g.setColor(Color.GREEN);

                g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);

                // Pheromone trail overlay
                if (tile.pheromoneStrength > 0) {
                    int alpha = Math.min(255, (int)(tile.pheromoneStrength * 2));
                    Set<Integer> trailIDs = tile.getPheromoneTrailIDs();
                    int colorHash = (trailIDs.isEmpty()) ? 0 : trailIDs.iterator().next() * 997;  // Use the first trail ID if available
                    Color trailColor = new Color((colorHash >> 16) & 0xFF, (colorHash >> 8) & 0xFF, colorHash & 0xFF, alpha);
                    g.setColor(trailColor);
                    g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }

        // Draw ants
        for (Ant ant : ants) {
            g.setColor(ant.carryingFood ? Color.ORANGE : Color.RED);
            g.fillRect(ant.x * tileSize, ant.y * tileSize, tileSize, tileSize);
        }
    }
}

