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

        public static String merge(String x1, String x2) {
                int len1 = x1.length();
                int len2 = x2.length();
                char[] buffer = new char[len1 + len2];
                x1.getChars(0, len1, buffer, 0);
                x2.getChars(0, len2, buffer, len1);
                return new String(buffer,0,len1 + len2);
         }
	
	@Benchmark
	public FooPrime[] basicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			return new FooPrime().gamma(it.getAlpha() + it.getBeta());
		}).toArray(FooPrime[]::new);
	}
	
	@Benchmark
	public FooPrime[] mergebasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			return new FooPrime().gamma(merge(it.getAlpha(), it.getBeta()));
		}).toArray(FooPrime[]::new);
	}


	@Benchmark
	public FooPrime[] basicstreamstringbuilder(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			return new FooPrime().gamma(new StringBuilder().append(it.getAlpha()).append(it.getBeta()).toString());
		}).toArray(FooPrime[]::new);
	}

	@Benchmark
	public FooPrime[] tweakedbasicstreamstringbuilder(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			int stuff = it.getAlpha().length();
			return new FooPrime().gamma(new StringBuilder().append(it.getAlpha()).append(it.getBeta()).toString());
		}).toArray(FooPrime[]::new);
	}

	@Benchmark
	public FooPrime[] nullbasicstreamstringbuilder(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			if( it.getAlpha() == null) throw new NullPointerException();
			return new FooPrime().gamma(new StringBuilder().append(it.getAlpha()).append(it.getBeta()).toString());
		}).toArray(FooPrime[]::new);
	}



	@Benchmark
	public FooPrime[] basicsum(BenchmarkState s) {
		int howmany = s.fooList.size();
		FooPrime[] answer = new FooPrime[s.fooList.size()];
		for(int k = 0; k < howmany ; ++k ) {
			Foo x = s.fooList.get(k);
			answer[k] = new FooPrime(x.getAlpha() + x.getBeta());
		}
		return answer;
	}

	
	@Benchmark
	public String[] basicsum_tostring(BenchmarkState s) {
		int howmany = s.fooList.size();
		String[] answer = new String[s.fooList.size()];
		for(int k = 0; k < howmany ; ++k ) {
			Foo x = s.fooList.get(k);
			answer[k] = x.getAlpha() + x.getBeta();
		}
		return answer;
	}

	@Benchmark
	public String[] basicsumnull_tostring(BenchmarkState s) {
		int howmany = s.fooList.size();
		String[] answer = new String[s.fooList.size()];
		for(int k = 0; k < howmany ; ++k ) {
			Foo x = s.fooList.get(k);
			if(x.getAlpha() == null) throw new NullPointerException();
			answer[k] = x.getAlpha() + x.getBeta();
		}
		return answer;
	}



	@Benchmark
	public FooPrime[] tweakedbasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			int stuff = it.getAlpha().length();
			return new FooPrime().gamma(it.getAlpha() + it.getBeta());
		}).toArray(FooPrime[]::new);
	}
	
	@Benchmark
	public String[] basicstream_tostring(BenchmarkState s) {
		return (String[]) s.fooList.stream().map(it -> {
			return it.getAlpha() + it.getBeta();
		}).toArray(String[]::new);
	}
	
	
	@Benchmark
	public String[] tweakedbasicstream_tostring(BenchmarkState s) {
		return (String[]) s.fooList.stream().map(it -> {
			int stuff = it.getAlpha().length();
			return it.getAlpha() + it.getBeta();
		}).toArray(String[]::new);
	}

	
	@Benchmark
	public FooPrime[] nullbasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			if( it.getAlpha() == null) throw new NullPointerException();
			return new FooPrime().gamma(it.getAlpha() + it.getBeta());
		}).toArray(FooPrime[]::new);
	}



   	@Benchmark
	public FooPrime[] betanullbasicstream(BenchmarkState s) {
		return (FooPrime[]) s.fooList.stream().map(it -> {
			if( it.getBeta() == null) throw new NullPointerException();
			return new FooPrime().gamma(it.getAlpha() + it.getBeta());
		}).toArray(FooPrime[]::new);
	}

 
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(MysteriousLambda.class.getSimpleName()).warmupIterations(5)
        .measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }

}

final class Foo {
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

final class FooPrime {
	private String gamma;
	
	public FooPrime() {
	}

	public FooPrime(String x) {
		gamma = x;
	}

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
