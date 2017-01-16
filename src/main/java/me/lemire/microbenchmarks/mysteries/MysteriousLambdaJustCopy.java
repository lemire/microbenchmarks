package me.lemire.microbenchmarks.mysteries;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
// http://stackoverflow.com/questions/41622613/why-is-my-java-lambda-with-a-dummy-assignment-much-faster-than-without-it
public class MysteriousLambdaJustCopy {

	@State(Scope.Benchmark)
	public static class BenchmarkState {
		ArrayList<Foo> fooList = new ArrayList<Foo>();

		public BenchmarkState() {
			for (int ctr = 0; ctr < 1000; ++ctr) {
				fooList.add(new Foo().alpha("a" + ctr).beta("b" + ctr));
			}
		}

	}

	
	@Benchmark
	public FooPrime[] basicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			return new FooPrime().gamma(it.getAlpha());
		}).toArray(FooPrime[]::new);
	}
	
	




	@Benchmark
	public FooPrime[] tweakedbasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			int stuff = it.getAlpha().length();
			return new FooPrime().gamma(it.getAlpha());
		}).toArray(FooPrime[]::new);
	}

	@Benchmark
	public FooPrime[] nullbasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			if( it.getAlpha() == null) throw new NullPointerException();
			return new FooPrime().gamma(it.getAlpha());
		}).toArray(FooPrime[]::new);
	}



   	@Benchmark
	public FooPrime[] betanullbasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			if( it.getBeta() == null) throw new NullPointerException();
			return new FooPrime().gamma(it.getAlpha());
		}).toArray(FooPrime[]::new);
	}

 
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(MysteriousLambdaJustCopy.class.getSimpleName()).warmupIterations(5)
        .measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }

}
