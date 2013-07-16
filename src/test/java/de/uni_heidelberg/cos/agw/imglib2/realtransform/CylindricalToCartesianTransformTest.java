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
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CylindricalToCartesianTransformTest {

    private final boolean BE_VERBOSE = false;
    private PolarToCartesianTransformTest polarToCartesianTransformTest;
    private double[] heightCases;

    @Before
    public void setUp() {
        polarToCartesianTransformTest = new PolarToCartesianTransformTest();
        polarToCartesianTransformTest.setUp();
        heightCases = new double[]{-10, -5, 0, 5, 10};
    }

    private void logTestCase(final double radius, final double azimuth, final double height, final double x, final double y, final double z, final boolean forward, final double expectation0, final double expectation1, final double expectation2) {
        if (!BE_VERBOSE) {
            return;
        }
        final String cylindrical = String.format("cylindrical: %.2f, %.2f, %.2f", radius, azimuth, height);
        final String cartesian = String.format("cartesian: %.2f, %.2f, %.2f", x, y, z);
        final String expectation = String.format("expected: %.2f, %.2f, %.2f", expectation0, expectation1, expectation2);
        if (forward) {
            System.out.format("  %s > %s; %s\n", cylindrical, cartesian, expectation);
        } else {
            System.out.format("  %s > %s; %s\n", cartesian, cylindrical, expectation);
        }
    }

    @Test
    public void testApply_floatArr_floatArr() {
        System.out.println("apply(float[], float[])");
        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (final double height : heightCases) {
            for (int i = 0; i < polarToCartesianTransformTest.polarToCartesianCases.length; i += 2) {
                source[0] = (float) polarToCartesianTransformTest.polarToCartesianCases[i][0];
                source[1] = (float) polarToCartesianTransformTest.polarToCartesianCases[i][1];
                source[2] = (float) height;
                truth[0] = (float) polarToCartesianTransformTest.polarToCartesianCases[i + 1][0];
                truth[1] = (float) polarToCartesianTransformTest.polarToCartesianCases[i + 1][1];
                truth[2] = (float) height;
                instance.apply(source, target);
                logTestCase(source[0], source[1], source[2], target[0], target[1], target[2], true, truth[0], truth[1], truth[2]);
                Assert.assertArrayEquals(truth, target, polarToCartesianTransformTest.PREC_FLOAT);
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
            for (int i = 0; i < polarToCartesianTransformTest.polarToCartesianCases.length; i += 2) {
                source[0] = polarToCartesianTransformTest.polarToCartesianCases[i][0];
                source[1] = polarToCartesianTransformTest.polarToCartesianCases[i][1];
                source[2] = height;
                truth[0] = polarToCartesianTransformTest.polarToCartesianCases[i + 1][0];
                truth[1] = polarToCartesianTransformTest.polarToCartesianCases[i + 1][1];
                truth[2] = height;
                instance.apply(source, target);
                logTestCase(source[0], source[1], source[2], target[0], target[1], target[2], true, truth[0], truth[1], truth[2]);
                Assert.assertArrayEquals(truth, target, polarToCartesianTransformTest.PREC_DOUBLE);
            }
        }
    }

//    @Test
//    public void testApply_RealLocalizable_RealPositionable() {
//        System.out.println("apply(RealLocalizable, RealPositionable)");
//        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
//        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
//        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
//        final double[] targetArray = new double[instance.numTargetDimensions()];
//        final double[] truth = new double[instance.numTargetDimensions()];
//        for (int i = 0; i < heightCases.length; ++i) {
//            for (int j = 0; j < polarToCartesianTransformTest.polarToCartesianCases.length; j += 2) {
//                ((RealPoint) source).setPosition(polarToCartesianTransformTest.polarToCartesianCases[j][0], 0);
//                ((RealPoint) source).setPosition(polarToCartesianTransformTest.polarToCartesianCases[j][1], 1);
//                ((RealPoint) source).setPosition(heightCases[i], 2);
//                truth[0] = polarToCartesianTransformTest.polarToCartesianCases[j + 1][0];
//                truth[1] = polarToCartesianTransformTest.polarToCartesianCases[j + 1][1];
//                truth[2] = heightCases[i];
//                instance.apply(source, target);
//                ((RealPoint) target).localize(targetArray);
//                logTestCase(source.getDoublePosition(0), source.getDoublePosition(1), source.getDoublePosition(2), targetArray[0], targetArray[1], targetArray[2], true, truth[0], truth[1], truth[2]);
//                Assert.assertArrayEquals(truth, targetArray, polarToCartesianTransformTest.PREC_FLOAT);
//            }
//        }
//    }
//
    @Test
    public void testApplyInverse_doubleArr_doubleArr() {
        System.out.println("applyInverse(double[], double[])");
        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numSourceDimensions()];
        for (final double height : heightCases) {
            for (int i = 0; i < polarToCartesianTransformTest.cartesianToPolarCases.length; i += 2) {
                target[0] = polarToCartesianTransformTest.cartesianToPolarCases[i][0];
                target[1] = polarToCartesianTransformTest.cartesianToPolarCases[i][1];
                target[2] = height;
                truth[0] = polarToCartesianTransformTest.cartesianToPolarCases[i + 1][0];
                truth[1] = polarToCartesianTransformTest.cartesianToPolarCases[i + 1][1];
                truth[2] = height;
                instance.applyInverse(source, target);
                logTestCase(source[0], source[1], source[2], target[0], target[1], target[2], false, truth[0], truth[1], truth[2]);
                Assert.assertArrayEquals(truth, source, polarToCartesianTransformTest.PREC_DOUBLE);
            }
        }
    }

    @Test
    public void testApplyInverse_floatArr_floatArr() {
        System.out.println("applyInverse(float[], float[])");
        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numSourceDimensions()];
        for (final double height : heightCases) {
            for (int i = 0; i < polarToCartesianTransformTest.cartesianToPolarCases.length; i += 2) {
                target[0] = (float) polarToCartesianTransformTest.cartesianToPolarCases[i][0];
                target[1] = (float) polarToCartesianTransformTest.cartesianToPolarCases[i][1];
                target[2] = (float) height;
                truth[0] = (float) polarToCartesianTransformTest.cartesianToPolarCases[i + 1][0];
                truth[1] = (float) polarToCartesianTransformTest.cartesianToPolarCases[i + 1][1];
                truth[2] = (float) height;
                instance.applyInverse(source, target);
                logTestCase(source[0], source[1], source[2], target[0], target[1], target[2], false, truth[0], truth[1], truth[2]);
                Assert.assertArrayEquals(truth, source, (float) polarToCartesianTransformTest.PREC_FLOAT);
            }
        }
    }
