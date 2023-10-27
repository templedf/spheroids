package templedf;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

class SpriteTest {
    @org.junit.jupiter.api.Test
    void testGetRegions() {
        Sprite s = new Sprite(400, 400, new Point[]{new Point(-10,10), new Point(10, 10), new Point(10, -10), new Point(-10, -10)}, new Rectangle(0, 0, 800, 800), new DragModel() {
            @Override
            public float[] drag(int x, int y, float xVel, float yVel) {
                return new float[0];
            }
        }) {
            @Override
            public void paint(Graphics g) {}
        };
    }
}