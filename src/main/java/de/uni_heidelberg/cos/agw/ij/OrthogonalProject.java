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
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;


public class OrthogonalProject implements PlugInFilter {

	private ImagePlus inputImp;
	private ImageStack inputStack;
	private double
		voxelSizeX,
		voxelSizeY,
		voxelSizeZ;
	private String unitOfLength;
	private int
		inputSizeX,
		inputSizeY,
		inputSizeZ,
		startPlane,
		endPlane,
		directionIndex;//,
//		projectionIndex;

	
	@Override
	public int setup(String args, ImagePlus imp) {
		inputImp = imp;
		if (inputImp != null)
			inputStack = inputImp.getImageStack();
		return STACK_REQUIRED + DOES_8G + DOES_16 + DOES_32;
	}

	
	@Override
	public void run(ImageProcessor inputIp) {
		Calibration calibration = inputImp.getCalibration();
		unitOfLength = calibration.getUnit();
		voxelSizeX = calibration.pixelWidth;
		voxelSizeY = calibration.pixelHeight;
		voxelSizeZ = calibration.pixelDepth;			
		inputSizeX = inputImp.getWidth();
		inputSizeY = inputImp.getHeight();
		inputSizeZ = inputImp.getNSlices();

		String[] directions = {"X", "Y"};
		String[] projections = {"Max Intensity"};

		IJ.showStatus("Orthogonal project ...");
		GenericDialog dialog = new GenericDialog("Orthogonal Project");
		dialog.addStringField("Unit of length", unitOfLength);
		dialog.addNumericField("Voxel_width", voxelSizeX, 3);
		dialog.addNumericField("Voxel_height", voxelSizeY, 3);
		dialog.addNumericField("Voxel_depth", voxelSizeZ, 3);
		dialog.addCheckbox("Save voxel size", false);
		dialog.addCheckbox("Scale output", true);
		dialog.addNumericField("Start plane", 1, 0);
		dialog.addNumericField("End plane", inputSizeZ, 0);
		dialog.addChoice("Projection_direction", directions, directions[0]);
		dialog.addChoice("Projection_type", projections, projections[0]);
		dialog.showDialog();
		if (dialog.wasCanceled())
			return;
		
		unitOfLength = dialog.getNextString();
		voxelSizeX = dialog.getNextNumber();
		voxelSizeY = dialog.getNextNumber();
		voxelSizeZ = dialog.getNextNumber();
		boolean doSaveCalibration = dialog.getNextBoolean();
		boolean doScaleImage = dialog.getNextBoolean();
		startPlane = (int)Math.round(dialog.getNextNumber());
		if (startPlane < 1)
			startPlane = 1;
		endPlane = (int)Math.round(dialog.getNextNumber());
		if (endPlane > inputSizeZ)
			endPlane = inputSizeZ;
		directionIndex = dialog.getNextChoiceIndex();
//		projectionIndex = dialog.getNextChoiceIndex();
		
		// If requested, update image metadata (voxel size) of input image
		// to what was entered in the dialog.
		if (doSaveCalibration) {
			calibration.setUnit(unitOfLength);
			calibration.pixelWidth = voxelSizeX;
			calibration.pixelHeight = voxelSizeY;
			calibration.pixelDepth = voxelSizeZ;
			inputImp.updateAndRepaintWindow();
		}

		// Project.
		ImagePlus outputImp;
		if (inputImp.isHyperStack())
			outputImp = doHyperStackProjection();
		else
			outputImp = doSingleProjection();
		setOutputCalibration(outputImp);

		outputImp.getProcessor().setLut(inputImp.getProcessor().getLut());

		// If requested, scale output image. outputImp is not visible
		// at this point. Since the scaled result is shown automatically,
		// close outputImp without ever showing it.
		if (doScaleImage) {
			scaleImage(outputImp);
			if (outputImp.isVisible())
				outputImp.close();
		}
		// Otherwise show unscaled outputImp.
		else
			outputImp.show();
	}
	
	
	// Projects a single stack.
	private ImagePlus doSingleProjection() {
		ImageProcessor outputIp = doSingleProjection(startPlane, endPlane);
		String direction = directionIndex == 0 ? "X" : "Y";
		ImagePlus outputImp = new ImagePlus("MAXflip" + direction + "_" + inputImp.getTitle(), outputIp);
		return outputImp;
	}
	
	
	// Projects a hyperstack.
	private ImagePlus doHyperStackProjection() {
		int nframes = inputImp.getNFrames();
		int nchannels = inputImp.getNChannels();
		int nslices = inputImp.getNSlices();
		
		int[] outputDimensions = getOutputDimensions();
		ImageStack outputStack = new ImageStack(outputDimensions[0], outputDimensions[1]);
		
		for (int frame = 1; frame <= nframes; ++frame) {
			for (int channel = 1; channel <= nchannels; ++channel) {
				int start = (frame-1)*nchannels*nslices + (channel-1)*nslices + startPlane;//(startPlane-1)*nchannels + channel;
				int end = (frame-1)*nchannels*nslices + (channel-1)*nslices + endPlane;//(endPlane-1)*nchannels + channel;
				ImageProcessor ip = doSingleProjection(start, end);
				outputStack.addSlice("", ip);
			}
		}
		
		String direction = directionIndex == 0 ? "X" : "Y";
		ImagePlus outputImp = new ImagePlus("MAXflip" + direction + "_" + inputImp.getTitle(), outputStack);
		outputImp.setDimensions(nchannels, 1, nframes);
		return outputImp;
	}
	
	
	// Calls the appropriate method for the projection direction.
	private ImageProcessor doSingleProjection(final int startPlane, final int endPlane) {
		ImageProcessor outputIp;
		if (directionIndex == 0)
			outputIp = projectAlongX(startPlane, endPlane);
		else
			outputIp = projectAlongY(startPlane, endPlane);
		return outputIp;
	}
	

