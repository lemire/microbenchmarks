package me.lemire.microbenchmarks.algorithms;

import java.util.Random;
import java.util.concurrent.TimeUnit;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Shuffle {
    
    
    private static void testfairness(int maxsize, MersenneTwisterFast r) {
        for(int size = 2; size <maxsize; ++size) {
            int i;
            int m2 = 1 << (32- Integer.numberOfLeadingZeros(size-1));
            double ratio = (double) size / m2;
            int mask = m2 -1;
            int count = 20000;
            double predicted = (1-ratio) * count;
            int missed = 0;
            for(i = 0 ; i < count; ++i ) {
                if((r.nextInt() & mask) >= size) ++missed;
            }
            if((double)missed > 1.2 * predicted + 20) {
                throw new RuntimeException("Bad RNG.");
            }
        }
    }
    
    private static void testfairness(int maxsize, Random r) {
        for(int size = 2; size <maxsize; ++size) {
            int i;
            int m2 = 1 << (32- Integer.numberOfLeadingZeros(size-1));
            double ratio = (double) size / m2;
            int mask = m2 -1;
            int count = 20000;
            double predicted = (1-ratio) * count;
            int missed = 0;
            for(i = 0 ; i < count; ++i ) {
                if((r.nextInt() & mask) >= size) ++missed;
            }
            if((double)missed > 1.2 * predicted + 20) {
                throw new RuntimeException("Bad RNG.");
            }
        }
    }

    private static int fastFairRandomInt(int size, int mask, int bused, MersenneTwisterFast r) {
        int candidate, rkey, budget;
        // such a loop is necessary for the result to be fair
        budget = 31;// assume that this is what we have
        rkey = r.nextInt();
        candidate = rkey & mask;
        while (candidate >= size) {
            budget -= bused;// we wasted bused bits
            if(budget >=  bused)  {
                rkey >>>= bused;
            } else {
                rkey = r.nextInt();
                budget = 31;
            }
            candidate = rkey & mask;
        }
        return candidate;
    }

    private static int fastFairRandomInt(RandomBuffer rb, int size, int mask, int bused, MersenneTwisterFast r) {
        int candidate = rb.grabBits(mask, bused);
        while (candidate >= size) {
            candidate = rb.grabBits(mask, bused);
        }
        return candidate;
    }

    
    static int fastFairRandomInt2(int size, int mask, int bused, MersenneTwisterFast r) {
        int candidate, rkey, budget;
// such a loop is necessary for the result to be fair
        rkey = r.nextInt();
        candidate = rkey & mask;
        if(bused <= 31/2) {
            rkey >>>= bused;
            int maski = (size-candidate-1)>>31;
            int candidate2 = (maski & (rkey & mask))|(~maski & candidate);
            if(candidate2 < size) return candidate2;
            budget = 31-bused;
        } else
            budget = 31;// assume that this is what we have
        while (candidate >= size) {
            budget -= bused;// we wasted bused bits
            if(budget >=  bused)  {
                rkey >>>= bused;
            } else {
                rkey = r.nextInt();
                budget = 31;
            }
            candidate = rkey & mask;
        }
        return candidate;
    }

    public static void fast_shuffle(int arr[], MersenneTwisterFast rnd) {
        final int size = arr.length;
        int bused = 32 - Integer.numberOfLeadingZeros(size);
        int m2 = 1 << (32 - Integer.numberOfLeadingZeros(size-1));
        int i = size;

        while (i > 1) {
            for (; 2 * i > m2; i--) {
                final int nextpos = fastFairRandomInt(i, m2 - 1, bused, rnd);
                swap(arr, i - 1, nextpos);
            }
            m2 = m2 >>> 1;
            bused--;
        }
    }
    public static void fast_shuffle_buffer(int arr[], MersenneTwisterFast rnd) {
        final int size = arr.length;
        int bused = 32 - Integer.numberOfLeadingZeros(size);
        int m2 = 1 << (32 - Integer.numberOfLeadingZeros(size-1));
        int i = size;
        RandomBuffer rb = new RandomBuffer();

        while (i > 1) {
            for (; 2 * i > m2; i--) {
                final int nextpos = fastFairRandomInt(rb, i, m2 - 1, bused, rnd);
                swap(arr, i - 1, nextpos);
            }
            m2 = m2 >>> 1;
            bused--;
        }
    }
    
    public static void fast_shuffle2(int arr[], MersenneTwisterFast rnd) {
        final int size = arr.length;
        int bused = 32 - Integer.numberOfLeadingZeros(size);
        int m2 = 1 << (32 - Integer.numberOfLeadingZeros(size-1));
        int i = size;
        while (i > 1) {
            for (; 2 * i > m2; i--) {
                final int nextpos = fastFairRandomInt2(i, m2 - 1, bused, rnd);
                swap(arr, i - 1, nextpos);
            }
            m2 = m2 >>> 1;
            bused--;
        }
    }
    
    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void shuffle(int arr[], Random rnd) {
        int size = arr.length;
        // Shuffle array
        for (int i = size; i > 1; i--)
            swap(arr, i - 1, rnd.nextInt(i));
    }

    public static void shuffle(int arr[], MersenneTwisterFast rnd) {
        int size = arr.length;

        // Shuffle array
        for (int i = size; i > 1; i--)
            swap(arr, i - 1, rnd.nextInt(i));
    }
    
    static int ranged_random_mult_lazy(int  range,  MersenneTwisterFast rnd) {
        long random32bit, candidate, multiresult;
        long leftover;
        final long mask = 0xFFFFFFFFL;
        random32bit = rnd.nextInt()  & mask;
        multiresult = random32bit * range;
        candidate =  multiresult >>> 32;
        leftover = multiresult & mask;

        if(leftover > ((1L<<32) - range) ) {
          final long threshold = ((1L<<32)/range * range  - 1);
          do {
              random32bit = rnd.nextInt() & mask;
              multiresult = random32bit * range;
              candidate =  multiresult >>> 32;
              leftover =  multiresult & mask;
          } while (leftover > threshold);
        }
        return (int) candidate; // [0, range)
    }
    
    public static void shuffle_fastF(int arr[], MersenneTwisterFast rnd) {
        int size = arr.length;

        // Shuffle array
        for (int i = size; i > 1; i--)
            swap(arr, i - 1, ranged_random_mult_lazy(i,rnd));
    }
    static int ranged_random_mult_lazy(int  range,  Random rnd) {
        long random32bit, candidate, multiresult;
        long leftover;
        final long mask = 0xFFFFFFFFL;
        random32bit = rnd.nextInt() & mask;
        multiresult = random32bit * range;
        candidate =  multiresult >>> 32;
        leftover = multiresult & mask;

        if(leftover > ((1L<<32) - range) ) {
          final long threshold = ((1L<<32)/range * range  - 1);
          do {
              random32bit = rnd.nextInt()  & mask;
              multiresult = random32bit * range;
              candidate =  multiresult >>> 32;
              leftover =  multiresult & mask;
          } while (leftover > threshold);
        }
        return (int) candidate; // [0, range)
    }
    
    public static void shuffle_fastF(int arr[], Random rnd) {
        int size = arr.length;

        // Shuffle array
        for (int i = size; i > 1; i--)
            swap(arr, i - 1, ranged_random_mult_lazy(i,rnd));
    }
    

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        int N = 1777216;
        int[] array = new int[N];

        public BenchmarkState() {
            testfairness(1000, rr);
            testfairness(1000, r);

            for (int k = 0; k < N; ++k)
                array[k] = k;
        }

    }
    static MersenneTwisterFast rr = new MersenneTwisterFast();
    static Random r = new Random();
    

    @Benchmark
    public void basicshuffle(BenchmarkState s) {
        shuffle(s.array, r);
    }
    @Benchmark
    public void basicshuffle_MT(BenchmarkState s) {
        shuffle(s.array, rr);
    }
    

    @Benchmark
    public void fastFshuffle(BenchmarkState s) {
        shuffle_fastF(s.array, r);
    }
    
    @Benchmark
    public void fastFshuffle_MT(BenchmarkState s) {
        shuffle_fastF(s.array, rr);
    }

    @Benchmark
    public void aa__fastshuffle(BenchmarkState s) {
        fast_shuffle(s.array, rr);
    }


    @Benchmark
    public void aa__fastshuffle2(BenchmarkState s) {
        fast_shuffle2(s.array, rr);
    }

    @Benchmark
    public void aa__fastshuffle_buffer(BenchmarkState s) {
        fast_shuffle_buffer(s.array, rr);
    }

    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(Shuffle.class.getSimpleName()).warmupIterations(2)
        .measurementIterations(3).forks(1).build();

        new Runner(opt).run();
    }

}

class RandomBuffer {
    long buffer;
    int available;
    MersenneTwisterFast r = new MersenneTwisterFast();
    
    public RandomBuffer() {
        init();
    }
    
    int grabBits(int mask, int bused ) {
        if(available >= bused) {
          int answer = (int) (buffer) & mask;
          buffer >>= bused;
          available -= bused;
          return answer;
        } else {
          // we use the bits we have
          int answer = (int) buffer;
          int consumed = available;
          init();
          answer |= (buffer << consumed);
          answer &= mask;
          int lastbit = bused - consumed;
          available = 64 - lastbit;
          buffer >>= lastbit;
          return answer;
        }
      }

    
    public void init() {
        available = 64;
        buffer = r.nextLong();
    }
}
