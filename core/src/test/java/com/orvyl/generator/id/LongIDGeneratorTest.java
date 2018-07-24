package com.orvyl.generator.id;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LongIDGeneratorTest {

    @Test
    public void testLongIDGenerator() throws IDGenerationException, InterruptedException, ExecutionException {
        ZonedDateTime zd = ZonedDateTime.now();
        Thread.sleep(200);
        IDGenerator<Long> idGenerator = new LongIDGenerator(zd);

        int size = 2000;
        ExecutorService service = Executors.newFixedThreadPool(100);
        List<Long> ids = Collections.synchronizedList(new ArrayList<>());
        for (int x = 0; x < size; x++) {
            service.execute(() -> {
                try {
                    ids.add(idGenerator.getNextID());
                } catch (IDGenerationException e) {
                    e.printStackTrace();
                }
            });
        }

        Thread.sleep(1000);

        service.shutdownNow();
        Assert.assertEquals(size, ids.size());
        Assert.assertEquals(size, ids.stream().distinct().count());
    }

}
