

Grab the appropriate library from https://kenai.com/projects/base-hsdis/downloads and install in /usr/lib (Linux).

Run:

```
javac Mysterious.java
java  -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly Mysterious > myst.txt
```
