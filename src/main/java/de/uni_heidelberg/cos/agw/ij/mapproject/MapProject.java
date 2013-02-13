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
import javax.vecmath.Point3i;

public class MapProject implements PlugInFilter {

    private final String pluginName = "Map Project";
    private ImagePlus inputImp;

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImp = imp;
        return STACK_REQUIRED + DOES_8G + DOES_16;
    }

    @Override
    public void run(ImageProcessor inputIp) {
        GenericDialog dialog = new GenericDialog(pluginName);
        dialog.addNumericField("Center_x", 480, 0, 4, "voxel");
        dialog.addNumericField("Center_y", 430, 0, 4, "voxel");
        dialog.addNumericField("Center_z", 380, 0, 4, "voxel");
        dialog.addNumericField("Pole_axis_angle_longitude", 0, 2, 4, "degrees");
        dialog.addNumericField("Pole_axis_angle_latitude", 0, 2, 4, "degrees");
        dialog.addNumericField("Zero_meridian", 0, 2, 4, "degrees");
        dialog.addNumericField("Inner_radius", 250, 0, 4, "voxels");
        dialog.addNumericField("Outer_radius", 375, 0, 4, "voxels");
        dialog.addNumericField("Plane_position", 0.67, 2, 4, "0-1");
        dialog.addNumericField("Nr_of_concentric_projections", 1, 0, 4, "");
        dialog.addNumericField("Scale", 1, 2, 4, "x");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        final int centerX = (int) Math.round(dialog.getNextNumber());
        final int centerY = (int) Math.round(dialog.getNextNumber());
        final int centerZ = (int) Math.round(dialog.getNextNumber());
        final double poleAxisLonAngle = dialog.getNextNumber();
        final double poleAxisLatAngle = dialog.getNextNumber();
        final double zeroMeridian = dialog.getNextNumber();
        final int innerRadius = (int) Math.round(dialog.getNextNumber());
        final int outerRadius = (int) Math.round(dialog.getNextNumber());
        final double planePosition = dialog.getNextNumber();
        final int nProjections = (int) Math.round(dialog.getNextNumber());
        final double scale = dialog.getNextNumber();

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
                inputImp, new Point3i(centerX, centerY, centerZ),
                poleAxisLonAngle, poleAxisLatAngle, zeroMeridian,
                planePosition, scale);

        // project
        ImageProcessor[] outputIps = new ImageProcessor[nProjections];
        final double interval = (double) (outerRadius - innerRadius) / nProjections;
        for (int i = 0; i < nProjections; ++i) {
            final double inner = innerRadius + i * interval;
            final double outer = inner + interval;
            IJ.showStatus(String.format("%s (%d/%d) ...", pluginName, i + 1, nProjections));
            outputIps[i] = plateCaree.project(inner, outer);
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
                "-%s-cx%d-cy%d-cz%d-lo%.2f-la%.2f-zm%.2f-ri%d-ro%d-pp%.2f-sc%.2f",
                pluginName, centerX, centerY, centerZ,
                poleAxisLonAngle, poleAxisLatAngle, zeroMeridian,
                innerRadius, outerRadius, planePosition, scale);
        ImagePlus outputImp = new ImagePlus(
                Util.addToFilename(inputImp.getTitle(), filenameParams), outputStack);
        outputImp.show();
    }
}