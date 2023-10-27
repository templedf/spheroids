package templedf;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controller extends KeyAdapter {
    int[] xDir = {0, 0};
    int[] yDir = {0, 0};
    boolean shoot[] = {false, false};

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                if (yDir[0] == 0) {
                    yDir[0] = 1;
                }

                break;
            case KeyEvent.VK_UP:
                if (yDir[0] == 0) {
                    yDir[0] = -1;
                }

                break;
            case KeyEvent.VK_LEFT:
                if (xDir[0] == 0) {
                    xDir[0] = -1;
                }

                break;
            case KeyEvent.VK_RIGHT:
                if (xDir[0] == 0) {
                    xDir[0] = 1;
                }

                break;
            case KeyEvent.VK_SPACE:
                shoot[0] = true;
                break;
            case KeyEvent.VK_S:
                if (yDir[1] == 0) {
                    yDir[1] = 1;
                }

                break;
            case KeyEvent.VK_W:
                if (yDir[1] == 0) {
                    yDir[1] = -1;
                }

                break;
            case KeyEvent.VK_A:
                if (xDir[1] == 0) {
                    xDir[1] = -1;
                }

                break;
            case KeyEvent.VK_D:
                if (xDir[1] == 0) {
                    xDir[1] = 1;
                }

                break;
            case KeyEvent.VK_F:
                shoot[1] = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                if (yDir[0] == 1) {
                    yDir[0] = 0;
                }

                break;
            case KeyEvent.VK_UP:
                if (yDir[0] == -1) {
                    yDir[0] = 0;
                }

                break;
            case KeyEvent.VK_LEFT:
                if (xDir[0] == -1) {
                    xDir[0] = 0;
                }

                break;
            case KeyEvent.VK_RIGHT:
                if (xDir[0] == 1) {
                    xDir[0] = 0;
                }

                break;
            case KeyEvent.VK_S:
                if (yDir[1] == 1) {
                    yDir[1] = 0;
                }

                break;
            case KeyEvent.VK_W:
                if (yDir[1] == -1) {
                    yDir[1] = 0;
                }

                break;
            case KeyEvent.VK_A:
                if (xDir[1] == -1) {
                    xDir[1] = 0;
                }

                break;
            case KeyEvent.VK_D:
                if (xDir[1] == 1) {
                    xDir[1] = 0;
                }

                break;
        }
    }

    public void reset() {
        xDir = new int[] {0, 0};
        yDir = new int[] {0, 0};
        shoot = new boolean[] {false, false};
    }

    public int getXDir(int player) {
        return xDir[player];
    }

    public int getYDir(int player) {
        return yDir[player];
    }

    public boolean isShooting(int player) {
        boolean ret = shoot[player];

        shoot[player] = false;

        return ret;
    }
}
