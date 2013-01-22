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
import de.uni_heidelberg.cos.agw.ij.util.Util;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import javax.vecmath.Point3i;

public class MapProject implements PlugInFilter {

    private ImagePlus inputImp;
    private Sphere sphere;
    private IntensityProjector intensityProjector;

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImp = imp;
        return STACK_REQUIRED + DOES_8G + DOES_16;
    }

    @Override
    public void run(ImageProcessor inputIp) {
        GenericDialogPlus dialog = new GenericDialogPlus("Map Project");
        dialog.addNumericField("Center_x", 480, 0, 4, "voxel");
        dialog.addNumericField("Center_y", 430, 0, 4, "voxel");
        dialog.addNumericField("Center_z", 380, 0, 4, "voxel");
        dialog.addNumericField("Radius_inner", 330, 0, 4, "voxels");
        dialog.addNumericField("Radius_outer", 330, 0, 4, "voxels");
        dialog.addNumericField("Plane_position", 0.75, 1, 4, "0-1");
        dialog.addNumericField("Pole_offset", 0, 1, 4, "degrees");
        dialog.addNumericField("Zero_meridian_offset", 0, 1, 4, "degrees");
        dialog.addNumericField("Scaling", 1, 1, 4, "x");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        int centerX = (int) Math.round(dialog.getNextNumber());
        int centerY = (int) Math.round(dialog.getNextNumber());
        int centerZ = (int) Math.round(dialog.getNextNumber());
        int radiusInner = (int) Math.round(dialog.getNextNumber());
        int radiusOuter = (int) Math.round(dialog.getNextNumber());
        double planePosition = dialog.getNextNumber();
        double poleOffset = dialog.getNextNumber();
        double zeroMeridianOffset = dialog.getNextNumber();
        double scaling = dialog.getNextNumber();

        final int planeRadius = (int) Math.round(radiusInner + planePosition * (radiusOuter - radiusInner));
        sphere = new Sphere(new Point3i(centerX, centerY, centerZ), planeRadius);
        sphere.setPoleOffset(poleOffset);
        sphere.setZeroMeridianOffset(zeroMeridianOffset);
        intensityProjector = new IntensityProjector(inputImp);

        IJ.showStatus("Map Project ...");
        ImageProcessor outputIp = projectPlateCaree(scaling, radiusInner, radiusOuter);
        String filenameParams = String.format("-map-cx%d-cy%d-cz%d-r%d-po%.2f-zo%.2f-s%.2f", centerX, centerY, centerZ, planeRadius, poleOffset, zeroMeridianOffset, scaling);
        ImagePlus outputImp = new ImagePlus(Util.addToFilename(inputImp.getTitle(), filenameParams), outputIp);
        outputImp.show();
    }

//    private ImageProcessor projectPlateCaree(double scaling) {
//        int outputSizeX = (int) Math.round(sphere.getVoxelCountAtEquator() * scaling);
//        int outputSizeY = (int) Math.round(outputSizeX / 2);
//        ImageProcessor outputIp = getNewOutputIp(outputSizeX, outputSizeY);
//        for (int x = 0; x < outputSizeX; x++) {
//            for (int y = 0; y < outputSizeY; y++) {
//                double theta = (2 * Math.PI / outputSizeX) * x;
//                double phi = (Math.PI / outputSizeY) * y;
//                double value = sphere.getValueOfPhiTheta(phi, theta);
//                outputIp.putPixelValue(x, y, value);
//            }
//            IJ.showProgress(x + 1, outputSizeX);
//        }
//        return outputIp;
//    }
    
    private ImageProcessor projectPlateCaree(double scaling, double innerRadius, double outerRadius) {
        int outputSizeX = (int) Math.round(sphere.getVoxelCountAtEquator() * scaling);
        int outputSizeY = (int) Math.round(outputSizeX / 2);
        ImageProcessor outputIp = Util.newProcessor(inputImp, outputSizeX, outputSizeY);
        double[] radii = {innerRadius, outerRadius};
        for (int x = 0; x < outputSizeX; x++) {
            for (int y = 0; y < outputSizeY; y++) {
                double theta = (2 * Math.PI / outputSizeX) * x;
                double phi = (Math.PI / outputSizeY) * y;
                Point3i[] points = sphere.sphericalToCartesianGrid(phi, theta, radii);
                intensityProjector.set(points[0], points[1]);
                int value = intensityProjector.getMaximum();
                outputIp.putPixelValue(x, y, value);
            }
            IJ.showProgress(x + 1, outputSizeX);
        }
        return outputIp;
    }
}