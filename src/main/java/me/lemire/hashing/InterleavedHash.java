package me.lemire.hashing;

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

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InterleavedHash {
    @Benchmark
    public int standardHash(BenchmarkState s) {
        char[] val = s.array;
        int len = val.length;
        int h = 0;
        for (int i = 0; i < len; i++) {
            h = 31 * h + val[i];
        }
        return h;

    }

    @Benchmark
    public int standardHash4(BenchmarkState s) {
        char[] val = s.array;
        int len = val.length;
        int h = 0;
        int i = 0;
        for (; i + 3 < len; i += 4) {
            h = 31 * 31 * 31 * 31 * h + 31 * 31 * 31 * val[i] + 31 * 31
                    * val[i + 1] + 31 * val[i + 2] + val[i + 3];
        }
        for (; i < len; i++) {
            h = 31 * h + val[i];
        }

        return h;

    }

    @Benchmark
    public int standardHash8(BenchmarkState s) {
        char[] val = s.array;
        int len = val.length;
        int h = 0;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            h = 31 * 31 * 31 * 31 * 31 * 31 * 31 * 31 * h + 31 * 31 * 31 * 31
                    * 31 * 31 * 31 * val[i] + 31 * 31 * 31 * 31 * 31 * 31
                    * val[i + 1] + 31 * 31 * 31 * 31 * 31 * val[i + 2] + 31
                    * 31 * 31 * 31 * val[i + 3] + 31 * 31 * 31 * val[i + 4]
                    + 31 * 31 * val[i + 5] + 31 * val[i + 6] + val[i + 7];
        }

        for (; i + 3 < len; i += 4) {
            h = 31 * 31 * 31 * 31 * h + 31 * 31 * 31 * val[i] + 31 * 31
                    * val[i + 1] + 31 * val[i + 2] + val[i + 3];
        }
        for (; i < len; i++) {
            h = 31 * h + val[i];
        }

        return h;

    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        char[] array;

        public BenchmarkState() {
            array = new char[64];
            for (int k = 0; k < array.length; ++k)
                array[k] = (char) k;

        }

    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(InterleavedHash.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
