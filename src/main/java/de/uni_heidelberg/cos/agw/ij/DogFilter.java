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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;


public class DogFilter<T extends NumericType<T> & NativeType<T> & RealType<T>> implements PlugInFilter {
	
	private ImagePlus inputImp;
	
	
	@Override
	public int setup(String args, ImagePlus imp) {
		inputImp = imp;
		return DOES_ALL;
	}
	
	
	@Override
	public void run(ImageProcessor ip) {
		IJ.showStatus("DoG Filter ...");
		GenericDialog dialog = new GenericDialog("DoG Filter");
		dialog.addNumericField("Sigma1", 2.0, 1);
		dialog.addNumericField("Sigma2", 4.0, 1);
		dialog.addCheckbox("Adjust kernel for sampling anisotropy", true);
		dialog.showDialog();
		if(dialog.wasCanceled())
			return;

		final double sigma1 = dialog.getNextNumber();
		final double sigma2 = dialog.getNextNumber();
		final boolean useAnisotropicKernel = dialog.getNextBoolean();
		final int currentPlane = inputImp.getCurrentSlice();
		final Calibration calibration = inputImp.getCalibration();

		final Img<T> inputImg = ImagePlusAdapter.wrap(inputImp);

		ImagePlus sigma1Imp, sigma2Imp;
		if (useAnisotropicKernel) {
			final double[] anisotropy = getAnisotropy(calibration);
			sigma1Imp = gaussianBlur(inputImg, sigma1, anisotropy);
			sigma2Imp = gaussianBlur(inputImg, sigma2, anisotropy);
		}
		else {
			sigma1Imp = gaussianBlur(inputImg, sigma1);
			sigma2Imp = gaussianBlur(inputImg, sigma2);
		}
		
		ImageCalculator imageCalculator = new ImageCalculator();
		ImagePlus resultImp = imageCalculator.run("Subtract create stack", sigma1Imp, sigma2Imp);
		sigma1Imp.close();
		sigma2Imp.close();
		
		resultImp.setTitle(inputImp.getTitle() + String.format("_DoG-%.1f-%.1f", sigma1, sigma2));
		resultImp.setSlice(currentPlane);
		resultImp.setCalibration(calibration);
		resultImp.show();
	}
	
	
	private ImagePlus gaussianBlur(final Img<T> img, final double sigma) {
		final double[] sigmaArray = getSigma(sigma, img.numDimensions());
		final Img<T> output = Gauss.inDouble(sigmaArray, img);
		return ImageJFunctions.wrap(output, "");
	}

	
	private ImagePlus gaussianBlur(final Img<T> img, final double sigma, final double[] anisotropy) {
		final double[] sigmaArray = getSigma(sigma, img.numDimensions(), anisotropy);
		final Img<T> output = Gauss.inDouble(sigmaArray, img);
		return ImageJFunctions.wrap(output, "");
	}
	
	
	private double[] getAnisotropy(final Calibration calibration) {
		final double[] anisotropy = {calibration.pixelWidth, calibration.pixelHeight, calibration.pixelDepth};
		double smallest = anisotropy[0];
		for (int i = 1; i < anisotropy.length; ++i) {
			if (anisotropy[i] < smallest)
				smallest = anisotropy[i];
		}
		for (int i = 0; i < anisotropy.length; ++i)
			anisotropy[i] = anisotropy[i] / smallest;
		return anisotropy;
	}
	
	
	private double[] getSigma(final double sigma, final int ndimensions) {
		final double[] sigmaArray = new double[ndimensions];
		for (int i = 0; i < sigmaArray.length; ++i)
			sigmaArray[i] = sigma;
		return sigmaArray;
	}
	
	
	private double[] getSigma(final double sigma, final int ndimensions, final double[] anisotropy) {
		final double[] sigmaArray = getSigma(sigma, ndimensions);
		for (int i = 0; i < sigmaArray.length; ++i)
			sigmaArray[i] = sigmaArray[i] / anisotropy[i];
		return sigmaArray;
	}

}