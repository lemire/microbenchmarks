
public class Bitset {

  public static void xor(long[] a, long[] b) {
    final int length = a.length < b.length ? a.length : b.length;
    for( int k = 0; k < length ; ++k ) {
      a[k] ^= b[k];
    }
  }


  public static void main(String[] args)  {
    final int N = 1024;
    long[] a = new long[N];
    long[] b = new long[N];
    for(int k = 0; k < N; ++k) {
      a[k] = k;
      b[k] = 1000 - k;
    }
    for (int k = 0; k < 100; k++) {
      xor(a,b);
    }
    System.out.println(a[N - 1]);
  }

}
