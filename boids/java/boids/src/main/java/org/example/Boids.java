package org.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.Timer;

public class Boids extends JPanel {
    Flock flock1, flock2;
    final int w, h;

    public Boids() {
        w = 1920;
        h = 1080;

        setPreferredSize(new Dimension(w, h));
        setBackground(Color.white);

        spawnFlocks();

        new Timer(17, (ActionEvent e) -> {
            repaint();
        }).start();
    }

    private void spawnFlocks() {
        flock1 = Flock.spawn(-300, h * 0.25, 200, Color.RED);  // Flock 1 (Red)
        flock2 = Flock.spawn(-300, h * 0.75, 200, Color.BLUE);  // Flock 2 (Blue)
    }

    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Run both flocks with each other's boids as obstacles
        flock1.run(g, flock2.boids, w, h);
        flock2.run(g, flock1.boids, w, h);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("Boids");
            f.setResizable(false);
            f.add(new Boids(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}

class Boid {
    static final Random r = new Random();
    static final Vec migrate = new Vec(0.02, 0);
    static final int size = 3;
    static final Path2D shape = new Path2D.Double();

    static {
        shape.moveTo(0, -size * 2);
        shape.lineTo(-size, size * 2);
        shape.lineTo(size, size * 2);
        shape.closePath();
    }

    final double maxForce, maxSpeed;
    Vec location, velocity, acceleration;
    private boolean included = true;
    Color color;  // New color attribute

    Boid(double x, double y, Color color) {
        acceleration = new Vec();
        velocity = new Vec(r.nextInt(3) + 1, r.nextInt(3) - 1);
        location = new Vec(x, y);
        this.color = color;  // Set the color based on the flock
        maxSpeed = 5.0;
        maxForce = 0.05;
    }

    void update(int w, int h) {
        velocity.add(acceleration);
        velocity.limit(maxSpeed);

        // Check boundaries and reflect the boid if it's out of bounds
        if (location.x > w) {
            location.x = w; // Keep the boid inside the right boundary
            velocity.x *= -1; // Reverse horizontal velocity
        }
        if (location.x < 0) {
            location.x = 0; // Keep the boid inside the left boundary
            velocity.x *= -1; // Reverse horizontal velocity
        }
        if (location.y > h) {
            location.y = h; // Keep the boid inside the bottom boundary
            velocity.y *= -1; // Reverse vertical velocity
        }
        if (location.y < 0) {
            location.y = 0; // Keep the boid inside the top boundary
            velocity.y *= -1; // Reverse vertical velocity
        }

        location.add(velocity);
        acceleration.mult(0);
    }

    void applyForce(Vec force) {
        acceleration.add(force);
    }

    Vec seek(Vec target) {
        Vec steer = Vec.sub(target, location);
        steer.normalize();
        steer.mult(maxSpeed);
        steer.sub(velocity);
        steer.limit(maxForce);
        return steer;
    }

    void flock(Graphics2D g, List<Boid> boids, List<Boid> otherFlockBoids) {
        view(g, boids);

        Vec rule1 = separation(boids, otherFlockBoids);
        Vec rule2 = alignment(boids);
        Vec rule3 = cohesion(boids);

        rule1.mult(10.5);
        rule2.mult(0.1);
        rule3.mult(2);

        applyForce(rule1);
        applyForce(rule2);
        applyForce(rule3);
        //applyForce(migrate);
    }

    void view(Graphics2D g, List<Boid> boids) {
        double sightDistance = 100;
        double peripheryAngle = PI * 0.85;

        for (Boid b : boids) {
            b.included = false;

            if (b == this)
                continue;

            double d = Vec.dist(location, b.location);
            if (d <= 0 || d > sightDistance)
                continue;

            Vec lineOfSight = Vec.sub(b.location, location);

            double angle = Vec.angleBetween(lineOfSight, velocity);
            if (angle < peripheryAngle)
                b.included = true;
        }
    }

    Vec separation(List<Boid> boids, List<Boid> otherFlockBoids) {
        double desiredSeparation = 55;

        Vec steer = new Vec(0, 0);
        int count = 0;

        // Avoid other flock members
        for (Boid b : otherFlockBoids) {
            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < desiredSeparation)) {
                Vec diff = Vec.sub(location, b.location);
                diff.normalize();
                diff.div(d);  // weight by distance
                steer.add(diff);
                count++;
            }
        }

        if (count > 0) {
            steer.div(count);
        }

        if (steer.mag() > 0) {
            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
            return steer;
        }
        return new Vec(0, 0);
    }

