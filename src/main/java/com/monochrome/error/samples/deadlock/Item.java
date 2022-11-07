package com.monochrome.error.samples.deadlock;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author monochrome
 * @date 2022/11/7
 */
@Data
@RequiredArgsConstructor
public class Item {

    final String name;
    int remaining = 1000;
    @ToString.Exclude
    ReentrantLock lock = new ReentrantLock();

}
