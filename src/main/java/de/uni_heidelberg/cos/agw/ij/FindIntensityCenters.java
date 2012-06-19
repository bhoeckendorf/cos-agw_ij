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
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3i;


public class FindIntensityCenters implements PlugInFilter {

	private ImagePlus inputImp;
	
	
	@Override
	public int setup(String args, ImagePlus imp) {
		inputImp = imp;
		return DOES_8G + DOES_16;
	}

	
	@Override
	public void run(ImageProcessor inputIp) {
		IJ.showStatus("Find intensity centers ...");
		GenericDialog dialog = new GenericDialog("Find Intensity Centers");
		dialog.addCheckbox("Display table", true);
		dialog.addCheckbox("Display image", false);
		dialog.addNumericField("Point radius", 0, 0);
		dialog.showDialog();
		if (dialog.wasCanceled())
			return;
		
		final boolean doDisplayTable = dialog.getNextBoolean();
		final boolean doDisplayImage = dialog.getNextBoolean();
		final int pointRadius = (int)Math.round(dialog.getNextNumber());
		final int xSize = inputImp.getWidth();
		final int ySize = inputImp.getHeight();
		final int zSize = inputImp.getStackSize();

		// Create intensity -> pixels mapping.
		Map<Integer, List<Point3i>> intensityMap = new HashMap<Integer, List<Point3i>>();
		ImageStack inputStack = inputImp.getImageStack();
		for (int z = 1; z <= zSize; ++z) {
			inputIp = inputStack.getProcessor(z);
			for (int y = 0; y < ySize; ++y) {
				for (int x = 0; x < xSize; ++x) {
					final int value = inputIp.getPixel(x, y);

					// ignore 0s
					if (value == 0)
						continue;

					Point3i pixel = new Point3i(x, y, z);
					if (!intensityMap.containsKey(value))
						intensityMap.put(value, new ArrayList<Point3i>());
					intensityMap.get(value).add(pixel);
				}
			}
		}

		// Create intensity -> centers mapping from above mapping.
		Map<Integer, Point3i> pointMap = new HashMap<Integer, Point3i>();
		for (int value : intensityMap.keySet()) {
			List<Point3i> pixels = intensityMap.get(value);
			double avgX = 0;
			double avgY = 0;
			double avgZ = 0;
			for (Point3i pixel : pixels) {
				avgX += pixel.x;
				avgY += pixel.y;
				avgZ += pixel.z;
			}
			avgX = avgX / pixels.size();
			avgY = avgY / pixels.size();
			avgZ = avgZ / pixels.size();
			final int x = (int)Math.round(avgX);
			final int y = (int)Math.round(avgY);
			final int z = (int)Math.round(avgZ);
			Point3i point = new Point3i(x, y, z);
			pointMap.put(value, point);
		}
		
		// Clear intensity -> pixels mapping. It's no longer needed.
		// intensityMap.clear();
		
		// Create and show an output image, if requested.
		if (doDisplayImage) {
			ImageStack outputStack = new ImageStack(xSize, ySize);
			for (int z = 1; z <= zSize; ++z)
				outputStack.addSlice("", newImageProcessor());
			
			ImageProcessor outputIp;
			for (int value : pointMap.keySet()) {
				Point3i point = pointMap.get(value);
				for (int z = point.z - pointRadius; z <= point.z + pointRadius; ++z) {
					if (z < 1 || z > zSize)
						continue;
					outputIp = outputStack.getProcessor(z);
					for (int x = point.x - pointRadius; x <= point.x + pointRadius; ++x) {
						for (int y = point.y - pointRadius; y <= point.y + pointRadius; ++y) {
							outputIp.putPixel(x, y, value);
						}
					}
				}
			}
			
			ImagePlus outputImp = new ImagePlus("Intensity centers", outputStack);
			outputImp.getProcessor().setLut(inputImp.getProcessor().getLut());
			outputImp.show();
		}

		// Create and show a results table, if requested.
		if (doDisplayTable) {
			ResultsTable table = new ResultsTable();
			table.setPrecision(1);
			List<Integer> values = new ArrayList<Integer>(pointMap.keySet());
			Collections.sort(values);
			for (int value : values) {
				table.incrementCounter();
				Point3i point = pointMap.get(value);
				table.addValue("Value", value);
				table.addValue("x", point.x);
				table.addValue("y", point.y);

				// z -= 1 because ImageJ's first z is 1, not 0
				table.addValue("z", point.z - 1); 
			}
			table.show("Intensity centers");
		}
	}


	// Returns a new ImageProcessor matching to input size and data type.
	private ImageProcessor newImageProcessor() {
		if (inputImp.getBitDepth() == 16)
			return new ShortProcessor(inputImp.getWidth(), inputImp.getHeight());
		else
			return new ByteProcessor(inputImp.getWidth(), inputImp.getHeight());
	}

}
