package templedf;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;

public class Spheroids {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        frame.add(new GamePanel());
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static class GamePanel extends JPanel implements DragModel  {
        private static final int PLAYER_SIZE = 25;
        private static final int AMMO_SIZE = 10;
        private static final int AMMO_COUNT = 10;
        private static final Dimension DIMENSIONS = new Dimension(800, 800);
        private static final float ROOT_TWO_OVER_TWO = (float) Math.sqrt(2) / 2;
        public static final float ACCELERATION = 0.5f;
        public static final float DRAG = 0.4f;
        public static final long TICK = 10L;
        private final Controller controller = new Controller();
        private final Player[] players;
        private final Ammo[] ammo;
        private final Map<Player, Integer> score = new HashMap<>();
        private final Thread ticker;
        private boolean reset = false;
        private boolean showScore = false;
        private short tickCounter = 0;

        public GamePanel() {
            Rectangle bounds = new Rectangle(0,0, DIMENSIONS.width, DIMENSIONS.height);
            Random rand = new Random();

            setBackground(Color.WHITE);
            setFocusable(true);

            players = new Player[] {
                    new Player(200, 400, PLAYER_SIZE, bounds, this, Color.BLUE),
                    new Player(600, 400, PLAYER_SIZE, bounds, this, Color.RED)
            };
            ammo = new Ammo[AMMO_COUNT];

            for (int i = 0; i < AMMO_COUNT / 2; i++) {
                int x = rand.nextInt(AMMO_SIZE, 800 - AMMO_SIZE);
                int y = rand.nextInt(AMMO_SIZE, 800 - AMMO_SIZE);

                ammo[2 * i] = new Ammo(x, y, AMMO_SIZE, bounds, this);
                ammo[2 * i + 1] = new Ammo(800 - x, 800 - y, AMMO_SIZE, bounds, this);
            };

            for (Player p : players) {
                score.put(p, 0);
            }

            addKeyListener(controller);
            reset = true; // Cheap hack to warm up the score display
            ticker = startTicker();
        }

        private Thread startTicker() {
            Thread ticker = Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        tick();

                        try {
                            Thread.sleep(TICK);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });
            ticker.start();

            return ticker;
        }

        private void reset() {
            removeKeyListener(controller);
            controller.reset();
            showScore = true;
            repaint();

            try {
                Thread.sleep(3000L);

            for (Sprite s : players) {
                s.reset();
            }

            for (Sprite s : ammo) {
                s.reset();
            }

            showScore = false;
            addKeyListener(controller);
        }

        @Override
        public float[] drag(int x, int y, float xVel, float yVel) {
            // The sign for the drag is the opposite of the velocity
            int xSign = xVel < 0 ? 1 : -1;
            int ySign = yVel < 0 ? 1 : -1;
            float xDrag;
            float yDrag;

            if ((xVel != 0) && (yVel != 0)) {
                final float magnitude = Utils.magnitude(xVel, yVel);

                xDrag = Math.min(Math.abs(xVel), Math.abs(xVel) / magnitude * DRAG) * xSign;
                yDrag = Math.min(Math.abs(yVel), Math.abs(yVel) / magnitude * DRAG) * ySign;
            } else {
                xDrag = Math.min(Math.abs(xVel), DRAG) * xSign;
                yDrag = Math.min(Math.abs(yVel), DRAG) * ySign;
            }

            return new float[] {xDrag, yDrag};
        }

        private void tick() {
            if (reset) {
                reset();
                reset = false;
            } else {
                for (int p = 0; p < players.length; p++) {
                    float xVel = ACCELERATION * controller.getXDir(p);
                    float yVel = ACCELERATION * controller.getYDir(p);

                    if ((xVel != 0) && (yVel != 0)) {
                        xVel *= ROOT_TWO_OVER_TWO;
                        yVel *= ROOT_TWO_OVER_TWO;
                    }

                    players[p].incrementVelocity(xVel, yVel);
                    players[p].move();

                    if (controller.isShooting(p)) {
                        players[p].shoot();
                    }
                }

                for (Ammo a : ammo) {
                    if (a.isLive()) {
                        Player shooter = a.getShooter();
                        a.move();

                        for (Player p : players) {
                            if ((p != shooter) && a.overlaps(p)) {
                                score.put(shooter, score.get(shooter) + 1);

                                reset = true;
                            }
                        }
                    } else if (a.getShooter() == null) {
                        for (int p = 0; p < players.length; p++) {
                            if (!players[p].isLoaded() && players[p].overlaps(a)) {
                                players[p].load(a);
                            }
                        }
                    }
                }
            }

            tickCounter += 1;

            if (tickCounter == 3) {
                this.repaint();
                tickCounter = 0;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return DIMENSIONS;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (Sprite sprite : players) {
                sprite.paint(g);
            }

            for (Sprite sprite : ammo) {
                sprite.paint(g);
            }

            if (showScore) {
                g.setColor(Color.GRAY);
                g.fillRect(300, 350, 200, 100);
                g.setColor(Color.BLACK);
                g.fillRect(398, 350, 4, 100);
                g.setFont(g.getFont().deriveFont(60.0f));
                g.setColor(Color.BLUE);
                g.drawString(Integer.toString(score.get(players[0])), 330, 425);
                g.setColor(Color.RED);
                g.drawString(Integer.toString(score.get(players[1])), 430, 425);
            }
        }
    }
}
