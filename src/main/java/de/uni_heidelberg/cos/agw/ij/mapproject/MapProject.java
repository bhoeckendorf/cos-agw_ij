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

import de.uni_heidelberg.cos.agw.ij.util.Util;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class MapProject implements PlugInFilter {

    private final String pluginName = "Map Project";
    private ImagePlus inputImp;
    private static double centerX = 480;
    private static double centerY = 430;
    private static double centerZ = 380;
    private static double poleAxisLonAngle = 0;
    private static double poleAxisLatAngle = 0;
    private static double zeroMeridian = 0;
    private static double innerRadius = 250;
    private static double outerRadius = 375;
    private static double planePosition = 0.7;
    private static int nProjections = 1;
    private static double scale = 1;

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImp = imp;
        return STACK_REQUIRED + DOES_8G + DOES_16;
    }

    @Override
    public void run(ImageProcessor inputIp) {
        GenericDialog dialog = new GenericDialog(pluginName);
        dialog.addNumericField("Center_x", centerX, 2, 7, "voxel");
        dialog.addNumericField("Center_y", centerY, 2, 7, "voxel");
        dialog.addNumericField("Center_z", centerZ, 2, 7, "voxel");
        dialog.addNumericField("Pole_axis_angle_longitude", poleAxisLonAngle, 2, 7, "degrees");
        dialog.addNumericField("Pole_axis_angle_latitude", poleAxisLatAngle, 2, 7, "degrees");
        dialog.addNumericField("Zero_meridian", zeroMeridian, 2, 7, "degrees");
        dialog.addNumericField("Inner_radius", innerRadius, 2, 7, "voxels");
        dialog.addNumericField("Outer_radius", outerRadius, 2, 7, "voxels");
        dialog.addNumericField("Plane_position", planePosition, 2, 7, "0-1");
        dialog.addNumericField("Nr_of_concentric_projections", nProjections, 0, 7, "");
        dialog.addNumericField("Scale", scale, 2, 7, "x");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        centerX = dialog.getNextNumber();
        centerY = dialog.getNextNumber();
        centerZ = dialog.getNextNumber();
        poleAxisLonAngle = dialog.getNextNumber();
        poleAxisLatAngle = dialog.getNextNumber();
        zeroMeridian = dialog.getNextNumber();
        innerRadius = dialog.getNextNumber();
        outerRadius = dialog.getNextNumber();
        planePosition = dialog.getNextNumber();
        nProjections = (int) Math.round(dialog.getNextNumber());
        scale = dialog.getNextNumber();

        if (nProjections < 1) {
            IJ.error(pluginName, "Nr of spheres must be 1 or more.");
            return;
        }
        if (planePosition <= 0 || planePosition > 1) {
            IJ.error(pluginName, "Plane position must be between 0 and 1.");
            return;
        }
        if (scale <= 0) {
            IJ.error(pluginName, "Scale must be greater than 0.");
            return;
        }

        PlateCaree plateCaree = new PlateCaree(
                ImageJFunctions.wrap(inputImp), new double[]{centerX, centerY, centerZ},
                poleAxisLonAngle, poleAxisLatAngle, zeroMeridian,
                planePosition, scale);

        // project
        ImageProcessor[] outputIps = new ImageProcessor[nProjections];
        final double interval = (double) (outerRadius - innerRadius) / nProjections;
        for (int i = 0; i < nProjections; ++i) {
            final double inner = innerRadius + i * interval;
            final double outer = inner + interval;
            IJ.showStatus(String.format("%s (%d/%d) ...", pluginName, i + 1, nProjections));
            outputIps[i] = ImageJFunctions.wrap(plateCaree.project(inner, outer), "").getProcessor();
        }

        // scale
        final int planeIndex = (int) (Math.round(planePosition * nProjections)) - 1;
        final int outputWidth = outputIps[planeIndex].getWidth();
        final int outputHeight = outputIps[planeIndex].getHeight();
        for (int i = 0; i < nProjections; ++i) {
            ImageProcessor outputIp = outputIps[i];
            if (outputIp.getWidth() != outputWidth || outputIp.getHeight() != outputHeight) {
                outputIp.setInterpolationMethod(ImageProcessor.BICUBIC);
                outputIp = outputIp.resize(outputWidth, outputHeight, true);
            }
            outputIps[i] = outputIp;
        }

        // stack, in inverse order (outermost first, innermost last)
        ImageStack outputStack = new ImageStack(outputWidth, outputHeight);
        for (int i = nProjections - 1; i >= 0; --i) {
            outputStack.addSlice(outputIps[i]);
        }

        String filenameParams = String.format(
                "-%s-cx%.2f-cy%.2f-cz%.2f-lo%.2f-la%.2f-zm%.2f-ri%.2f-ro%.2f-pp%.2f-sc%.2f",
                pluginName, centerX, centerY, centerZ,
                poleAxisLonAngle, poleAxisLatAngle, zeroMeridian,
                innerRadius, outerRadius, planePosition, scale);
        ImagePlus outputImp = new ImagePlus(
                Util.addToFilename(inputImp.getTitle(), filenameParams), outputStack);
        outputImp.show();
    }
}