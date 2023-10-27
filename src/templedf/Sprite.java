package templedf;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Sprite {
    static int REGION_DIVS = 4;
    private final int initialX;
    private final int initialY;
    private float x;
    private float y;
    protected float xVelocity;
    protected float yVelocity;
    protected final Point[] vertices;
    protected final Rectangle bounds;
    protected final DragModel drag;
    protected final float[] axisSlopes;
    protected final float[][] projections;
    protected final int[] xRegionBoundaries;
    protected final int[] yRegionBoundaries;
    protected final int minX;
    protected final int maxX;
    protected final int minY;
    protected final int maxY;
    protected Integer[] regionCache = null;

    Sprite(int x, int y, Point[] vertices, Rectangle bounds, DragModel drag) {
        initialX = x;
        initialY = y;
        setX(initialX);
        setY(initialY);
        this.bounds = new Rectangle(bounds);
        this.vertices = new Point[vertices.length];
        this.drag = drag;

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < vertices.length; i++) {
            this.vertices[i] = new Point(vertices[i]);

            minX = Math.min(minX, vertices[i].x);
            maxX = Math.min(maxX, vertices[i].x);
            minY = Math.min(minY, vertices[i].y);
            maxY = Math.min(maxY, vertices[i].y);
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        axisSlopes = calculateAxisSlopes();
        projections = calculateProjections();
        xRegionBoundaries = calculateBoundaries(bounds.x, bounds.width);
        yRegionBoundaries = calculateBoundaries(bounds.y, bounds.height);
    }

    /**
     * Calculate the slopes of the axes as the negative inverse of the slopes between each pair
     * of vertices. Strictly speaking we want a vector, but if we calculate the slope, then the
     * slope is y when x is 1. The nth element of the returned array is the slope of the axis that is
     * normal to the side formed by the nth vertex and the n+1st vertex.
     * @return the slopes of the normal lines to each pair of vertices
     */
    private float[] calculateAxisSlopes() {
        float[] axisSlopes = new float[vertices.length];

        for (int i = 0; i < vertices.length - 1; i++) {
            // We shortcut taking the inverse by calculating (x2-x1)/(y1-y2)
            axisSlopes[i] = (float)(vertices[i + 1].x - vertices[i].x) / (vertices[i].y - vertices[i + 1].y);
        }

        axisSlopes[vertices.length - 1] = (float)(vertices[0].x - vertices[vertices.length - 1].x) / (vertices[vertices.length - 1].y - vertices[0].y);

        return axisSlopes;
    }

    /**
     * Precalculate the projections of each vertex onto each axis. This calculation is half
     * of the work needed to perform SAT collision detection. The nth element of the returned array
     * is a 2-element array containing the min and max projections for the nth axis.
     * @return the procalculated min and max projections
     */
    private float[][] calculateProjections() {
        float[][] projections = new float[axisSlopes.length][];

        for (int i = 0; i < axisSlopes.length; i++) {
            // If slope is infinite, the axis magnitude is infinite, but we ignore it
            final float axisMagnitude = Utils.magnitude(axisSlopes[i]);
            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;

            for (int j = 0; j < vertices.length; j++) {
                // We calculate the dot product of the vertex as a vector starting at the origin with
                // the axis as a vector starting at the origin. To get the axis we let the slope be
                // the y coordinate and let the x coordinate be 1. We then divide the dot product by
                // the magnitude of the axis to get the projection.
                final float projection;

                if ((axisSlopes[i] == Float.NEGATIVE_INFINITY) || (axisSlopes[i] == Float.POSITIVE_INFINITY)) {
                    projection = vertices[j].y;
                } else {
                    projection = Utils.dot(vertices[j], axisSlopes[i]) / axisMagnitude;
                }

                if (projection < min) {
                    min = projection;
                }

                if (projection > max) {
                    max = projection;
                }
            }

            projections[i] = new float[] {min, max};
        }

        return projections;
    }

    private int[] calculateBoundaries(int min, int max) {
        int[] bounds = new int[REGION_DIVS];
        int region = (max - min) / (REGION_DIVS + 1);

        bounds[REGION_DIVS - 1] = max - region;

        for (int i = REGION_DIVS - 2; i >= 0; i--) {
            bounds[i] = bounds[i + 1] - region;
        }

        return bounds;
    }

    /**
     * Get the x coordinate of the center of the sprite.
     * @return the x coordinate of the center of the sprite
     */
    public int getX() {
        return (int)x;
    }

    /**
     * Get the y coordinate of the center of the sprite.
     * @return the y coordinate of the center of the sprite
     */
    public int getY() {
        return (int)y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void incrementVelocity(float x, float y) {
        xVelocity += x;
        yVelocity += y;
    }

    public void move() {
        if ((xVelocity != 0) || (yVelocity != 0)) {
            float[] drag = this.drag.drag(getX(), getY(), xVelocity, yVelocity);

            xVelocity += drag[0];
            yVelocity += drag[1];

            setX(Math.min(Math.max(x + xVelocity, bounds.x), bounds.width));
            setY(Math.min(Math.max(y + yVelocity, bounds.y), bounds.height));
            regionCache = null; // Invalidate the cache now that we've moved

            if ((x <= bounds.x) || (x >= bounds.width)) {
                xVelocity = 0;
            }

            if ((y <= bounds.y) || (y >= bounds.height)) {
                yVelocity = 0;
            }
        }
    }

    public void reset() {
        xVelocity = 0;
        yVelocity = 0;
        setX(initialX);
        setY(initialY);
        regionCache = null;
    }

    public boolean overlaps(Sprite sprite) {
        return overlaps(this, sprite) && overlaps(sprite, this);
    }

    private boolean overlaps(Sprite a, Sprite b) {
        // First check whether the regions overlap and fail fast if they don't
        List<Integer> regions = b.getRegions();

        regions.retainAll(a.getRegions());

        if (regions.isEmpty()) {
            return false;
        }

        for (int i = 0; i < b.axisSlopes.length; i++) {
            // Calculate the distance between the centers projected onto the axis
            final float axisMagnitude;
            final float centerDist;
            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;

            if ((b.axisSlopes[i] == Float.POSITIVE_INFINITY) || (b.axisSlopes[i] == Float.NEGATIVE_INFINITY)) {
                axisMagnitude = 1;
                centerDist = b.y - a.y;
            } else {
                axisMagnitude = Utils.magnitude(b.axisSlopes[i]);
                // Project the distance between the centers onto the axis
                centerDist = Utils.dot(Utils.dist(a, b), b.axisSlopes[i]) / axisMagnitude;
            }

            // Calculate the projection of each of a's vertices onto b's axis
            for (int j = 0; j < a.vertices.length; j++) {
                final float projection;

                if ((b.axisSlopes[i] == Float.POSITIVE_INFINITY) || (b.axisSlopes[i] == Float.NEGATIVE_INFINITY)) {
                    projection = a.vertices[j].y;
                } else {
                    projection = Utils.dot(a.vertices[j], b.axisSlopes[i]) / axisMagnitude;
                }

                if (projection < min) {
                    min = projection;
                }

                if (projection > max) {
                    max = projection;
                }
            }

            // Test for overlap
            if (((centerDist >= 0) && (centerDist > max - b.projections[i][0])) ||
                    ((centerDist < 0) && (centerDist < min - b.projections[i][1]))) {
                return false;
            }
        }

        return true;
    }

    protected List<Integer> getRegions() {
        if (regionCache != null) {
            return new ArrayList<>(Arrays.asList(regionCache));
        }

        List<Integer> list = new ArrayList<>(4);
        int row;
        int column;

        for (row = REGION_DIVS; row >= 1; row--) {
            if (getX() > xRegionBoundaries[row - 1]) {
                break;
            }
        }

        for (column = REGION_DIVS; column >= 1; column--) {
            if (getY() > yRegionBoundaries[column - 1]) {
                break;
            }
        }

        list.add(row * (REGION_DIVS + 1) + column);

        int xNeighbor = 0;
        int yNeighbor = 0;

        if ((row > 0) && (x + minX <= xRegionBoundaries[row - 1])) {
            xNeighbor = -1;
        } else if ((row + 1 < xRegionBoundaries.length) && (x + maxX >= xRegionBoundaries[row])) {
            xNeighbor = 1;
        }

        if ((column > 0) && (y + minY <= yRegionBoundaries[column - 1])) {
            yNeighbor = -1;
        } else if ((column + 1 < yRegionBoundaries.length) && (y + maxY >= yRegionBoundaries[column])) {
            yNeighbor = 1;
        }

        if ((xNeighbor != 0) && (yNeighbor != 0)) {
            list.add((row + xNeighbor) * (REGION_DIVS + 1) + column + yNeighbor);
        }

        if (xNeighbor != 0) {
            list.add((row + xNeighbor) * (REGION_DIVS + 1) + column);
        }

        if (yNeighbor != 0) {
            list.add(row * (REGION_DIVS + 1) + column + yNeighbor);
        }

        // Cache the regions because we typically will use them several times
        regionCache = new Integer[list.size()];
        regionCache = list.toArray(regionCache);

        return list;
    }

    public abstract void paint(Graphics g);
}
