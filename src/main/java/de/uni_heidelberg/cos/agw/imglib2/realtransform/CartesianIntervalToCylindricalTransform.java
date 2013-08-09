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

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.RealTransform;

public class CartesianIntervalToCylindricalTransform implements RealTransform, Interval {

    private final double outerRadius, radiusInterval, scale;
    private final double TWO_PI = 2d * Math.PI;
    private final double[] temp = new double[3];
    private final long[] cylindricalDimensions = new long[3];

    public CartesianIntervalToCylindricalTransform(final double cylinderHeight, final double innerRadius, final double outerRadius, final double stdRadiusOffset, final double scale) {
        this.outerRadius = outerRadius;
        this.scale = scale;
        radiusInterval = this.outerRadius - innerRadius + 1;
        final double stdRadius = innerRadius + stdRadiusOffset * radiusInterval;
        cylindricalDimensions[0] = Math.round(this.scale * cylinderHeight);
        cylindricalDimensions[1] = Math.round(this.scale * stdRadius * TWO_PI);
        cylindricalDimensions[2] = Math.round(this.scale * radiusInterval);
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        cartesianIntervalToCylindrical(source[0], source[1], source[2], target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        cartesianIntervalToCylindrical(source[0], source[1], source[2], temp);
        for (int d = 0; d < temp.length; ++d) {
            target[d] = (float) temp[d];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        cartesianIntervalToCylindrical(source.getDoublePosition(0), source.getDoublePosition(1), source.getDoublePosition(2), temp);
        target.setPosition(temp);
    }

    private void cartesianIntervalToCylindrical(final double x, final double y, final double z, final double[] target) {
        target[0] = outerRadius - (z / cylindricalDimensions[2]) * radiusInterval;
        target[1] = (y / cylindricalDimensions[1]) * TWO_PI;
        target[2] = (x - 0.5d * cylindricalDimensions[0]) * (1 / scale);
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

    @Override
    public long min(final int d) {
        return 0;
    }

    @Override
    public void min(final long[] min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min[d] = 0;
        }
    }

    @Override
    public void min(final Positionable min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min.setPosition(0, d);
        }
    }

    @Override
    public long max(final int d) {
        return cylindricalDimensions[d] - 1;
    }

    @Override
    public void max(final long[] max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max[d] = cylindricalDimensions[d] - 1;
        }
    }

    @Override
    public void max(final Positionable max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max.setPosition(cylindricalDimensions[d] - 1, d);
        }
    }

    @Override
    public void dimensions(final long[] dimensions) {
        System.arraycopy(cylindricalDimensions, 0, dimensions, 0, cylindricalDimensions.length);
    }

    @Override
    public long dimension(final int d) {
        return cylindricalDimensions[d];
    }

    @Override
    public double realMin(final int d) {
        return 0;
    }

    @Override
    public void realMin(final double[] min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min[d] = 0;
        }
    }

    @Override
    public void realMin(final RealPositionable min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min.setPosition(0, d);
        }
    }

    @Override
    public double realMax(final int d) {
        return cylindricalDimensions[d] - 1;
    }

    @Override
    public void realMax(final double[] max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max[d] = cylindricalDimensions[d] - 1;
        }
    }

    @Override
    public void realMax(final RealPositionable max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max.setPosition(cylindricalDimensions[d] - 1, d);
        }
    }

    @Override
    public int numDimensions() {
        return 3;
    }
}
