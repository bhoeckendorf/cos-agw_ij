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
package de.uni_heidelberg.cos.agw.ij;

import de.uni_heidelberg.cos.agw.ij.util.Util;
import de.uni_heidelberg.cos.agw.imglib2.realtransform.AzimuthalEquidistantToCartesianTransform;
import de.uni_heidelberg.cos.agw.imglib2.realtransform.CylindricalToCartesianIntervalTransform;
import de.uni_heidelberg.cos.agw.imglib2.realtransform.EquirectangularToCartesianTransform;
import de.uni_heidelberg.cos.agw.imglib2.realtransform.PositionableRealTransform;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.GenericShortType;
import net.imglib2.view.Views;

public class MapTransform<T extends NumericType<T> & RealType<T> & NativeType<T>, V extends RealTransform & Interval> implements PlugInFilter {

    private static double centerX = 600;
    private static double centerY = 600;
    private static double centerZ = 425;
    private static double rotationX = 0;
    private static double rotationY = 0;
    private static double rotationSelf = 0;
    private static double innerRadius = 200;
    private static double outerRadius = 425;
    private static double stdRadiusOffset = 0.7;
    private static double scale = 1;
    private static int interpolationIndex = 1;
    private static int transformationIndex = 0;
    private static boolean doMakeImageJ1Output = true;
    private static double cylinderHeight = 512;
    private final String pluginName = "Map Transform";
    private final String[] interpolations = {"Nearest Neighbor", "Linear", "Lanczos"};
    private final String[] transformations = {"Equirectangular", "Azimuthal Equidistant", "Cylindrical"};
    private Img<T> inputImg;
    private String imageTitle;

    @Override
    public int setup(String args, ImagePlus imp) {
        try {
            inputImg = ImageJFunctions.wrap(imp);
            imageTitle = imp.getTitle();
        } catch (NullPointerException ex) {
        }
        return STACK_REQUIRED + DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        cylinderHeight = inputImg.dimension(0);

        GenericDialog dialog = new GenericDialog(pluginName);
        dialog.addChoice("Transformation", transformations, transformations[transformationIndex]);
        dialog.addNumericField("Center_x", centerX, 2, 7, "voxel");
        dialog.addNumericField("Center_y", centerY, 2, 7, "voxel");
        dialog.addNumericField("Center_z", centerZ, 2, 7, "voxel");
        dialog.addNumericField("Rotation_x", Math.toDegrees(rotationX), 2, 7, "degrees");
        dialog.addNumericField("Rotation_y", Math.toDegrees(rotationY), 2, 7, "degrees");
        dialog.addNumericField("Rotation_self", Math.toDegrees(rotationSelf), 2, 7, "degrees");
        dialog.addNumericField("Inner_radius", innerRadius, 2, 7, "voxels");
        dialog.addNumericField("Outer_radius", outerRadius, 2, 7, "voxels");
        dialog.addNumericField("Standard_radius_offset", stdRadiusOffset, 2, 7, "0-1");
        dialog.addNumericField("Scale", scale, 2, 7, "x");
        dialog.addChoice("3D_interpolation", interpolations, interpolations[interpolationIndex]);
        dialog.addCheckbox("ImageJ1_output", doMakeImageJ1Output);
        dialog.addNumericField("Cylinder_height", cylinderHeight, 2, 7, "voxels");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        transformationIndex = dialog.getNextChoiceIndex();
        centerX = dialog.getNextNumber();
        centerY = dialog.getNextNumber();
        centerZ = dialog.getNextNumber();
        rotationX = Math.toRadians(dialog.getNextNumber());
        rotationY = Math.toRadians(dialog.getNextNumber());
        rotationSelf = Math.toRadians(dialog.getNextNumber());
        innerRadius = dialog.getNextNumber();
        outerRadius = dialog.getNextNumber();
        stdRadiusOffset = dialog.getNextNumber();
        scale = dialog.getNextNumber();
        interpolationIndex = dialog.getNextChoiceIndex();
        doMakeImageJ1Output = dialog.getNextBoolean();
        cylinderHeight = dialog.getNextNumber();

        final double[] translation = {centerX, centerY, centerZ};
        final double[] rotation = {rotationX, rotationY, rotationSelf};

        if (stdRadiusOffset <= 0 || stdRadiusOffset > 1) {
            IJ.error(pluginName, "Standard radius offset must be between 0 and 1.");
            return;
        }
        if (scale <= 0) {
            IJ.error(pluginName, "Scale must be greater than 0.");
            return;
        }

        InterpolatorFactory interpolation = null;
        switch (interpolationIndex) {
            case 0:
                interpolation = new NearestNeighborInterpolatorFactory<T>();
                break;
            case 2:
                interpolation = new LanczosInterpolatorFactory<T>();
                break;
            default:
                interpolation = new NLinearInterpolatorFactory<T>();
                break;
        }
        final RealRandomAccess<T> inputRa = Views.interpolate(inputImg, interpolation).realRandomAccess();

        V transform = null;
        String transformName;
        switch (transformationIndex) {
            case 1:
                transform = (V) (new AzimuthalEquidistantToCartesianTransform(innerRadius, outerRadius, stdRadiusOffset, scale));
                transformName = "AzimuthalEquidistant";
                break;
            case 2:
                transform = (V) (new CylindricalToCartesianIntervalTransform(cylinderHeight, innerRadius, outerRadius, stdRadiusOffset, scale));
                transformName = "Cylindrical";
                break;
            default:
                transform = (V) (new EquirectangularToCartesianTransform(innerRadius, outerRadius, stdRadiusOffset, scale));
                transformName = "Equirectangular";
                break;
        }
        final Transformation transformation = new Transformation(transform, translation, rotation, inputRa);
        final String filenameParams = String.format(
            "-%s-cx%.2f-cy%.2f-cz%.2f-rx%.2f-ry%.2f-rs%.2f-ri%.2f-ro%.2f-sr%.2f-sc%.2f",
            transformName, centerX, centerY, centerZ,
            rotationX, rotationY, rotationSelf,
            innerRadius, outerRadius, stdRadiusOffset, scale);
        final String fileName = Util.addToFilename(imageTitle, filenameParams);


        if (doMakeImageJ1Output) {
            ImagePlus outputImp = transformation.computeIj1(inputImg.firstElement());
            outputImp.setTitle(fileName);
            outputImp.show();
        } else {
            Img<T> outputImg = transformation.compute(inputImg.factory(), inputImg.firstElement());
            ImageJFunctions.show(outputImg, fileName);
        }
    }
}

