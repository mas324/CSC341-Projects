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
}

