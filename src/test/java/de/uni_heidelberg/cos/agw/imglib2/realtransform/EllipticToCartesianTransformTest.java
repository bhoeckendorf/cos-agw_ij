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
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EllipticToCartesianTransformTest {

    protected final double PREC_DOUBLE = 1E-14;
    protected final float PREC_FLOAT = 1E-5f;
    protected double[][] ellipticToCartesianCases, cartesianToEllipticCases;

    @Before
    public void setUp() {
        // test case, followed by true result
        ellipticToCartesianCases = new double[][]{
                {0, 0}, {0, 0}
        };

        // test case, followed by true result
        cartesianToEllipticCases = new double[][]{
                {0, 0}, {0, 0}
        };
    }

    @Test
    public void testApply_floatArr_floatArr() {
        System.out.println("apply(float[], float[])");
        final EllipticToCartesianTransform instance = new EllipticToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (int i = 0; i < ellipticToCartesianCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                source[j] = (float) ellipticToCartesianCases[i][j];
                truth[j] = (float) ellipticToCartesianCases[i + 1][j];
            }
            instance.apply(source, target);
            Assert.assertArrayEquals(truth, target, PREC_FLOAT);
        }
    }

    @Test
    public void testApply_doubleArr_doubleArr() {
        System.out.println("apply(double[], double[])");
        final EllipticToCartesianTransform instance = new EllipticToCartesianTransform();
        final double[] target = new double[instance.numTargetDimensions()];
        for (int i = 0; i < ellipticToCartesianCases.length; i += 2) {
            instance.apply(ellipticToCartesianCases[i], target);
            Assert.assertArrayEquals(ellipticToCartesianCases[i + 1], target, PREC_DOUBLE);
        }
    }

    @Test
    public void testApply_RealLocalizable_RealPositionable() {
        System.out.println("apply(RealLocalizable, RealPositionable)");
        final EllipticToCartesianTransform instance = new EllipticToCartesianTransform();
        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
        final double[] targetArray = new double[instance.numTargetDimensions()];
        for (int i = 0; i < ellipticToCartesianCases.length; i += 2) {
            ((RealPoint) source).setPosition(ellipticToCartesianCases[i]);
            instance.apply(source, target);
            ((RealPoint) target).localize(targetArray);
            Assert.assertArrayEquals(ellipticToCartesianCases[i + 1], targetArray, PREC_DOUBLE);
        }
    }
}