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
package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;

public class SphericalToCartesianTransform implements InvertibleRealTransform {

    private final double[] temp = new double[3];
    private final InverseRealTransform inverse;

    public SphericalToCartesianTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        sphericalToCartesian(source[0], source[1], source[2], temp);
        for (int i = 0; i < temp.length; ++i) {
            target[i] = (float) temp[i];
        }
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        sphericalToCartesian(source[0], source[1], source[2], target);
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        sphericalToCartesian(source.getDoublePosition(0), source.getDoublePosition(1), source.getDoublePosition(2), temp);
        target.setPosition(temp);
    }

    @Override
    public void applyInverse(final float[] source, final float[] target) {
        cartesianToSpherical(target[0], target[1], target[2], temp);
        for (int i = 0; i < temp.length; ++i) {
            source[i] = (float) temp[i];
        }
    }

    @Override
    public void applyInverse(final double[] source, final double[] target) {
        cartesianToSpherical(target[0], target[1], target[2], source);
    }

    @Override
    public void applyInverse(final RealPositionable source, final RealLocalizable target) {
        cartesianToSpherical(target.getDoublePosition(0), target.getDoublePosition(1), target.getDoublePosition(2), temp);
        source.setPosition(temp);
    }

    private void sphericalToCartesian(final double radius, final double polar, final double azimuth, final double[] target) {
        final double radiusSinPolar = radius * Math.sin(polar);
        target[0] = radiusSinPolar * Math.cos(azimuth);
        target[1] = radiusSinPolar * Math.sin(azimuth);
        target[2] = radius * Math.cos(polar);
    }

    private void cartesianToSpherical(final double x, final double y, final double z, final double[] target) {
        target[0] = Math.sqrt(x * x + y * y + z * z);
        target[1] = Math.atan2(y, x);
        target[2] = Math.acos(z / target[0]);
    }

    @Override
    public int numSourceDimensions() {
        return 3;
    }

    @Override
    public int numTargetDimensions() {
        return 3;
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
