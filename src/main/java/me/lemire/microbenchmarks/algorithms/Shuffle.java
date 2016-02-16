package me.lemire.microbenchmarks.algorithms;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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
    
    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void shuffle(int arr[], ThreadLocalRandom rnd) {
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
    
    static int fancy_ranged_random_mult_lazy(int  range,  MersenneTwisterFast rnd) {
        long random32bit, multiresult;
        long leftover;
        if((range & (range - 1)) == 0)
            return rnd.nextInt() & (range - 1);
        final long mask = 0xFFFFFFFFL;
        random32bit = rnd.nextInt()  & mask;
        multiresult = random32bit * range;
        leftover = multiresult & mask;
        if(leftover < range) {
          final long threshold = 0xFFFFFFFF % range;
          while (leftover <= threshold) {
              random32bit = rnd.nextInt() & mask;
              multiresult = random32bit * range;
              leftover =  multiresult & mask;
          } 
        }
        return (int) (multiresult >>> 32); // [0, range)
    }
    
    static int ranged_random_mult_lazy(int  range,  MersenneTwisterFast rnd) {
        long random32bit, multiresult;
        long leftover;
        final long mask = 0xFFFFFFFFL;
        random32bit = rnd.nextInt()  & mask;
        multiresult = random32bit * range;
        leftover = multiresult & mask;
        if(leftover < range) {
          final long threshold = (-range) % range;
          while (leftover <= threshold) {
              random32bit = rnd.nextInt() & mask;
              multiresult = random32bit * range;
              leftover =  multiresult & mask;
          } 
        }
        return (int) (multiresult >>> 32); // [0, range)
    }
    
    public static void shuffle_fastF(int arr[], MersenneTwisterFast rnd) {
        int size = arr.length;

        // Shuffle array
        for (int i = size; i > 1; i--)
            swap(arr, i - 1, ranged_random_mult_lazy(i,rnd));
    }
    static int fancy_ranged_random_mult_lazy(int  range,  ThreadLocalRandom rnd) {
        long random32bit, multiresult;
        long leftover;
        if((range & (range - 1)) == 0)
            return rnd.nextInt() & (range - 1);
        final long mask = 0xFFFFFFFFL;
        random32bit = rnd.nextInt() & mask;
        multiresult = random32bit * range;
        leftover = multiresult & mask;
        if(leftover < range) {
          final long threshold = 0xFFFFFFFF % range;
          while (leftover <= threshold) {
              random32bit = rnd.nextInt()  & mask;
              multiresult = random32bit * range;
              leftover =  multiresult & mask;
          } 
        }
        return (int) (multiresult >>> 32); // [0, range)
    }
    
    static int ranged_random_mult_lazy(int  range,  ThreadLocalRandom rnd) {
        long random32bit, multiresult;
        long leftover;
        final long mask = 0xFFFFFFFFL;
        random32bit = rnd.nextInt() & mask;
        multiresult = random32bit * range;
        leftover = multiresult & mask;
        if(leftover < range) {
          final long threshold = (-range) % range;
          while (leftover <= threshold) {
              random32bit = rnd.nextInt()  & mask;
              multiresult = random32bit * range;
              leftover =  multiresult & mask;
          } 
        }
        return (int) (multiresult >>> 32); // [0, range)
    }
    
    public static void shuffle_fastF(int arr[], ThreadLocalRandom rnd) {
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

            for (int k = 0; k < N; ++k)
                array[k] = k;
        }

    }
    static MersenneTwisterFast rr = new MersenneTwisterFast();
    static ThreadLocalRandom r = ThreadLocalRandom.current();
    

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

    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(Shuffle.class.getSimpleName()).warmupIterations(2)
        .measurementIterations(3).forks(1).build();

        new Runner(opt).run();
    }

}

