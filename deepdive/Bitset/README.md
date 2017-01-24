

Grab the appropriate library from https://kenai.com/projects/base-hsdis/downloads and install in /usr/lib (Linux).

Run:
```
$ java  -XX:+PrintFlagsFinal  -version |grep -E "(SSE|AVX)"
     intx UseAVX                                    = 2                                   {ARCH product}
     intx UseSSE                                    = 4                                   {product}
     bool UseSSE42Intrinsics                        = true                                {product}
```

http://hg.openjdk.java.net/jdk9/dev/hotspot/file/a9fdfd55835e/src/cpu/x86/vm/stubRoutines_x86_64.cpp

OpenJDK8/hotspot/src/share/vm/classfile/vmSymbols.hpp

Run:

```
javac Bitset.java
java  -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly Bitset
```
