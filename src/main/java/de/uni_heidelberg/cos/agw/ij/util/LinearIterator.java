package de.uni_heidelberg.cos.agw.ij.util;

import java.util.Iterator;

public class LinearIterator implements Iterable<int[]> {

    private final MutableLinearIterator linearIterator;

    public LinearIterator() {
        linearIterator = new MutableLinearIterator();
    }

    public void set(int[] start, int[] end, final boolean includeFirst) {
        linearIterator.set(start, end, includeFirst);
    }

    public int nRemainingSteps() {
        return linearIterator.nRemainingSteps();
    }

    @Override
    public Iterator<int[]> iterator() {
        Iterator<int[]> it = new Iterator<int[]>() {
            private Iterator<int[]> linearIt = linearIterator.iterator();

            @Override
            public boolean hasNext() {
                return linearIt.hasNext();
            }

            @Override
            public int[] next() {
                return linearIt.next().clone();
            }

            @Override
            public void remove() {
                linearIt.remove();
            }
        };

        return it;
    }
}