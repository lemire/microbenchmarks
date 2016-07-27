package me.lemire.microbenchmarks.random;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;



public class Division {

    
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        final float divisor = (float) (1<<14);
        final float precomputeddivisor = 1 / divisor;
        final float divisor3 = (float) (3);
        final float precomputeddivisor3 = 1 / divisor3;
         int length = 10;
    }
    

    @Benchmark
    public float precompDivision(BenchmarkState s) {
    	float sum = 0;
    	for(int k = 0; k < s.length; ++k)
    		sum += s.precomputeddivisor * k;
    	return sum;
    }

    @Benchmark
    public float division(BenchmarkState s) {
    	float sum = 0;
    	for(int k = 0; k < s.length; ++k)
    		sum +=  k / s.divisor;
    	return sum;
    }

    @Benchmark
    public float divisionBy3(BenchmarkState s) {
    	float sum = 0;
    	for(int k = 0; k < s.length; ++k)
    		sum +=  k / (float) (3);
    	return sum;
    }
   
    @Benchmark
    public float divisionBy3ThroughMultiplication(BenchmarkState s) {
    	float sum = 0;
    	for(int k = 0; k < s.length; ++k)
    		sum +=  k * s.precomputeddivisor3;
    	return sum;
    }
    
 
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Division.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }

}
