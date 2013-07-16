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

public class GeographicToCartesianTransform extends SphericalToCartesianTransform {

    private final double HALF_PI = 0.5 * Math.PI;
    private final double[] tempSource = new double[3];
    private final double[] tempTarget = new double[3];
    private final InverseRealTransform inverse;

    public GeographicToCartesianTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(double[] source, double[] target) {
        geographicToSpherical(source[0], source[1], source[2], tempSource);
        super.apply(tempSource, target);
    }

    @Override
    public void apply(float[] source, float[] target) {
        geographicToSpherical(source[0], source[1], source[2], tempSource);
        super.apply(tempSource, tempTarget);
        for (int i = 0; i < tempTarget.length; ++i) {
            target[i] = (float) tempTarget[i];
        }
    }

    @Override
    public void apply(RealLocalizable source, RealPositionable target) {
        source.localize(tempSource);
        geographicToSpherical(tempSource[0], tempSource[1], tempSource[2], tempSource);
        super.apply(tempSource, tempTarget);
        target.setPosition(tempTarget);
    }

    private void geographicToSpherical(final double radius, final double polar, final double azimuth, final double[] target) {
        target[0] = radius;
        target[1] = polar + HALF_PI;
        target[2] = azimuth + Math.PI;
    }

    private void sphericalToGeographic(final double radius, final double polar, final double azimuth, final double[] target) {
        target[0] = radius;
        target[1] = polar - HALF_PI;
        target[2] = azimuth - Math.PI;
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
