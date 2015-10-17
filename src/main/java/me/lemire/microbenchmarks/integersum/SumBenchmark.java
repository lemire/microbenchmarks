package me.lemire.microbenchmarks.integersum;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SumBenchmark {
	
  ReverseFastSummer refs;
  FastSummer fs;
  BasicSummer gs;
  ReverseBasicSummer regs;
  SmartSummer ss;
  ReverseSmartSummer ress;
  SillySummer sis;
  FixedSummer ffs;
  ReverseFixedSummer reffs;

  @Benchmark
  public int RFastbench() {
  	return refs.compute();
  }

  @Benchmark
  public int Fastbench() {
  	return fs.compute();
  }

  @Benchmark
  public int Basicbench() {
  	return gs.compute();
  }

  @Benchmark
  public int RBasicbench() {
  	return regs.compute();
  }

  @Benchmark
  public int Smartbench() {
  	return ss.compute();
  }

  @Benchmark
  public int RSmartbench() {
  	return ress.compute();
  }
	
  @Benchmark
  public int Sillybench() {
  	return sis.compute();
  }

  @Benchmark
  public int Fixedbench() {
  	return ffs.compute();
  }

  @Benchmark
  public int RFixedbench() {
  	return reffs.compute();
  }

  
  @Setup
	public void setUp() {
	   int N = 10000000;
     refs = new ReverseFastSummer(N);
     fs = new FastSummer(N);
     gs = new BasicSummer(new NaiveArray(N));
     regs = new ReverseBasicSummer(new NaiveArray(N));
     ss = new SmartSummer(new NaiveArray(N));
     ress = new ReverseSmartSummer(new NaiveArray(N));
     sis = new SillySummer(new NaiveArray(N));
     ffs = new FixedSummer(N);
     reffs = new ReverseFixedSummer(N);
  }
	
  /*
   *    $ mvn clean install
   *    $ java -cp target/microbenchmarks-0.0.1.jar me.lemire.microbenchmarks.integersum.SumBenchmark -wi 5 -i 5 -f 1
   */

  public static void main(String[] args) throws RunnerException {
      Options opt = new OptionsBuilder()
              .include(SumBenchmark.class.getSimpleName())
              .warmupIterations(5)
              .measurementIterations(5)
              .forks(1)
              .build();

      new Runner(opt).run();
  }
  
    public static void oldmain(String[] args) {
        int N = 10000000;
        ReverseFastSummer refs = new ReverseFastSummer(N);
        FastSummer fs = new FastSummer(N);
        BasicSummer gs = new BasicSummer(new NaiveArray(N));
        ReverseBasicSummer regs = new ReverseBasicSummer(new NaiveArray(N));
        SmartSummer ss = new SmartSummer(new NaiveArray(N));
        ReverseSmartSummer ress = new ReverseSmartSummer(new NaiveArray(N));
        SillySummer sis = new SillySummer(new NaiveArray(N));
        FixedSummer ffs = new FixedSummer(N);
        ReverseFixedSummer reffs = new ReverseFixedSummer(N);
        int bogus = 0;
        for(int k = 0; k < 100; ++k) {
            long t0 = System.nanoTime();
            bogus += refs.compute();
            long t1 = System.nanoTime();
            bogus += fs.compute();
            long t2 = System.nanoTime();
            bogus += gs.compute();
            long t3 = System.nanoTime();
            bogus += ss.compute();
            long t4 = System.nanoTime();
            bogus += ress.compute();
            long t5 = System.nanoTime();
            bogus += sis.compute();
            long t6 = System.nanoTime();
            bogus += ffs.compute();
            long t7 = System.nanoTime();
            bogus += reffs.compute();
            long t8 = System.nanoTime();
            bogus += regs.compute();
            long t9 = System.nanoTime();
 
              System.out.println("refast fast basic smart resmart silly fixed rebasic");
            System.out.println((t1-t0)*1.0/N+" "+(t2-t1)*1.0/N+" "+(t3-t2)*1.0/N+" "+(t4-t3)*1.0/N+" "+(t5-t4)*1.0/N+" "+(t6-t5)*1.0/N+" "+(t7-t6)*1.0/N+" "+(t8-t7)*1.0/N+" "+(t9-t8)*1.0/N);
        }
    }
}
