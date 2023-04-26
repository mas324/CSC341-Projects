import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Test {
    private Lock lock = new ReentrantLock();
    private Condition[] condition = new Condition[5];
}