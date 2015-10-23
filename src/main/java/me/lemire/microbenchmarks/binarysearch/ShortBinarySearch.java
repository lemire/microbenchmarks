package me.lemire.microbenchmarks.binarysearch;

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
public class ShortBinarySearch {
    @Param({ "1024" })
    static int N;
    
    @Param({"5", "1000" })
    static int howmanyarrays;
    

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        short[][] array;
        short[] queries;
        Blackhole bh = new Blackhole();
        Random rand = new Random();

        public short nextQuery() {
            return (short) rand.nextInt();
        }

        public BenchmarkState() {
            array = new short[howmanyarrays][N];

            queries = new short[1024];
            for (int z = 0; z < howmanyarrays; ++z) {
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
            for (int k = 0; k < queries.length; ++k) {
                queries[k] = (short) rand.nextInt();
            }

        }

    }

    public static int sequentialUnsignedSearch(final short[] array,
            final short ikey) {
        int c = array.length;
        int k = 0;
        int key = ikey & 0xFFFF;
        for (; k < c; ++k) {
            if ((array[k] & 0xFFFF) >= key)
                return k;
        }
        return k;
    }

    public static int branchlessUnsignedBinarySearch(final short[] array,
            final short k) {
        int ikey = toIntUnsigned(k);
        int n = array.length;
        int pos = 0;
        while (n > 1) {
            final int half = n >>> 1;
            n -= half;
            final int index = pos + half;            
            final int val = array[index] & 0xFFFF;
            final int diff = val - ikey;
            final int mask = diff >> 31;
            final int addition = half & mask;
            pos += addition;
        }
        // next line is upper bound
        if (toIntUnsigned(array[pos]) < ikey)
            pos = pos + 1;
        if ((pos < array.length) && (toIntUnsigned(array[pos]) == ikey))
            return pos;
        return -(pos + 1);
    }

    public static int unrolledUnsignedBinarySearch(final short[] array,
            final short k) {
        int ikey = toIntUnsigned(k);
        int n = array.length;
        int pos = 0;
        while(n>=16) {
            final int half = n >>> 1;
            final int index = pos + half;
            n-= half;
            final int half2 = n>>>1; 
            n -= half2;
            final int val = array[index] & 0xFFFF;
            final int index2 = pos + half + half2;
            final int val2 = array[index2] & 0xFFFF;
            final int index1 = pos +half2;
            final int val1 = array[index1] & 0xFFFF;
            if(ikey < val) {
                if(ikey < val1) {
                    // no change
                } else {
                    pos = index1;
                }
            } else {
                if(ikey < val2)
                    pos = index;
                else 
                    pos = index2;
            }

        }
        int x = 0;
        for(; x < n; ++x) {
            final int val = toIntUnsigned(array[pos + x]);
            if(val >= ikey) {
                if(val == ikey) return pos;
                break;
            }
        }
        return -(pos + x + 1);
    }

    
    public static int toIntUnsigned(short x) {
        return x & 0xFFFF;
    }

    public static int unsignedBinarySearch(final short[] array, final short k) {
        int ikey = toIntUnsigned(k);
        int low = 0;
        int high = array.length - 1;
        while (low <= high) {
            final int middleIndex = (low + high) >>> 1;
            final int middleValue = toIntUnsigned(array[middleIndex]);

            if (middleValue < ikey)
                low = middleIndex + 1;
            else if (middleValue > ikey)
                high = middleIndex - 1;
            else
                return middleIndex;
        }
        return -(low + 1);
    }


    @Benchmark
    public void aaa_unrolledUnsignedBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for (int k = 0; k < l; ++k) {
            for (int z = 0; z < howmanyarrays; ++z) {

                bogus += unrolledUnsignedBinarySearch(s.array[z],
                        s.queries[k]);
            }
        }
        s.bh.consume(bogus);
    }
    @Benchmark
    public void branchlessBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for (int k = 0; k < l; ++k) {
            for (int z = 0; z < howmanyarrays; ++z) {

                bogus += branchlessUnsignedBinarySearch(s.array[z],
                        s.queries[k]);
            }
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void branchyBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for (int k = 0; k < l; ++k) {
            for (int z = 0; z < howmanyarrays; ++z) {
                bogus += unsignedBinarySearch(s.array[z], s.queries[k]);
            }
        }
        s.bh.consume(bogus);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ShortBinarySearch.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
