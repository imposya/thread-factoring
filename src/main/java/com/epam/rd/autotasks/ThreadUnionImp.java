package com.epam.rd.autotasks;

import java.util.ArrayList;
import java.util.List;

public class ThreadUnionImp implements ThreadUnion {
    public int size;
    public String name;
    public ArrayList<Thread> threads = new ArrayList<>();
    public ArrayList<FinishedThreadResult> result = new ArrayList<>();
    public boolean isShutdown;

    public ThreadUnionImp(final String name) {
        this.name = name;
    }
    public class ThreadTest extends Thread {
        public ThreadTest(final Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            super.run();
            synchronized (result) {
                if (!isInResult()) {
                    result.add(new FinishedThreadResult(this.getName()));
                }
            }
        }

        public boolean isInResult() {
            for (FinishedThreadResult ftr : result) {
                if (ftr.getThreadName().equals(this.getName())) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public int totalSize() {
        return size;
    }

    @Override
    public int activeSize() {
        int size = 0;
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                size++;
            }
        }
        return size;
    }

    @Override
    public void shutdown() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        isShutdown = true;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public void awaitTermination() {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                thread.interrupt();
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isFinished() {
        return activeSize() == 0 && isShutdown;
    }

    @Override
    public List<FinishedThreadResult> results() {
        return result;
    }

    @Override
    public synchronized Thread newThread(final Runnable r) {
        if (isShutdown) {
            throw new IllegalStateException();
        }
        Thread thread = new ThreadTest(r);
        thread.setName(String.format("%s-worker-%d", name, size++));
        thread.setUncaughtExceptionHandler((thisThread, exception) -> result.add(new FinishedThreadResult(thisThread.getName(), exception)));
        threads.add(thread);
        return thread;
    }
}