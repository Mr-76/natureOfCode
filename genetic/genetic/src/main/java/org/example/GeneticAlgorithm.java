package org.example;
import javax.swing.*;
import java.awt.*;
//cap9
//https://github.com/nature-of-code/noc-examples-processing/blob/master/chp09_ga/GA_Shakespeare_fancyfitness/Population.pde
public class GeneticAlgorithm extends JPanel {
    private final String target = "hello this is a test witch the algortim shoud be able to guess this exact string if it did congratz if not try HARDER";
    private final int popmax = 150;
    private final float mutationRate = 0.01f;
    private Population population;
    private Timer timer;

    public GeneticAlgorithm() {
        population = new Population(target, mutationRate, popmax);

        // Timer to simulate Processing's draw() loop (runs every 100 ms)
        timer = new Timer(100, e -> {
            population.naturalSelection();
            population.generate();
            population.calcFitness();
            repaint(); // Redraw GUI

            if (population.isFinished()) {
                System.out.println("Finished in: " + (System.currentTimeMillis() / 1000.0) + " seconds");
                timer.stop(); // Stop execution when target is found
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Courier", Font.PLAIN, 32));
        g.setColor(Color.BLACK);

        String bestPhrase = population.getBest();

        g.drawString("Best phrase:", 20, 30);
        g.drawString(bestPhrase, 20, 75);

        g.setFont(new Font("Courier", Font.PLAIN, 16));
        g.drawString("Total generations: " + population.getGenerations(), 20, 140);
        g.drawString("Average fitness: " + String.format("%.2f", population.getAverageFitness()), 20, 160);
        g.drawString("Total population: " + popmax, 20, 180);
        g.drawString("Mutation rate: " + (int) (mutationRate * 100) + "%", 20, 200);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Genetic Algorithm - Evolving Shakespeare");
        GeneticAlgorithm panel = new GeneticAlgorithm();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 250);
        frame.add(panel);
        frame.setVisible(true);
    }
}