    Vec alignment(List<Boid> boids) {
        double preferredDist = 50;

        Vec steer = new Vec(0, 0);
        int count = 0;

        for (Boid b : boids) {
            if (!b.included)
                continue;

            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < preferredDist)) {
                steer.add(b.velocity);
                count++;
            }
        }

        if (count > 0) {
            steer.div(count);
//            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
        }
        return steer;
    }

    Vec cohesion(List<Boid> boids) {
        double preferredDist = 50;

        Vec target = new Vec(0, 0);
        int count = 0;

        for (Boid b : boids) {
            if (!b.included)
                continue;

            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < preferredDist)) {
                target.add(b.location);
                count++;
            }
        }
        if (count > 0) {
            target.div(count);
            return seek(target);
        }
        return target;
    }

    void draw(Graphics2D g) {
        AffineTransform save = g.getTransform();

        g.translate(location.x, location.y);
        g.rotate(velocity.heading() + PI / 2);
        g.setColor(color);  // Use the boid's color
        g.fill(shape);
        g.setColor(Color.black);
        g.draw(shape);

        g.setTransform(save);
    }

    void run(Graphics2D g, List<Boid> boids, int w, int h, List<Boid> otherFlockBoids) {
        flock(g, boids, otherFlockBoids);
        update(w, h);
        draw(g);
    }
}

class Flock {
    List<Boid> boids;
    Color color;

    Flock(Color color) {
        this.color = color;
        boids = new ArrayList<>();
    }

    void run(Graphics2D g, List<Boid> otherFlockBoids, int w, int h) {
        for (Boid b : boids) {
            b.run(g, boids, w, h, otherFlockBoids);  // Pass the other flock boids
        }
    }

    void addBoid(Boid b) {
        boids.add(b);
    }

    static Flock spawn(double w, double h, int numBoids, Color color) {
        Flock flock = new Flock(color);
        Random r = new Random();  // Create a new Random object here
        for (int i = 0; i < numBoids; i++) {
            // Generate random positions within the bounds of the screen
            double randomX = r.nextDouble() * w;
            double randomY = r.nextDouble() * h;

            // Create a new boid with random positions and add it to the flock
            flock.addBoid(new Boid(randomX, randomY, color));  // Pass the color
        }
        return flock;
    }
}

class Vec {
    double x, y;

    Vec() {
    }

    Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void add(Vec v) {
        x += v.x;
        y += v.y;
    }

    void sub(Vec v) {
        x -= v.x;
        y -= v.y;
    }

    void div(double val) {
        x /= val;
        y /= val;
    }

    void mult(double val) {
        x *= val;
        y *= val;
    }

    double mag() {
        return sqrt(pow(x, 2) + pow(y, 2));
    }

    double dot(Vec v) {
        return x * v.x + y * v.y;
    }

    void normalize() {
        double mag = mag();
        if (mag != 0) {
            x /= mag;
            y /= mag;
        }
    }

    void limit(double lim) {
        double mag = mag();
        if (mag != 0 && mag > lim) {
            x *= lim / mag;
            y *= lim / mag;
        }
    }

    double heading() {
        return atan2(y, x);
    }

    static Vec sub(Vec v, Vec v2) {
        return new Vec(v.x - v2.x, v.y - v2.y);
    }

    static double dist(Vec v, Vec v2) {
        return sqrt(pow(v.x - v2.x, 2) + pow(v.y - v2.y, 2));
    }

    static double angleBetween(Vec v, Vec v2) {
        return acos(v.dot(v2) / (v.mag() * v2.mag()));
    }
}
