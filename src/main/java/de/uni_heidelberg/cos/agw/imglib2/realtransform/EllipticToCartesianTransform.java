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
package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;

public class EllipticToCartesianTransform implements InvertibleRealTransform {

    private double a = 0;
    private final double[] temp = new double[2];
    private final InverseRealTransform inverse;

    public EllipticToCartesianTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(double[] source, double[] target) {
        ellipticToCartesian(source[0], source[1], target);
    }

    @Override
    public void apply(float[] source, float[] target) {
        ellipticToCartesian(source[0], source[1], temp);
        for (int i = 0; i < temp.length; ++i) {
            target[i] = (float) temp[i];
        }
    }

    @Override
    public void apply(RealLocalizable source, RealPositionable target) {
        ellipticToCartesian(source.getDoublePosition(0), source.getDoublePosition(1), temp);
        // Manual copy prevents ArrayIndexOutOfBoundsException when target has 3 dimensions,
        // as in EllipticCylindricalCoordinates.
        for (int i = 0; i < temp.length; ++i) {
            target.setPosition(temp[i], i);
        }
    }

    @Override
    public void applyInverse(double[] source, double[] target) {
        cartesianToElliptic(target[0], target[1], source);
    }

    @Override
    public void applyInverse(float[] source, float[] target) {
        cartesianToElliptic(target[0], target[1], temp);
        for (int i = 0; i < temp.length; ++i) {
            source[i] = (float) temp[i];
        }
    }

    @Override
    public void applyInverse(RealPositionable source, RealLocalizable target) {
        cartesianToElliptic(target.getDoublePosition(0), target.getDoublePosition(1), temp);
        // Manual copy prevents ArrayIndexOutOfBoundsException when target has 3 dimensions,
        // as in EllipticCylindricalCoordinates.
        for (int i = 0; i < temp.length; ++i) {
            source.setPosition(temp[i], i);
        }
    }

    // m = something like radius, v = perimeter position (0 -- 2pi)
    private void ellipticToCartesian(final double m, final double v, final double[] target) {
        target[0] = a * Math.cosh(m) * Math.cos(v);
        target[1] = a * Math.sinh(m) * Math.sin(v);
    }

    private void cartesianToElliptic(final double x, final double y, final double[] target) {
        throw new UnsupportedOperationException("EllipticToCartesianTransform.applyInverse(...) is unsupported.");
    }

    @Override
    public int numSourceDimensions() {
        return 2;
    }

    @Override
    public int numTargetDimensions() {
        return 2;
    }

    @Override
    public InvertibleRealTransform inverse() {
        return inverse;
    }

    @Override
    public InvertibleRealTransform copy() {
        return this;
    }
}
