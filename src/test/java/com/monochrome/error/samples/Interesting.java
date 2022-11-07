package com.monochrome.error.samples;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Interesting {

    private Logger log = LoggerFactory.getLogger(Interesting.class);

    volatile int a = 1;
    volatile int b = 1;

    public void add() {
        log.info("add start");
        for (int i = 0; i < 10000; i++) {
            a++;
            b++;
        }
        log.info("add done");
    }

    public void compare() {
        log.info("compare start");
        for (int i = 0; i < 10000; i++) {
            //a始终等于b吗？
            if (a < b) {
                log.info("a:{},b:{},{}", a, b, a > b);
                //最后的a>b应该始终是false吗？
            }
        }
        log.info("compare done");
    }

    @Test
    public void test() {
        new Thread(this::add).start();
        new Thread(this::compare).start();
    }
}