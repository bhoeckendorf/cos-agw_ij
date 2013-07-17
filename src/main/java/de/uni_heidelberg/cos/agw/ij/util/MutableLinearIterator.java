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

public class MutableLinearIterator implements Iterable<int[]> {

    protected int[] currentGrid, end;
    protected double[] currentSubgrid, step;

    public void set(int[] start, int[] end, final boolean includeFirst) {
        set(start, end);
        if (includeFirst) {
            oneStepBack();
        }
    }

    private void set(int[] start, int[] end) {
        if (start.length == 0 || start.length != end.length) {
            throw new IllegalArgumentException("Arrays must not be empty or differ in length.");
        }
        currentGrid = start.clone();
        currentSubgrid = new double[currentGrid.length];
        for (int i = 0; i < currentGrid.length; ++i) {
            currentSubgrid[i] = currentGrid[i];
        }
        this.end = end.clone();
        step = getStep();
    }

    private double[] getStep() {
        double[] step = new double[currentGrid.length];
        for (int i = 0; i < step.length; ++i) {
            step[i] = end[i] - currentGrid[i];
        }
        double maxValue = step[0] > 0 ? step[0] : -step[0];
        for (int i = 1; i < step.length; ++i) {
            double nextValue = step[i] > 0 ? step[i] : -step[i];
            if (nextValue > maxValue) {
                maxValue = nextValue;
            }
        }
        for (int i = 0; i < step.length; ++i) {
            step[i] /= maxValue;
        }
        return step;
    }

    public int nRemainingSteps() {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < currentGrid.length; ++i) {
            int next = (int) ((end[0] - currentGrid[0]) / step[0]);
            if (next > max) {
                max = next;
            }
        }
        return max;
    }

    private int[] oneStepForward() {
        for (int i = 0; i < currentSubgrid.length; ++i) {
            currentSubgrid[i] += step[i];
            currentGrid[i] = (int) Math.round(currentSubgrid[i]);
        }
        return currentGrid;
    }

    private int[] oneStepBack() {
        for (int i = 0; i < currentSubgrid.length; ++i) {
            currentSubgrid[i] -= step[i];
            currentGrid[i] = (int) Math.round(currentSubgrid[i]);
        }
        return currentGrid;
    }

    @Override
    public Iterator<int[]> iterator() {
        Iterator<int[]> it = new Iterator<int[]>() {
            @Override
            public boolean hasNext() {
                for (int i = 0; i < currentGrid.length; ++i) {
                    if (currentGrid[i] != end[i]) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int[] next() {
                for (int i = 0; i < currentSubgrid.length; ++i) {
                    currentSubgrid[i] += step[i];
                    currentGrid[i] = (int) Math.round(currentSubgrid[i]);
                }
                return currentGrid;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove() is not supported.");
            }
        };

        return it;
    }
}