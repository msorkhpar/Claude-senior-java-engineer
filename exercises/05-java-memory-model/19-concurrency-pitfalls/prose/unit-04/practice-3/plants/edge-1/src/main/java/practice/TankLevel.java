package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

/**
 * The level of one storage tank, as its sensor last reported it. Subscribers (the control panel,
 * the overflow alarm, pump controllers) hear every new reading. Any thread may call any method.
 */
public class TankLevel {

    private final Object lock = new Object();
    private long litres;                                       // guarded by lock
    private final List<LongConsumer> subscribers = new ArrayList<>();   // guarded by lock

    public void subscribe(LongConsumer subscriber) {
        synchronized (lock) {
            subscribers.add(subscriber);
        }
    }

    public long litres() {
        synchronized (lock) {
            return litres;
        }
    }

    /** Records a new reading and tells every subscriber about it. */
    public void update(long newLitres) {
        synchronized (lock) {
            litres = newLitres;
            for (LongConsumer s : subscribers) s.accept(newLitres);   // one consistent delivery
        }
    }
}
