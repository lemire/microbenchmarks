
public class Bitset {

  public static void xor(int[] a, int[] b) {
    final int length = a.length < b.length ? a.length : b.length;
    for( int k = 0; k < length ; ++k ) {
      a[k] = b[k];
    }
  }
  
  public static long  sum(int[] a) {
    long x = 0;
    for(int i=0; i < a.length; i++) x+= a[i];
    return x;
  }
  public static void crap(int[] a, int[] b) {
   for (int ibeg=0; ibeg<a.length; ibeg+=1024)
      for (int i=ibeg; i<(ibeg+1024); ++i)
          a[i] += b[i];
  }

  public static void main(String[] args)  {
    final int N = 1024;
    int[] a = new int[N];
    int[] b = new int[N];
    for(int k = 0; k < N; ++k) {
      a[k] = k;
      b[k] = 1000 - k;
    }
    for (int k = 0; k < 100; k++) {
      crap(a,b);
    }
    int supersum = 0;
    for (int k = 0; k < 100; k++) {
      xor(a,b);
      supersum += sum(a);
    }
    System.out.println(supersum);
  }

}
