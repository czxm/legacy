package com.intel.xml.rss.util;

public class Pair<A, B> {
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A a;

    public B b;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair another = (Pair) obj;
        if (a != null && b != null) {
            return a.equals(another.a) && b.equals(another.b);
        }
        if (a != null) {
            // b must == null
            return a.equals(another.a) && another.b == null;
        }
        if (b != null) {
            return b.equals(another.b) && another.a == null;
        }
        return another.a == null && another.b == null;
    }

}