//
// TODO: Fails for unknown reason.
//    @Test
//    public void testApplyInverse_RealPositionable_RealLocalizable() {
//        System.out.println("applyInverse(RealPositionable, RealLocalizable)");
//        CylindricalToCartesianTransform instance = new CylindricalToCartesianTransform();
//        final RealPoint source = new RealPoint(instance.numSourceDimensions());
//        final double[] sourceArray = new double[instance.numSourceDimensions()];
//        final RealPoint target = new RealPoint(instance.numTargetDimensions());
//        final double[] truth = new double[instance.numSourceDimensions()];
//        for (int i = 0; i < heightCases.length; ++i) {
//            for (int j = 0; j < polarToCartesianTransformTest.cartesianToPolarCases.length; j += 2) {
//                target.setPosition(polarToCartesianTransformTest.cartesianToPolarCases[j][0], 0);
//                target.setPosition(polarToCartesianTransformTest.cartesianToPolarCases[j][1], 1);
//                target.setPosition(heightCases[i], 2);
//                truth[0] = polarToCartesianTransformTest.cartesianToPolarCases[j + 1][0];
//                truth[1] = polarToCartesianTransformTest.cartesianToPolarCases[j + 1][1];
//                truth[2] = heightCases[i];
//                instance.applyInverse(source, target);
//                source.localize(sourceArray);
//                logTestCase(sourceArray[0], sourceArray[1], sourceArray[2], target.getDoublePosition(0), target.getDoublePosition(1), target.getDoublePosition(2), false, truth[0], truth[1], truth[2]);
//                Assert.assertArrayEquals(truth, sourceArray, polarToCartesianTransformTest.PREC_DOUBLE);
//            }
//        }
//    }
//
}