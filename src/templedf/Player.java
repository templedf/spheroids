package templedf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class Player extends Sprite {
    public static final float LAUNCH_SPEED = 20f;
    private final Color color;
    private Ammo ammo = null;
    private float lastXVel = 1f;
    private float lastYVel = 0f;

    public Player(int x, int y, int size, Rectangle bounds, DragModel drag, Color color) {
        super(x, y, generatePoints(size), pruneBounds(bounds, size), drag);
        this.color = color;
    }

    private static Point[] generatePoints(int size) {
        final int span = size / 2;

        return new Point[] {new Point(span, 0), new Point(0, span), new Point(-span, 0), new Point(0, -span)};
    }

    private static Rectangle pruneBounds(Rectangle bounds, int size) {
        final int span = size / 2;

        return new Rectangle(bounds.x + span, bounds.y + span, bounds.width - span, bounds.width - span);
    }

    @Override
    public void reset() {
        super.reset();
        ammo = null;
    }

    public void load(Ammo ammo) {
        this.ammo = ammo;
        ammo.setShooter(this);
    }

    public boolean isLoaded() {
        return ammo != null;
    }

    public void shoot() {
        if (ammo == null) {
            return;
        }

        float xVel = 0;
        float yVel = 0;

        if (lastXVel == 0) {
            yVel = lastYVel < 0 ? lastYVel - LAUNCH_SPEED : lastYVel + LAUNCH_SPEED;
        } else if (lastYVel == 0) {
            xVel = lastXVel < 0 ? lastXVel - LAUNCH_SPEED : lastXVel + LAUNCH_SPEED;
        } else {
            final float magnitude = Utils.magnitude(lastXVel, lastYVel);

            xVel = lastXVel / magnitude * (LAUNCH_SPEED + magnitude);
            yVel = lastYVel / magnitude * (LAUNCH_SPEED + magnitude);
        }

        ammo.shoot(xVel, yVel);
        ammo = null;
    }

    @Override
    public void setX(float x) {
        super.setX(x);

        if (ammo != null) {
            ammo.setX(x);
        }
    }

    @Override
    public void setY(float y) {
        super.setY(y);

        if (ammo != null) {
            ammo.setY(y);
        }
    }

    @Override
    public void move() {
        super.move();

        if ((xVelocity != 0) || (yVelocity != 0)) {
            lastXVel = xVelocity;
            lastYVel = yVelocity;
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

        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, vertices.length);
    }
}
