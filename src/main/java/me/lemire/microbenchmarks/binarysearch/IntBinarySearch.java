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
public class IntBinarySearch {
    
    @State(Scope.Benchmark)
    public static class BenchmarkState {

        int N =128;
        int[] array;
        int[] queries;
        Blackhole bh = new Blackhole(); 
        Random rand = new Random();
        public int nextQuery()  {
            return  rand.nextInt();
        }
        
        public BenchmarkState() {
            array = new int[N];
            queries = new int[1024];

            
            for(int k = 0; k < N ; ++k )
                array[k] =  rand.nextInt();
            List<Integer> wrapper = new AbstractList<Integer>() {

                @Override
                public Integer get(int index) {
                    return array[index];
                }

                @Override
                public Integer set(int index, Integer element) {
                    int v = array[index];
                    array[index] = element;
                    return v;
                }

                @Override
                public int size() {
                    return array.length;
                }

            };
            Collections.sort(wrapper, new Comparator<Integer>(){

                @Override
                public int compare(Integer o1, Integer o2) {
                    return (o1.intValue() & 0xFFFF) - (o2.intValue() & 0xFFFF);
                }});
            for(int k = 0; k < queries.length ; ++k )
                queries[k] =  rand.nextInt();
            
        }

    }
    public static int branchlessUnsignedBinarySearch2(final int[] array, final int ikey) {
        int length = array.length;
        if(length == 0) return 0;
        int pos = 0;
        for (int half = length/2; half != 0; length -= half, half = length/2) {
            final int test = pos + half;   // test index will be halfway point
            if (ikey < array[test]) {pos = test; // update index with CMOV 
            }
        }
        if(array[pos] < ikey) pos = pos + 1;
        return pos;
    }
    
    
    
    public static int branchlessUnsignedBinarySearch(final int[] array, final int ikey) {
        int n = array.length;
        if (n == 0) return 0;
        int pos = 0;
        while(n>1) {
            final int half = n >>> 1;
            n -= half;
            final int index = pos + half;
            if(array[index] < ikey) 
                pos = index;
        }
        return pos + ((array[pos] < ikey)?1:0);
    }
   
   
    
    
    public static int unsignedBinarySearch(final int[] array, final int ikey) {
        int low = 0;
        int high = array.length - 1;
        while (low <= high) {
            final int middleIndex = (low + high) >>> 1;
            final int middleValue = array[middleIndex];

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
            bogus += branchlessUnsignedBinarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }

    @Benchmark
    public void branchlessBinarySearch2(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus += branchlessUnsignedBinarySearch2(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    
    @Benchmark
    public void branchyBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus +=  unsignedBinarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);

    }
    
    
    public static void main(String[] args) throws RunnerException {
       Options opt = new OptionsBuilder()
                .include(IntBinarySearch.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
