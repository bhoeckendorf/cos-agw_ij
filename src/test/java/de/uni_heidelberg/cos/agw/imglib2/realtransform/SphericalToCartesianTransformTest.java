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

    public final double PREC_DOUBLE = 1E-14;
    public final float PREC_FLOAT = 1E-5f;
    private double[][] sphericalToCartesianCases, cartesianToSphericalCases;

    @Before
    public void setUp() {
        sphericalToCartesianCases = new double[][]{
            {-10, -3 * Math.PI, 0}, {0, 0, 10},
            {-5, -3 * Math.PI, 0}, {0, 0, 5},
            {0, -3 * Math.PI, 0}, {0, 0, 0},
            {5, -3 * Math.PI, 0}, {0, 0, -5},
            {10, -3 * Math.PI, 0}, {0, 0, -10},
            {-10, -0.5 * Math.PI, 0}, {10, 0, 0},
            {-5, -0.5 * Math.PI, 0}, {5, 0, 0},
            {0, -0.5 * Math.PI, 0}, {0, 0, 0},
            {5, -0.5 * Math.PI, 0}, {-5, 0, 0},
            {10, -0.5 * Math.PI, 0}, {-10, 0, 0},
            {-10, 0, 0}, {0, 0, -10},
            {-5, 0, 0}, {0, 0, -5},
            {0, 0, 0}, {0, 0, 0},
            {5, 0, 0}, {0, 0, 5},
            {10, 0, 0}, {0, 0, 10},
            {-10, 0.5 * Math.PI, 0}, {-10, 0, 0},
            {-5, 0.5 * Math.PI, 0}, {-5, 0, 0},
            {0, 0.5 * Math.PI, 0}, {0, 0, 0},
            {5, 0.5 * Math.PI, 0}, {5, 0, 0},
            {10, 0.5 * Math.PI, 0}, {10, 0, 0},
            {-10, Math.PI, 0}, {0, 0, 10},
            {-5, Math.PI, 0}, {0, 0, 5},
            {0, Math.PI, 0}, {0, 0, 0},
            {5, Math.PI, 0}, {0, 0, -5},
            {10, Math.PI, 0}, {0, 0, -10},
            {-10, 1.5 * Math.PI, 0}, {10, 0, 0},
            {-5, 1.5 * Math.PI, 0}, {5, 0, 0},
            {0, 1.5 * Math.PI, 0}, {0, 0, 0},
            {5, 1.5 * Math.PI, 0}, {-5, 0, 0},
            {10, 1.5 * Math.PI, 0}, {-10, 0, 0},
            {-10, 2 * Math.PI, 0}, {0, 0, -10},
            {-5, 2 * Math.PI, 0}, {0, 0, -5},
            {0, 2 * Math.PI, 0}, {0, 0, 0},
            {5, 2 * Math.PI, 0}, {0, 0, 5},
            {10, 2 * Math.PI, 0}, {0, 0, 10},
            {-10, 3 * Math.PI, 0}, {0, 0, 10},
            {-5, 3 * Math.PI, 0}, {0, 0, 5},
            {0, 3 * Math.PI, 0}, {0, 0, 0},
            {5, 3 * Math.PI, 0}, {0, 0, -5},
            {10, 3 * Math.PI, 0}, {0, 0, -10},
            {-10, 0, -3 * Math.PI}, {0, 0, -10},
            {-5, 0, Math.PI}, {0, 0, -5},
            {0, 0, 1.5 * Math.PI}, {0, 0, 0},
            {5, 0, 2 * Math.PI}, {0, 0, 5},
            {10, 0, 3 * Math.PI}, {0, 0, 10},
            {-10, Math.PI, Math.PI}, {0, 0, 10},
            {-5, 0.5 * Math.PI, -3 * Math.PI}, {5, 0, 0},
            {-5, 0.5 * Math.PI, 0.5 * Math.PI}, {0, -5, 0},
            {5, 0.25 * Math.PI, 0.25 * Math.PI}, {2.5, 2.5, 3.5355339059327373},
            {5, 02.5 * Math.PI, -0.75 * Math.PI}, {-3.5355339059327373, -3.5355339059327373, 0}
        };

        cartesianToSphericalCases = new double[][]{
            {2.5, 2.5, 3.5355339059327373}, {5, 0.25 * Math.PI, 0.25 * Math.PI},
            {0, 0, -5}, {5, Math.PI, 0},
            {0, 0, 0}, {0, 0, 0},
            {0, 0, 5}, {5, 0, 0},
            {0, 0, 10}, {10, 0, 0}
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
        final RealPositionable truth = new RealPoint(instance.numTargetDimensions());
        final double[] targetArray = new double[instance.numTargetDimensions()];
        final double[] truthArray = new double[instance.numTargetDimensions()];
        for (int i = 0; i < sphericalToCartesianCases.length; i += 2) {
            ((RealPoint) source).setPosition(sphericalToCartesianCases[i]);
            truth.setPosition(sphericalToCartesianCases[i + 1]);
            ((RealPoint) truth).localize(truthArray);
            instance.apply(source, target);
            ((RealPoint) target).localize(targetArray);
            Assert.assertArrayEquals(truthArray, targetArray, PREC_DOUBLE);
        }
    }
//
//    @Test
//    public void testApplyInverse_floatArr_floatArr() {
//        System.out.println("applyInverse(float[], float[])");
//        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
//        final float[] source = new float[instance.numSourceDimensions()];
//        final float[] target = new float[instance.numTargetDimensions()];
//        final float[] truth = new float[instance.numSourceDimensions()];
//        for (int i = 0; i < sphericalValues.length; ++i) {
//            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
//                target[j] = (float) cartesianValues[i][j];
//                truth[j] = (float) sphericalValues[i][j];
//            }
//            instance.applyInverse(source, target);
//            Assert.assertArrayEquals(truth, source, 0);
//        }
//    }
//
//    @Test
//    public void testApplyInverse_doubleArr_doubleArr() {
//        System.out.println("applyInverse(double[], double[])");
//        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
//        final double[] source = new double[instance.numSourceDimensions()];
//        final double[] target = new double[instance.numTargetDimensions()];
//        final double[] truth = new double[instance.numSourceDimensions()];
//        for (int i = 0; i < cartesianToSphericalCases.length; ++i) {
//            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
//                target[j] = cartesianToSphericalCases[i + 1][j];
//                truth[j] = cartesianToSphericalCases[i][j];
//            }
//            instance.applyInverse(source, target);
//            System.out.format("%.2f, %.2f, %.2f < %.2f, %.2f, %.2f | %.2f, %.2f, %.2f\n", source[0], source[1], source[2], target[0], target[1], target[2], truth[0], truth[1], truth[2]);
//            Assert.assertArrayEquals(truth, source, 0);
//        }
//    }
//
//    @Test
//    public void testApplyInverse_RealPositionable_RealLocalizable() {
//        System.out.println("applyInverse(RealLocalizable, RealPositionable)");
//        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
//        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
//        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
//        final RealPositionable truth = new RealPoint(instance.numSourceDimensions());
//        final double[] targetArray = new double[instance.numTargetDimensions()];
//        final double[] truthArray = new double[instance.numSourceDimensions()];
//        for (int i = 0; i < sphericalValues.length; ++i) {
//            target.setPosition(cartesianValues[i]);
//            ((RealPoint) source).setPosition(sphericalValues[i]);
//            truth.setPosition(sphericalValues[i]);
//            ((RealPoint) truth).localize(truthArray);
//            instance.applyInverse(source, target);
//            ((RealPoint) target).localize(targetArray);
//            Assert.assertArrayEquals(truthArray, targetArray, 0);
//        }
//    }
//
//    /**
//     * Test of applyInverse method, of class SphericalToCartesianTransform.
//     */
//    @Test
//    public void testApplyInverse_floatArr_floatArr() {
//        System.out.println("applyInverse");
//        float[] source = null;
//        float[] target = null;
//        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
//        instance.applyInverse(source, target);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of applyInverse method, of class SphericalToCartesianTransform.
//     */
//    @Test
//    public void testApplyInverse_doubleArr_doubleArr() {
//        System.out.println("applyInverse");
//        double[] source = null;
//        double[] target = null;
//        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
//        instance.applyInverse(source, target);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of applyInverse method, of class SphericalToCartesianTransform.
//     */
//    @Test
//    public void testApplyInverse_RealPositionable_RealLocalizable() {
//        System.out.println("applyInverse");
//        RealPositionable source = null;
//        RealLocalizable target = null;
//        SphericalToCartesianTransform instance = new SphericalToCartesianTransform();
//        instance.applyInverse(source, target);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}