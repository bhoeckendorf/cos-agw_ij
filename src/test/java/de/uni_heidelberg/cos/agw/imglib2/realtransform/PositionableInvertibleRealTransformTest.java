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
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PositionableInvertibleRealTransformTest {

    private final double PREC_DOUBLE = 1E-14;
    private final float PREC_FLOAT = 1E-5f;
    private double[][] fwdCases, inverseCases;

    @Before
    public void setUp() {
        // rotation, translation, source, target
        fwdCases = new double[][]{
            {0, 0, 0}, {0, 0, 0}, {-5, 0, 0}, {-5, 0, 0},
            {0, 0, 0}, {0, 0, 0}, {0, -5, 0}, {0, -5, 0},
            {0, 0, 0}, {0, 0, 0}, {0, 0, -5}, {0, 0, -5},
            {0, 0, 0}, {0, 0, 0}, {-5, 0, 5}, {-5, 0, 5},
            {0, 0, 0}, {0, 0, 0}, {0, -5, 5}, {0, -5, 5},
            {0, 0, 0}, {0, 0, 0}, {5, 0, -5}, {5, 0, -5},
            {0.5 * Math.PI, 0, 0}, {0, 0, 0}, {-5, 0, 0}, {-5, 0, 0},
            {0.5 * Math.PI, 0, 0}, {0, 0, 0}, {0, -5, 0}, {0, 5, 0},
            {0.5 * Math.PI, 0, 0}, {0, 0, 0}, {0, 0, -5}, {0, -5, 0},
            {0, 0.5 * Math.PI, 0}, {0, 0, 0}, {-5, 0, 0}, {0, 5, 0},
            {0, 0.5 * Math.PI, 0}, {0, 0, 0}, {0, -5, 0}, {0, 0, 5},
            {0, 0.5 * Math.PI, 0}, {0, 0, 0}, {0, 0, -5}, {5, 0, 0},
            {0, 0, 0.5 * Math.PI}, {0, 0, 0}, {-5, 0, 0}, {0, 0, 5},
            {0, 0, 0.5 * Math.PI}, {0, 0, 0}, {0, -5, 0}, {0, 0, -5},
            {0, 0, 0.5 * Math.PI}, {0, 0, 0}, {0, 0, -5}, {5, 0, 0},
            {0, 0.5 * Math.PI, -0.5 * Math.PI}, {0, 0, 0}, {-5, 0, 0}, {5, 0, 0},
            {0, 0.5 * Math.PI, -0.5 * Math.PI}, {0, 0, 0}, {0, -5, 0}, {0, -5, 0},
            {0, 0.5 * Math.PI, -0.5 * Math.PI}, {0, 0, 0}, {0, 0, -5}, {0, 5, 0}
        };
    }

    @Test
    public void testApply_doubleArr_doubleArr() {
        System.out.println("apply(double[], double[])");
        final PositionableInvertibleRealTransform instance = new PositionableInvertibleRealTransform(new NullTransform());
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < fwdCases.length; i += 4) {
            for (int j = 0; j < instance.numTargetDimensions(); ++j) {
                instance.rotate(j, fwdCases[i][j]);
                instance.setPosition(fwdCases[i + 1][j], j);
                source[j] = fwdCases[i + 2][j];
                truth[j] = fwdCases[i + 3][j];
            }
            instance.apply(source, target);
            Assert.assertArrayEquals(truth, target, PREC_DOUBLE);
        }
    }

    @Test
    public void testApply_floatArr_floatArr() {
        System.out.println("apply(float[], float[])");
        final PositionableInvertibleRealTransform instance = new PositionableInvertibleRealTransform(new NullTransform());
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (int i = 0; i < fwdCases.length; i += 4) {
            for (int j = 0; j < instance.numTargetDimensions(); ++j) {
                instance.rotate(j, (float) fwdCases[i][j]);
                instance.setPosition((float) fwdCases[i + 1][j], j);
                source[j] = (float) fwdCases[i + 2][j];
                truth[j] = (float) fwdCases[i + 3][j];
            }
            instance.apply(source, target);
            Assert.assertArrayEquals(truth, target, PREC_FLOAT);
        }
    }

    @Test
    public void testApply_RealLocalizable_RealPositionable() {
        System.out.println("apply(RealLocalizable, RealPositionable)");
        final PositionableInvertibleRealTransform instance = new PositionableInvertibleRealTransform(new NullTransform());
        final RealLocalizable source = new RealPoint(instance.numSourceDimensions());
        final RealPositionable target = new RealPoint(instance.numTargetDimensions());
        final double[] targetArray = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < fwdCases.length; i += 4) {
            for (int j = 0; j < instance.numTargetDimensions(); ++j) {
                instance.rotate(j, fwdCases[i][j]);
                instance.setPosition(fwdCases[i + 1][j], j);
                ((RealPoint) source).setPosition(fwdCases[i + 2][j], j);
                truth[j] = fwdCases[i + 3][j];
            }
            instance.apply(source, target);
            ((RealPoint) target).localize(targetArray);
            Assert.assertArrayEquals(truth, targetArray, PREC_DOUBLE);
        }
    }

    @Test
    public void testApplyInverse_doubleArr_doubleArr() {
        System.out.println("applyInverse(double[], double[])");
        final PositionableInvertibleRealTransform instance = new PositionableInvertibleRealTransform(new NullTransform());
        final double[] source = new double[instance.numSourceDimensions()];
        final double[] target = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < fwdCases.length; i += 4) {
            for (int j = 0; j < instance.numTargetDimensions(); ++j) {
                instance.rotate(j, fwdCases[i][j]);
                instance.setPosition(fwdCases[i + 1][j], j);
                target[j] = fwdCases[i + 3][j];
                truth[j] = fwdCases[i + 2][j];
            }
            instance.applyInverse(source, target);
            Assert.assertArrayEquals(truth, source, PREC_DOUBLE);
        }
    }

    @Test
    public void testApplyInverse_floatArr_floatArr() {
        System.out.println("applyInverse(float[], float[])");
        final PositionableInvertibleRealTransform instance = new PositionableInvertibleRealTransform(new NullTransform());
        final float[] source = new float[instance.numSourceDimensions()];
        final float[] target = new float[instance.numTargetDimensions()];
        final float[] truth = new float[instance.numTargetDimensions()];
        for (int i = 0; i < fwdCases.length; i += 4) {
            for (int j = 0; j < instance.numTargetDimensions(); ++j) {
                instance.rotate(j, (float) fwdCases[i][j]);
                instance.setPosition((float) fwdCases[i + 1][j], j);
                target[j] = (float) fwdCases[i + 3][j];
                truth[j] = (float) fwdCases[i + 2][j];
            }
            instance.applyInverse(source, target);
            Assert.assertArrayEquals(truth, source, PREC_FLOAT);
        }
    }

    @Test
    public void testApplyInverse_RealPositionable_RealLocalizable() {
        System.out.println("applyInverse(RealPositionable, RealLocalizable)");
        final PositionableInvertibleRealTransform instance = new PositionableInvertibleRealTransform(new NullTransform());
        final RealPositionable source = new RealPoint(instance.numSourceDimensions());
        final RealLocalizable target = new RealPoint(instance.numTargetDimensions());
        final double[] sourceArray = new double[instance.numTargetDimensions()];
        final double[] truth = new double[instance.numTargetDimensions()];
        for (int i = 0; i < fwdCases.length; i += 4) {
            for (int j = 0; j < instance.numTargetDimensions(); ++j) {
                instance.rotate(j, fwdCases[i][j]);
                instance.setPosition(fwdCases[i + 1][j], j);
                ((RealPoint) target).setPosition(fwdCases[i + 3][j], j);
                truth[j] = fwdCases[i + 2][j];
            }
            instance.applyInverse(source, target);
            ((RealPoint) source).localize(sourceArray);
            Assert.assertArrayEquals(truth, sourceArray, PREC_DOUBLE);
        }
    }
}

class NullTransform implements InvertibleRealTransform {

    private final InverseRealTransform inverse;

    public NullTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        for (int d = 0; d < source.length; ++d) {
            target[d] = source[d];
        }
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        for (int d = 0; d < source.length; ++d) {
            target[d] = source[d];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        target.setPosition(source);
    }

    @Override
    public void applyInverse(final double[] source, final double[] target) {
        for (int d = 0; d < source.length; ++d) {
            source[d] = target[d];
        }
    }

    @Override
    public void applyInverse(final float[] source, final float[] target) {
        for (int d = 0; d < source.length; ++d) {
            source[d] = target[d];
        }
    }

    @Override
    public void applyInverse(final RealPositionable source, final RealLocalizable target) {
        source.setPosition(target);
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
    public InvertibleRealTransform inverse() {
        return inverse;
    }

    @Override
    public InvertibleRealTransform copy() {
        return this;
    }
}