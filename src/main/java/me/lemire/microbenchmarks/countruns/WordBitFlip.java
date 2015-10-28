package me.lemire.microbenchmarks.countruns;

import java.util.Random;
import java.util.concurrent.TimeUnit;

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


@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class WordBitFlip {
    @Param({ "1024" })
    static int N;

    @Param({ "5" })
    static int howmanyarrays;


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
    

    @Benchmark
    public int simpleFlip(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {
            long[] b = s.bitmap[z];
            for(int i = 0; i < N; ++i) {
                long curWord = b[i];
                int localRunStart = Long.numberOfTrailingZeros(curWord);
                s.buffer[i] =  curWord | ((1L << localRunStart) - 1);
                bogus += localRunStart;
            }
        }
        return bogus;
    }
    
    @Benchmark
    public int simpleFlip2(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {
            long[] b = s.bitmap[z];
            for(int i = 0; i < N; ++i) {
                long curWord = b[i];
                int localRunStart = Long.numberOfTrailingZeros(curWord);
                s.buffer[i] = curWord | (curWord-1);
                bogus += localRunStart;
            }
        }
        return bogus;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WordBitFlip.class.getSimpleName())
                .warmupIterations(3).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
