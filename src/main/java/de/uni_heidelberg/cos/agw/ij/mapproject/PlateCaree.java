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
import ij.ImagePlus;
import ij.process.ImageProcessor;
import javax.vecmath.Point3i;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.ops.operation.real.binary.RealMax;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;

public class PlateCaree {

    private final ImagePlus inputImp;
    private final IntensityProjector projector;
    private final Sphere sphere;
    private final double planePosition, scale;

    public PlateCaree(final ImagePlus imp, final Point3i origin,
            final double poleAxisLonAngle, final double poleAxisLatAngle, final double zeroMeridian,
            final double planePosition, final double scale) {
        inputImp = imp;
        this.planePosition = planePosition;
        this.scale = scale;
        projector = new IntensityProjector(ImageJFunctions.wrap(imp), new NearestNeighborInterpolatorFactory());
        sphere = new Sphere();
        sphere.setOrigin(origin);
        sphere.setPoleAxisLonAngle(poleAxisLonAngle);
        sphere.setPoleAxisLatAngle(poleAxisLatAngle);
        sphere.setZeroMeridian(zeroMeridian);
    }

    public ImageProcessor project(final double innerRadius, final double outerRadius) {
        final double planeRadius = innerRadius + planePosition * (outerRadius - innerRadius);
        sphere.setRadius(planeRadius);
        final int width = (int) Math.round(scale * sphere.getVoxelCountAtEquator());
        final int height = (int) Math.round((double) width / 2);
        RealMax max = new RealMax();
        ImageProcessor outputIp = inputImp.getProcessor().createProcessor(width, height);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                final double lon = (2 * Math.PI / (width - 1)) * x;
                final double lat = (Math.PI / (height - 1)) * y;
                Point3i innerPoint = sphere.getCartesianGrid(lon, lat, innerRadius, true);
                Point3i outerPoint = sphere.getCartesianGrid(lon, lat, outerRadius, true);
                projector.set(new double[]{innerPoint.x, innerPoint.y, innerPoint.z}, new double[]{outerPoint.x, outerPoint.y, outerPoint.z});
                Type value = projector.compute(max);
                if (value instanceof IntegerType) {
                    int v = ((IntegerType) value).getInteger();
                    outputIp.putPixelValue(x, y, v);
                }
            }
            IJ.showProgress(x + 1, width);
        }
        return outputIp;
    }
}
