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

public class EllipticCylindricalToCartesianTransformTest {

    private EllipticToCartesianTransformTest ellipticToCartesianTransformTest;
    private double[] heightCases;

    @Before
    public void setUp() {
        ellipticToCartesianTransformTest = new EllipticToCartesianTransformTest();
        ellipticToCartesianTransformTest.setUp();
        heightCases = new double[]{-5, 0, 5};
    }

    @Test
    public void testApply_floatArr_floatArr() {
        System.out.println("apply(float[], float[])");
        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (final double height : heightCases) {
            for (int i = 0; i < ellipticToCartesianTransformTest.ellipticToCartesianCases.length; i += 2) {
                for (int j = 0; j < 2; ++j) {
                    source[j] = (float) ellipticToCartesianTransformTest.ellipticToCartesianCases[i][j];
                    truth[j] = (float) ellipticToCartesianTransformTest.ellipticToCartesianCases[i + 1][j];
                }
                source[2] = (float) height;
                truth[2] = (float) height;
                instance.apply(source, target);
                Assert.assertArrayEquals(truth, target, ellipticToCartesianTransformTest.PREC_FLOAT);
            }
        }
    }

    @Test
    public void testApply_doubleArr_doubleArr() {
        System.out.println("apply(double[], double[])");
        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (final double height : heightCases) {
            for (int i = 0; i < ellipticToCartesianTransformTest.ellipticToCartesianCases.length; i += 2) {
                for (int j = 0; j < 2; ++j) {
                    source[j] = ellipticToCartesianTransformTest.ellipticToCartesianCases[i][j];
                    truth[j] = ellipticToCartesianTransformTest.ellipticToCartesianCases[i + 1][j];
                }
                source[2] = height;
                truth[2] = height;
                instance.apply(source, target);
                Assert.assertArrayEquals(truth, target, ellipticToCartesianTransformTest.PREC_DOUBLE);
            }
        }
    }

    @Test
    public void testApply_RealLocalizable_RealPositionable() {
        System.out.println("apply(RealLocalizable, RealPositionable)");
        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
        final double[] targetArray = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (final double height : heightCases) {
            for (int i = 0; i < ellipticToCartesianTransformTest.ellipticToCartesianCases.length; i += 2) {
                for (int j = 0; j < 2; ++j) {
                    ((RealPoint) source).setPosition(ellipticToCartesianTransformTest.ellipticToCartesianCases[i][j], j);
                    truth[j] = ellipticToCartesianTransformTest.ellipticToCartesianCases[i + 1][j];
                }
                ((RealPoint) source).setPosition(height, 2);
                truth[2] = height;
                instance.apply(source, target);
                ((RealPoint) target).localize(targetArray);
                Assert.assertArrayEquals(truth, targetArray, ellipticToCartesianTransformTest.PREC_DOUBLE);
            }
        }
    }
}