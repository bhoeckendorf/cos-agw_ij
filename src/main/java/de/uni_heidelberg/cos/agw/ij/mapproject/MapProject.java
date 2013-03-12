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
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

public class MapProject<T extends NumericType<T> & NativeType<T>> implements PlugInFilter {

    private final String pluginName = "Map Project";
    private Img<T> inputImg;
    private String imageTitle;
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
    private static int interpolation3dIndex = 1;
    private final String[] interpolations3d = {"Nearest Neighbor", "Linear", "Lanczos"};

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImg = ImageJFunctions.wrap(imp);
        imageTitle = imp.getTitle();
        return STACK_REQUIRED + DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
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
        dialog.addChoice("3D_interpolation", interpolations3d, interpolations3d[interpolation3dIndex]);
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
        interpolation3dIndex = dialog.getNextChoiceIndex();

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

        InterpolatorFactory<T, T> interpolation = null;
        switch (interpolation3dIndex) {
            case 0:
                interpolation = new NearestNeighborInterpolatorFactory();
                break;
            case 2:
                interpolation = new LanczosInterpolatorFactory();
                break;
            default:
                interpolation = new NLinearInterpolatorFactory();
                break;
        }

        PlateCaree<T> plateCaree = new PlateCaree<T>(
                inputImg, new double[]{centerX, centerY, centerZ},
                poleAxisLonAngle, poleAxisLatAngle, zeroMeridian,
                planePosition, scale, interpolation);
        Img<T> outputImg = plateCaree.project(innerRadius, outerRadius, nProjections);

        String filenameParams = String.format(
                "-%s-cx%.2f-cy%.2f-cz%.2f-lo%.2f-la%.2f-zm%.2f-ri%.2f-ro%.2f-pp%.2f-sc%.2f",
                pluginName, centerX, centerY, centerZ,
                poleAxisLonAngle, poleAxisLatAngle, zeroMeridian,
                innerRadius, outerRadius, planePosition, scale);
        ImageJFunctions.show(outputImg, Util.addToFilename(imageTitle, filenameParams));
    }
}