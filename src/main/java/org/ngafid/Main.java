package org.ngafid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class to count prime numbers up to a specified limit using multiple threads.
 * Divides the range into segments and processes each segment in parallel.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int poolSize = 10; // Number of threads in the pool
        long upperLimit = 200_000_000L; // Count primes up to this number
        int segmentSize = 2_000_000; // Size of each segment to process

        runPrimeCount(poolSize, upperLimit, segmentSize);
    }

    /**
     * Runs the prime counting process using a thread pool.
     *
     * @param poolSize   the number of threads in the pool
     * @param upperLimit the upper limit to count primes up to (inclusive)
     * @param segmentSize the size of each segment to divide the work into
     * @throws Exception if there was an error during execution
     */
    private static void runPrimeCount(int poolSize, long upperLimit, int segmentSize) throws Exception {
        int segments = (int) ((upperLimit + segmentSize - 1) / segmentSize);

        AtomicInteger threadId = new AtomicInteger(1);
        ThreadFactory namedFactory = runnable -> {
            Thread t = new Thread(runnable);
            t.setName("prime-worker-" + threadId.getAndIncrement());
            return t;
        };

        ExecutorService executor = Executors.newFixedThreadPool(poolSize, namedFactory);
        List<Future<Integer>> futures = new ArrayList<>(segments);

        long start = System.nanoTime();
        System.out.println("\nStarting segmented prime count up to " + upperLimit +
                " using " + poolSize + " threads and " + segments + " segments.\n");

        try {
            for (int i = 0; i < segments; i++) {
                long low = (long) i * segmentSize + 1;
                long high = Math.min(upperLimit, low + segmentSize - 1);
                System.out.println("Submitting segment " + (i + 1) + ": [" + low + ", " + high + "]");
                futures.add(executor.submit(new SegmentPrimeTask(low, high)));
            }

            long totalPrimes = 0;
            for (int i = 0; i < futures.size(); i++) {
                Future<Integer> f = futures.get(i);
                try {
                    int segmentCount = f.get(); // blocks until that segment is done
                    totalPrimes += segmentCount;
                    System.out.println("Collected segment " + (i + 1) + ": " + segmentCount + " primes");
                } catch (ExecutionException e) {
                    System.err.println("Segment " + (i + 1) + " failed: " + e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }

            long elapsedNs = System.nanoTime() - start;
            System.out.println("Primes up to " + upperLimit + ": " + totalPrimes);
            System.out.printf("Elapsed time: %.3f s (threads=%d, segments=%d)%n",
                    elapsedNs / 1_000_000_000.0, poolSize, segments);
        } finally {
            executor.shutdown(); // stop accepting new tasks
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.err.println("Timed out waiting for tasks to finish, forcing shutdown...");
                executor.shutdownNow();
            }
        }
    }
}