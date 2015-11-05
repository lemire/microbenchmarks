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

    static int fastFairRandomInt(int size, int mask, int bused, MersenneTwisterFast r) {
        int candidate, rkey, budget;
        // such a loop is necessary for the result to be fair
        budget = 31;// assume that this is what we have
        rkey = r.nextInt();
        candidate = rkey & mask;
        while (candidate >= size) {
            budget -= bused;// we wasted bused bits
            if(budget >=  bused)  {
              rkey >>= bused;
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
            for (; 2 * i >= m2; i--) {
                final int nextpos = fastFairRandomInt(i, m2 - 1, bused, rnd);
                swap(arr, i - 1, nextpos);
            }
            m2 = m2 / 2;
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

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        int N = 2048;//16777216;
        int[] array = new int[N];

        public BenchmarkState() {
            for (int k = 0; k < N; ++k)
                array[k] = k;
        }

    }
    MersenneTwisterFast rr = new MersenneTwisterFast();
    Random r = new Random();
    
    @Benchmark
    public void basicshuffle(BenchmarkState s) {
        shuffle(s.array, r);
    }
    @Benchmark
    public void basicshuffle_MT(BenchmarkState s) {
        shuffle(s.array, r);
    }

    @Benchmark
    public void fastshuffle(BenchmarkState s) {
        fast_shuffle(s.array, rr);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Shuffle.class.getSimpleName()).warmupIterations(2)
                .measurementIterations(3).forks(1).build();

        new Runner(opt).run();
    }

}
