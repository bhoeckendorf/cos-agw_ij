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
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import javax.vecmath.Point3i;

public class MapProject implements PlugInFilter {

    private final String pluginName = "Map Project";
    private ImagePlus inputImp;
    private Sphere sphere;
    private IntensityProjector intensityProjector;
    private double planePosition = 0.67;
    private double scaling = 1;

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImp = imp;
        return STACK_REQUIRED + DOES_8G + DOES_16;
    }

    @Override
    public void run(ImageProcessor inputIp) {
        GenericDialogPlus dialog = new GenericDialogPlus(pluginName);
        dialog.addNumericField("Center_x", 480, 0, 4, "voxel");
        dialog.addNumericField("Center_y", 430, 0, 4, "voxel");
        dialog.addNumericField("Center_z", 380, 0, 4, "voxel");
        dialog.addNumericField("Pole_offset", 0, 2, 4, "degrees");
        dialog.addNumericField("Zero_meridian_offset", 0, 2, 4, "degrees");
        dialog.addNumericField("Radius_inner", 330, 0, 4, "voxels");
        dialog.addNumericField("Radius_outer", 330, 0, 4, "voxels");
        dialog.addNumericField("Plane_position", planePosition, 2, 4, "0-1");
        dialog.addNumericField("Nr_of_spheres", 1, 0, 4, "");
        dialog.addNumericField("Scaling", scaling, 2, 4, "x");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        final int centerX = (int) Math.round(dialog.getNextNumber());
        final int centerY = (int) Math.round(dialog.getNextNumber());
        final int centerZ = (int) Math.round(dialog.getNextNumber());
        final double poleOffset = dialog.getNextNumber();
        final double zeroMeridianOffset = dialog.getNextNumber();
        final int radiusInner = (int) Math.round(dialog.getNextNumber());
        final int radiusOuter = (int) Math.round(dialog.getNextNumber());
        planePosition = dialog.getNextNumber();
        int nSpheres = (int) Math.round(dialog.getNextNumber());
        scaling = dialog.getNextNumber();

        if (nSpheres < 1) {
            IJ.error(pluginName, "Nr of spheres must be 1 or more.");
            return;
        }
        if (planePosition <= 0 || planePosition > 1) {
            IJ.error(pluginName, "Plane position must be between 0 and 1.");
            return;
        }
        if (scaling <= 0) {
            IJ.error(pluginName, "Scaling must be greater than 0.");
            return;
        }

        sphere = new Sphere(new Point3i(centerX, centerY, centerZ), radiusOuter);
        sphere.setPoleOffset(poleOffset);
        sphere.setZeroMeridianOffset(zeroMeridianOffset);
        intensityProjector = new IntensityProjector(inputImp);

        // project
        ImageProcessor[] outputIps = new ImageProcessor[nSpheres];
        final double interval = (double) (radiusOuter - radiusInner) / nSpheres;
        for (int i = 0; i < outputIps.length; ++i) {
            final double inner = radiusInner + i * interval;
            final double outer = inner + interval;
            IJ.showStatus(String.format("%s (%d/%d) ...", pluginName, i + 1, outputIps.length));
            outputIps[i] = projectPlateCaree(inner, outer);
        }

        // scale
        final int planeIndex = (int) (Math.round(planePosition * outputIps.length)) - 1;
        final int outputWidth = outputIps[planeIndex].getWidth();
        final int outputHeight = outputIps[planeIndex].getHeight();
        for (int i = 0; i < outputIps.length; ++i) {
            ImageProcessor outputIp = outputIps[i];
            if (outputIp.getWidth() != outputWidth || outputIp.getHeight() != outputHeight) {
                outputIp.setInterpolationMethod(ImageProcessor.BICUBIC);
                outputIp = outputIp.resize(outputWidth, outputHeight, true);
            }
            outputIps[i] = outputIp;
        }

        // stack
        ImageStack outputStack = new ImageStack(outputWidth, outputHeight);
        for (ImageProcessor outputIp : outputIps) {
            outputStack.addSlice(outputIp);
        }

        String filenameParams = String.format("-map-cx%d-cy%d-cz%d-po%.2f-zo%.2f-ri%d-ro%d-pp%.2f-s%.2f",
                centerX, centerY, centerZ, poleOffset, zeroMeridianOffset, radiusInner, radiusOuter, planePosition, scaling);
        ImagePlus outputImp = new ImagePlus(Util.addToFilename(inputImp.getTitle(), filenameParams), outputStack);
        outputImp.show();
    }

    private ImageProcessor projectPlateCaree(final double innerRadius, final double outerRadius) {
        final double sphereRadius = (int) Math.round(innerRadius + planePosition * (outerRadius - innerRadius));
        sphere.setRadius(sphereRadius);
        final int outputSizeX = (int) Math.round(sphere.getVoxelCountAtEquator() * scaling);
        final int outputSizeY = (int) Math.round(outputSizeX / 2);
        ImageProcessor outputIp = inputImp.getProcessor().createProcessor(outputSizeX, outputSizeY);
        double[] radii = {innerRadius, outerRadius};
        for (int x = 0; x < outputSizeX; ++x) {
            for (int y = 0; y < outputSizeY; ++y) {
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