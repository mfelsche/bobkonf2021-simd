package de.m7w3.simd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringJoiner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FindByteTest {
    FindFirst ff = new FindFirst();
    @Test
    @DisplayName("empty")
    public void findByteEmpty() {

        assertThat(ff.findFirst2(new byte[0], (byte)1), equalTo(-1));
    }

    @Test
    @DisplayName("find-bytes-not-there")
    public void findBytesNotThere() {
        assertThat(ff.findFirst2("foo".getBytes(StandardCharsets.UTF_8), (byte)'a'), equalTo(-1));
        assertThat(ff.findFirst2("baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaar".getBytes(StandardCharsets.UTF_8), (byte)'o'), equalTo(-1));
        assertThat(ff.findFirst("ABCDEFHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.UTF_8), (byte)'1'), equalTo(-1));
    }

    @Test
    @DisplayName("find-bytes")
    public void findBytes() {
        assertThat(
                ff.findFirst2("foo".getBytes(StandardCharsets.UTF_8), (byte)'f'), equalTo(0)
        );
        assertThat(
                ff.findFirst2("foo".getBytes(StandardCharsets.UTF_8), (byte)'o'), equalTo(1)
        );
        assertThat(
                ff.findFirst2("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".getBytes(StandardCharsets.UTF_8), (byte)'0'), equalTo(52)
        );

    }


}
