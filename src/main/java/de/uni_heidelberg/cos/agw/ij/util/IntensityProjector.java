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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/* TODO: Are 2 separate methods required for 2D and 3D? Is there a significant
 * performance difference?
 */
public class IntensityProjector {

    private final ImagePlus imp;
    private final ImageStack stack;
    private final LinearIterator linearIterator;
    private boolean is3d;

    public IntensityProjector(ImagePlus imp) {
        this.imp = imp;
        stack = this.imp.getStack();
        linearIterator = new LinearIterator();
    }

    public void set(int[] start, int[] end) {
        if (start.length > 3 || end.length > 3) {
            throw new IllegalArgumentException("IntensityProjector: Max 3 dimensions.");
        }
        linearIterator.set(start, end, true);
        is3d = (start.length == 3) ? true : false;
    }

    public int getMaximum() {
        if (is3d) {
            return getMaximum3D();
        } else {
            return getMaximum2D();
        }
    }

    private int getMaximum2D() {
        ImageProcessor ip = imp.getProcessor();
        int max = Integer.MIN_VALUE;
        for (int[] point : linearIterator) {
            int next = ip.getPixel(point[0], point[1]);
            if (next > max) {
                max = next;
            }
        }
        return max;
    }

    private int getMaximum3D() {
        ImageProcessor ip;
        int max = Integer.MIN_VALUE;
        for (int[] point : linearIterator) {
            ip = stack.getProcessor(point[2]);
            int next = ip.getPixel(point[0], point[1]);
            if (next > max) {
                max = next;
            }
        }
        return max;
    }

    public int getMinimum() {
        if (is3d) {
            return getMaximum3D();
        } else {
            return getMaximum2D();
        }
    }

    private int getMinimum2D() {
        ImageProcessor ip = imp.getProcessor();
        int min = Integer.MAX_VALUE;
        for (int[] point : linearIterator) {
            int next = ip.getPixel(point[0], point[1]);
            if (next < min) {
                min = next;
            }
        }
        return min;
    }

    private int getMinimum3D() {
        ImageProcessor ip;
        int min = Integer.MAX_VALUE;
        for (int[] point : linearIterator) {
            ip = stack.getProcessor(point[2]);
            int next = ip.getPixel(point[0], point[1]);
            if (next < min) {
                min = next;
            }
        }
        return min;
    }

    public double getMean() {
        if (is3d) {
            return getMean3D();
        } else {
            return getMean2D();
        }
    }

    private double getMean2D() {
        ImageProcessor ip = imp.getProcessor();
        double sum = 0;
        int n = 0;
        for (int[] point : linearIterator) {
            sum += ip.getPixel(point[0], point[1]);
            n += 1;
        }
        return sum / n;
    }

    private double getMean3D() {
        ImageProcessor ip;
        double sum = 0;
        int n = 0;
        for (int[] point : linearIterator) {
            ip = stack.getProcessor(point[2]);
            sum += ip.getPixel(point[0], point[1]);
            n += 1;
        }
        return sum / n;
    }
}
