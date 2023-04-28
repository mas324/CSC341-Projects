
/*
 * Shawn Merana
 * proj05
 * smerana1
 *
 * A version of the Dinning Philosophers Problem
 * In Java with Jave Threads 
 * It has an ASCII art display (its own thread)
 * There are only four philosophers (easier to draw) 
 *
 * The chopsticks are an array of Semaphors  
 *   with a wait as chopsticks[left].acquire();
 *    and a signal as chopsticks[right].release();
 *
 * but there is no cordination so that the philosphers 
 * will deadlock - circular waiting each with their left 
 * chopstick 
 *
 */

import java.util.concurrent.Semaphore;
import java.util.Random;

public class DiningPhilosopher {

    // 2-dimensional array for display
    private static Board board;

    // Delay for times
    private static final int Delay = 5000; // 2500;

    // Random generator
    private static final Random RAND = new Random();

    // Number of philosophers
    private static final int NUM_PHILOSOPHERS = 4;

    // States of a philosopher
    static enum pState {
        STARTING, THINKING, HUNGRY, LCHOP, RCHOP, EATING,
        SLEEPING, NONE
    }

    static pState[] states = new pState[NUM_PHILOSOPHERS];

    // Semaphores for chopsticks
    static Semaphore[] chopsticks = new Semaphore[NUM_PHILOSOPHERS];

    // Semaphores for display
    static Semaphore talk = new Semaphore(1);

    private static Philosopher[] p;

    // Timer
    private static long timer;

    /***********************
     * 
     * 000000000011111111112
     * 012345678901234567890
     * 
     * 0 | 3 (3) 0
     * 1 | \ /
     * 2 | ---
     * 3 | / \
     * 4 |(2) | (+) | (0)
     * 5 | \ /
     * 6 | ---
     * 7 | / \
     * 8 | 2 (1) 1
     * 
     ************************/

    class Board {
        public char world[][];
        private int lineno;
        // unused chopsticks
        public final int tchop = 1; // row
        public final int bchop = 7;

        public final int lchop = 4; // cols
        public final int rchop = 13;

        // used chopsticks
        public final int lchopu = 7;
        public final int rchopu = 11;
        public final int tchopu = 3;
        public final int bchopu = 5;

        public final int uchopOddlc = 7;
        public final int uchopOddrc = 11;
        public final int uchop1row = 7;
        public final int uchop3row = 1;
        public final int uchopEvenlr = 3;
        public final int uchopEvenrr = 5;
        public final int uchop0col = 13;
        public final int uchop2col = 3;

        // philiosophers
        public final int ph3r = 0;
        public final int ph3c = 9;
        public final int ph0r = 4;
        public final int ph0c = 16;
        public final int ph1r = 8;
        public final int ph1c = 9;
        public final int ph2r = 4;
        public final int ph2c = 2;

