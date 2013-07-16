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
package de.uni_heidelberg.cos.agw.imglib2;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

public class LineIntensityProjector<T extends NumericType<T>> {

    private final RealRandomAccess<T> ra;
    private final int nDimensions;
    private final double[] m_start, m_end, m_step;
    private int nSteps;

    public LineIntensityProjector(RandomAccessibleInterval<T> img, InterpolatorFactory interpolation) {
        ra = Views.interpolate(Views.extendZero(img), interpolation).realRandomAccess();
        nDimensions = img.numDimensions();
        m_start = new double[nDimensions];
        m_end = new double[nDimensions];
        m_step = new double[nDimensions];
    }

    public void set(final double[] start, final double[] end) {
        for (int d = 0; d < nDimensions; ++d) {
            m_start[d] = start[d];
            m_end[d] = end[d];
        }
        computeStep(m_start, m_end);
    }

    public void set(final Localizable start, final Localizable end) {
        for (int d = 0; d < nDimensions; ++d) {
            m_start[d] = start.getDoublePosition(d);
            m_end[d] = end.getDoublePosition(d);
        }
        computeStep(m_start, m_end);
    }

    public void set(final RealLocalizable start, final RealLocalizable end) {
        for (int d = 0; d < nDimensions; ++d) {
            m_start[d] = start.getDoublePosition(d);
            m_end[d] = end.getDoublePosition(d);
        }
        computeStep(m_start, m_end);
    }

    private void computeStep(final double[] start, final double[] end) {
        for (int i = 0; i < nDimensions; ++i) {
            m_step[i] = end[i] - this.m_start[i];
        }
        double maxValue = m_step[0] > 0 ? m_step[0] : -m_step[0];
        for (int i = 1; i < nDimensions; ++i) {
            double nextValue = m_step[i] > 0 ? m_step[i] : -m_step[i];
            if (nextValue > maxValue) {
                maxValue = nextValue;
            }
        }
        for (int i = 0; i < nDimensions; ++i) {
            m_step[i] /= maxValue;
        }
        computeNSteps();
    }

    private void computeNSteps() {
        nSteps = Integer.MIN_VALUE;
        for (int i = 0; i < nDimensions; ++i) {
            if (m_step[i] == 0) {
                continue;
            }
            int next = (int) ((m_end[i] - m_start[i]) / m_step[i]);
            if (next < 0) {
                next = -next;
            }
            if (next > nSteps) {
                nSteps = next;
            }
        }
    }

    public int getNSteps() {
        return nSteps;
    }

    // This method returns null if no point of the line is within the volume.
    public T compute(final BinaryOperation<T, T, T> op) {
        T value = null;
        int currentStep = 0;
        ra.setPosition(m_start);

        // Find first point within volume, get its value as initial value.
        do {
            try {
                value = ra.get().copy();
            } catch (ArrayIndexOutOfBoundsException ex) {
                ra.move(m_step);
                currentStep++;
            }
        } while (value == null && currentStep < nSteps);

        // Handle subsequent points within volume, break when reaching boundary.
        while (currentStep < nSteps) {
            try {
                currentStep++;
                ra.move(m_step);
                T next = ra.get();
                op.compute(next, value, value);
            } catch (ArrayIndexOutOfBoundsException ex) {
                break;
            }
        }
        return value;
    }
}