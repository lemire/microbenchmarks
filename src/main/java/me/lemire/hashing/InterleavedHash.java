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

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.*;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InterleavedHash {

    @Benchmark
    public int standardHash(BenchmarkState s) {
        char[] val = s.array;
        int len = val.length;
        int h = 1;
        for (int i = 0; i < len; i++) {
            h = 31 * h + val[i];
        }
        return h;

    }


    @Benchmark
    public int hashCode(BenchmarkState s) {
        return Arrays.hashCode(s.array);
    }

    @Benchmark
    public int standardHash4(BenchmarkState s) {
        char[] val = s.array;
        int len = val.length;
        int h = 1;
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
        int h = 1;
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

    @Benchmark
    public int standardHashByte(BenchmarkState s) {
        byte[] val = s.barray;
        int len = val.length;
        int h = 1;
        for (int i = 0; i < len; i++) {
            h = 31 * h + val[i];
        }
        return h;

    }


    @Benchmark
    public int hashCodeByte(BenchmarkState s) {
        return Arrays.hashCode(s.barray);
    }

    @Benchmark
    public int standardHashByte4(BenchmarkState s) {
        byte[] val = s.barray;
        int len = val.length;
        int h = 1;
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
    public int standardHashByte8(BenchmarkState s) {
        byte[] val = s.barray;
        int len = val.length;
        int h = 1;
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

    @Benchmark
    public int standardHashByteBuffer(BenchmarkState s) {
        ByteBuffer val = s.bb;
        int len = val.limit();
        int h = 1;
        for (int i = 0; i < len; i++) {
            byte b = val.get(i);
            h = 31 * h + b;
        }
        return h;

    }


    @Benchmark
    public int standardHashByteBuffer4(BenchmarkState s) {
        ByteBuffer val = s.bb;
        int len = val.limit();
        int h = 1;
        int i = 0;
        for (; i + 3 < len; i += 4) {
            byte b1 = val.get(i);
            byte b2 = val.get(i+1);
            byte b3 = val.get(i+2);
            byte b4 = val.get(i+3);

            h = 31 * 31 * 31 * 31 * h + 31 * 31 * 31 * b1 + 31 * 31
                    * b2 + 31 * b3 + b4;
        }
        for (; i < len; i++) {
            byte b = val.get(i);
            h = 31 * h + b;
        }

        return h;

    }

    @Benchmark
    public int standardHashByteBuffer8(BenchmarkState s) {
        ByteBuffer val = s.bb;
        int len = val.limit();
        int h = 1;
        int i = 0;
        for (; i + 7 < len; i += 8) {
            byte b1 = val.get(i);
            byte b2 = val.get(i+1);
            byte b3 = val.get(i+2);
            byte b4 = val.get(i+3);
            byte b5 = val.get(i+4);
            byte b6 = val.get(i+5);
            byte b7 = val.get(i+6);
            byte b8 = val.get(i+7);
            h = 31 * 31 * 31 * 31 * 31 * 31 * 31 * 31 * h + 31 * 31 * 31 * 31
                    * 31 * 31 * 31 * b1 + 31 * 31 * 31 * 31 * 31 * 31
                    * b2 + 31 * 31 * 31 * 31 * 31 * b3 + 31
                    * 31 * 31 * 31 * b4 + 31 * 31 * 31 * b5
                    + 31 * 31 * b6 + 31 * b7 + b8;
        }

        for (; i + 3 < len; i += 4) {
            byte b1 = val.get(i);
            byte b2 = val.get(i+1);
            byte b3 = val.get(i+2);
            byte b4 = val.get(i+3);
            h = 31 * 31 * 31 * 31 * h + 31 * 31 * 31 * b1 + 31 * 31
                    * b2 + 31 * b3 + b4;
        }
        for (; i < len; i++) {
            byte b = val.get(i);
            h = 31 * h + b;
        }

        return h;

    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        char[] array;
        byte[] barray;

        ByteBuffer bb;

        public BenchmarkState() {
            array = new char[64];
            barray = new byte[64];
            for (int k = 0; k < array.length; ++k) {
                array[k] = (char) k;
                barray[k] = (byte) k;
            }
            bb = ByteBuffer.wrap(barray);
        }

    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(InterleavedHash.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }
}
