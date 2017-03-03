package me.lemire.microbenchmarks.rangequery;

import java.util.Random;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;



@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RangeMinimum {
    final static int N = 1000000;
    final static int t = 100;
    static int[] b = new int[N];
    static int[] m = new int[t];
    static int[] M = new int[t];

    @Setup
    public void populate() {
        System.out.println("populating...");
        Random r = new Random();
        for (int i = 0; i < N; i++) {
            b[i] = r.nextInt();
        }
        for (int i = 0; i < t; i++) {
            m[i] = r.nextInt(N - 1);
            M[i] = r.nextInt(N - m[i] - 1) + m[i] + 1;
        }
        System.out.println("populating... ok");

    }


    @Benchmark
    public int embeddedmin() {
        int sum = 0;
        for (int i = 0; i < t; i++) {
            final int minrange = m[i];
            final int maxrange = M[i];
            int thismin = b[minrange];
            for(int j = minrange + 1; j < maxrange; j ++) {
                if(thismin < b[j]) thismin = b[j];
            }
            sum += thismin;
        }
        return sum;
    }

    @Benchmark
    public int fncmin() {
        int sum = 0;
        for (int i = 0; i < t; i++) {
            sum += RangeMinimum.rangemin(b,m[i], M[i]);
        }
        return sum;
    }

    static int rangemin(int[] b, int minrange, int maxrange) {
        int thismin = b[minrange];
        for(int j = minrange + 1; j < maxrange; j ++) {
            if(thismin < b[j]) thismin = b[j];
        }
        return thismin;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(RangeMinimum.class.getSimpleName())
        .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }

}
