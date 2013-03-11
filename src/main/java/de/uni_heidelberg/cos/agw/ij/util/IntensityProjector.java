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

import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

public class IntensityProjector<T extends Type<T>> {

    private final RealRandomAccessible<T> img;
    private final RealRandomAccess<T> ra;
    private double[] start, step;
    private int nSteps;

    public IntensityProjector(RealRandomAccessible<T> img) {
        this.img = img;
        ra = this.img.realRandomAccess();
    }

    public IntensityProjector(RandomAccessibleInterval<T> img, InterpolatorFactory interpolation) {
        this.img = Views.interpolate(img, interpolation);
        ra = this.img.realRandomAccess();
    }

    public void set(final double[] start, final double[] end) {
        final int nDims = start.length;
        if (end.length != nDims) {
            throw new IllegalArgumentException("Number of dimensions of start and end don't match.");
        }
        if (start.length == 1 || start.length > 3 || end.length == 1 || end.length > 3) {
            throw new IllegalArgumentException("IntensityProjector works only in 2 and 3 dimensions.");
        }
        this.start = start.clone();
        step = getStep(start, end);
        nSteps = getNSteps(start, end, step);
    }

    private double[] getStep(final double[] start, final double[] end) {
        double[] step = new double[start.length];
        for (int i = 0; i < start.length; ++i) {
            step[i] = end[i] - this.start[i];
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

    private int getNSteps(final double[] start, final double[] end, final double[] step) {
        int nSteps = Integer.MIN_VALUE;
        for (int i = 0; i < start.length; ++i) {
            if (step[i] == 0) {
                continue;
            }
            int next = (int) ((end[i] - this.start[i]) / step[i]);
            if (next < 0) {
                next = -next;
            }
            if (next > nSteps) {
                nSteps = next;
            }
        }
        return nSteps;
    }

    public int getNSteps() {
        return nSteps;
    }

    public void set(final Localizable start, final Localizable end) {
        double[] startArray = new double[start.numDimensions()];
        start.localize(startArray);
        double[] endArray = new double[end.numDimensions()];
        end.localize(endArray);
        set(startArray, endArray);
    }

    //TODO: This method returns null if no point of the line is within the volume.
    public T compute(final BinaryOperation<T, T, T> op) {
        T value = null;
        int currentStep = 0;
        ra.setPosition(start);

        // Find first point within volume, get its value as initial value.
        do {
            try {
                value = ra.get().copy();
            } catch (ArrayIndexOutOfBoundsException ex) {
                ra.move(step);
                currentStep++;
            }
        } while (value == null && currentStep < nSteps);

        // Handle subsequent points within volume, break when reaching boundary.
        while (currentStep < nSteps) {
            try {
                currentStep++;
                ra.move(step);
                T next = ra.get();
                op.compute(next, value, value);
            } catch (ArrayIndexOutOfBoundsException ex) {
                break;
            }
        }
        return value;
    }
}