class Transformation<T extends NumericType<T> & RealType<T> & NativeType<T>, V extends RealTransform & Interval> {

    private final long[] outputDimensions;
    private final PositionableRealTransform transform;
    private final RealRandomAccess<T> inputRa;

    public Transformation(final V transformInterval, final double[] translation, final double[] rotation, final RealRandomAccess<T> sourceRa) {
        outputDimensions = new long[transformInterval.numDimensions()];
        transformInterval.dimensions(outputDimensions);

        transform = new PositionableRealTransform(transformInterval);
        for (int d = 0; d < outputDimensions.length; ++d) {
            transform.setPosition(translation[d], d);
            transform.rotate(d, rotation[d]);
        }

        inputRa = sourceRa;
    }

    public Img<T> compute(final ImgFactory<T> factory, final T element) {
        final Img<T> outputImg = factory.create(outputDimensions, element);
        final Cursor<T> outputCur = outputImg.localizingCursor();
        while (outputCur.hasNext()) {
            outputCur.next();
            transform.apply(outputCur, inputRa);
            outputCur.get().set(inputRa.get());
        }
        return outputImg;
    }

    public ImagePlus computeIj1(final T element) {
        int bits = 8;
        if (element instanceof GenericShortType) {
            bits = 16;
        }
        final ImagePlus outputImp = IJ.createImage("", (int) outputDimensions[0], (int) outputDimensions[1], (int) outputDimensions[2], bits);
        final ImageStack stack = outputImp.getImageStack();
        final double[] cylindrical = new double[3];
        final double[] cartesian = new double[3];
        for (int z = 0; z < outputDimensions[2]; ++z) {
            cylindrical[2] = z;
            for (int y = 0; y < outputDimensions[1]; ++y) {
                cylindrical[1] = y;
                for (int x = 0; x < outputDimensions[0]; ++x) {
                    cylindrical[0] = x;
                    transform.apply(cylindrical, cartesian);
                    inputRa.setPosition(cartesian);
                    stack.setVoxel((int) Math.round(cylindrical[0]), (int) Math.round(cylindrical[1]), (int) Math.round(cylindrical[2]), inputRa.get().getRealDouble());
                }
            }
            IJ.showProgress(z + 1, (int) outputDimensions[2]);
        }
        return outputImp;
    }
}
