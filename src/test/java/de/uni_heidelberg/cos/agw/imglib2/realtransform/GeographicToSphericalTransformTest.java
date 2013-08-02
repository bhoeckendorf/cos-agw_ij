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

public class GeographicToSphericalTransformTest {

    private final double PREC_DOUBLE = 1E-14;
    private final float PREC_FLOAT = 1E-5f;
    private double[][] geographicToSphericalCases, sphericalToGeographicCases;

    @Before
    public void setUp() {
        // TODO: Test both coordinates outside normal range, test negative radii.
        geographicToSphericalCases = new double[][]{
            // Test case followed by true result.
            // latitude within normal range (-90 -- 90), constant longitude
            {0, 0, 0}, {0, 0.5 * Math.PI, Math.PI},
            {5, Math.toRadians(90), 0}, {5, Math.toRadians(0), Math.PI},
            {5, Math.toRadians(60), 0}, {5, Math.toRadians(30), Math.PI},
            {5, Math.toRadians(30), 0}, {5, Math.toRadians(60), Math.PI},
            {5, Math.toRadians(0), 0}, {5, Math.toRadians(90), Math.PI},
            {5, Math.toRadians(-30), 0}, {5, Math.toRadians(120), Math.PI},
            {5, Math.toRadians(-60), 0}, {5, Math.toRadians(150), Math.PI},
            {5, Math.toRadians(-90), 0}, {5, Math.toRadians(180), Math.PI},

            // latitude outside normal range, constant longitude
            {5, Math.toRadians(390), 0}, {5, Math.toRadians(60), Math.PI},
            {5, Math.toRadians(360), 0}, {5, Math.toRadians(90), Math.PI},
            {5, Math.toRadians(330), 0}, {5, Math.toRadians(120), Math.PI},
            {5, Math.toRadians(300), 0}, {5, Math.toRadians(150), Math.PI},
            {5, Math.toRadians(270), 0}, {5, Math.toRadians(180), Math.PI},
            {5, Math.toRadians(240), 0}, {5, Math.toRadians(150), Math.PI},
            {5, Math.toRadians(210), 0}, {5, Math.toRadians(120), Math.PI},
            {5, Math.toRadians(120), 0}, {5, Math.toRadians(30), Math.PI},
            {5, Math.toRadians(-120), 0}, {5, Math.toRadians(150), Math.PI},
            {5, Math.toRadians(-210), 0}, {5, Math.toRadians(60), Math.PI},
            {5, Math.toRadians(-240), 0}, {5, Math.toRadians(30), Math.PI},
            {5, Math.toRadians(-270), 0}, {5, Math.toRadians(0), Math.PI},
            {5, Math.toRadians(-300), 0}, {5, Math.toRadians(30), Math.PI},
            {5, Math.toRadians(-360), 0}, {5, Math.toRadians(90), Math.PI},
            {5, Math.toRadians(-390), 0}, {5, Math.toRadians(120), Math.PI},

            // longitude within normal range (180 -- -180), latitude within normal range as above
            {5, Math.toRadians(90), Math.toRadians(180)}, {5, Math.toRadians(0), Math.toRadians(0)},
            {5, Math.toRadians(60), Math.toRadians(150)}, {5, Math.toRadians(30), Math.toRadians(30)},
            {5, Math.toRadians(30), Math.toRadians(120)}, {5, Math.toRadians(60), Math.toRadians(60)},
            {5, Math.toRadians(0), Math.toRadians(90)}, {5, Math.toRadians(90), Math.toRadians(90)},
            {5, Math.toRadians(-30), Math.toRadians(60)}, {5, Math.toRadians(120), Math.toRadians(120)},
            {5, Math.toRadians(-60), Math.toRadians(30)}, {5, Math.toRadians(150), Math.toRadians(150)},
            {5, Math.toRadians(-90), Math.toRadians(0)}, {5, Math.toRadians(180), Math.toRadians(180)},
            {5, Math.toRadians(90), Math.toRadians(-30)}, {5, Math.toRadians(0), Math.toRadians(210)},
            {5, Math.toRadians(60), Math.toRadians(-60)}, {5, Math.toRadians(30), Math.toRadians(240)},
            {5, Math.toRadians(30), Math.toRadians(-90)}, {5, Math.toRadians(60), Math.toRadians(270)},
            {5, Math.toRadians(0), Math.toRadians(-120)}, {5, Math.toRadians(90), Math.toRadians(300)},
            {5, Math.toRadians(-30), Math.toRadians(-150)}, {5, Math.toRadians(120), Math.toRadians(330)},
            {5, Math.toRadians(-60), Math.toRadians(-180)}, {5, Math.toRadians(150), Math.toRadians(0)},

            // longitude outside normal range, constant latitude
            {5, 0, Math.toRadians(570)}, {5, 0.5 * Math.PI, Math.toRadians(330)},
            {5, 0, Math.toRadians(540)}, {5, 0.5 * Math.PI, Math.toRadians(0)},
            {5, 0, Math.toRadians(510)}, {5, 0.5 * Math.PI, Math.toRadians(30)},
            {5, 0, Math.toRadians(480)}, {5, 0.5 * Math.PI, Math.toRadians(60)},
            {5, 0, Math.toRadians(450)}, {5, 0.5 * Math.PI, Math.toRadians(90)},
            {5, 0, Math.toRadians(420)}, {5, 0.5 * Math.PI, Math.toRadians(120)},
            {5, 0, Math.toRadians(390)}, {5, 0.5 * Math.PI, Math.toRadians(150)},
            {5, 0, Math.toRadians(210)}, {5, 0.5 * Math.PI, Math.toRadians(330)},
            {5, 0, Math.toRadians(-210)}, {5, 0.5 * Math.PI, Math.toRadians(30)},
            {5, 0, Math.toRadians(-390)}, {5, 0.5 * Math.PI, Math.toRadians(210)},
            {5, 0, Math.toRadians(-420)}, {5, 0.5 * Math.PI, Math.toRadians(240)},
            {5, 0, Math.toRadians(-450)}, {5, 0.5 * Math.PI, Math.toRadians(270)},
            {5, 0, Math.toRadians(-480)}, {5, 0.5 * Math.PI, Math.toRadians(300)},
            {5, 0, Math.toRadians(-510)}, {5, 0.5 * Math.PI, Math.toRadians(330)},
            {5, 0, Math.toRadians(-540)}, {5, 0.5 * Math.PI, Math.toRadians(0)},
            {5, 0, Math.toRadians(-570)}, {5, 0.5 * Math.PI, Math.toRadians(30)}
        };

        sphericalToGeographicCases = new double[][]{
            // Test case followed by true result.
            // polar within normal range (0 -- 180), constant azimuth
            {0, 0, 0}, {0, Math.toRadians(90), Math.PI},
            {0, Math.toRadians(90), Math.PI}, {0, 0, 0},
            {5, Math.toRadians(0), Math.PI}, {5, Math.toRadians(90), 0},
            {5, Math.toRadians(30), Math.PI}, {5, Math.toRadians(60), 0},
            {5, Math.toRadians(60), Math.PI}, {5, Math.toRadians(30), 0},
            {5, Math.toRadians(90), Math.PI}, {5, Math.toRadians(0), 0},
            {5, Math.toRadians(120), Math.PI}, {5, Math.toRadians(-30), 0},
            {5, Math.toRadians(150), Math.PI}, {5, Math.toRadians(-60), 0},
            {5, Math.toRadians(180), Math.PI}, {5, Math.toRadians(-90), 0}
        };
    }

