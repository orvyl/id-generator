package com.orvyl.generator.id;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public final class LongIDGenerator implements IDGenerator<Long> {

    private long startTime;
    private short machineId;
    private short sequence;

    private long elapsedTime;

    LongIDGenerator(ZonedDateTime dateTimeSeed) throws IDGenerationException {
        try {
            startTime = createStartTime(dateTimeSeed);
            machineId = getLower16BitPrivateIP();
            sequence = 128;
        } catch (UnknownHostException e) {
            throw new IDGenerationException(e);
        }
    }

    @Override
    public synchronized Long getNextID() throws IDGenerationException {

        long currentTime = createStartTime(ZonedDateTime.now()) - startTime;
        if (elapsedTime < currentTime) {
            elapsedTime = currentTime;
            sequence = 0;
        } else {
            sequence = ++sequence == 256 ? 0 : sequence;
            if (sequence == 0) {
                elapsedTime++;
                try {
                    long overtime = elapsedTime - currentTime;
                    long timeNow = (ZonedDateTime.now().toEpochSecond() * 1_000_000_000) % (long) 1e7;
                    long timeout = (overtime * 1_000_000 * 10) - timeNow;

                    TimeUnit.NANOSECONDS.sleep(timeout);
                } catch (InterruptedException e) {
                    throw new IDGenerationException(e);
                }
            }
        }

        return (elapsedTime << 24) | ((long) sequence) << 16 | (long) machineId;
    }

    private static long createStartTime(ZonedDateTime zonedDateTime) throws IDGenerationException {
        if (zonedDateTime.isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
            throw new IDGenerationException("DateTime seed must not be future date/time");
        }

        return (zonedDateTime.toEpochSecond() * 1_000_000_000) / (long) 1e7;
    }

    private static short getLower16BitPrivateIP() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        byte[] ip = address.getAddress();
        if (isPrivateIP(ip)) {
            return (short) ((ip[2] << 1) + ip[3]);
        }

        throw new RuntimeException("IP " + address.getHostAddress() + " is not a private one.");
    }

    private static boolean isPrivateIP(byte[] ip) {
        if (ip == null || ip.length < 2) return false;

        int firstOctet = ip[0] & 0xFF;
        int secondOctet = ip[1] & 0xFF;

        return firstOctet == 10 || firstOctet == 172 &&
                (secondOctet >= 16 && secondOctet < 32) ||
                firstOctet == 192 && secondOctet == 168;
    }
}
