package com.scoreloop.client.android.core.demo.labs;

import android.os.Looper;

class AbstractBlockingControllerObserver {

    private final Thread workerThread;
    private volatile boolean receivedResponse = false;
    private volatile Exception exception;

    public AbstractBlockingControllerObserver() {
        // constructor must be called from the worker thread
        workerThread = Thread.currentThread();
        //  enable Handler on this thread
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }

    protected void receivedResponse() {
        receivedResponse = true;
        // interrupt worker thread if worker thread is waiting
        if (workerThread.getState() == Thread.State.WAITING) {
            workerThread.interrupt();
        }
    }

    public void waitForSuccess() throws Exception {
        try {
            // save starting time
            long start = System.currentTimeMillis();
            // wait until response is received (time out 120 seconds)
            while (!receivedResponse && System.currentTimeMillis() - start < 120 * 1000) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            if (!receivedResponse) {
                // throw exception in case of timeout
                throw new IllegalStateException("reached timeout");
            } else if (exception != null) {
                // rethrow cause if request failed
                throw exception;
            }
        } finally {
            // reset state for later reuse
            receivedResponse = false;
        }
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
