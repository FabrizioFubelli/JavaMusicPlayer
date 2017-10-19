package ulg.utils;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * Created by: Fabrizio Fubelli
 * Date: 15/01/2017.
 */

public class RequestSem {

    private final AtomicInteger requestes = new AtomicInteger(0);

    public void semWait() {
        requestes.getAndIncrement();
    }

    public void semSignalAll() {
        if (requestes.get() == 0) return;
        requestes.set(0);
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void semSignal() {
        requestes.getAndDecrement();
    }

    public boolean canPass() {
        return requestes.get() == 0;
    }
}
