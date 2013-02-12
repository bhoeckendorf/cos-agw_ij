/*
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
import javax.vecmath.Point2i;

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
