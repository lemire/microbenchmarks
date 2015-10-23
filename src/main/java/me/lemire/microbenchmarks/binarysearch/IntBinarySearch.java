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
public class IntBinarySearch {
    @Param ({
        "10000","10000000" 
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
            queries = new int[64];

            
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
    

    
    public static int branchlessBinarySearch(final int[] array, final int ikey) {
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
    

    public static int unrolledBinarySearch(final int[] array, final int ikey) {
        int n = array.length;
        if (n == 0) return 0;
        int pos = 0;
        while(n>=16) {
            final int half = n >>> 1;
            final int index = pos + half;
            n-= half;
            final int half2 = n>>>1; 
            n -= half2;
            final int val = array[index] ;
            final int index2 = pos + half + half2;
            final int val2 = array[index2];
            final int index1 = pos +half2;
            final int val1 = array[index1];
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
            final int val = array[pos + x];
            if(val >= ikey) {
                if(val == ikey) return pos;
                break;
            }
        }
        return -(pos + x + 1);
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
    

    
    public void SequentialSearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;

        for(int k = 0; k < l; ++k) {
            bogus += sequentialSearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }


    @Benchmark
    public void branchlessBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus += branchlessBinarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    
    public void branchyBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus +=  BinarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);

    }
    
    

    @Benchmark
    public void standardBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus +=  Arrays.binarySearch(s.array, s.queries[k]); 
        }
        s.bh.consume(bogus);
    }
    
    @Benchmark
    public void aaa_unrolledBinarySearch(BenchmarkState s) {
        final int l = s.queries.length;
        int bogus = 0;
        for(int k = 0; k < l; ++k) {
            bogus +=  unrolledBinarySearch(s.array, s.queries[k]); 
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
