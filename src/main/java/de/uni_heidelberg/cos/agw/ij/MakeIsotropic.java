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
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class MakeIsotropic implements PlugInFilter {

    private ImagePlus inputImp;

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImp = imp;
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        Calibration calibration = inputImp.getCalibration();
        double[] anisotropy = {calibration.pixelWidth, calibration.pixelHeight,
            calibration.pixelDepth};
        double smallest = anisotropy[0];
        for (int i = 1; i < anisotropy.length; ++i) {
            if (anisotropy[i] < smallest) {
                smallest = anisotropy[i];
            }
        }
        for (int i = 0; i < anisotropy.length; ++i) {
            anisotropy[i] /= smallest;
        }

        int[] dimensions = {inputImp.getWidth(), inputImp.getHeight(),
            inputImp.getStackSize()};
        for (int i = 0; i < dimensions.length; ++i) {
            dimensions[i] = (int) Math.round(dimensions[i] * anisotropy[i]);
        }

        final String outputTitle = Util.addToFilename(inputImp.getTitle(),
                "-isotropic");
        IJ.run(inputImp, "Scale...",
                "x=" + anisotropy[0]
                + " y=" + anisotropy[1]
                + " z=" + anisotropy[2]
                + " width=" + dimensions[0]
                + " height=" + dimensions[1]
                + " depth=" + dimensions[2]
                + " interpolation=Bicubic average process create title="
                + outputTitle);
        ImagePlus outputImp = IJ.getImage();
        outputImp.getProcessor().setLut(inputImp.getProcessor().getLut());
    }

    public void run(ImagePlus imp) {
        inputImp = imp;
        run(inputImp.getProcessor());
    }
}
