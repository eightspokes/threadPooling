# Prime counter 

Counts primes in the range [1, upperLimit] by splitting the range into contiguous segments and processing each segment concurrently.
Uses a fixed-size thread pool so at most poolSize threads run at once; finished threads are reused to pick up queued segments.
—
## Requirements
- JDK 11+ (or compatible)
- Maven

## Build & run
You can run the main class from your IDE of choice.


Or Compile and run with Maven :
```bash
mvn clean compile exec:java -Dexec.mainClass="org.ngafid.Main"mvn package
java -cp target/classes org.ngafid.Main
```

## Adjust number of threads


Open src/main/java/org/ngafid/Main.java and change the poolSize value in main (or the runPrimeCount call).
Example: 
```bash
int poolSize = 10; // set desired number of worker threads
```

## Make the work larger 

```bash
long upperLimit = 200_000_000L;
int segmentSize = 2_000_000; // 100 segments
```



Edit src/main/java/org/ngafid/Main.java and increase upperLimit or decrease segmentSize so the number of segments becomes much greater than the thread pool size. Example:

## How the thread pool is executed

* A fixed-size pool is created with Executors.newFixedThreadPool(poolSize, namedFactory).
* Submitting tasks (executor.submit(new SegmentPrimeTask(...))) places them onto the internal work queue.
* Up to poolSize worker threads run concurrently. When a worker finishes a task it immediately pulls the next queued task and continues (threads are reused).
* Each task returns a Future<Integer>; the main thread calls Future.get() to collect per-segment results (blocking until ready).
* Shutdown is performed with executor.shutdown() followed by awaitTermination(...) and a fallback shutdownNow() if needed.


## Libraries used

java.util.concurrent — ExecutorService, Executors, Future, Callable, ThreadFactory,



## Output

See example execution output in the exampleOutput.txt file in the root directory of the project.

Example:
...

prime-worker-4: processed 2000000/2000000 (~100%) in [198000001, 200000000]
prime-worker-4: finished segment [198000001, 200000000], found 104659 primes in 618 ms
Collected segment 100: 104659 primes
Primes up to 200000000: 11078937
Elapsed time: 5.502 s (threads=10, segments=100)