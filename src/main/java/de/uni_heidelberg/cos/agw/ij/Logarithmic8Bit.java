/*
 * This file is part of the COS AGW ImageJ plugin bundle.
 * https://github.com/bhoeckendorf/cos-agw_ij
 *
 * Copyright 2012 B. Hoeckendorf <b.hoeckendorf at web dot de>
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
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.ZProjector;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.StackConverter;

public class Logarithmic8Bit implements PlugInFilter {

    private ImagePlus inputImp;
    private double logMinIntensity,
            logMaxIntensity,
            slope;

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImp = imp;
        return DOES_8G + DOES_16 + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        // Get dynamic range, compute slope of log scale.
        double[] minMax = getMinMaxIntensity();

        GenericDialog dialog = new GenericDialog("Logarithmic 8Bit");
        dialog.addNumericField("Min", minMax[0], 1);
        dialog.addNumericField("Max", minMax[1], 1);
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return;
        }

        minMax[0] = dialog.getNextNumber();
        minMax[1] = dialog.getNextNumber();


        logMinIntensity = log(minMax[0]);
        logMaxIntensity = log(minMax[1]);
        slope = 255.0d / (logMaxIntensity - logMinIntensity);

        // Generate debug message regarding the slope.
        // IJ.log(String.format("Intensity range: %.2f - %.2f\n", minMax[0], minMax[1]));
        // IJ.log("Intensity -> log scale");
        // for (double i = 0.0; i <= 1.0; i += 0.1) {
        //     double value = minMax[0] + i * (minMax[1] - minMax[0]);
        //     IJ.log(String.format("%.2f -> %.2f\n", value, getValue(value)));
        // }

        // Replace every pixel or voxel with it's log scaled value in-place.
        final ImageStack stack = inputImp.getImageStack();
        final int xSize = inputImp.getWidth();
        final int ySize = inputImp.getHeight();
        final int zSize = inputImp.getStackSize();
        for (int z = 1; z <= zSize; ++z) {
            IJ.showProgress(z, zSize);
            ip = stack.getProcessor(z);
            for (int y = 0; y < ySize; ++y) {
                for (int x = 0; x < xSize; ++x) {
                    double value = ip.getPixelValue(x, y);
                    value = getValue(value);
                    ip.putPixelValue(x, y, value);
                }
            }
        }

        // Cast to 8 bit. Turn off scaling to displayed values first.
        if (inputImp.getBitDepth() != 8) {
            ImagePlus.setDefault16bitRange(0);
            IJ.run("Conversions...", " ");
            if (zSize > 1) {
                new StackConverter(inputImp).convertToGray8();
            } else {
                new ImageConverter(inputImp).convertToGray8();
            }
        }

        inputImp.setTitle(Util.addToFilename(inputImp.getTitle(), "-log"));
    }

    // Returns the log scaled value of an input intensity value,
    // or 0 if its negative.
    private double getValue(final double value) {
        double outValue = (log(value) - logMinIntensity) * slope;
        if (outValue < 0.0) {
            return 0.0;
        }
        return outValue;
    }

    // Returns the log of a value, or 0 if its negative.
    private double log(final double value) {
        double logValue = Math.log10(value);
        if (logValue < 0) {
            return 0.0;
        }
        return logValue;
    }

    // Returns min and max intensities used to compute range and slope.
    // Currently uses a maximum intensity projection for that. This
    // approach leads to a slight overestimation of background, which
    // in fact may be quite good.
    // ImageStatistics only works on the currently displayed plane. The
    // alternative is StackStatistics, which is slower than using a
    // projection and ImageStatistics.
    private double[] getMinMaxIntensity() {
        ZProjector zProjector = new ZProjector();
        zProjector.setImage(inputImp);
        zProjector.setMethod(ZProjector.MAX_METHOD);
        zProjector.setStartSlice(1);
        zProjector.setStopSlice(inputImp.getStackSize());
        zProjector.doProjection();
        IJ.showStatus("Logarithmic 8Bit ..."); // get rid of the mip message
        ImagePlus projectionImp = zProjector.getProjection();
        ImageStatistics stats = projectionImp.getStatistics();
        final double[] minMax = {stats.min, stats.max};
        projectionImp.close();
        projectionImp.flush();
        return minMax;
    }
}