/*
 *
 * The Dining Philosophers problem is a classic synchronization problem in 
 * which involves a set of philosophers sitting around a and there are only 
 * as many forks as there are philosophers. The challenge is to design a 
 * solution to prevent deadlock and starvation.
 *
 * This solution uses chopstick class to protect and encapselate the 
 * shared chopsticks.  The bowl of rice or spaghetti is not considered a 
 * shared resource.   Access to the chopsticks is protected, considered a 
 * critical section, as only one philosopher can use a chopstick at a time.
 * 
 * This code, as orginaly given does NOT handle DEADLOCK or STARVATION
 * But there is code to detect DEADLOCK   This runs as a separate thread 
 * and looks for the situation where all the chopsticks have been claimed 
 * but all by different philosophers (thus none has both/two chopsticks)
 *
*/

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// class to protect the shared chopsticks is a hole
// this is used to provide a entry and exit of a critical section 
// for the picking-up and putting-down of the chopsticks
class Chopsticks {

    private Lock lock = new ReentrantLock();
    private Condition[] condition = new Condition[5];
    private boolean[] chop = new boolean[5];
    private int[] owner = new int[5];
    public int numberofphilosophers;

    public Chopsticks(int n) {
        super();
        // lock = new ReentrantLock();
        // condition = = new Condition[5];
        numberofphilosophers = n;
        for (int i = 0; i < n; i++) {
            condition[i] = lock.newCondition();
            chop[i] = true;
            owner[i] = -1;
        }
    }

    public void pickup(int id) throws InterruptedException {
        lock.lock();
        try {
            int left = id;
            int right = (id + 1) % numberofphilosophers;
            // Philosopher 4 wants to eat
            System.out.println("Philosopher " + id + " is try to get chopsticks ("
                    + left + " " + right + ")");
            while (!chop[left] || !chop[right]) {
                condition[id].await();
            }

            System.out.println("Philosopher " + id + " is has chopsticks ("
                    + left + " " + right + ")");
            chop[left] = false;
            chop[right] = false;
            owner[left] = id;
            owner[right] = id;
        } finally {
            lock.unlock();
        }
    }

    public void putdown(int id) {
        lock.lock();
        try {
            int left = id;
            int right = (id + 1) % numberofphilosophers;

            chop[left] = true;
            chop[right] = true;

            condition[left].signal();
            condition[right].signal();
            owner[left] = -1;
            owner[right] = -1;
            System.out.println("Philosopher " + id + " is " +
                    "puts back chopsticks (" + left + " " + right + ")");
        } finally {
            lock.unlock();
        }
    }

    // return a stirng with who (if anyone) has claimed this chopstick
    public String getInfo(int n) {
        String byWhom = " ";
        String locked = "(is free)";
        if (owner[n] != -1) {
            byWhom = "by " + owner[n];
            locked = "(is in use) ";
        }
        String str = new String("chopstick" + n + " " + locked + " " + byWhom);
        return str;
    }

    // the Lock toString give way too much info - keep it simple
    /*
     * public String toString(){
     * String str = new String("chopstick"+number);
     * return str;
     * }
     */

    // the number of the philosopher we a claimed by (or -1)
    public int Lockedby(int n) {
        return owner[n];

    }

    public String owner(int n) {
        if (owner[n] != -1) {
            return "" + owner[n];
        } else {
            return "none";
        }
    }
}

// main class (public - name of file)
public class DiningPhilosophers extends Thread {
    public static final int NUM_PHILOSOPHERS = 5; // how many 5 is the clasic anwser
    public State state; // an enum for EAT THINK HUNGRY
    public int philosopherNumber; // which philosopher are we
    // public Chopstick left, right; // guard for the left and right chopsticks
    public Chopsticks chopsticks;
    public int leftnum, rightnum; // which chopstick number is our left and right
    public boolean hasleft, hasright; // do we currrenly have our left or righ chop?

    public DiningPhilosophers(int pNum, Chopsticks chopsticks) {
        super("Philosopher" + pNum);
        philosopherNumber = pNum;
        // left = chopsticks[philosopherNumber]; // our left is same number
        // right = chopsticks[(philosopherNumber + 1) % NUM_PHILOSOPHERS]; // right is
        // +1 mod 5)
        this.chopsticks = chopsticks;
        leftnum = philosopherNumber;
        rightnum = (philosopherNumber + 1) % NUM_PHILOSOPHERS;
        state = State.THINKING;
        hasleft = hasright = false;
    }

