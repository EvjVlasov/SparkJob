package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class LimitsTest {
    private long max = 1073741824;
    private long min = 1024;
    Limits limits = new Limits();
    Timestamp currentTime= new Timestamp(System.currentTimeMillis());


    @Test
    void shouldGetLimit() {
        assertEquals(limits.getLimit("max"), max);
        assertEquals(limits.getLimit("min"), min);
    }

    @Test
    void shouldUpdateLimit() {
        System.out.println(currentTime);
        long newMax = 2147483648L;
        long newMin = 128;
        limits.updateLimit("max", newMax, currentTime);
        limits.updateLimit("min", newMin, currentTime);
        assertEquals(limits.getLimit("max"), newMax);
        assertEquals(limits.getLimit("min"), newMin);

    }

    @AfterAll
    static void afterAll() {
        Limits limits = new Limits();
        limits.updateLimit("max", 1073741824, new Timestamp(System.currentTimeMillis()));
        limits.updateLimit("min", 1024, new Timestamp(System.currentTimeMillis()));
    }
}