package me.lemire.microbenchmarks.bytebuffer;

import java.nio.*;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import me.lemire.hashing.InterleavedHash;


public class DirectVSHeapVSArray {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
    	long[] array = new long[(1<<16)/64];
    	LongBuffer direct = ByteBuffer.allocateDirect((1<<16)/8).asLongBuffer();
    	LongBuffer heap = ByteBuffer.allocate((1<<16)/8).asLongBuffer();
    }
    
    @Benchmark
    public long arrayTest(BenchmarkState s) {
    	long sum = 0;
    	for(int k = 0; k < (1<<16)/64; ++k) {
    		sum += s.array[k];
     	}
    	return sum;
    }
    

    @Benchmark
    public long directTest(BenchmarkState s) {
    	long sum = 0;
    	for(int k = 0; k < (1<<16)/64; ++k) {
    		sum += s.direct.get(k);
     	}
    	return sum;
    }

    @Benchmark
    public long heapTest(BenchmarkState s) {
    	long sum = 0;
    	for(int k = 0; k < (1<<16)/64; ++k) {
    		sum += s.heap.get(k);;
     	}
    	return sum;
    }
    

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DirectVSHeapVSArray.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