    @Test
    public void testApply_doubleArr_doubleArr() {
        System.out.println("apply(double[], double[])");
        GeographicToSphericalTransform instance = new GeographicToSphericalTransform();
        final double[] target = new double[instance.numTargetDimensions()];
        for (int i = 0; i < geographicToSphericalCases.length; i += 2) {
            instance.apply(geographicToSphericalCases[i], target);
            Assert.assertArrayEquals(geographicToSphericalCases[i + 1], target, PREC_DOUBLE);
        }
    }

    @Test
    public void testApply_floatArr_floatArr() {
        System.out.println("apply(float[], float[])");
        GeographicToSphericalTransform instance = new GeographicToSphericalTransform();
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (int i = 0; i < geographicToSphericalCases.length; i += 2) {
            for (int j = 0; j < instance.numSourceDimensions(); ++j) {
                source[j] = (float) geographicToSphericalCases[i][j];
                truth[j] = (float) geographicToSphericalCases[i + 1][j];
            }
            instance.apply(source, target);
            if (isAround360(truth[2], PREC_FLOAT)) {
                // This is needed to account for the wrapping around 360°.
                Assert.assertEquals(truth[0], target[0], PREC_FLOAT);
                Assert.assertEquals(truth[1], target[1], PREC_FLOAT);
                Assert.assertTrue(isAround360(target[2], PREC_FLOAT));
            } else {
                Assert.assertArrayEquals(truth, target, PREC_FLOAT);
            }
        }
    }

    @Test
    public void testApply_RealLocalizable_RealPositionable() {
        System.out.println("apply(RealLocalizable, RealPositionable)");
        GeographicToSphericalTransform instance = new GeographicToSphericalTransform();
        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
        final double[] targetArray = new double[instance.numTargetDimensions()];
        for (int i = 0; i < geographicToSphericalCases.length; i += 2) {
            ((RealPoint) source).setPosition(geographicToSphericalCases[i]);
            instance.apply(source, target);
            ((RealPoint) target).localize(targetArray);
            Assert.assertArrayEquals(geographicToSphericalCases[i + 1], targetArray, PREC_DOUBLE);
        }
    }

    @Test
    public void testApplyInverse_doubleArr_doubleArr() {
        System.out.println("applyInverse(double[], double[])");
        GeographicToSphericalTransform instance = new GeographicToSphericalTransform();
        final double[] source = new double[instance.numSourceDimensions()];
        for (int i = 0; i < sphericalToGeographicCases.length; i += 2) {
            instance.applyInverse(source, sphericalToGeographicCases[i]);
            Assert.assertArrayEquals(sphericalToGeographicCases[i + 1], source, PREC_DOUBLE);
        }
    }

//    @Test
//    public void testApplyInverse_floatArr_floatArr() {
//    }
//
//    @Test
//    public void testApplyInverse_RealPositionable_RealLocalizable() {
//    }

    private boolean isAround360(final double value, final double precision) {
        if (value >= 0 - 0.5 * precision && value <= 0 + 0.5 * precision) {
            return true;
        }
        if (value >= 2 * Math.PI - 0.5d * precision && value <= 2 * Math.PI + precision) {
            return true;
        }
        return false;
    }
}