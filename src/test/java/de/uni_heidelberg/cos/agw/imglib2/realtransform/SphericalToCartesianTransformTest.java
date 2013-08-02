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

public class SphericalToCartesianTransformTest {

    protected final double PREC_DOUBLE = 1E-14;
    protected final float PREC_FLOAT = 1E-5f;
    private double[][] sphericalToCartesianCases, cartesianToSphericalCases;

    @Before
    public void setUp() {
        sphericalToCartesianCases = new double[][]{
                {-5, -3 * Math.PI, 0}, {0, 0, 5},
                {0, -3 * Math.PI, 0}, {0, 0, 0},
                {5, -3 * Math.PI, 0}, {0, 0, -5},
                {-5, -0.5 * Math.PI, 0}, {5, 0, 0},
                {0, -0.5 * Math.PI, 0}, {0, 0, 0},
                {5, -0.5 * Math.PI, 0}, {-5, 0, 0},
                {-5, 0, 0}, {0, 0, -5},
                {0, 0, 0}, {0, 0, 0},
                {5, 0, 0}, {0, 0, 5},
                {-5, 0.5 * Math.PI, 0}, {-5, 0, 0},
                {0, 0.5 * Math.PI, 0}, {0, 0, 0},
                {5, 0.5 * Math.PI, 0}, {5, 0, 0},
                {-5, Math.PI, 0}, {0, 0, 5},
                {0, Math.PI, 0}, {0, 0, 0},
                {5, Math.PI, 0}, {0, 0, -5},
                {-5, 1.5 * Math.PI, 0}, {5, 0, 0},
                {0, 1.5 * Math.PI, 0}, {0, 0, 0},
                {5, 1.5 * Math.PI, 0}, {-5, 0, 0},
                {-5, 2 * Math.PI, 0}, {0, 0, -5},
                {0, 2 * Math.PI, 0}, {0, 0, 0},
                {5, 2 * Math.PI, 0}, {0, 0, 5},
                {-5, 3 * Math.PI, 0}, {0, 0, 5},
                {0, 3 * Math.PI, 0}, {0, 0, 0},
                {5, 3 * Math.PI, 0}, {0, 0, -5},
                {-5, 0, Math.PI}, {0, 0, -5},
                {0, 0, 1.5 * Math.PI}, {0, 0, 0},
                {5, 0, 2 * Math.PI}, {0, 0, 5},
                {5, 0, 3 * Math.PI}, {0, 0, 5},
                {-5, Math.PI, Math.PI}, {0, 0, 5},
                {-5, 0.5 * Math.PI, -3 * Math.PI}, {5, 0, 0},
                {-5, 0.5 * Math.PI, 0.5 * Math.PI}, {0, -5, 0},
                {5, 0.25 * Math.PI, 0.25 * Math.PI}, {2.5, 2.5, 3.5355339059327373},
                {5, 02.5 * Math.PI, -0.75 * Math.PI}, {-3.5355339059327373, -3.5355339059327373, 0}
        };

        cartesianToSphericalCases = new double[][]{
                {2.5, 2.5, 3.5355339059327373}, {5, 0.25 * Math.PI, 0.25 * Math.PI},
                {0, 0, 0}, {0, 0, 0},
                {0, 0, -5}, {5, 0, Math.PI},
                {0, 0, 5}, {5, 0, 0},
                {0, -5, 0}, {5, -0.5*Math.PI, 0.5*Math.PI},
                {0, 5, 0}, {5, 0.5*Math.PI, 0.5*Math.PI},
                {-5, 0, 0}, {5, Math.PI, 0.5*Math.PI},
                {5, 0, 0}, {5, 0, 0.5*Math.PI}
        };
    }

    @Test
    public void testApply_floatArr_floatArr() {
        System.out.println("apply(float[], float[])");
        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (int i = 0; i < sphericalToCartesianCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                source[j] = (float) sphericalToCartesianCases[i][j];
                truth[j] = (float) sphericalToCartesianCases[i + 1][j];
            }
            instance.apply(source, target);
            Assert.assertArrayEquals(truth, target, PREC_FLOAT);
        }
    }

    @Test
    public void testApply_doubleArr_doubleArr() {
        System.out.println("apply(double[], double[])");
        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < sphericalToCartesianCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                source[j] = sphericalToCartesianCases[i][j];
                truth[j] = sphericalToCartesianCases[i + 1][j];
            }
            instance.apply(source, target);
            Assert.assertArrayEquals(truth, target, PREC_DOUBLE);
        }
    }

    @Test
    public void testApply_RealLocalizable_RealPositionable() {
        System.out.println("apply(RealLocalizable, RealPositionable)");
        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
        final double[] targetArray = new double[instance.numTargetDimensions()];
        for (int i = 0; i < sphericalToCartesianCases.length; i += 2) {
            ((RealPoint) source).setPosition(sphericalToCartesianCases[i]);
            instance.apply(source, target);
            ((RealPoint) target).localize(targetArray);
            Assert.assertArrayEquals(sphericalToCartesianCases[i + 1], targetArray, PREC_DOUBLE);
        }
    }

    @Test
    public void testApplyInverse_floatArr_floatArr() {
        System.out.println("applyInverse(float[], float[])");
        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numSourceDimensions()];
        for (int i = 0; i < cartesianToSphericalCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                target[j] = (float) cartesianToSphericalCases[i][j];
                truth[j] = (float) cartesianToSphericalCases[i + 1][j];
            }
            instance.applyInverse(source, target);
            Assert.assertArrayEquals(truth, source, PREC_FLOAT);
        }
    }

    @Test
    public void testApplyInverse_doubleArr_doubleArr() {
        System.out.println("applyInverse(double[], double[])");
        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numSourceDimensions()];
        for (int i = 0; i < cartesianToSphericalCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                target[j] = cartesianToSphericalCases[i][j];
                truth[j] = cartesianToSphericalCases[i + 1][j];
            }
            instance.applyInverse(source, target);
            Assert.assertArrayEquals(truth, source, PREC_DOUBLE);
        }
    }

    @Test
    public void testApplyInverse_RealPositionable_RealLocalizable() {
        System.out.println("applyInverse(RealLocalizable, RealPositionable)");
        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
        final RealPositionable source = new RealPoint(instance.numSourceDimensions());
        final RealLocalizable target = new RealPoint(instance.numTargetDimensions());
        final double[] sourceArray = new double[instance.numTargetDimensions()];
        for (int i = 0; i < cartesianToSphericalCases.length; i += 2) {
            ((RealPoint) target).setPosition(cartesianToSphericalCases[i]);
            instance.applyInverse(source, target);
            ((RealPoint) source).localize(sourceArray);
            Assert.assertArrayEquals(cartesianToSphericalCases[i + 1], sourceArray, PREC_DOUBLE);
        }
    }
}