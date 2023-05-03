import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/****************************************************/
public class DiningPhilosopher {

    public static Random randy = new Random(1); // seed so always get same sequence of numbers

    // 2-dimensional array for display
    private static Board board;

    // Delay for times
    private static final int Delay = 9000; // 2500;

    // Number of philosophers
    private static final int NUM_PHILOSOPHERS = 4;

    // States of a philosopher
    static enum pState {
        STARTING, THINKING, HUNGRY, LCHOP, RCHOP, EATING,
        SLEEPING, NONE
    }

    static pState[] states = new pState[NUM_PHILOSOPHERS];

    // CHOPSTICKs
    Semaphore[] chopsticks = new Semaphore[NUM_PHILOSOPHERS];
    // Boolean Array for chopsticks
    /* static */ Lock lock = new ReentrantLock();
    /* static */ Condition[] condition = new Condition[5];

    // Semaphores for display
    static Semaphore talk = new Semaphore(1);

    Philosopher philos[] = new Philosopher[NUM_PHILOSOPHERS];

    public DiningPhilosopher() {
        board = new Board(); // create world
        lock = new ReentrantLock();
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            states[i] = pState.THINKING;
            chopsticks[i] = new Semaphore(1);
            condition[i] = lock.newCondition();
            philos[i] = new Philosopher(i, chopsticks, condition, lock);
        }
    }

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

    // ******************************************
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
                world[5] = "     \\      /        .".toCharArray();
                world[6] = "        ---          .".toCharArray();
                world[7] = "    /        \\       .".toCharArray();
                world[8] = "        (2)          .".toCharArray();
                System.out.print("\033[2J"); // clear the screen
                talk.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // display();
        }
        /*
         * 
         * ESC Code Sequence Description
         * ESC[J erase in display (same as ESC[0J)
         * ESC[0J erase from cursor until end of screen
         * ESC[1J erase from cursor to beginning of screen
         * ESC[2J erase entire screen
         * ESC[3J erase saved lines
         * ESC[K erase in line (same as ESC[0K)
         * ESC[0K erase from cursor to end of line
         * ESC[1K erase start of line to the cursor
         * ESC[2K erase the entire line
         * ESC[H moves cursor to home position (0, 0)
         * ESC[{line};{column}H
         * ESC[{line};{column}f moves cursor to line #, column #
         * ESC[#A moves cursor up # lines
         * ESC[#B moves cursor down # lines
         * ESC[#C moves cursor right # columns
         * ESC[#D moves cursor left # columns
         * ESC[#E moves cursor to beginning of next line, # lines down
         * ESC[#F moves cursor to beginning of previous line, # lines up
         * ESC[#G moves cursor to column #
         * ESC[6n request cursor position (reports as ESC[#;#R)
         * ESC M moves cursor one line up, scrolling if needed
         * ESC 7 save cursor position (DEC)
         * ESC 8 restores the cursor to the last saved position (DEC)
         * ESC[s save cursor position (SCO)
         * ESC[u restores the cursor to the last saved position (SCO)
         * 
         */

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

        public void clear() {
            System.out.print("\033[2J"); // clear the screen
            System.out.print("\033[2;1H"); // move curse to top left corner
        }

        public void message(boolean clear, String msg) {
            try {
                talk.acquire();
                // if(lineno>20||clear) {
                // System.out.print("\033[2J"); // clear the screen
                // lineno=1;
                // }
                // int offset = 25; // output to right
                int offset = lineno + 10; // output at bottom
                // System.out.print("\033[1;"+offset+"H"); // move curse
                System.out.print("\033[" + offset + ";2H"); // output at bottom
                System.out.print("\033[12"); // output at bottom
                // System.out.print("\033["+lineno+";"+offset+"H"); // output to the Right
                System.out.println("\t\t\t" + lineno + " " + msg);
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
                    c = 192 + (randy.nextInt(100) % 6);
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
                    c = 200 + (randy.nextInt(100) % 4);
                    break; // 200-203 Latin E with grave, acute, circumflex, or diaerese;
                case SLEEPING:
                    int tmp = (randy.nextInt(100) % 4); // sequence through 4 values
                    c = (tmp == 0) ? 's' : (tmp == 1) ? 'S' : 167;
                    break; // (tmp==2)?138:167; // 's', 'S', S with caron or Section sign
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
    // Board
    // *********************************************

    public static void main(String[] args) {
        long totalwaittime = 0;
        DiningPhilosopher dp = new DiningPhilosopher();

        // start display of table
        table tab = new table(board);
        tab.start();

        // Start philosophers
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            dp.philos[i].start();
        }
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            try {
                dp.philos[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            totalwaittime += dp.philos[i].getwaittime();
        }
        tab.should_shutdown = true;// interrupt();
        try {
            tab.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double elapsedTimeInSeconds = TimeUnit.MILLISECONDS.convert(totalwaittime, TimeUnit.NANOSECONDS) / 1000.0;
        System.out.println("\n\n");
        System.out.println("Total waiting time for 2nd chopstick for all philosophers was "
                + elapsedTimeInSeconds + "seconds");
    }

    public static class table extends Thread {
        public boolean should_shutdown = false;
        Board board;

        public table(Board b) {
            board = b;
        }

        public void run() {
            while (!should_shutdown) {
                for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
                    board.setPhilosopher(i, states[i]);
                    board.display();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            board.clear(); // clear screen
        }

    }

    /************************************************************/
    // Philosopher Thread
    /************************************************************/
    public static class Philosopher extends Thread {
        private int id;
        public long waittime = 0;
        public final static int MAXTHINKROUNDS = 2;
        Lock lock;
        Semaphore[] chopsticks;
        Condition[] condition;

        public Philosopher(int id, Semaphore[] sticks, Condition[] con, Lock l) {
            this.id = id;
            this.lock = l;
            waittime = 0;
            this.chopsticks = sticks;
            this.condition = con;
        }

        public long getwaittime() {
            return waittime;
        }

        public void run() {
            board.message(false, "Philosopher[" + id + "]: starting");
            states[id] = pState.STARTING;

            // random delay before we start
            try {
                Thread.sleep(randy.nextInt(100) % 4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int round = 0; round < MAXTHINKROUNDS; round++) {
                eat();
                sleep();
                think();
            }
        }

        private void sleep() {
            states[id] = pState.SLEEPING;
            board.message(false, "Philosopher[" + id + "]: SLEEPING");
            try {
                Thread.sleep(randy.nextInt(Delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void think() {
            states[id] = pState.THINKING;
            board.message(false, "Philosopher[" + id + "]: THINKING");
            try {
                Thread.sleep(randy.nextInt(Delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void pickup_chopsticks() {
            int left = (id + 1) % NUM_PHILOSOPHERS;
            int right = id;
            board.message(false, "Philosopher[" + id + "]: reaching for first chopstick");

            // time how long they have to wait to get iboth chopsticks
            long startTime = System.nanoTime();

            if (id % 2 == 0) { // EVEN PHILOSOPHER GET RIGHT CHOPSTICK FIRST
                try {
                    chopsticks[right].acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                board.getChopstick(right, id);
                states[id] = pState.RCHOP;
                board.message(false, "Philosopher[" + id + "]: has RIGHT chopstick(" + right + ")");
            } else { // ODD PHILOSOPHER GET LEFT CHOPSTICK FIRST
                board.message(false, "Philosopher[" + id + "]: reaching for LEFT chopstick(" + left + ")");
                try {
                    chopsticks[left].acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                board.getChopstick(left, id);
                states[id] = pState.LCHOP;
                board.message(false, "Philosopher[" + id + "]: has LEFT chopstick(" + left + ")");
            }
            /** DO NOT REMOVE THIS **/
            try {
                // delay so another philosopher might come along
                Thread.sleep(1); // /*Delay*/ // no random sleep here - no if we are going to time wait
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /** DO NOT REMOVE THIS **/

            if (id % 2 == 0) { // EVEN PHILOSOPHERS
                try {
                    chopsticks[left].acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                board.getChopstick(left, id);
            } else { // ODD PHILOSOPHERS GETTING RIGHT CHOPSTICK
                try {
                    chopsticks[right].acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                board.getChopstick(right, id);
            }
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            waittime += duration;
        }

        private void putdown_chopsticks() {
            int left = (id + 1) % NUM_PHILOSOPHERS;
            int right = id;

            chopsticks[right].release();
            board.putChopstick(right, id);
            chopsticks[left].release();
            board.putChopstick(left, id);
            states[id] = pState.NONE;
            board.message(false, "Philosopher[" + id + "]: putting back both choopstick(" + right + "," + left + ")");
        }

        private void eat() {
            int left = (id + 1) % NUM_PHILOSOPHERS;
            int right = id;
            states[id] = pState.HUNGRY;
            board.message(false, "Philosopher[" + id + "]: HUNGRY");

            pickup_chopsticks();

            states[id] = pState.EATING;
            board.message(false, "Philosopher[" + id + "]: EATING");
            try {
                Thread.sleep(randy.nextInt(Delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            putdown_chopsticks();
        }
    }
    /************************************************************/
    // Philosopher Thread
    /************************************************************/
}
/****************************************************/
/****************************************************/
/****************************************************/

/*
 * ----------------------
 * \ /
 * \ ( ) /
 * ---
 * / \
 * ( ) | (+) | ( )
 * \ /
 * ---
 * / \
 * / ( ) \
 * 
 * $ - sleeping
 * # - thinking
 * h - hungry
 * E - eating
 * 
 * |(E)|
 * | |
 * ---
 * / \
 * ( ) | (+) | ( )
 * \ /
 * ---
 * / \
 * / ( ) \
 * 
 * 
 * \
 * \ ( )
 * ---
 * / \ --
 * ( ) | (+) | (E)
 * \ / --
 * ---
 * /
 * / ( )
 * 
 * ----------------------
 */
