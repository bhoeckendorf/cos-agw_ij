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

import ij.ImageStack;

import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point3i;


public class IntensityProjector {
	
	private final ImageStack volume;
	private final int[] volumeSize;
	
	
	public IntensityProjector(ImageStack volume) {
		this.volume = volume;
		volumeSize = new int[]{ this.volume.getWidth(), this.volume.getHeight(), this.volume.getSize() };
	}
	
	
	public double maxAlongRay(Point3i origin, Point3i destination) {
		Set<Point3i> voxels = voxelsAlongRay(origin, destination);
		double max = 0;
		for (Point3i voxel : voxels) {
			double value = volume.getVoxel(voxel.x, voxel.y, voxel.z);
			if (value > max)
				max = value;
		}
		return max;
	}

	
	private Set<Point3i> voxelsAlongRay(Point3i origin, Point3i destination) {
		int[] vector = { destination.x - origin.x, destination.y - origin.y, destination.z - origin.z };
		int biggestDimension = vector[0];
		for (int i = 1; i < 3; ++i) {
			int value = vector[i] > 0 ? vector[i] : -vector[i];
			if (value > biggestDimension)
				biggestDimension = value;
		}

		double[] step = new double[vector.length];
		for (int i = 0; i < step.length; ++i)
			step[i] = (double)vector[i] / biggestDimension;

		Set<Point3i> voxels = new HashSet<Point3i>();
		voxels.add(origin);
		double[] currentVoxel = { origin.x, origin.y, origin.z };
		while (true) {
			for (int i = 0; i < 3; ++i)
				currentVoxel[i] += step[i];
			if (!isPointInVolume(currentVoxel))
				break;
			Point3i gridVoxel = new Point3i((int)Math.round(currentVoxel[0]), (int)Math.round(currentVoxel[1]), (int)Math.round(currentVoxel[2]));
			voxels.add(gridVoxel);
		}
		return voxels;
	}
	
	
	private boolean isPointInVolume(double[] point) {
		for (int i = 0; i < 2; ++i) {
			if (point[i] < 0 || point[i] >= volumeSize[i])
				return false;
		}
		return (point[2] > 0 && point[2] <= volumeSize[2]);
	}
	
}
