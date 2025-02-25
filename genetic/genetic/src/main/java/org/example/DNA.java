package org.example;

import java.util.Random;

import java.util.Random;

public class DNA {

    private char[] genes;
    private double fitness;
    private static final Random random = new Random();

    // Constructor (makes a random DNA)
    public DNA(int num) {
        genes = new char[num];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = (char) (random.nextInt(96) + 32); // Pick from range of chars (32-127)
        }
    }

    // Converts character array to a String
    public String getPhrase() {
        return new String(genes);
    }

    // Fitness function (returns floating point % of "correct" characters)
    public void calculateFitness(String target) {
        int score = 0;
        for (int i = 0; i < genes.length; i++) {
            if (genes[i] == target.charAt(i)) {
                score++;
            }
        }
        //fitness = 1000*((float)score/target.length());
        fitness = Math.pow(2, score);
    }

    public double getFitness() {
        return fitness;
    }

    // Crossover
    public DNA crossover(DNA partner) {
        DNA child = new DNA(genes.length);
        int midpoint = random.nextInt(genes.length); // Pick a midpoint

        // Half from one, half from the other
        for (int i = 0; i < genes.length; i++) {
            if (i > midpoint) child.genes[i] = genes[i];
            else child.genes[i] = partner.genes[i];
        }
        return child;
    }

    // Based on a mutation probability, picks a new random character
    public void mutate(double mutationRate) {
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < mutationRate) {
                genes[i] = (char) (random.nextInt(96) + 32);
            }
        }
    }
}
