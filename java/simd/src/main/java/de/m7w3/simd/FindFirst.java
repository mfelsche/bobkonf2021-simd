package de.m7w3.simd;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;


public class FindFirst {

    public static VectorSpecies<Byte> species = ByteVector.SPECIES_PREFERRED;

    public int findFirst(byte[] haystack, byte needle) {
        var vector_len = species.length();
        var idx = 0;
        var needleVec = ByteVector.broadcast(species, needle);
        // vector loop
        while (idx <= (haystack.length - vector_len)) {
            var vec = ByteVector.fromArray(species, haystack, idx);
            var mask = vec.compare(VectorOperators.EQ, needleVec);
            vec.add(vec);
            var firstTrue = mask.firstTrue();
            if (firstTrue < vector_len) {
                return idx + firstTrue;
            }
            idx += vector_len;
        }
        // scalar tail
        for (; idx < haystack.length; idx++) {
            if (haystack[idx] == needle) {
                return idx;
            }
        }

        return -1;
    }

    public int findFirst2(byte[] haystack, byte needle) {
        if (haystack.length < species.length()) {
            // scalar
            for (int i = 0; i < haystack.length; i++) {
                if (haystack[i] == needle) {
                    return i;
                }
            }
            return -1;
        } else {
            var needleVec = ByteVector.broadcast(species, needle);
            var upperBound = species.loopBound(haystack.length);
            // vector loop
            ByteVector vec;
            VectorMask<Byte> mask;
            int firstTrue;
            int idx = 0;
            for (; idx < upperBound; idx += species.length()) {
                vec = ByteVector.fromArray(species, haystack, idx);
                mask = vec.compare(VectorOperators.EQ, needleVec);
                if (mask.anyTrue()) { // firstTrue is expensive, so guard it here
                    firstTrue = mask.firstTrue();
                    if (firstTrue < species.length()) {
                        return idx + firstTrue;
                    }
                }
            }

            if (idx < haystack.length) {
                // reset index to end - vector-size to handle tail with 1 iteration
                idx = haystack.length - species.length();
                vec = ByteVector.fromArray(species, haystack, idx);
                mask = vec.compare(VectorOperators.EQ, needleVec);

                if (mask.anyTrue()) { // firstTrue is expensive, so guard it here
                    firstTrue = mask.firstTrue();
                    if (firstTrue < species.length()) {
                        return idx + firstTrue;
                    }
                }
            }
        }
        return -1;
    }
    public int findFirst3(byte[] haystack, byte needle) {
        var needleVec = ByteVector.broadcast(species, needle);
        var holes = ByteVector.broadcast(species, 0x7F);
        var zero = ByteVector.zero(species);
        var upperBound = species.loopBound(haystack.length);
        // vector loop
        ByteVector vec;
        VectorMask<Byte> mask;
        int firstTrue;
        int idx = 0;
        for (; idx <= upperBound; idx += species.length()) {
            vec = ByteVector.fromArray(species, haystack, idx);
            var tmp = vec.and(holes).add(holes).or(vec).or(holes).not();
            mask = tmp.eq(zero);
            if (!mask.allTrue()) {
                firstTrue = mask.firstTrue();
                if (firstTrue < species.length()) {
                    return idx + firstTrue;
                }
            }
        }
        if (idx < haystack.length) {
            // reset index to end - vector-size to handle tail with 1 iteration
            idx = haystack.length - species.vectorByteSize();
            vec = ByteVector.fromArray(species, haystack, idx);
            var tmp = vec.and(holes).add(holes).or(vec).or(holes).not();
            mask = tmp.eq(zero);
            if (!mask.allTrue()) {
                firstTrue = mask.firstTrue();
                if (firstTrue < species.length()) {
                    return idx + firstTrue;
                }
            }
        }
        return -1;
    }
}
