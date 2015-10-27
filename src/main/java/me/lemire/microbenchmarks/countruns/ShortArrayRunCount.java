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
public class ShortArrayRunCount {
    @Param({ "1024" })
    static int N;

    @Param({ "5" })
    static int howmanyarrays;
    
    @Param({ "true", "false" })
    static boolean onerun;

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        short[][] array;
        Blackhole bh = new Blackhole();
        Random rand = new Random();

        public short nextQuery() {
            return (short) rand.nextInt();
        }

        public BenchmarkState() {
            array = new short[howmanyarrays][N];
            System.out.println("one run mode  :"+onerun);
            System.out.println("*** Array memory usage is at least "
                    + ((N * howmanyarrays * 2) / 1024) + "Kb");

            for (int z = 0; z < howmanyarrays; ++z) {
                final short[] arr = array[z];
                if (onerun) {
                    for (int k = 0; k < z; ++k)
                        arr[k] = (short) k;
                } else {

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

    }

    public static int countRun(final short[] array, int cardinality) {
        int numRuns = 1;
        for (int i = 0; i < cardinality - 1; i++) {
            // this way of doing the computation maximizes superscalar
            // opportunities, can even vectorize!
            if (toIntUnsigned(array[i]) + 1 != toIntUnsigned(array[i + 1]))
                ++numRuns;
        }
        return numRuns;
    }

    public static int unrolledCountRun(final short[] array, int cardinality) {
        int numRuns = 1;
        int i = 0;
        for (; i + 3 < cardinality - 1; i += 4) {
            if (toIntUnsigned(array[i]) + 1 != toIntUnsigned(array[i + 1]))
                ++numRuns;
            if (toIntUnsigned(array[i + 1]) + 1 != toIntUnsigned(array[i + 2]))
                ++numRuns;
            if (toIntUnsigned(array[i + 2]) + 1 != toIntUnsigned(array[i + 3]))
                ++numRuns;
            if (toIntUnsigned(array[i + 3]) + 1 != toIntUnsigned(array[i + 4]))
                ++numRuns;
        }
        for (; i < cardinality - 1; i++) {
            // this way of doing the computation maximizes superscalar
            // opportunities, can even vectorize!
            if (toIntUnsigned(array[i]) + 1 != toIntUnsigned(array[i + 1]))
                ++numRuns;
        }
        return numRuns;
    }

    public static int simplerCountRun(final short[] array, int cardinality) {
        int numRuns = 1;
        for (int i = 0; i < cardinality - 1; i++) {
            // this way of doing the computation maximizes superscalar
            // opportunities, can even vectorize!
            if (array[i] + 1 != array[i + 1])
                ++numRuns;
        }
        return numRuns;
    }

    public static int branchlessCountRun(final short[] array, int cardinality) {
        int numRuns = 1;
        for (int i = 0; i < cardinality - 1; i++) {
            numRuns += (toIntUnsigned(array[i]) + 1 - toIntUnsigned(array[i + 1])) >>> 31;
        }
        return numRuns;
    }

    public static int simplerBranchlessCountRun(final short[] array, int cardinality) {
        int numRuns = 1;
        for (int i = 0; i < cardinality - 1; i++) {
            numRuns += (array[i] + 1 - array[i + 1]) >>> 31;
        }
        return numRuns;
    }
    public static int simplerBranchlessCountRun2(final short[] array, int cardinality) {
        int numRuns = 1;
        short oldv = array[0];
        for (int i = 0; i < cardinality - 1; i++) {
            short newv = array[i + 1];
            numRuns += (oldv + 1 - newv) >>> 31;
            oldv = newv;
        }
        return numRuns;
    }

    public static int toIntUnsigned(short x) {
        return x & 0xFFFF;
    }

    @Benchmark
    public void simplerCountRun(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += simplerCountRun(s.array[z], s.array[z].length);
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void unrolledCountRun(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += unrolledCountRun(s.array[z], s.array[z].length);
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void countRun(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += countRun(s.array[z], s.array[z].length);
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void branchlessCountRun(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += branchlessCountRun(s.array[z], s.array[z].length);
        }
        s.bh.consume(bogus);
    }
    @Benchmark
    public void simplerBranchlessCountRun(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += simplerBranchlessCountRun(s.array[z], s.array[z].length);
        }
        s.bh.consume(bogus);
    }
    @Benchmark
    public void simplerBranchlessCountRun2(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += simplerBranchlessCountRun2(s.array[z], s.array[z].length);
        }
        s.bh.consume(bogus);
    }
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ShortArrayRunCount.class.getSimpleName())
                .warmupIterations(3).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
