package community.coins.plugin.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Eli
 * @since April 19, 2026
 */
public final class BlockCache {
    private final int durationMillis;
    public BlockCache(int durationMillis) {
        this.durationMillis = durationMillis;
    }

    // both 0 because nothing has happened yet when the object is created
    private final AtomicInteger amount = new AtomicInteger(0);
    private final AtomicLong lastTime = new AtomicLong(0);

    public int getAndIncrement() {
        lastTime.set(System.currentTimeMillis());
        return amount.getAndIncrement();
    }

    public boolean isWithinConfiguredTime() {
        return lastTime.get() > System.currentTimeMillis() - durationMillis;
    }
}
