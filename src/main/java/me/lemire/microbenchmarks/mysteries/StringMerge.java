package me.lemire.microbenchmarks.mysteries;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringMerge {
	
	
	@State(Scope.Benchmark)
	public static class BenchmarkState {
		final int N = 1000;
		String[] list1 = new String[N]; 
		String[] list2 = new String[N]; 
		String[] list3 = new String[N]; 

		public BenchmarkState() {
			for (int ctr = 0; ctr < N; ++ctr) {
				list1[ctr] = "a" + ctr;
				list2[ctr] = "a" + ctr;
			}
		}
	}

	@Benchmark
	public void stringsum(BenchmarkState s) {
		for(int k = 0; k < s.N; ++k) s.list3[k] = s.list1[k] + s.list2[k];
	}


	@Benchmark
	public void stringsum_withexcept(BenchmarkState s) {
		for(int k = 0; k < s.N; ++k) {
			if(s.list1[k] == null) throw new NullPointerException();
			s.list3[k] = s.list1[k] + s.list2[k];
		}
	}
	
	public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(StringMerge.class.getSimpleName()).warmupIterations(5)
        .measurementIterations(5).forks(1).build();
        new Runner(opt).run();
    }


}
