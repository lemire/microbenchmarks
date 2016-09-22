package me.lemire.microbenchmarks.bitset;

import java.util.BitSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;



@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class Bitset {
    static BitSet b = new BitSet();

    @Setup
    public void populate() {
      for (int i = 0; i <= 100000000; i+= 100){
                b.set(i);
      }
    }


    @Benchmark
    public BitSet construct() {
    	BitSet b = new BitSet();
      for (int i = 0; i <= 100000000; i+= 100){
                b.set(i);
      }
      return b;
    }

    @Benchmark
    public int count() {
    	return b.cardinality();
    }

    @Benchmark
    public int iterate() {
    	int sum = 0;
    	for (int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i+1)) {
        sum++;
      }
    	return sum;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Bitset.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }

}
