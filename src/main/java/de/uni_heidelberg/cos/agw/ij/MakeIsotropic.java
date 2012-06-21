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
		final double voxelSizeX = calibration.pixelWidth;
		final double voxelSizeY = calibration.pixelHeight;
		final double voxelSizeZ = calibration.pixelDepth;
		
		double smallestSize = voxelSizeX;
		if (voxelSizeY < smallestSize)
			smallestSize = voxelSizeY;
		if (voxelSizeZ < smallestSize)
			smallestSize = voxelSizeZ;
		
		final double voxelScaleX = voxelSizeX / smallestSize;
		final double voxelScaleY = voxelSizeY / smallestSize;
		final double voxelScaleZ = voxelSizeZ / smallestSize;
		final int outputWidth = (int)Math.round(inputImp.getWidth() * voxelScaleX);
		final int outputHeight = (int)Math.round(inputImp.getHeight() * voxelScaleY);
		final int outputDepth = (int)Math.round(inputImp.getStackSize() * voxelScaleZ);
		
		IJ.run(inputImp, "Scale...", "x=" + voxelScaleX + " y=" + voxelScaleY + " z=" + voxelScaleZ + " width=" + outputWidth + " height=" + outputHeight + " depth=" + outputDepth + " interpolation=Bicubic average process create title=isotropic.tif");
	}

}
