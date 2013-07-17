/**
 * This file is part of the COS AGW ImageJ plugin bundle.
 * https://github.com/bhoeckendorf/cos-agw_ij
 *
 * Copyright 2012, 2013  B. Hoeckendorf <b.hoeckendorf at web dot de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni_heidelberg.cos.agw.ij.util;

import java.util.Iterator;
import javax.vecmath.Point3i;

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
