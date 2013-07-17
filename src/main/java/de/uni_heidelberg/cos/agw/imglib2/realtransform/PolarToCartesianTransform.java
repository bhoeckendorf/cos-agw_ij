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

public class PolarToCartesianTransform implements InvertibleRealTransform {

    private final double TWO_PI = 2 * Math.PI;
    private final double[] tempTarget = new double[2];
    private final InverseRealTransform inverse;

    public PolarToCartesianTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        polarToCartesian(source[0], source[1], tempTarget);
        for (int i = 0; i < tempTarget.length; ++i) {
            target[i] = (float) tempTarget[i];
        }
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        polarToCartesian(source[0], source[1], target);
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        polarToCartesian(source.getDoublePosition(0), source.getDoublePosition(1), tempTarget);
        target.setPosition(tempTarget);
    }

    @Override
    public void applyInverse(final float[] source, final float[] target) {
        cartesianToPolar(target[0], target[1], tempTarget);
        for (int i = 0; i < tempTarget.length; ++i) {
            source[i] = (float) tempTarget[i];
        }
    }

    @Override
    public void applyInverse(final double[] source, final double[] target) {
        cartesianToPolar(target[0], target[1], source);
    }

    @Override
    public void applyInverse(final RealPositionable source, final RealLocalizable target) {
        cartesianToPolar(target.getDoublePosition(0), target.getDoublePosition(1), tempTarget);
        source.setPosition(tempTarget);
    }

    private void polarToCartesian(final double radius, final double azimuth, final double[] target) {
        target[0] = radius * Math.cos(azimuth);
        target[1] = radius * Math.sin(azimuth);
    }

    private void cartesianToPolar(final double x, final double y, final double[] target) {
        target[0] = Math.sqrt(x * x + y * y);
        if (x == 0 && y == 0) {
            target[1] = 0;
        } else if (x >= 0) {
            target[1] = Math.asin(y / target[0]);
        } else { // (x < 0)
            target[1] = -Math.asin(y / target[0]) + Math.PI;
        }

        if (target[1] < 0) {
            target[1] += TWO_PI;
        }
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
