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

public class EllipticCylindricalToCartesianTransform extends EllipticToCartesianTransform {

    private final InverseRealTransform inverse;

    public EllipticCylindricalToCartesianTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(double[] source, double[] target) {
        super.apply(source, target);
        target[2] = source[2];
    }

    @Override
    public void apply(float[] source, float[] target) {
        super.apply(source, target);
        target[2] = source[2];
    }

    @Override
    public void apply(RealLocalizable source, RealPositionable target) {
        super.apply(source, target);
        target.setPosition(source.getDoublePosition(2), 2);
    }

    @Override
    public void applyInverse(double[] source, double[] target) {
        super.applyInverse(source, target);
        source[2] = target[2];
    }

    @Override
    public void applyInverse(float[] source, float[] target) {
        super.applyInverse(source, target);
        source[2] = target[2];
    }

    @Override
    public void applyInverse(RealPositionable source, RealLocalizable target) {
        super.applyInverse(source, target);
        source.setPosition(target.getDoublePosition(2), 2);
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
