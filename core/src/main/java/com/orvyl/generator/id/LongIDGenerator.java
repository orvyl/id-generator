package com.orvyl.generator.id;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class LongIDGenerator implements IDGenerator<Long> {

    private long startTime;
    private short machineId;
    private short sequence;

    private long elapsedTime;

    LongIDGenerator(ZonedDateTime dateTimeSeed) throws IDGenerationException {
        try {
            startTime = createStartTime(dateTimeSeed);
            machineId = getLower16BitPrivateIP();
            sequence = 0x00FF & (1 << 7);
        } catch (UnknownHostException e) {
            throw new IDGenerationException(e);
        }
    }

    @Override
    public synchronized Long getNextID() throws IDGenerationException {
        final short mask = 0x00FF & (1 << 7);

        long currentTime = createStartTime(ZonedDateTime.now()) - startTime;
        if (elapsedTime < currentTime) {
            elapsedTime = currentTime;
            sequence = 0;
            System.out.println();
        } else {
            sequence = (short) ((sequence + 1) & mask);
            if (sequence == 0) {
                elapsedTime++;
                /*try {
                    long overtime = elapsedTime - currentTime;
                    long timeNow = ZonedDateTime.now().getNano() % (long) 1e7;

                    long timeout = (overtime * 10 * 1_000_000) - timeNow;
                    System.out.println("Sleeping now for " + timeNow + " nanos...");
                    TimeUnit.NANOSECONDS.sleep(timeout);
                } catch (InterruptedException e) {
                    throw new IDGenerationException(e);
                }*/
            }
        }

        return (elapsedTime << 24) | ((long) sequence) << 16 | (long) machineId;
    }

    private static long createStartTime(ZonedDateTime zonedDateTime) throws IDGenerationException {
        if (zonedDateTime.isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
            throw new IDGenerationException("DateTime seed must not be future date/time");
        }

        return zonedDateTime.getNano() / (long) 1e7;
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
