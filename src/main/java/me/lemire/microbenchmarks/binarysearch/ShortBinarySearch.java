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
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ShortBinarySearch {
    
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param ({
           "128" 
        })
        int N;
        short[] array;
        short[] queries;
        Blackhole bh = new Blackhole(); 
        Random rand = new Random();
        public short nextQuery()  {
            return (short) rand.nextInt();
        }
        
        public BenchmarkState() {
            array = new short[N];
            
            queries = new short[1024*1024];

            
            for(int k = 0; k < N ; ++k )
                array[k] = (short) rand.nextInt();
            List<Short> wrapper = new AbstractList<Short>() {

                @Override
                public Short get(int index) {
                    return array[index];
                }

                @Override
                public Short set(int index, Short element) {
                    short v = array[index];
                    array[index] = element;
                    return v;
                }

                @Override
                public int size() {
                    return array.length;
                }

            };
            Collections.sort(wrapper, new Comparator<Short>(){

                @Override
                public int compare(Short o1, Short o2) {
                    return (o1.shortValue() & 0xFFFF) - (o2.shortValue() & 0xFFFF);
                }});
            for(int k = 0; k < queries.length ; ++k )
                queries[k] = (short) rand.nextInt();
            
        }

    }
    

    public static int branchlessUnsignedBinarySearch(final short[] array, final short k) {
        int ikey = toIntUnsigned(k);
        int n = array.length;
        int pos = 0;
        while(n>1) {
            final int half = n >>> 1;
            n -= half;
            final int index = pos + half;
            final int val = array[index] & 0xFFFFFFFF;
            if(val < ikey)
                pos = index;
        }
        return pos + ((pos < array.length)&&(toIntUnsigned(array[pos]) < ikey)?1:0);
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
    public void branchlessBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
             bogus+=branchlessUnsignedBinarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void branchyBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus+= unsignedBinarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ShortBinarySearch.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