	// Returns the dimensions of the output image [width, height].
	private int[] getOutputDimensions() {
		int outputSizeX, outputSizeY;
		if (directionIndex == 0) {
			outputSizeX = endPlane - startPlane;
			outputSizeY = inputSizeY;
		}
		else {
			outputSizeX = inputSizeX;
			outputSizeY = endPlane - startPlane;
		}
		return new int[]{outputSizeX, outputSizeY};
	}
	
	
	// Returns an ImageProcessor with appropriate size and data type for
	// the output image.
	private ImageProcessor getOutputImageProcessor() {
		int[] outputDimensions = getOutputDimensions();
		final int inputBitDepth = inputImp.getBitDepth();
		if (inputBitDepth == 32)
			return new FloatProcessor(outputDimensions[0], outputDimensions[1]);
		else if (inputBitDepth == 16)
			return new ShortProcessor(outputDimensions[0], outputDimensions[1]);
		else // (inputBitDepth == 8)
			return new ByteProcessor(outputDimensions[0], outputDimensions[1]);
	}

	
	// Project along x, return ImageProcessor.
	private ImageProcessor projectAlongX(final int startPlane, final int endPlane) {
		ImageProcessor outputIp = getOutputImageProcessor();
		ImageProcessor inputIp;
		for (int inputZ = startPlane; inputZ <= endPlane; ++inputZ) {
			inputIp = inputStack.getProcessor(inputZ);
			int outputX = inputZ - 1 - startPlane;
			for (int inputY = 0; inputY < inputSizeY; ++inputY) {
				int outputY = inputY;
				float max = 0;
				for (int inputX = 0; inputX < inputSizeX; ++inputX) {
					float pixelValue = inputIp.getPixelValue(inputX, inputY);
					if (pixelValue > max)
						max = pixelValue;
				}
				outputIp.putPixelValue(outputX, outputY, max);
			}
			IJ.showProgress(inputZ, endPlane - startPlane);
		}
		return outputIp;
	}
	
	
	// Project along y, return ImageProcessor.
	private ImageProcessor projectAlongY(final int startPlane, final int endPlane) {
		ImageProcessor outputIp = getOutputImageProcessor();
		ImageProcessor inputIp;
		for (int inputZ = startPlane; inputZ <= endPlane; ++inputZ) {
			inputIp = inputStack.getProcessor(inputZ);
			int outputY = inputZ - 1 - startPlane;
			for (int inputX = 0; inputX < inputSizeX; ++inputX) {
				int outputX = inputX;
				float max = 0;
				for (int inputY = 0; inputY < inputSizeY; ++inputY) {
					float pixelValue = inputIp.getPixelValue(inputX, inputY);
					if (pixelValue > max)
						max = pixelValue;
				}
				outputIp.putPixelValue(outputX, outputY, max);
			}
			IJ.showProgress(inputZ, endPlane - startPlane);
		}
		return outputIp;
	}
	

	// Sets voxel size metadata of the output image to the flipped voxel
	// size of the input image. Then updated image and displaying window.
	private void setOutputCalibration(ImagePlus outputImp) {
		Calibration calibration = outputImp.getCalibration();
		calibration.setUnit(unitOfLength);
		if (directionIndex == 0) {
			calibration.pixelWidth = voxelSizeZ;
			calibration.pixelHeight = voxelSizeY;
		}
		else {
			calibration.pixelWidth = voxelSizeX;
			calibration.pixelHeight = voxelSizeZ;
		}
		outputImp.updateAndRepaintWindow();
	}

	
	// Scale image to isotropic sampling.
	private void scaleImage(ImagePlus outputImp) {
		Calibration calibration = outputImp.getCalibration();
		double voxelSizeX = calibration.pixelWidth;
		double voxelSizeY = calibration.pixelHeight;
		double smallerSize = voxelSizeX < voxelSizeY ? voxelSizeX : voxelSizeY;
		double voxelScaleX = voxelSizeX / smallerSize;
		double voxelScaleY = voxelSizeY / smallerSize;

		IJ.run(outputImp, "Scale...", "x=" + voxelScaleX + " y=" + voxelScaleY + " interpolation=Bicubic average process create title=[" + outputImp.getTitle() + "]");		
	}

}