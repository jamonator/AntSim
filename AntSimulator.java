import javax.swing.*;

public class AntSimulator {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Ant Simulator with Pheromones");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GamePanel());
        frame.pack();
        frame.setVisible(true);
    }
} 


