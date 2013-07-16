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

public class PolarToCartesianTransformTest {

    protected final boolean BE_VERBOSE = false;
    protected final double PREC_DOUBLE = 1E-14;
    protected final float PREC_FLOAT = 1E-5f;
    protected double[][] polarToCartesianCases, cartesianToPolarCases;

    @Before
    public void setUp() {
        // test case, followed by true result
        polarToCartesianCases = new double[][]{
            {-10, -3 * Math.PI}, {10, 0},
            {-5, -3 * Math.PI}, {5, 0},
            {0, -3 * Math.PI}, {0, 0},
            {5, -3 * Math.PI}, {-5, 0},
            {10, -3 * Math.PI}, {-10, 0},
            {-10, -0.5 * Math.PI}, {0, 10},
            {-5, -0.5 * Math.PI}, {0, 5},
            {0, -0.5 * Math.PI}, {0, 0},
            {5, -0.5 * Math.PI}, {0, -5},
            {10, -0.5 * Math.PI}, {0, -10},
            {-10, 0}, {-10, 0},
            {-5, 0}, {-5, 0},
            {0, 0}, {0, 0},
            {5, 0}, {5, 0},
            {10, 0}, {10, 0},
            {-10, 0.5 * Math.PI}, {0, -10},
            {-5, 0.5 * Math.PI}, {0, -5},
            {0, 0.5 * Math.PI}, {0, 0},
            {5, 0.5 * Math.PI}, {0, 5},
            {10, 0.5 * Math.PI}, {0, 10},
            {-10, Math.PI}, {10, 0},
            {-5, Math.PI}, {5, 0},
            {0, Math.PI}, {0, 0},
            {5, Math.PI}, {-5, 0},
            {10, Math.PI}, {-10, 0},
            {-10, 1.5 * Math.PI}, {0, 10},
            {-5, 1.5 * Math.PI}, {0, 5},
            {0, 1.5 * Math.PI}, {0, 0},
            {5, 1.5 * Math.PI}, {0, -5},
            {10, 1.5 * Math.PI}, {0, -10},
            {-10, 2 * Math.PI}, {-10, 0},
            {-5, 2 * Math.PI}, {-5, 0},
            {0, 2 * Math.PI}, {0, 0},
            {5, 2 * Math.PI}, {5, 0},
            {10, 2 * Math.PI}, {10, 0},
            {-10, 3 * Math.PI}, {10, 0},
            {-5, 3 * Math.PI}, {5, 0},
            {0, 3 * Math.PI}, {0, 0},
            {5, 3 * Math.PI}, {-5, 0},
            {10, 3 * Math.PI}, {-10, 0},
            {10, 0.25 * Math.PI}, {7.0710678118654755, 7.0710678118654755},
            {10, 0.75 * Math.PI}, {-7.0710678118654755, 7.0710678118654755},
            {10, 1.25 * Math.PI}, {-7.0710678118654755, -7.0710678118654755},
            {10, 1.75 * Math.PI}, {7.0710678118654755, -7.0710678118654755}
        };

        // test case, followed by true result
        cartesianToPolarCases = new double[][]{
            {-10, 0}, {10, Math.PI},
            {-5, 0}, {5, Math.PI},
            {0, 0}, {0, 0},
            {5, 0}, {5, 0},
            {10, 0}, {10, 0},
            {0, -10}, {10, 1.5 * Math.PI},
            {0, -5}, {5, 1.5 * Math.PI},
            {0, 5}, {5, 0.5 * Math.PI},
            {0, 10}, {10, 0.5 * Math.PI},
            {7.0710678118654755, 7.0710678118654755}, {10, 0.25 * Math.PI},
            {-7.0710678118654755, 7.0710678118654755}, {10, 0.75 * Math.PI},
            {-7.0710678118654755, -7.0710678118654755}, {10, 1.25 * Math.PI},
            {7.0710678118654755, -7.0710678118654755}, {10, 1.75 * Math.PI}
        };
    }

    private void logTestCase(final double radius, final double azimuth, final double x, final double y, final boolean forward, final double expectation0, final double expectation1) {
        if (!BE_VERBOSE) {
            return;
        }
        final String polar = String.format("polar: %.2f, %.2f", radius, azimuth);
        final String cartesian = String.format("cartesian: %.2f, %.2f", x, y);
        final String expectation = String.format("expected: %.2f, %.2f", expectation0, expectation1);
        if (forward) {
            System.out.format("  %s > %s; %s\n", polar, cartesian, expectation);
        } else {
            System.out.format("  %s > %s; %s\n", cartesian, polar, expectation);
        }
    }

