import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet; // <-- Add this


public class GamePanel extends JPanel {
    final int width = 90, height = 510, tileSize = 2;
    final Tile[][] world = new Tile[width][height];
    public static List<Ant> antsList;  // List to store ants 
    final AntPathfinder pathfinder = new AntPathfinder(width / 2, height / 2);
    private boolean queenCreated = false; // Flag to ensure only one queen is created
    public static int totalFoodCollected = 0;  // Food collected counter
    public static List<Egg> eggsList = new ArrayList<>();
    private List<Point> nurseryTiles;
    private Nest nest;
    public static QueenAnt queenAnt;
    

    public GamePanel() {
        nest = new Nest(45, 255, 90, 510); // Or wherever your nest center is
        nurseryTiles = nest.getNurseryArea(15);
        antsList = new ArrayList<>();  // Initialize ants list
        eggsList = new ArrayList<>();  // Initialize eggs list
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
        for (int i = 0; i < 40; i++) {
            int fx = (int)(Math.random() * width);
            int fy = (int)(Math.random() * height);
            world[fx][fy].type = Tile.Type.FOOD;
        }

        // Spawn ants
        for (int i = 0; i < 10; i++) {
            System.out.println("[+] Worker ant created!");
            antsList.add(new Ant(width / 2, height / 2, pathfinder));
        }

        // Create the nest around the queen's center (width, height, nest size)
        Nest nest = new Nest(width / 2, height / 2, 20, 20); // Adjust size if needed

        // Queen Ant (Ensure only one queen is added)
        if (!queenCreated) {
            queenAnt = new QueenAnt(width / 2, height / 2, pathfinder, nest);  // Pass the nest
            antsList.add(queenAnt);
            queenCreated = true;
        }

        // NurseAnts
        for (int i = 0; i < 3; i++) { // Spawn 10 NurseAnts (adjust as needed)
            System.out.println("[+] Nurse ant created!");
            antsList.add(new NurseAnt(width / 2, height / 2, pathfinder, nest, this));
        }

        // Main game loop
        new javax.swing.Timer(50, e -> {
            for (Tile[] row : world) {
                for (Tile tile : row) {
                    tile.decayTrail();
                }
            }

            for (Ant ant : antsList) {
                ant.update(world);
            }

            // Update eggs
            List<Egg> hatchedEggs = new ArrayList<>();
            for (Egg egg : eggsList) {
                if (egg.isReadyToHatch()) {
                    antsList.add(new Ant(egg.getX(), egg.getY(), pathfinder));
                    hatchedEggs.add(egg);
                }
            }
            eggsList.removeAll(hatchedEggs);

            // Check if all food is collected
            if (isAllFoodCollected()) {
                resetSimulation();
            }

            repaint();
        }).start();
    }

    public void spawnAnt(Ant ant) {
        antsList.add(ant);  // Add a new ant to the list
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Set<Integer> uniqueTrailIDs = new HashSet<>();
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
                    uniqueTrailIDs.addAll(trailIDs);
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

        // Draw eggs with black outline
        for (Egg egg : eggsList) {
            int drawX = egg.getX() * tileSize;
            int drawY = egg.getY() * tileSize;

            // Fill the egg
            g.setColor(egg.getColor());
            g.fillRect(drawX, drawY, tileSize, tileSize);

            // Draw the black outline
            g.setColor(Color.BLACK);
            g.drawRect(drawX, drawY, tileSize, tileSize);
        }

        // Draw ants
        for (Ant ant : antsList) {
            if (ant.isQueen()) {
                QueenAnt queen = (QueenAnt) ant;
                QueenAnt.Direction dir = queen.getFacingDirection();

                g.setColor(Color.BLACK);

                int drawX = queen.x * tileSize;
                int drawY = queen.y * tileSize;

                int w = QueenAnt.WIDTH * tileSize;
                int h = QueenAnt.HEIGHT * tileSize;

                // Rotate queen rectangle based on direction
                switch (dir) {
                    case UP -> g.fillRect(drawX + w / 4, drawY, w / 2, h);  // Facing up
                    case DOWN -> g.fillRect(drawX + w / 4, drawY + h / 2, w / 2, h);  // Facing down
                    case LEFT -> g.fillRect(drawX, drawY + h / 4, w, h / 2);  // Facing left
                    case RIGHT -> g.fillRect(drawX + w / 2, drawY + h / 4, w, h / 2);  // Facing right
                }
            } else {
                g.setColor(ant.getColor());  // Use each ant's custom color
                g.fillRect(ant.x * tileSize, ant.y * tileSize, tileSize, tileSize);
            }
        }

        // Draw semi-transparent black background for stats box
        Graphics2D g2d = (Graphics2D) g;
        Color transparentBlack = new Color(0, 0, 0, 150); // RGBA: last value is alpha (transparency)
        g2d.setColor(transparentBlack);
        g2d.fillRect(5, 5, 150, 95); // Adjust width/height if you add more text

        g2d.setColor(Color.WHITE);
        g2d.drawString("Food collected: ", 10, 20);
        g2d.drawString("Ants: ", 10, 35);
        g2d.drawString("Eggs: ", 10, 50);
        g2d.drawString("Trails: ", 10, 65);
        g2d.drawString("Queen's thought:", 10, 80);

        g2d.setColor(Color.GREEN);
        g2d.drawString(String.valueOf(totalFoodCollected), 100, 20);
        g2d.drawString(String.valueOf(antsList.size()), 55, 35);
        g2d.drawString(String.valueOf(eggsList.size()), 55, 50);
        g2d.setColor(new Color(173, 216, 230));
        g2d.drawString(String.valueOf(uniqueTrailIDs.size()), 55, 65);

        g2d.setColor(Color.YELLOW);
        if (queenAnt != null) {
            g2d.drawString(queenAnt.getQueenThoughts(), 10, 95);
        }

    }


    public List<Point> getNurseryTiles() {
        return nurseryTiles;
    }

    public boolean tileHasEgg(int x, int y) {
        for (Egg egg : eggsList) {
            if (egg.getX() == x && egg.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllFoodCollected() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (world[x][y].type == Tile.Type.FOOD) {
                    return false;
                }
            }
        }
        return true;
    }

    public void resetSimulation() {
        System.out.println("[!] Resetting simulation...");

        // Reset world
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = new Tile(Tile.Type.DIRT);
            }
        }

        // Clear ants and eggs
        antsList.clear();
        eggsList.clear();

        // Reset nest center
        world[width / 2][height / 2].type = Tile.Type.TUNNEL;

        // Respawn food
        for (int i = 0; i < 40; i++) {
            int fx = (int)(Math.random() * width);
            int fy = (int)(Math.random() * height);
            world[fx][fy].type = Tile.Type.FOOD;
        }

        // Respawn ants
        for (int i = 0; i < 10; i++) {
            System.out.println("[+] Worker ant respawned!");
            antsList.add(new Ant(width / 2, height / 2, pathfinder));
        }

        // Respawn queen
        QueenAnt queen = new QueenAnt(width / 2, height / 2, pathfinder, nest);
        antsList.add(queen);

        // Respawn nurse ants
        for (int i = 0; i < 3; i++) {
            System.out.println("[+] Nurse ant respawned!");
            antsList.add(new NurseAnt(width / 2, height / 2, pathfinder, nest, this));
        }
        totalFoodCollected = 0; // reset counter

    }



}
