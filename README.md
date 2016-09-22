microbenchmarks
===============


Bitset
---------
        mvn clean install
        java -cp target/microbenchmarks-0.0.1-jar-with-dependencies.jar me.lemire.microbenchmarks.bitset.Bitset

Compare direct vs. heap buffers
-------------------------------


        mvn clean install
        java -cp target/microbenchmarks-0.0.1-jar-with-dependencies.jar me.lemire.microbenchmarks.bytebuffer.DirectVSHeapVSArray



Java RNG is slow
-----------------

        mvn clean install
        java -cp target/microbenchmarks-0.0.1-jar-with-dependencies.jar me.lemire.microbenchmarks.random.RandomNumberGenerator


Integer sums
--------------

This is my answer to the following blog post: http://www.vitavonni.de/blog/201412/2014122201-java-sum-of-array-comparisons.html

        mvn clean install
        java -cp target/microbenchmarks-0.0.1-jar-with-dependencies.jar me.lemire.microbenchmarks.integersum.SumBenchmark -wi 5 -i 5 -f 1



Hashing
---------

        java -cp target/microbenchmarks-0.0.1-jar-with-dependencies.jar me/lemire/hashing/InterleavedHash


Shuffle
-----------
        java -cp target/microbenchmarks-0.0.1-jar-with-dependencies.jar me.lemire.microbenchmarks.algorithms.Shuffle
 
