package com.arcsq.redis.distributedlock;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class DistributedLockingEndPoint {

    private RedissonClient redissonClient;

    DistributedLockingEndPoint() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redissonClient = Redisson.create(config);
    }

    @GetMapping(path = "/endpoint1/{deviceId}")
    public String testFunction(@PathVariable("deviceId") String deviceId) {

        RLock lock = redissonClient.getLock("myLock_" + deviceId);
        if (lock.isLocked() == false) {
            lock.lock(30, TimeUnit.SECONDS);
            try {
                // Performed the core of the scheduled task
                System.out.println("Saving Data for " + deviceId + "...");
                // Thread.sleep is only kept here for testing by running two instances of this project
                // and calling those from different browsers. Actual implementation should remove these
                Thread.sleep(10000);
                System.out.println("Data saved for " + deviceId);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "Lock Lease expired, this is an error condition saving data for " + deviceId;
            } finally {
                lock.unlock();
                return "Lock released";
            }

        }
        return "Ok";
    }

}
