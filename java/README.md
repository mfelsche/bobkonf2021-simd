# Java Vector API memchr implementation

Depends on openjdk 16 with the `jdk.incubator.vector` module.

## Prepare

```sh
export JAVA_HOME=/path/to/jdk-16
./mvnw clean verify
```

## Running the benchmark

The benchmarks use jmh.

```
"$JAVA_HOME"/bin/java -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0 --add-modules jdk.incubator.vector -jar ./bench/target/benchmarks.jar -f 1
```

Control benchmark execution:

Use the system properties `haystack.size` and `needle.index` to control the length of the array to search on and the index of the element to be found.

Disable out of bounds checks in the vector api by setting `jdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK` to `0` to gain a slight speedup.
