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
public class BitmapRunCount {
    @Param({ "1024" })
    static int N;

    @Param({ "5" })
    static int howmanyarrays;

    @Param({ "false" })
    static boolean onerun;

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        short[][] array;
        long[][] bitmap;

        Blackhole bh = new Blackhole();
        Random rand = new Random();

        public short nextQuery() {
            return (short) rand.nextInt();
        }

        public BenchmarkState() {
            array = new short[howmanyarrays][N];
            bitmap = new long[howmanyarrays][(1 << 16) / 64];
            System.out.println("one run mode  :" + onerun);
            System.out.println("*** Array memory usage is at least "
                    + ((N * howmanyarrays * 2) / 1024) + "Kb");

            for (int z = 0; z < howmanyarrays; ++z) {
                long[] b = bitmap[z];
                for (int k = 0; k < b.length; ++k)
                    b[k] = rand.nextLong();
                final short[] arr = array[z];
                for (int k = 0; k < N; ++k)
                    arr[k] = (short) rand.nextInt();
                List<Short> wrapper = new AbstractList<Short>() {

                    @Override
                    public Short get(int index) {
                        return arr[index];
                    }

                    @Override
                    public Short set(int index, Short element) {
                        short v = arr[index];
                        arr[index] = element;
                        return v;
                    }

                    @Override
                    public int size() {
                        return arr.length;
                    }

                };
                Collections.sort(wrapper, new Comparator<Short>() {

                    @Override
                    public int compare(Short o1, Short o2) {
                        return (o1.shortValue() & 0xFFFF)
                                - (o2.shortValue() & 0xFFFF);
                    }
                });
                // check that it is actually sorted!
                for (int k = 1; k < arr.length; ++k) {
                    if ((arr[k] & 0xFFFF) < (arr[k - 1] & 0xFFFF))
                        throw new RuntimeException("bug");
                }
            }

        }
    }

    public static int numberOfRuns(final long[] bitmap) {
        int numRuns = 0;
        long nextWord = bitmap[0];

        for (int i = 0; i < bitmap.length - 1; i++) {
            long word = nextWord;
            nextWord = bitmap[i + 1];
            numRuns += Long.bitCount((~word) & (word << 1))
                    + ((word >>> 63) & ~nextWord);
        }

        long word = nextWord;
        numRuns += Long.bitCount((~word) & (word << 1));
        if ((word & 0x8000000000000000L) != 0)
            numRuns++;

        return numRuns;
    }
    public static int approxNumberOfRuns(final long[] bitmap) {
        int numRuns = 0;
        long nextWord = bitmap[0];

        for (int i = 0; i < bitmap.length - 1; i++) {
            long word = nextWord;
            nextWord = bitmap[i + 1];
            numRuns += Long.bitCount((~word) & (word << 1));
        }

        long word = nextWord;
        numRuns += Long.bitCount((~word) & (word << 1));
        if ((word & 0x8000000000000000L) != 0)
            numRuns++;

        return numRuns;
    }
    
    @Benchmark
    public void approxNumberOfRuns(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += approxNumberOfRuns(s.bitmap[z]);
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void numberOfRuns(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += numberOfRuns(s.bitmap[z]);
        }
        s.bh.consume(bogus);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BitmapRunCount.class.getSimpleName())
                .warmupIterations(3).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
