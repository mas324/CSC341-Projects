public class DiningPv2 {

    private static Board board;

    static enum states {SLEEPING, THINKING, EATING};

    class Board {
        private char[][] world = new char[9][20];

        public Board() {
            createWorld();
        }

        private void createWorld() {
            world[0] = "\t\t\tP".toCharArray();
            world[1] = "\t\t_________________".toCharArray();
            world[2] = "\t\t|\t\t|".toCharArray();
            world[3] = "\t\t|\t\t|".toCharArray();
            world[4] = "\t    P\t|\t\t|   P".toCharArray();
            world[5] = "\t\t|\t\t|".toCharArray();
            world[6] = "\t\t|\t\t|".toCharArray();
            world[7] = "\t\t‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾".toCharArray();
            world[8] = "\t\t\tP".toCharArray();
        }

        private void display() {
            for (int i = 0; i < world.length; i++) {
                System.out.println(world[i]);
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
            // TODO Auto-generated method stub
            super.run();
        }

        public void sleep() {
            
        }

        public void think() {
            
        }

        public void eat() {
            
        }


    }

    DiningPv2() {
        board = new Board();
    }

    public static void main(String[] args) {
        new DiningPv2();
    }

}
