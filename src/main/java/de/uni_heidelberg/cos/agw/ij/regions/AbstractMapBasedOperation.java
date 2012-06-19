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

package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3i;


abstract public class AbstractMapBasedOperation implements Operation {

	protected static Map<Integer, List<Point3i>> intensityMap = new HashMap<Integer, List<Point3i>>();
	protected final ImagePlus imp;
	protected final ImageProcessor ip;
	protected final ImageStack stack;
	
	
	public AbstractMapBasedOperation(final ImagePlus imp) {
		this.imp = imp;
		ip = this.imp.getProcessor();
		stack = this.imp.getImageStack();
		if (intensityMap.isEmpty())
			intensityMap = getIntensityMap(this.imp);
		run();
	}
	
	
	@Override
	abstract public String getName();
	abstract public void run();
	
	
	protected void postRun() {
		imp.setRoi(null, false);
		imp.updateAndDraw();
	}
	
	
	protected final Map<Integer, List<Point3i>> getIntensityMap(ImagePlus imp) {
		Map<Integer, List<Point3i>> map = new HashMap<Integer, List<Point3i>>();
		ImageStack stack = imp.getImageStack();
		for (int z = 1; z <= imp.getStackSize(); ++z) {
			ImageProcessor planeIp = stack.getProcessor(z);
			for (int y = 0; y < planeIp.getHeight(); ++y) {
				for (int x = 0; x < planeIp.getWidth(); ++x) {
					final int value = planeIp.getPixel(x, y);
					if (value == 0)
						continue;
					
					if (!map.containsKey(value))
						map.put(value, new ArrayList<Point3i>());
					map.get(value).add(new Point3i(x, y, z));
				}
			}
		}
		return map;
	}
	
	
	protected final int getFreeValue() {
		int value = 1;
		while (intensityMap.containsKey(value))
			value++;
		return value;
	}
	
	
	public final void clearIntensityMap() {
		intensityMap.clear();
	}

}
