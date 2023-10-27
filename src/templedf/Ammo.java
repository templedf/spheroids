package templedf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class Ammo extends Sprite {
    private Player shooter = null;
    private boolean live = false;

    public Ammo(int x, int y, int size, Rectangle bounds, DragModel drag) {
        super(x, y, generatePoints(size), bounds, drag);
    }

    private static Point[] generatePoints(int size) {
        final int span = size / 2;
        return new Point[] {new Point(span, span), new Point(-span, span), new Point(-span, -span), new Point(span, -span)};
    }

    @Override
    public void reset() {
        super.reset();
        shooter = null;
        live = false;
    }

    public void shoot(float xVel, float yVel) {
        this.xVelocity = xVel;
        this.yVelocity = yVel;
        live = true;
    }

    void setShooter(Player shooter) {
        this.shooter = shooter;
    }

    public Player getShooter() {
        return shooter;
    }

    public boolean isLive() {
        return live;
    }

    @Override
    public void move() {
        float[] drag = this.drag.drag(getX(), getY(), xVelocity, yVelocity);

        xVelocity += drag[0];
        yVelocity += drag[1];
        int x = getX() + (int)xVelocity;
        int y = getY() + (int)yVelocity;

        if (x < bounds.x) {
            x = bounds.x - x;
            xVelocity = -xVelocity;
        } else if (x > bounds.width) {
            x = bounds.width - (x - bounds.width);
            xVelocity = -xVelocity;
        }

        if (y < bounds.y) {
            y = bounds.y - y;
            yVelocity = -yVelocity;
        } else if (y > bounds.height) {
            y = bounds.height - (y - bounds.height);
            yVelocity = -yVelocity;
        }

        setX(x);
        setY(y);
        regionCache = null; // Invalid region cache

        if ((xVelocity == 0) && (yVelocity == 0)) {
            shooter = null;
            live = false;
        }
    }

    @Override
    public void paint(Graphics g) {
        int[] xPoints = new int[vertices.length];
        int[] yPoints = new int[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            xPoints[i] = vertices[i].x + getX();
            yPoints[i] = vertices[i].y + getY();
        }

        g.setColor(Color.GRAY);
        g.fillPolygon(xPoints, yPoints, vertices.length);
    }
}
