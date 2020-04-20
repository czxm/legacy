package com.intel.cedar.feature.util;

import java.util.Random;

public class RandomUtility {
    private static final long MAGICALSEED = 47;

    private RandomUtility() {
    }

    public static long getRandom() {
        long system = System.currentTimeMillis();
        long seed = MAGICALSEED ^ system;
        return getRandom(seed);
    }

    public static long getRandom(String name) {
        if (name == null) {
            name = "RandomUtilitySeed";
        }

        long seed = getSeed(name);
        seed = seed ^ MAGICALSEED;
        return getRandom(seed);
    }

    public static long getRandom(long seed) {
        Random r = new Random(seed);
        return Math.abs(r.nextLong());
    }

    protected static long getSeed(String name) {
        long seed = 0;
        int i = 0;
        for (i = 0; i < name.length() && i < 20; i++) {
            long v = (long) name.charAt(i);
            seed = (seed << 3) ^ v;
        }

        for (i = 0; i < 20; i++) {
            seed = seed ^ (seed << 3);
        }

        for (; i < name.length(); i++) {
            long v = (long) name.charAt(i);
            seed = seed ^ (v << (i % 60));
        }
        return seed;
    }
}
