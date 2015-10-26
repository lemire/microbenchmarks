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
public class BitSetFun {
    @Param({ "1024" })
    static int N;

    @Param({ "5" })
    static int howmanyarrays;

    @Param({ "true", "false" })
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

    public static int branchlessModifyAndCard(final short[] array,
            final long[] bitmap) {
        int card = array.length;
        long[] bit = Arrays.copyOf(bitmap, bitmap.length);
        int cardinality = 0;
        for (int k = 0; k < card; ++k) {
            final int i = toIntUnsigned(array[k]) >>> 6;
            cardinality += ((~bit[i]) & (1l << array[k])) >>> array[k];
            bit[i] = bit[i] | (1l << array[k]);
        }
        return cardinality;
    }

    

    public static int branchlessModifyAndCard2(final short[] array,
            final long[] bitmap) {
        int card = array.length;
        long[] bit = Arrays.copyOf(bitmap, bitmap.length);
        int cardinality = 0;
        for (int k = 0; k < card; ++k) {
            final int i = toIntUnsigned(array[k]) >>> 6;
            long w = bit[i];
            long aft = w | (1l << array[k]);
            cardinality += (w - aft) >>> 63;
            bit[i] = aft;
        }
        return cardinality;
    }

    

    public static int branchlessModify(final short[] array,
            final long[] bitmap) {
        int card = array.length;
        long[] bit = Arrays.copyOf(bitmap, bitmap.length);
        int cardinality = 0;
        for (int k = 0; k < card; ++k) {
            final int i = toIntUnsigned(array[k]) >>> 6;
            long w = bit[i];
            long aft = w | (1l << array[k]);
            bit[i] = aft;
        }
        return cardinality;
    }

    public static int branchymodifyAndCard(final short[] array,
            final long[] bitmap) {
        int card = array.length;
        long[] bit = Arrays.copyOf(bitmap, bitmap.length);
        int cardinality = 0;
        for (int k = 0; k < card; ++k) {
            final int i = toIntUnsigned(array[k]) >>> 6;
            long w = bit[i];
            bit[i] = bit[i] | (1l << array[k]);
            if (w != bit[i])
                ++cardinality;
        }
        return cardinality;
    }

    public static int toIntUnsigned(short x) {
        return x & 0xFFFF;
    }

    @Benchmark
    public void branchlessModifyAndCard(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += branchlessModifyAndCard(s.array[z], s.bitmap[z]);
        }
        s.bh.consume(bogus);
    }
    @Benchmark
    public void branchlessModifyAndCard2(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += branchlessModifyAndCard2(s.array[z], s.bitmap[z]);
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void branchlessModify(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += branchlessModify(s.array[z], s.bitmap[z]);
        }
        s.bh.consume(bogus);
    }
    @Benchmark
    public void branchymodifyAndCard(BenchmarkState s) {
        int bogus = 0;
        for (int z = 0; z < howmanyarrays; ++z) {

            bogus += branchymodifyAndCard(s.array[z], s.bitmap[z]);
        }
        s.bh.consume(bogus);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BitSetFun.class.getSimpleName()).warmupIterations(3)
                .measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
