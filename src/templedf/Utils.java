package templedf;

import java.awt.Point;

public abstract class Utils {
    public static float dot(Point p1, Point p2) {
        return (p2.y * p1.y) + (p2.x * p1.x);
    }

    public static float dot(Point p1, float y2) {
        return (y2 * p1.y) + p1.x;
    }

    public static float magnitude(Point p) {
        return magnitude(p.x, p.y);
    }

    public static float magnitude(float x, float y) {
        return (float)Math.abs(Math.sqrt(Math.pow(y, 2) + Math.pow(x, 2)));
    }

    public static float magnitude(float y) {
        return (float)Math.abs(Math.sqrt(Math.pow(y, 2) + 1));
    }

    public static Point dist(Sprite s1, Sprite s2) {
        return new Point(s2.getX() - s1.getX(), s2.getY() - s1.getY());
    }
}
