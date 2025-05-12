import java.util.HashSet;
import java.util.Set;

public class Tile {
    public enum Type { DIRT, TUNNEL, FOOD }

    public Type type;
    public double pheromoneStrength = 0;
    private Set<Integer> pheromoneTrailIDs;  // Holds multiple pheromone trail IDs
    public int foodAmount = 200;
    

    public Tile(Type type) {
        this.type = type;
        this.pheromoneTrailIDs = new HashSet<>();
    }

    public boolean hasTrail() {
        return pheromoneStrength > 0;
    }

    // Add pheromone trail ID to the set
    public void addPheromoneTrailID(int trailID) {
        pheromoneTrailIDs.add(trailID);
    }

    // Get all pheromone trail IDs associated with the tile
    public Set<Integer> getPheromoneTrailIDs() {
        return pheromoneTrailIDs;
    }

    public void decayTrail() {
        if (pheromoneStrength > 0) {
            pheromoneStrength -= 0.7;
            if (pheromoneStrength <= 0) {
                pheromoneStrength = 0;
                pheromoneTrailIDs.clear();  // Clear all trail IDs when the trail fades
            }
        }
    }
}