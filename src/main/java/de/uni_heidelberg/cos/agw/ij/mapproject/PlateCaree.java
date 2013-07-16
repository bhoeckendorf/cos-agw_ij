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
package de.uni_heidelberg.cos.agw.ij.mapproject;

import de.uni_heidelberg.cos.agw.imglib2.LineIntensityProjector;
import ij.IJ;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.real.binary.RealBinaryOperation;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

public class PlateCaree<T extends NumericType<T> & NativeType<T>> extends Projection<T> {

    public PlateCaree(final RealTransform transform, final LineIntensityProjector<T> projector, final T element) {
        super(transform, projector, element);
    }

    @Override
    public String getName() {
        return "PlateCaree";
    }

    @Override
    public Img<T> project(final double innerRadius, final double outerRadius, final double planePosition, final double scale, final int nProjections, final RealBinaryOperation operation) {
        final double planeRadius = innerRadius + planePosition * (outerRadius - innerRadius);
        final int width = (int) Math.round(scale * 2 * planeRadius * Math.PI);
        final int height = (int) Math.round(0.5d * width);
        final double interval = (double) (outerRadius - innerRadius) / nProjections;

        final Img<T> outputImg = getOutputImg(width, height, nProjections);
        final RandomAccess<T> outputRa = outputImg.randomAccess();
        final double[] spherical = new double[transform.numSourceDimensions()];
        final double[] innerRadiusCartesian = new double[transform.numTargetDimensions()];
        final double[] outerRadiusCartesian = new double[innerRadiusCartesian.length];
        final double xFactor = 2 * Math.PI / width;
        final double yFactor = Math.PI / height;
        for (int x = 0; x < width; ++x) {
            outputRa.setPosition(x, 0);
            spherical[2] = xFactor * x;
            for (int y = 0; y < height; ++y) {
                outputRa.setPosition(y, 1);
                spherical[1] = yFactor * y;
                for (int z = 0; z < nProjections; ++z) {
                    outputRa.setPosition(z, 2);
                    spherical[0] = outerRadius - z * interval;
                    transform.apply(spherical, outerRadiusCartesian);
                    spherical[0] -= interval;
                    transform.apply(spherical, innerRadiusCartesian);
                    projector.set(innerRadiusCartesian, outerRadiusCartesian);
                    try {
                        outputRa.get().set(projector.compute(operation));
                    } catch (NullPointerException ex) {
                    }
                }
            }
            IJ.showProgress(x, width);
        }
        return outputImg;
    }
}