public class Main {
    final static int delay = 10000;

    public static void main(String[] args) {
        DiningPhilosophersMonitor monitor = new DiningPhilosophersMonitor();
        Philosopher[] philosophers = new Philosopher[5];

        for (int i = 0; i < 5; i++) {
            philosophers[i] = new Philosopher(i, monitor);
        }

        for (int i = 0; i < 5; i++) {
            philosophers[i].start();
        }
    }

    static class Philosopher extends Thread {
        private int id;
        private DiningPhilosophersMonitor monitor;

        public Philosopher(int id, DiningPhilosophersMonitor monitor) {
            this.id = id;
            this.monitor = monitor;
        }

        public void run() {
            try {
                while (true) {
                    System.out.println("Philosopher " + id + " is thinking");
                    Thread.sleep((int) (Math.random() * delay));

                    System.out.println("Philosopher " + id + " is hungry");
                    monitor.pickup(id);

                    System.out.println("Philosopher " + id + " is eating");
                    Thread.sleep((int) (Math.random() * delay));

                    monitor.putdown(id);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class DiningPhilosophersMonitor {

        int[] state = new int[5];

        public void pickup(int id) {
            state[id] = 0;

            test(id);

            while (state[id] != 1)
                ;
        }

        public void putdown(int id) {
            state[id] = 2;
            test((id + 1) % 5);
            test((id + 4) % 5);
        }

        private void test(int id) {
            if (state[(id + 1) % 5] != 1 && state[(id + 4) % 5] != 1 && state[id] == 0) {
                state[id] = 1;
            }
        }
    }
}
