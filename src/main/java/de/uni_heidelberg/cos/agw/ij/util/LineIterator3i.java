package de.uni_heidelberg.cos.agw.ij.util;

import javax.vecmath.Point3i;
import java.util.Iterator;

public class LineIterator3i implements Iterable<Point3i> {

    private final MutableLinearIterator linearIterator;

    public LineIterator3i() {
        linearIterator = new MutableLinearIterator();
    }

    public void set(Point3i start, Point3i end, final boolean includeFirst) {
        int[] startArray = {start.x, start.y, start.z};
        int[] endArray = {end.x, end.y, end.z};
        linearIterator.set(startArray, endArray, includeFirst);
    }

    public int nRemainingSteps() {
        return linearIterator.nRemainingSteps();
    }

    @Override
    public Iterator<Point3i> iterator() {
        Iterator<Point3i> it = new Iterator<Point3i>() {
            private Iterator<int[]> linearIt = linearIterator.iterator();

            @Override
            public boolean hasNext() {
                return linearIt.hasNext();
            }

            @Override
            public Point3i next() {
                int[] next = linearIt.next();
                return new Point3i(next[0], next[1], next[2]);
            }

            @Override
            public void remove() {
                linearIt.remove();
            }
        };

        return it;
    }
}
