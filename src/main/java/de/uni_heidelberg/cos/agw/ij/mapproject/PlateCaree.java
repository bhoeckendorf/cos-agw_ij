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

import de.uni_heidelberg.cos.agw.ij.util.IntensityProjector;
import ij.IJ;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.ops.operation.real.binary.RealMax;
import net.imglib2.type.Type;

public class PlateCaree<T extends Type<T>> {

    private final Img<T> inputImg;
    private final IntensityProjector<T> projector;
    private final Sphere sphere;
    private final double planePosition, scale;

    public PlateCaree(final Img<T> img, final double[] origin,
            final double poleAxisLonAngle, final double poleAxisLatAngle, final double zeroMeridian,
            final double planePosition, final double scale, InterpolatorFactory<T, T> interpolation) {
        inputImg = img;
        this.planePosition = planePosition;
        this.scale = scale;
        projector = new IntensityProjector<T>(inputImg, interpolation);
        sphere = new Sphere();
        sphere.setOrigin(origin);
        sphere.setPoleAxisLonAngle(poleAxisLonAngle);
        sphere.setPoleAxisLatAngle(poleAxisLatAngle);
        sphere.setZeroMeridian(zeroMeridian);
    }

    public Img<T> project(final double innerRadius, final double outerRadius, final int nProjections) {
        final double planeRadius = innerRadius + planePosition * (outerRadius - innerRadius);
        sphere.setRadius(planeRadius);
        final int width = (int) Math.round(scale * sphere.getVoxelCountAtEquator());
        final int height = (int) Math.round((double) width / 2);
        final double interval = (double) (outerRadius - innerRadius) / nProjections;
        final RealMax max = new RealMax();
        final Img<T> outputImg = inputImg.factory().create(new int[]{width, height, nProjections}, inputImg.firstElement());
        final RandomAccess<T> outputRa = outputImg.randomAccess();
        final double[] inner = new double[3];
        final double[] outer = new double[3];
        final int[] position = new int[3];
        for (int x = 0; x < width; ++x) {
            position[0] = x;
            for (int y = 0; y < height; ++y) {
                position[1] = y;
                final double lon = (2 * Math.PI / (width - 1)) * x;
                final double lat = (Math.PI / (height - 1)) * y;
                for (int z = 0; z < nProjections; ++z) {
                    position[2] = z;
                    final double outerR = outerRadius - z * interval;
                    final double innerR = outerR - interval;
                    outputRa.setPosition(position);
                    sphere.getCartesian(lon, lat, innerR, true, inner);
                    sphere.getCartesian(lon, lat, outerR, true, outer);
                    projector.set(inner, outer);
                    try {
                        outputRa.get().set(projector.compute(max));
                    } catch (NullPointerException ex) {
                    }
                }
            }
            IJ.showProgress(x, width);
        }
        return outputImg;
    }
}