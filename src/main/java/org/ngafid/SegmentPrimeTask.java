package org.ngafid;

import java.util.concurrent.Callable;

/**
    * A task that counts the number of prime numbers in a given segment [low, high].
    * This class implements Callable<Integer> so it can be executed by an ExecutorService.
 */
public class SegmentPrimeTask implements Callable<Integer> {
    private final long low;
    private final long high;

    /**
     * Constructs a SegmentPrimeTask for the range [low, high].
     *
     * @param low  the lower bound of the segment (inclusive)
     * @param high the upper bound of the segment (inclusive)
     */
    public SegmentPrimeTask(long low, long high) {
        this.low = low;
        this.high = high;
    }

    /**
     * Counts the number of prime numbers in the segment [low, high].
     * Reports progress at 10% intervals.
     *
     * @return the count of prime numbers found in the segment
     */
    @Override
    public Integer call() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + ": starting segment [" + low + ", " + high + "]");

        long startNs = System.nanoTime();
        int count = 0;
        long total = high - low + 1;
        long progressInterval = Math.max(1L, total / 10L); // report at 0% steps
        long processed = 0;

        for (long n = low; n <= high; n++) {
            if (isPrime(n)) count++;
            processed++;
            if (processed % progressInterval == 0) {
                int percent = (int) ((processed * 100) / total);
                System.out.println(threadName + ": processed " + processed + "/" + total + " (~" + percent + "%) in [" + low + ", " + high + "]");
            }
        }

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        System.out.println(threadName + ": finished segment [" + low + ", " + high + "], found " + count + " primes in " + elapsedMs + " ms");
        return count;
    }

    /**
     * Checks if a number is prime.
     *
     * @param n the number to check
     * @return true if n is prime, false otherwise
     */
    private boolean isPrime(long n) {
        if (n < 2) return false;
        if (n % 2 == 0) return n == 2;
        long r = (long) Math.sqrt(n);
        for (long i = 3; i <= r; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}