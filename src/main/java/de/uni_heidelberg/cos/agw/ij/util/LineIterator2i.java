package de.uni_heidelberg.cos.agw.ij.util;

import javax.vecmath.Point2i;
import java.util.Iterator;

public class LineIterator2i implements Iterable<Point2i> {

    private final MutableLinearIterator linearIterator;

    public LineIterator2i() {
        linearIterator = new MutableLinearIterator();
    }

    public void set(Point2i start, Point2i end, final boolean includeFirst) {
        int[] startArray = {start.x, start.y};
        int[] endArray = {end.x, end.y};
        linearIterator.set(startArray, endArray, includeFirst);
    }

    public int nRemainingSteps() {
        return linearIterator.nRemainingSteps();
    }

    @Override
    public Iterator<Point2i> iterator() {
        Iterator<Point2i> it = new Iterator<Point2i>() {
            private Iterator<int[]> linearIt = linearIterator.iterator();

            @Override
            public boolean hasNext() {
                return linearIt.hasNext();
            }

            @Override
            public Point2i next() {
                int[] next = linearIt.next();
                return new Point2i(next[0], next[1]);
            }

            @Override
            public void remove() {
                linearIt.remove();
            }
        };

        return it;
    }
}
