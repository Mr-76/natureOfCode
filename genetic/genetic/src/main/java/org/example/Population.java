package org.example;


import java.util.ArrayList;
import java.util.Random;

public class Population {

    private double mutationRate;      // Mutation rate
    private DNA[] population;         // Array to hold the current population
    private ArrayList<DNA> matingPool; // ArrayList for the "mating pool"
    private String target;            // Target phrase
    private int generations;          // Number of generations
    private boolean finished;         // Are we finished evolving?
    private double perfectScore;
    private static final Random random = new Random();

    public Population(String target, double mutationRate, int num) {
        this.target = target;
        this.mutationRate = mutationRate;
        this.population = new DNA[num];
        for (int i = 0; i < population.length; i++) {
            population[i] = new DNA(target.length());
        }
        calcFitness();
        this.matingPool = new ArrayList<>();
        this.finished = false;
        this.generations = 0;
        this.perfectScore = Math.pow(2, target.length());
    }

    // Fill the fitness array with a value for every member of the population
    public void calcFitness() {
        for (DNA dna : population) {
            dna.calculateFitness(target);
        }
    }

    // Generate a mating pool
    public void naturalSelection() {
        matingPool.clear();
        double maxFitness = 0;

        for (DNA dna : population) {
            if (dna.getFitness() > maxFitness) {
                maxFitness = dna.getFitness();
            }
        }

        for (DNA dna : population) {
            double fitness = map(dna.getFitness(), 0, maxFitness, 0, 1);
            int n = (int) (fitness * 100);
            for (int j = 0; j < n; j++) {
                matingPool.add(dna);
            }
        }
    }

    // Create a new generation
    public void generate() {
        for (int i = 0; i < population.length; i++) {
            int a = random.nextInt(matingPool.size());
            int b = random.nextInt(matingPool.size());
            DNA partnerA = matingPool.get(a);
            DNA partnerB = matingPool.get(b);
            DNA child = partnerA.crossover(partnerB);
            child.mutate(mutationRate);
            population[i] = child;
        }
        generations++;
    }

    // Compute the current "most fit" member of the population
    public String getBest() {
        double worldRecord = 0.0;
        int index = 0;
        for (int i = 0; i < population.length; i++) {
            if (population[i].getFitness() > worldRecord) {
                index = i;
                worldRecord = population[i].getFitness();
            }
        }

        if (worldRecord == perfectScore) {
            finished = true;
        }
        return population[index].getPhrase();
    }

    public boolean isFinished() {
        return finished;
    }

    public int getGenerations() {
        return generations;
    }

    // Compute average fitness for the population
    public double getAverageFitness() {
        double total = 0;
        for (DNA dna : population) {
            total += dna.getFitness();
        }
        return total / population.length;
    }

    public String allPhrases() {
        StringBuilder everything = new StringBuilder();
        int displayLimit = Math.min(population.length, 50);

        for (int i = 0; i < displayLimit; i++) {
            everything.append(population[i].getPhrase()).append("\n");
        }
        return everything.toString();
    }

    // Utility function to map values (similar to Processing's map function)
    private double map(double value, double start1, double stop1, double start2, double stop2) {
        return start2 + (value - start1) * (stop2 - start2) / (stop1 - start1);
    }
}