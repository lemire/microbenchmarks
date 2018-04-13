package me.lemire.microbenchmarks.arrays;

import java.util.concurrent.TimeUnit;
import java.util.*;

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
public class ArraysBench {

    private static int  sum(int[] arr) {
      int s = 0;
      for(int k = 0; k < arr.length; ++k) {
        s += arr[k];
      }
      return s;
    }

    private static int sum2(int[] arr) {
      int s = 0;
      final int l = arr.length;
      for(int k = 0; k < l; ++k) {
        s += arr[k];
      }
      return s;
    }

    private static int  listsum(ArrayList<Integer> arr) {
      int s = 0;
      for(int k = 0; k < arr.size(); ++k) {
        s += arr.get(k);
      }
      return s;
    }

    private static int listsum2(ArrayList<Integer> arr) {
      int s = 0;
      final int l = arr.size();
      for(int k = 0; k < l; ++k) {
        s += arr.get(k);
      }
      return s;
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        int N = 17773;
        int[] array = new int[N];
        ArrayList<Integer> list = new ArrayList<Integer>();

        public BenchmarkState() {

            for (int k = 0; k < N; ++k) {
                array[k] = k;
                list.add(k);
            }
        }
    }

    @Benchmark
    public int basicsum(BenchmarkState s) {
        return sum(s.array);
    }
    @Benchmark
    public int bufbasicsum(BenchmarkState s) {
        return sum2(s.array);
    }

    @Benchmark
    public int basiclistsum(BenchmarkState s) {
        return listsum(s.list);
    }
    @Benchmark
    public int bufbasiclistsum(BenchmarkState s) {
        return listsum2(s.list);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
        .include(ArraysBench.class.getSimpleName()).warmupIterations(5)
        .measurementIterations(20).forks(1).build();

        new Runner(opt).run();
    }

}
