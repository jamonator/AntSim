import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;  // Import ArrayList
import java.util.Set;  // Import Set

public class GamePanel extends JPanel {
    final int width = 100, height = 70, tileSize = 4;
    final Tile[][] world = new Tile[width][height];
    final List<Ant> ants = new ArrayList<>();
    final AntPathfinder pathfinder = new AntPathfinder(width / 2, height / 2);
    private boolean queenCreated = false;
    private JPanel statsPanel; // Stats panel for colony stats
    
    public GamePanel() {
        setPreferredSize(new Dimension(width * tileSize, height * tileSize));
        setBackground(Color.BLACK);

        // Initialize tiles
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = new Tile(Tile.Type.DIRT);
            }
        }

        // Nest center
        world[width / 2][height / 2].type = Tile.Type.TUNNEL;

        // Spawn food
        for (int i = 0; i < 30; i++) {
            int fx = (int)(Math.random() * width);
            int fy = (int)(Math.random() * height);
            world[fx][fy].type = Tile.Type.FOOD;
        }

        // Spawn ants
        for (int i = 0; i < 50; i++) {
            ants.add(new Ant(width / 2, height / 2, pathfinder));
        }

        // Create the nest around the queen's center (width, height, nest size)
        Nest nest = new Nest(width / 2, height / 2, 20, 20); // Adjust size

        // Queen Ant (Ensure only one queen is added)
        if (!queenCreated) {
            QueenAnt queen = new QueenAnt(width / 2, height / 2, pathfinder, nest);  // Pass the nest
            ants.add(queen);
            queenCreated = true;  // Set flag to true after queen is created
        }

        // Main game loop
        new javax.swing.Timer(50, e -> {
            for (Tile[] row : world) {
                for (Tile tile : row) {
                    tile.decayTrail();
                }
            }

            for (Ant ant : ants) {
                ant.update(world);
            }

            repaint();
            updateStats();
        }).start();

        // Create and set up the stats panel
        createStatsPanel();
    }

    // Method to create and set up the stats panel
    private void createStatsPanel() {
        statsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Make the panel transparent
                setOpaque(false);

                // Display the colony stats
                g.setColor(Color.WHITE);
                g.setFont(new Font("Monospaced", Font.PLAIN, 12));

                int offsetY = 20;
                g.drawString("Food: " + getFoodAmount(), 10, offsetY);
                offsetY += 20;
                g.drawString("Ants: " + ants.size(), 10, offsetY);
                offsetY += 20;
                g.drawString("Pheromone Trails: " + getActivePheromoneTrails(), 10, offsetY);
                offsetY += 20;
                g.drawString("Total Trails: " + getTotalPheromoneTrails(), 10, offsetY);
            }
        };

        statsPanel.setBounds(0, 0, getWidth() / 4, getHeight() / 5);  // Positioning top-right
        statsPanel.setBackground(new Color(0, 0, 0, 128));  // Semi-transparent background
        add(statsPanel, BorderLayout.NORTH);
    }

    // Method to update stats dynamically
    private void updateStats() {
        // Dynamically update the stats
        statsPanel.repaint();
    }

    // Helper methods to get colony stats
    private int getFoodAmount() {
        int foodAmount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (world[x][y].type == Tile.Type.FOOD) {
                    foodAmount += world[x][y].foodAmount;
                }
            }
        }
        return foodAmount;
    }

    private int getActivePheromoneTrails() {
        int activeTrails = 0;
        for (Tile[] row : world) {
            for (Tile tile : row) {
                if (tile.hasTrail()) {
                    activeTrails++;
                }
            }
        }
        return activeTrails;
    }

    private int getTotalPheromoneTrails() {
        int totalTrails = 0;
        for (Tile[] row : world) {
            for (Tile tile : row) {
                totalTrails += tile.getPheromoneTrailIDs().size();
            }
        }
        return totalTrails;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Drawing the world and pheromone trails
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
                    if (!trailIDs.isEmpty()) {
                        int trailID = trailIDs.iterator().next();

                        // Generate a unique hue using golden angle spacing for better distribution
                        float goldenAngle = 137.508f;
                        float hue = (trailID * goldenAngle) % 360;

                        float saturation = 0.85f; // High saturation
                        float brightness = 0.95f; // High brightness

                        Color baseColor = Color.getHSBColor(hue / 360f, saturation, brightness);
                        Color trailColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);

                        g.setColor(trailColor);
                        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    }
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