    public String toSting() {
        int num = philosopherNumber;
        String stat = "SLEEPING";
        switch (state) {
            case EATING:
                stat = "EATING";
                break;
            case HUNGRY:
                stat = "HUNGRY";
                break;
            case THINKING:
                stat = "THINKING";
                break;
        }
        String str = new String("philosopher" + num + " " + stat + " chopsticks " + hasleft + " " + hasright + "\n");
        return str;
    }

    public long getId() {
        return philosopherNumber;
    }

    private void pickUpForks() { // throws InterruptedException {
        // Acquire both chopsticks
        try {
            chopsticks.pickup(philosopherNumber);
        } catch (InterruptedException e) {
        }
    }

    // order of putting down is unimportant
    private void putDownForks() {
        chopsticks.putdown(philosopherNumber);
    }

    public void eating() {
        state = State.EATING;
        System.err.println("Philosopher " + philosopherNumber + " is eating");
        sleep();
        putDownForks();
    }

    public void thinking() {
        state = State.THINKING;
        System.err.println("Philosopher " + philosopherNumber + " is thinking");
        sleep();
    }

    public void hungry() {
        state = State.HUNGRY;
        System.err.println("Philosopher " + philosopherNumber + " is hungry");
        pickUpForks();
    }

    public void run() {
        while (true) {
            thinking();
            hungry();
            eating();
        }
    }

    public void sleep() {
        try {
            Thread.sleep((long) (Math.random() * 2000));
        } catch (InterruptedException e) {
        }
    }

    enum State {
        EATING,
        THINKING,
        HUNGRY
    }

    public static void main(String[] args) {
        Chopsticks chopsticks = new Chopsticks(NUM_PHILOSOPHERS);
        DiningPhilosophers pth[] = new DiningPhilosophers[NUM_PHILOSOPHERS];

        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            System.out.println("creating Philosophers " + i);
            pth[i] = new DiningPhilosophers(i, chopsticks);
        }

        // Thread dchecker = new deadlockChecker(chopsticks);
        // dchecker.start();

        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            System.out.println("starting Philosophers " + i);
            pth[i].start();
            System.out.println("Philosophers " + i + " started");
        }

        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            System.out.println("waiting for Philosophers " + i);
            try {
                pth[i].join();
            } catch (InterruptedException e) {
            }
        }
        // try{
        // dchecker.join(); // not needed - we are using Monitors
        // } catch(InterruptedException e){}
    }
}

//
// class to check for deadlock
// - all chop in use but by different philosophers
//
/*
 * class deadlockChecker extends Thread {
 * private Chopstick[] chopsticks;
 * private final int NUM_PHILOSOPHERS;
 * 
 * public deadlockChecker(Chopstick[] chop){
 * NUM_PHILOSOPHERS = chop.length;
 * chopsticks = chop;
 * }
 * 
 * public void run(){
 * while (true) {
 * try {
 * Thread.sleep((long) (Math.random() * 2000));
 * } catch (InterruptedException e){}
 * boolean possible_deadlock = true;
 * for (int i=0; i< NUM_PHILOSOPHERS; i++) {
 * if ( chopsticks[i].Lockedby() == -1)
 * possible_deadlock = false;
 * //break; // could stop look here
 * }
 * if(possible_deadlock) {
 * int owners[] = new int[NUM_PHILOSOPHERS];
 * for (int i=0; i< NUM_PHILOSOPHERS; i++) {
 * owners[i] = chopsticks[i].Lockedby();
 * }
 * for (int i=0; i < NUM_PHILOSOPHERS; i++) {
 * for (int j=i+1; j < NUM_PHILOSOPHERS; j++) {
 * if(owners[i] == owners[j])
 * possible_deadlock = false;
 * }
 * }
 * if(possible_deadlock) {
 * System.err.println("DEADLOCK DETECTED");
 * for (int i=0; i< NUM_PHILOSOPHERS; i++) {
 * System.err.println("chopstick"+i+" is held by philosopher"+chopsticks[i].
 * Lockedby());
 * }
 * System.exit(1);
 * }
 * }
 * }
 * }
 * }
 */