        public Board() {
            talk = new Semaphore(1);
            lineno = 1;
            try {
                talk.acquire();
                world = new char[9][20];
                // 0000000000111111111
                // 0123456789012345678
                world[0] = "        (3)          .".toCharArray();
                world[1] = "    \\        /       .".toCharArray();
                world[2] = "        ---          .".toCharArray();
                world[3] = "      /     \\        .".toCharArray();
                world[4] = " (1) |  (+)  | (0)   .".toCharArray();
                world[5] = "      \\     /        .".toCharArray();
                world[6] = "        ---          .".toCharArray();
                world[7] = "    /         \\      .".toCharArray();
                world[8] = "        (2)          .".toCharArray();
                System.out.print("\033[2J"); // clear the screen
                talk.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // display();
        }

        public void display() {
            try {
                talk.acquire();
                // System.out.print("\033[2J"); // clear the screen
                System.out.print("\033[2;1H"); // move curse to top left corner
                for (int i = 0; i < 9; i++) {
                    System.out.println(world[i]);
                }
                talk.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void message(boolean clear, String msg) {
            try {
                talk.acquire();
                if (lineno > 20 || clear) {
                    System.out.print("\033[2J"); // clear the screen
                    lineno = 1;
                }
                // int offset = 25; // output to right
                int offset = lineno + 10; // output at bottom
                // System.out.print("\033[1;"+offset+"H"); // move curse
                System.out.print("\033[" + offset + ";2H"); // output at bottom
                // System.out.print("\033["+lineno+";"+offset+"H"); // output to the Right
                System.out.println("" + lineno + " " + msg);
                lineno++;
                talk.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void setPhilosopher(int who, pState action) {
            if (who < 0 || who > NUM_PHILOSOPHERS)
                System.exit(1);
            int row = 0, col = 0;
            int c = ' ', id = '0';
            // where is the Philosopher in the board (row, col)
            switch (who) {
                case 0:
                    row = ph0r;
                    col = ph0c;
                    id = '0';
                    break;
                case 1:
                    row = ph1r;
                    col = ph1c;
                    id = '1';
                    break;
                case 2:
                    row = ph2r;
                    col = ph2c;
                    id = '2';
                    break;
                default:
                    row = ph3r;
                    col = ph3c;
                    id = '3';
                    break;
                // case 3: row = ph3r; col = ph3c; id='3';break;

            }
            // what cah do we want to out at row col
            switch (action) {
                case STARTING:
                    c = id;
                    break;
                case THINKING:
                    c = 192 + (RAND.nextInt(100) % 6);
                    break; // 192-198 Latin A with grave/acute/circumflex/tilda/diaerese/ring
                case HUNGRY:
                    c = 'H';
                    break;
                case LCHOP:
                    c = '<';
                    break;
                case RCHOP:
                    c = '>';
                    break;
                case EATING:
                    c = 200 + (RAND.nextInt(100) % 4);
                    break; // 200-203 Latin E with grave, acute, circumflex, or diaerese;
                case SLEEPING:
                    int tmp = (RAND.nextInt(100) % 4); // sequence through 4 values
                    c = (tmp == 0) ? 's' : (tmp == 1) ? 'S' : 167;// (tmp==2)?138:167; // 's', 'S', S with caron or
                                                                  // Section sign
                    break;
                default:
                    c = id; // 186; // Ordinal indicator or could be '.';
            }
            world[row][col] = (char) c;
            // display();
        }

        /***********************
         * 000000000011111111112
         * 012345678901234567890
         * 0 | 3 (3) 0
         * 1 | \ /
         * 2 | ---
         * 3 | / \
         * 4 |(2) | (+) | (0)
         * 5 | \ /
         * 6 | ---
         * 7 | / \
         * 8 | 2 (1) 1
         ************************/
        public void getChopstick(int chop, int who) {
            if (chop < 0 || chop > NUM_PHILOSOPHERS)
                System.exit(1);
            switch (who) {
                case 0:
                    if (chop == 1) { // zero's right chopstick
                        world[bchop][rchop] = ' '; // remove from table
                        world[uchopEvenrr][uchop0col] = '-';
                    } else if (chop == 0) { // zero's right chopstick
                        world[tchop][rchop] = ' '; // remove from table
                        world[uchopEvenlr][uchop0col] = '-';
                    } else {
                        System.err.printf("Philosopher zero tried for wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }

                    break;
                case 1:
                    if (chop == 2) {
                        world[bchop][lchop] = ' '; // remove from table
                        world[uchop1row][uchopOddlc] = '|';
                    } else if (chop == 1) {
                        world[bchop][rchop] = ' '; // remove from table
                        world[uchop1row][uchopOddrc] = '|';
                    } else {
                        System.err.printf("Philosopher one tried for wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }
                    break;
                case 2:
                    if (chop == 3) {
                        world[tchop][lchop] = ' '; // remove from table
                        world[uchopEvenlr][uchop2col] = '-';
                    } else if (chop == 2) {
                        world[bchop][lchop] = ' '; // remove from table
                        world[uchopEvenrr][uchop2col] = '-';
                    } else {
                        System.err.printf("Philosopher two tried for wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }
                    break;
                case 3:
                    if (chop == 0) {
                        world[tchop][rchop] = ' '; // remove from table
                        world[uchop3row][uchopOddrc] = '|';
                    } else if (chop == 3) {
                        world[tchop][lchop] = ' '; // remove from table
                        world[uchop3row][uchopOddlc] = '|';
                    } else {
                        System.err.printf("Philosopher three tried for wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }
                    break;
            }
            // display();
        }

        public void putChopstick(int chop, int who) {
            if (chop < 0 || chop > NUM_PHILOSOPHERS)
                System.exit(1);
            switch (who) {
                case 0:
                    if (chop == 1) { // zero's right chopstick
                        world[bchop][rchop] = '\\'; // put on table
                        world[uchopEvenlr][uchop0col] = ' ';
                    } else if (chop == 0) { // zero's right chopstick
                        world[tchop][rchop] = '/'; // put on table
                        world[uchopEvenrr][uchop0col] = ' ';
                    } else {
                        System.err.printf("Philosopher zero tried to put back wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }
                    break;
                case 1:
                    if (chop == 2) {
                        world[bchop][lchop] = '/'; // put on table
                        world[uchop1row][uchopOddlc] = ' ';
                    } else if (chop == 1) {
                        world[bchop][rchop] = '\\'; // put on table
                        world[uchop1row][uchopOddrc] = ' ';
                    } else {
                        System.err.printf("Philosopher one tried to put back wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }
                    break;
                case 2:
                    if (chop == 3) {
                        world[tchop][lchop] = '\\'; // put on table
                        world[uchopEvenlr][uchop2col] = ' ';
                    } else if (chop == 2) {
                        world[bchop][lchop] = '/'; // put on table
                        world[uchopEvenrr][uchop2col] = ' ';
                    } else {
                        System.err.printf("Philosopher two tried to put back wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }
                    break;
                case 3:
                    if (chop == 0) {
                        world[tchop][rchop] = '/'; // put on table
                        world[uchop3row][uchopOddlc] = ' ';
                    } else if (chop == 3) {
                        world[tchop][lchop] = '\\'; // put on table
                        world[uchop3row][uchopOddrc] = ' ';
                    } else {
                        System.err.printf("Philosopher zero tried to put back wrong chopstick(" + chop + ")\n");
                        System.exit(1);
                    }
                    break;
            }
            // display();
        }

    }

    public DiningPhilosopher() {
        // talk = new Semaphore(1);
        board = new Board(); // create world
    }

    public static void main(String[] args) {

        new DiningPhilosopher();
        // Initialize states and chopsticks
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            states[i] = pState.THINKING;
            chopsticks[i] = new Semaphore(1);
        }

        // start display of table
        new table(board).start();

        p = new Philosopher[NUM_PHILOSOPHERS];

        timer = System.currentTimeMillis();
        // Start philosophers
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            p[i] = new Philosopher(i);
            p[i].start();
        }
    }

    public static class table extends Thread {
        Board board;

        public table(Board b) {
            board = b;
        }

        public void run() {
            boolean done = false;
            while (!done) {
                for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
                    board.setPhilosopher(i, states[i]);
                    board.display();
                }

                for (int x = 0; x < NUM_PHILOSOPHERS; x++) {
                    if (!p[x].done) {
                        done = false;
                        break;
                    } else
                        done = true;
                }
                if (done) {
                    timer = System.currentTimeMillis() - timer;
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\033[2J Program run time was " + (timer / 1000) + " seconds.");
        }
    }

    public static class Philosopher extends Thread {
        private int id;
        private static boolean special = false;
        private boolean chosen;
        private boolean done;
        private int cycle;
        private final int ROUNDS = 3;

        public Philosopher(int id) {
            chosen = false;
            done = false;
            cycle = 0;
            this.id = id;
        }

        public boolean getDone() {
            return done;
        }

        public void run() {
            board.message(false, "Philosopher[" + id + "]: starting");
            states[id] = pState.STARTING;

            // random delay before we start
            try {
                Thread.sleep(RAND.nextInt(Delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (cycle < ROUNDS) {
                eat();
                sleep();
                think();
                cycle++;
            }

            done = true;
        }

        private void sleep() {
            states[id] = pState.SLEEPING;
            board.message(false, "Philosopher[" + id + "]: SLEEPING");
            try {
                Thread.sleep(RAND.nextInt(Delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void think() {
            states[id] = pState.THINKING;
            board.message(false, "Philosopher[" + id + "]: THINKING");
            try {
                Thread.sleep(RAND.nextInt(Delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void eat() {
            int left = (id + 1) % NUM_PHILOSOPHERS;
            int right = id;
            states[id] = pState.HUNGRY;
            board.message(false, "Philosopher[" + id + "]: HUNGRY");
            try {
                OE(left, right); // this is faster, using this
                // reverse(left, right); // this is slower

                Thread.sleep(RAND.nextInt(2 * Delay));
                chopsticks[right].release();
                board.putChopstick(right, id);
                chopsticks[left].release();
                board.putChopstick(left, id);
                states[id] = pState.NONE;
                board.message(false,
                        "Philosopher[" + id + "]: putting back both chopstick(" + right + "," + left + ")");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void reverse(int left, int right) throws InterruptedException {
            if (!special || chosen) {
                special = chosen = true;
                pickup(right, left);
            } else
                pickup(left, right);
        }

        private void OE(int left, int right) throws InterruptedException {
            if (id % 2 == 0)
                pickup(right, left);
        }

        private void pickup(int left, int right) throws InterruptedException {
            /* getting left chopstick */
            board.message(false, "Philosopher[" + id + "]: reaching for LEFT chopstick(" + left + ")");
            chopsticks[left].acquire();
            board.getChopstick(left, id);
            states[id] = pState.LCHOP;
            board.message(false, "Philosopher[" + id + "]: has LEFT chopstick(" + left + ")");
            /* done getting left chopstick */

            /** DO NOT REMOVE THIS **/
            Thread.sleep(Delay); // delay so another philosopher might come and grab the chopstick we want

            /* getting right chopstick */
            chopsticks[right].acquire();
            board.getChopstick(right, id);
            board.message(false, "Philosopher[" + id + "]: has Right+Left chopstick(" + right + "," + left + ")");
            states[id] = pState.EATING;
            board.message(false, "Philosopher[" + id + "]: EATING");
            /* done getting right chopstick */
        }
    }
}
