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

public class EquirectangularToCartesianTransform extends EquirectangularToSphericalTransform {

    private final double[] temp0 = new double[3];
    private final double[] temp1 = new double[3];
    private final SphericalToCartesianTransform sphericalToCartesianTransform;

    public EquirectangularToCartesianTransform(final double innerRadius, final double outerRadius, final double standardRadiusOffset, final double scale) {
        super(innerRadius, outerRadius, standardRadiusOffset, scale);
        sphericalToCartesianTransform = new SphericalToCartesianTransform();
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        super.apply(source, temp0);
        sphericalToCartesianTransform.apply(temp0, target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        for (int d = 0; d < temp0.length; ++d) {
            temp0[d] = source[d];
        }
        super.apply(temp0, temp1);
        sphericalToCartesianTransform.apply(temp1, temp0);
        for (int i = 0; i < temp0.length; ++i) {
            target[i] = (float) temp0[i];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        source.localize(temp0);
        super.apply(temp0, temp1);
        sphericalToCartesianTransform.apply(temp1, temp0);
        target.setPosition(temp0);
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
    public RealTransform copy() {
        return this;
    }
}
