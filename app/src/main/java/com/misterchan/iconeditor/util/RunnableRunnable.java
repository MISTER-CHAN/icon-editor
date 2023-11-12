package com.misterchan.iconeditor.util;

public interface RunnableRunnable {
    /**
     * @param wait Wait for the lock.
     */
    void runRunnable(final Runnable target, final boolean wait);
}
