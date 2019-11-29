package com.epson.epos2_hybridprinter;

public  class ESemaphore
{
    public static final int ESEMAPHORE_NONE = 0;
    public static final int ESEMAPHORE_ONRECEIVE = 1;
    public static final int ESEMAPHORE_STATUSCHANGE = 2;
    public static int mCurrentWait = ESEMAPHORE_NONE;

    protected static Object mESemaphore = new Object();
    protected static boolean mlocked = true;
    protected static Thread mESemaphoreThread = null;

    static private void semaphoreWaitStart(int currentWait){

        synchronized(mESemaphore) {
            try {
                mlocked = true;
                mCurrentWait = currentWait;
                while(mlocked) {
                    mESemaphore.wait();
                }
            } catch (InterruptedException ex) {
                mCurrentWait = ESEMAPHORE_NONE;
                ex.printStackTrace();
            }
        }
    }

    static public void semaphoreStart(int currentWait) {
        mCurrentWait = currentWait;
        mlocked = false;

        mESemaphoreThread = new Thread(new Runnable() {
            @Override
            public void run() {
                semaphoreWaitStart(mCurrentWait);
            }
        });
        mESemaphoreThread.start();
    }

    static public void semaphoreWait(int currentWait) {
        if(mCurrentWait == currentWait) {
            try {
                mESemaphoreThread.join();
            }catch (InterruptedException ex) {
                mCurrentWait = ESEMAPHORE_NONE;
                ex.printStackTrace();
            }
        }
    }

    static public void semaphoreSignal(int currentWait) {
        synchronized(mESemaphore) {
            if(mCurrentWait == currentWait) {
                mESemaphore.notify();
                mlocked = false;
            }
        }
    }
}