    @Test
    public void testApply_floatArr_floatArr() {
        System.out.println("apply(float[], float[])");
        PolarToCartesianTransform instance = new PolarToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (int i = 0; i < polarToCartesianCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                source[j] = (float) polarToCartesianCases[i][j];
                truth[j] = (float) polarToCartesianCases[i + 1][j];
            }
            instance.apply(source, target);
            logTestCase(source[0], source[1], target[0], target[1], true, truth[0], truth[1]);
            Assert.assertArrayEquals(truth, target, PREC_FLOAT);
        }
    }

    @Test
    public void testApply_doubleArr_doubleArr() {
        System.out.println("apply(double[], double[])");
        PolarToCartesianTransform instance = new PolarToCartesianTransform();
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < polarToCartesianCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                source[j] = polarToCartesianCases[i][j];
                truth[j] = polarToCartesianCases[i + 1][j];
            }
            instance.apply(source, target);
            logTestCase(source[0], source[1], target[0], target[1], true, truth[0], truth[1]);
            Assert.assertArrayEquals(truth, target, PREC_DOUBLE);
        }
    }

    @Test
    public void testApply_RealLocalizable_RealPositionable() {
        System.out.println("apply(RealLocalizable, RealPositionable)");
        PolarToCartesianTransform instance = new PolarToCartesianTransform();
        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
        final double[] targetArray = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < polarToCartesianCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                truth[j] = polarToCartesianCases[i + 1][j];
            }
            ((RealPoint) source).setPosition(polarToCartesianCases[i]);
            instance.apply(source, target);
            ((RealPoint) target).localize(targetArray);
            logTestCase(source.getDoublePosition(0), source.getDoublePosition(1), targetArray[0], targetArray[1], true, truth[0], truth[1]);
            Assert.assertArrayEquals(truth, targetArray, PREC_DOUBLE);
        }
    }

    @Test
    public void testApplyInverse_floatArr_floatArr() {
        System.out.println("applyInverse(float[], float[])");
        PolarToCartesianTransform instance = new PolarToCartesianTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numSourceDimensions()];
        for (int i = 0; i < cartesianToPolarCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                target[j] = (float) cartesianToPolarCases[i][j];
                truth[j] = (float) cartesianToPolarCases[i + 1][j];
            }
            instance.applyInverse(source, target);
            logTestCase(source[0], source[1], target[0], target[1], false, truth[0], truth[1]);
            Assert.assertArrayEquals(truth, source, PREC_FLOAT);
        }
    }

    @Test
    public void testApplyInverse_doubleArr_doubleArr() {
        System.out.println("applyInverse(double[], double[])");
        PolarToCartesianTransform instance = new PolarToCartesianTransform();
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numSourceDimensions()];
        for (int i = 0; i < cartesianToPolarCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                target[j] = cartesianToPolarCases[i][j];
                truth[j] = cartesianToPolarCases[i + 1][j];
            }
            instance.applyInverse(source, target);
            logTestCase(source[0], source[1], target[0], target[1], false, truth[0], truth[1]);
            Assert.assertArrayEquals(truth, source, PREC_DOUBLE);
        }
    }

    @Test
    public void testApplyInverse_RealPositionable_RealLocalizable() {
        System.out.println("applyInverse(RealPositionable, RealLocalizable)");
        PolarToCartesianTransform instance = new PolarToCartesianTransform();
        final RealPositionable source = new RealPoint(instance.numTargetDimensions());
        final RealLocalizable target = new RealPoint(instance.numSourceDimensions());
        final double[] sourceArray = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < cartesianToPolarCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                truth[j] = cartesianToPolarCases[i + 1][j];
            }
            ((RealPoint) target).setPosition(cartesianToPolarCases[i]);
            instance.applyInverse(source, target);
            ((RealPoint) source).localize(sourceArray);
            logTestCase(sourceArray[0], sourceArray[1], target.getDoublePosition(0), target.getDoublePosition(1), false, truth[0], truth[1]);
            Assert.assertArrayEquals(truth, sourceArray, PREC_DOUBLE);
        }
    }
}