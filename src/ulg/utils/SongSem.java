package ulg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Created by: Fabrizio Fubelli
 * Date: 15/01/2017.
 */

/**
 * A strong semaphore
 */
public class SongSem implements SongSemaphore {

    private final List<Integer> requestesQueue = Collections.synchronizedList(new ArrayList<>());

    @Override
    public synchronized void newRequest(final Integer requestTicket) {
        requestesQueue.add(requestTicket);
    }

    @Override
    public void semWait(final Integer requestNumber) {
        while (!requestNumber.equals(requestesQueue.get(0))) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void semSignal() {
        requestesQueue.remove(0);
        synchronized (this) {
            this.notifyAll();
        }
    }

}
