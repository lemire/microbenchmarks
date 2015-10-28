package me.lemire.microbenchmarks.countruns;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BitmapOR {
    @Param({ "1024" })
    static int N;

    @Param({ "5" })
    static int howmanyarrays;

    @Param({ "false" })
    static boolean onerun;

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        long[][] bitmap;
        long[][] bitmap2;
        long[] buffer = new long[(1 << 16) / 64];

        Blackhole bh = new Blackhole();
        Random rand = new Random();

        public short nextQuery() {
            return (short) rand.nextInt();
        }

        public BenchmarkState() {
            bitmap2 = new long[howmanyarrays][(1 << 16) / 64];
            bitmap = new long[howmanyarrays][(1 << 16) / 64];
            System.out.println("one run mode  :" + onerun);
            System.out.println("*** Array memory usage is at least "
                    + ((N * howmanyarrays * 2) / 1024) + "Kb");

            for (int z = 0; z < howmanyarrays; ++z) {
                long[] b = bitmap[z];
                for (int k = 0; k < b.length; ++k)
                    b[k] = rand.nextLong();
                b = bitmap2[z];
                for (int k = 0; k < b.length; ++k)
                    b[k] = rand.nextLong();
            }

        }
    }

    public static void justOr(final long[] bitmap1, final long[] bitmap2, final long[] out) {
        for (int i = 0; i < bitmap1.length; i++) {
            out[i] = bitmap1[i] | bitmap2[i];
        }
    }
    public static int orCard(final long[] bitmap1, final long[] bitmap2, final long[] out) {
        int card = 0;
        for (int i = 0; i < bitmap1.length; i++) {
            long w1 = bitmap1[i];
            long w2 = bitmap2[i];
            long a =  w1 | w2;
            out[i] = a;
            card += Long.bitCount(a);
        }
        return card;
    }
        
    @Benchmark
    public void justOr(BenchmarkState s) {
        for (int z = 0; z < howmanyarrays; ++z) {
            justOr(s.bitmap[z],s.bitmap2[z],s.buffer);
        }
    }
    
    @Benchmark
    public int orCard(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {
            bogus += orCard(s.bitmap[z],s.bitmap2[z],s.buffer);
        }
        return bogus;
    }



    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BitmapOR.class.getSimpleName())
                .warmupIterations(3).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
