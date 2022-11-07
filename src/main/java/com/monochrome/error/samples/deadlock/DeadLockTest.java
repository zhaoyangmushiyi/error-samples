package com.monochrome.error.samples.deadlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author monochrome
 * @date 2022/11/7
 */
@RestController
@RequestMapping("deadlock")
public class DeadLockTest {

    private Logger log = LoggerFactory.getLogger(DeadLockTest.class);

    Map<String, Item> items = new HashMap<>();
    {
        IntStream.range(0, 10).parallel()
                .forEach(i -> items.put("item" + i, new Item("item" + i)));
    }

    private List<Item> createCart() {
        return IntStream.rangeClosed(1, 3)
                .mapToObj(i -> "item" + ThreadLocalRandom.current().nextInt(items.size()))
                .map(name -> items.get(name))
                .collect(Collectors.toList());
    }

    private boolean createOrder(List<Item> order) {
        // 存放所有获得的锁
        List<ReentrantLock> locks = new ArrayList<>();
        for (Item item : order) {
            try {
                // 获得锁10秒超时
                if (item.lock.tryLock(10, TimeUnit.SECONDS)) {
                    locks.add(item.lock);
                } else {
                    locks.forEach(ReentrantLock::unlock);
                    return false;
                }
            } catch (InterruptedException ignore) {
            }
        }
        // 锁全部拿到之后执行扣减库存业务逻辑
        try {
            order.forEach(item -> item.remaining--);
        } finally {
            locks.forEach(ReentrantLock::unlock);
        }
        return true;
    }

    @GetMapping("wrong")
    public long wrong() {
        long begin = System.currentTimeMillis();
        long success = IntStream.rangeClosed(1, 100).parallel()
                .mapToObj(i -> {
                    List<Item> cart = this.createCart();
                    return createOrder(cart);
                })
                .filter(result -> result)
                .count();
        log.info("success:{} totalRemaining:{} took:{}ms",
                success,
                items.values().stream().map(Item::getRemaining).reduce(0, Integer::sum),
                System.currentTimeMillis() - begin);
        return success;
    }
    @GetMapping("right")
    public long right() {
        long begin = System.currentTimeMillis();
        long success = IntStream.rangeClosed(1, 100).parallel()
                .mapToObj(i -> {
                    // 通过排序让获取锁的顺序可控
                    List<Item> cart = this.createCart().stream()
                            .sorted(Comparator.comparing(Item::getName))
                            .collect(Collectors.toList());
                    return createOrder(cart);
                })
                .filter(result -> result)
                .count();
        log.info("success:{} totalRemaining:{} took:{}ms",
                success,
                items.values().stream().map(Item::getRemaining).reduce(0, Integer::sum),
                System.currentTimeMillis() - begin);
        return success;
    }

}
