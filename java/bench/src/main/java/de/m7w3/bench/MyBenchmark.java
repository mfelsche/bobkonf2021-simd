/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.m7w3.bench;

import de.m7w3.simd.FindFirst;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.Random;

public class MyBenchmark {

    public int findArrayScalar(byte[] haystack, byte needle) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    @State(Scope.Thread)
    public static class BenchState {
        public int size;
        public int needle_idx;
        public byte[] haystack;
        public byte needle;
        public FindFirst ff;

        public BenchState() {
        }

        @Setup(Level.Trial)
        public void prepare() {
            Random rnd = new Random(System.currentTimeMillis());
            this.size = Integer.getInteger("haystack.size", 1000);
            this.needle_idx = Integer.getInteger("needle.index", 500);
            this.needle = (byte)0x97; // a
            byte[] bytes = new byte[size];
            for (int i = 0; i < this.needle_idx; i++) {
                bytes[i] = 0x65;
            }
            bytes[this.needle_idx] = this.needle;

            this.haystack = bytes;
            this.ff = new FindFirst();
        }
    }



    //@Benchmark
    public void vectorFindFirst(Blackhole bh, BenchState state) {
        var res = state.ff.findFirst(state.haystack, state.needle);
        bh.consume(res);
    }

    @Benchmark
    public void vectorFindFirst2(Blackhole bh, BenchState state) {
        var res = state.ff.findFirst2(state.haystack, state.needle);
        bh.consume(res);
    }

    @Benchmark
    public void scalarFindFirst(Blackhole bh, BenchState state) {
        var res = findArrayScalar(state.haystack, state.needle);
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.
        bh.consume(res);
    }


    //@Benchmark
    public void vectorFindFirst3(Blackhole bh, BenchState state) {
        var res = state.ff.findFirst3(state.haystack, state.needle);
        bh.consume(res);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
