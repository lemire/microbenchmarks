package me.lemire.microbenchmarks.mysteries;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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
public class MysteriousLambda {

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
			return new FooPrime().gamma(it.getAlpha() + it.getBeta());
		}).toArray(FooPrime[]::new);
	}

	@Benchmark
	public FooPrime[] tweakedbasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			int stuff = it.getAlpha().length();
			return new FooPrime().gamma(it.getAlpha() + it.getBeta());
		}).toArray(FooPrime[]::new);
	}

    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(MysteriousLambda.class.getSimpleName()).warmupIterations(2)
        .measurementIterations(3).forks(1).build();

        new Runner(opt).run();
    }

}

class Foo {
	private String alpha;
	private String beta;

	public String getAlpha() {
		return alpha;
	}

	public String getBeta() {
		return beta;
	}

	public void setAlpha(String alpha) {
		this.alpha = alpha;
	}

	public void setBeta(String beta) {
		this.beta = beta;
	}

	public Foo alpha(String alpha) {
		this.alpha = alpha;
		return this;
	}

	public Foo beta(String beta) {
		this.beta = beta;
		return this;
	}
}

class FooPrime {
	private String gamma;

	public String getGamma() {
		return gamma;
	}

	public void setGamma(String gamma) {
		this.gamma = gamma;
	}

	public FooPrime gamma(String gamma) {
		this.gamma = gamma;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gamma == null) ? 0 : gamma.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FooPrime other = (FooPrime) obj;
		if (gamma == null) {
			if (other.gamma != null)
				return false;
		} else if (!gamma.equals(other.gamma))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FooPrime [gamma=" + gamma + "]";
	}
}
