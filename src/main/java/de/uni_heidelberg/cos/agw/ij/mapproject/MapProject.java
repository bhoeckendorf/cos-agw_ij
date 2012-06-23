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

package de.uni_heidelberg.cos.agw.ij.mapproject;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import javax.vecmath.Point3i;

import de.uni_heidelberg.cos.agw.ij.util.Util;


public class MapProject implements PlugInFilter {
	
	private ImagePlus inputImp;
	private Sphere sphere;

	
	@Override
	public int setup(String args, ImagePlus imp) {
		inputImp = imp;
		return STACK_REQUIRED + DOES_8G + DOES_16 + DOES_32;
	}


	@Override
	public void run(ImageProcessor inputIp) {
		GenericDialogPlus dialog = new GenericDialogPlus("World Map Project");
		dialog.addNumericField("Center X", 480, 0, 4, "voxel");
		dialog.addNumericField("Center Y", 430, 0, 4, "voxel");
		dialog.addNumericField("Center Z", 380, 0, 4, "voxel");
		dialog.addNumericField("Radius", 330, 0, 4, "voxel");
		dialog.addNumericField("Scaling", 1.0, 1, 4, "x");
//		dialog.addNumericField("Pole Offset Phi", 0, 1);
//		dialog.addNumericField("Pole Offset Theta", 0, 1);
		dialog.addNumericField("Zero Meridian Offset", Sphere.getZeroMeridianOffset(), 1, 4, "degrees");
		dialog.showDialog();
		if(dialog.wasCanceled())
			return;

		int centerX = (int)Math.round(dialog.getNextNumber());
		int centerY = (int)Math.round(dialog.getNextNumber());
		int centerZ = (int)Math.round(dialog.getNextNumber());
		int radius = (int)Math.round(dialog.getNextNumber());
		double scaling = dialog.getNextNumber();
//		double poleOffsetPhi = dialog.getNextNumber();
//		double poleOffsetTheta = dialog.getNextNumber();
		double zeroMeridianOffset = dialog.getNextNumber();
		
		Sphere.setCenter(new Point3i(centerX, centerY, centerZ));
//		Sphere.poleOffsetPhi = Math.toRadians(poleOffsetPhi);
//		Sphere.poleOffsetTheta = Math.toRadians(poleOffsetTheta);
		Sphere.setZeroMeridianOffset(zeroMeridianOffset);
		Sphere.setIntensityProjector(new IntensityProjector(inputImp.getImageStack()));

		IJ.showStatus("World Map Project ...");
		sphere = new Sphere(radius);
		ImageProcessor outputIp = projectPlateCaree(scaling);
		ImagePlus outputImp = new ImagePlus(Util.addToFilename(inputImp.getTitle(), "-WorldMap"), outputIp);
		outputImp.show();
	}
	
	
	private ImageProcessor projectPlateCaree(double scaling) {
		int outputSizeX = (int)Math.round(sphere.getVoxelCountAtEquator() * scaling);
		int outputSizeY = (int)Math.round(outputSizeX / 2);
		ImageProcessor outputIp = getNewOutputIp(outputSizeX, outputSizeY);
		for (int x = 0; x < outputSizeX; x++) {
			for (int y = 0; y < outputSizeY; y++) {
				double theta = ( 2 * Math.PI / outputSizeX ) * x;
				double phi = ( Math.PI / outputSizeY ) * y;
				double value = sphere.getValueOfPhiTheta(phi, theta);
				outputIp.putPixelValue(x, y, value);
			}
			IJ.showProgress(x + 1, outputSizeX);
		}
		return outputIp;
	}
	
	
	private ImageProcessor getNewOutputIp(int inputSizeX, int inputSizeY) {
		final int bits = inputImp.getBitDepth();
		if (bits == 16)
			return new ShortProcessor(inputSizeX, inputSizeY);
		else if (bits == 32)
			return new FloatProcessor(inputSizeX, inputSizeY);
		else
			return new ByteProcessor(inputSizeX, inputSizeY);
	}

}