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
import net.imglib2.realtransform.RealTransform;

public class CylindricalToCartesianIntervalTransform extends CartesianIntervalToCylindricalTransform {

    private final double[] temp0 = new double[3];
    private final double[] temp1 = new double[3];
    private final CylindricalToCartesianTransform cylindricalToCartesianTransform = new CylindricalToCartesianTransform();

    public CylindricalToCartesianIntervalTransform(final double cylinderHeight, final double innerRadius, final double outerRadius, final double stdRadiusOffset, final double scale) {
        super(cylinderHeight, innerRadius, outerRadius, stdRadiusOffset, scale);
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        super.apply(source, temp0);
        cylindricalToCartesianTransform.apply(temp0, target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        for (int d = 0; d < temp0.length; ++d) {
            temp0[d] = source[d];
        }
        super.apply(temp0, temp1);
        cylindricalToCartesianTransform.apply(temp1, temp0);
        for (int d = 0; d < temp0.length; ++d) {
            target[d] = (float) temp0[d];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        source.localize(temp0);
        super.apply(temp0, temp1);
        cylindricalToCartesianTransform.apply(temp1, temp0);
        target.setPosition(temp0);
    }

    @Override
    public RealTransform copy() {
        return this;
    }
}
