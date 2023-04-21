import java.util.Random;
import java.util.concurrent.Semaphore;

public class DiningPv2 {

    // 2-D array for display
    private static Board board;

    // Delay for times
    private static final int DELAY = 5000; // 2500;

    // Random sleep times
    private static final Random RAND = new Random(2023);

    // Number of philosophers
    private static final int NUM_PHILOSOPHERS = 4;

    // States of philosopher
    static enum pStates {
        STARTING, THINKING, HUNGRY, LCHOP, RCHOP, EATING, SLEEPING, NONE
    };

    static pStates[] s = new pStates[NUM_PHILOSOPHERS];

    // Semaphors for chopsticks
    static Semaphore[] stickSemaphores = new Semaphore[4];

    // Semaphors for display
    static Semaphore talk = new Semaphore(1);

    class Board {
        private char[][] world = new char[9][20];
        private int line;

        public Board() {
            line = 1;
            simWorld();
        }

        private void simWorld() {
            world[0] = String.format("\t      %s %s %s", 1, 2, 1).toCharArray();
            world[1] = "\t_________________".toCharArray();
            world[2] = "\t|\t\t|".toCharArray();
            world[3] = String.format("    %s\t|\t\t|   %s", 1, 1).toCharArray();
            world[4] = String.format("    %s\t|\t\t|   %s", 2, 2).toCharArray();
            world[5] = String.format("    %s\t|\t\t|   %s", 1, 1).toCharArray();
            world[6] = "\t|\t\t|".toCharArray();
            world[7] = "\t‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾".toCharArray();
            world[8] = String.format("\t      %s %s %s", 1, 2, 1).toCharArray();
        }

        public void display() {
            try {
                talk.acquire();
                System.out.print("\033[;H");
                for (int i = 0; i < world.length; i++) {
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
                if (line > 20 || clear) {
                    System.out.print("\033[2J");
                    line = 1;
                }
                int offset = line + 10;
                System.out.print("\033[" + offset + ";2H");
                System.out.println(line + " " + msg);
                line++;
                talk.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public String drawPhilosopher(int who) {
            if (who < 0 || who > NUM_PHILOSOPHERS)
                System.exit(-1);

            switch (s[who]) {
                case STARTING: return String.valueOf(who);
                case THINKING: return "...";
                case HUNGRY: return "H";
                case LCHOP: return "<";
                case RCHOP: return ">";
                case EATING: return "nom";
                case SLEEPING: return "zzz";
                default: return String.valueOf(who);
            }
        }
    }

    static class Philosopher extends Thread {
        private int id;

        public Philosopher(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            s[id] = pStates.STARTING;

            try {
                Thread.sleep(RAND.nextInt(DELAY));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (true) {
                eat();
                sleep();
                think();
            }
        }

        private void sleep() {
            s[id] = pStates.SLEEPING;
            //
            try {
                Thread.sleep(RAND.nextInt(DELAY));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void think() {
            s[id] = pStates.THINKING;
            //
            try {
                Thread.sleep(RAND.nextInt(DELAY));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void eat() {
            int left = (id + 1) % NUM_PHILOSOPHERS;
            int right = id;
            s[id] = pStates.HUNGRY;
            //
            try {
                // TODO: Add grabbing chopsticks
                Thread.sleep(RAND.nextInt(DELAY * 2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    DiningPv2() {
        board = new Board();
        board.display();
    }

    public static void main(String[] args) {
        new DiningPv2();
    }

}
