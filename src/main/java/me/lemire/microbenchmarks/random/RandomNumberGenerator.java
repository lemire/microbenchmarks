package me.lemire.microbenchmarks.random;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.github.alexeyr.pcg.Pcg32;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class RandomNumberGenerator {
    
    
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        Random r = new Random();
        long seed = System.nanoTime();
        Pcg32 rnd = new Pcg32();

        public int manualJavaNext(int bits) {
            seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            return (int) (seed >>> (48 - bits));
        }
    }
    

    @Benchmark
    public int basicJavaRandom(BenchmarkState s) {
        return  s.r.nextInt();
    }
    

    @Benchmark
    public int manualJavaRandom(BenchmarkState s) {
        return  s.manualJavaNext(32);
    }
    
    @Benchmark
    public int pcgJavaRandom(BenchmarkState s) {
        return  s.rnd.nextInt();
    }
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(RandomNumberGenerator.class.getSimpleName()).warmupIterations(2)
        .measurementIterations(3).forks(1).build();

        new Runner(opt).run();
    }

}

