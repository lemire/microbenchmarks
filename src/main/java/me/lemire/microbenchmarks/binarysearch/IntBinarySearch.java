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
    @Param ({
        "32", "128", "1024", "2048" 
    })
    static
    int N;
    @State(Scope.Benchmark)
    public static class BenchmarkState {

        int[] array;
        int[] queries;
        private static int MMAX = 10000000;
        Blackhole bh = new Blackhole(); 
        Random rand = new Random();
        public int nextQuery()  {
            return  rand.nextInt();
        }
        
        public BenchmarkState() {
            array = new int[N];
            queries = new int[1024];

            
            for(int k = 0; k < N ; ++k )
                array[k] =  rand.nextInt(MMAX);
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
                    return (o1.intValue() ) - (o2.intValue());
                }});
            // check that it is actually sorted!
            for(int k = 1; k < array.length; ++k){
                if(array[k] < array[k-1]) throw new RuntimeException("bug");
            }
            for(int k = 0; k < queries.length ; ++k )
                queries[k] = rand.nextInt(MMAX);
            
        }

    }
    
    
    
/*    public static int branchlessBinarySearch(final int[] array, final int ikey) {
        int length = array.length;
        if(length == 0) return 0;
        int pos = 0;
        for (int half = length/2; half != 0; length -= half, half = length/2) {
            final int test = pos + half;   
            if ( array[test] < ikey) {
                pos = test; // update index with CMOV 
            }
        }
        if(array[pos] < ikey) pos = pos + 1;
        // that would be the answer if upper bound sought
        //return pos;
        if ((pos < array.length) && (array[pos] == ikey))
            return pos;
        return -(pos + 1);
    }
    
    
    public static int branchlessBinarySearch2(final int[] array, final int ikey) {
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
        if(array[pos] < ikey) pos = pos + 1;
        // that would be the answer if upper bound sought
        //return pos;
        if ((pos < array.length) && (array[pos] == ikey))
            return pos;
        return -(pos + 1);
    }
  */ 
    public static int branchlessBinarySearch3(final int[] array, final int ikey) {
        int n = array.length;
        if (n == 0) return 0;
        int pos = 0;
        while(n>1) {
            final int half = n >>> 1;
            pos += ((array[pos + half] - ikey)>>31) & half;
            n -= half;
        }
        if(array[pos] < ikey) pos = pos + 1;
        // that would be the answer if upper bound sought
        //return pos;
        if ((pos < array.length) && (array[pos] == ikey))
            return pos;
        return -(pos + 1);
    }
    
    public static int branchlessBinarySearch3b(final int[] array, final int ikey) {
        int n = array.length;
        if (n == 0) return 0;
        int pos = 0;
        while(n>1) {
            final int half = n >>> 1;
            n -= half;
            final int index = pos + half;
            final int val = array[index];
            final int diff = val - ikey;
            final int mask = diff >> 31;
            final int addition = half & mask;
            pos += addition;
        }
        if(array[pos] < ikey) pos = pos + 1;
        // that would be the answer if upper bound sought
        //return pos;
        if ((pos < array.length) && (array[pos] == ikey))
            return pos;
        return -(pos + 1);
    }
    

    
    
    
    public static int sequentialSearch(final int[] array, final int ikey) {
        int c = array.length;
        int k = 0;
        for(; k < c; ++k) {
            if(array[k] >= ikey) return k;
        }
        return k;
    }
       

    
    public static int BinarySearch(final int[] array, final int ikey) {
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
    public void SequentialSearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;

        for(int k = 0; k < l; ++k) {
            bogus += sequentialSearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    /*
    @Benchmark
    public void branchlessBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus += branchlessBinarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    

    @Benchmark
    public void branchlessBinarySearch2(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus += branchlessBinarySearch2(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    */

    @Benchmark
    public void branchlessBinarySearch3(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus += branchlessBinarySearch3(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    

    @Benchmark
    public void branchlessBinarySearch3b(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus += branchlessBinarySearch3b(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    
    @Benchmark
    public void branchyBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus +=  BinarySearch(s.array, s.queries[k]); 
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
