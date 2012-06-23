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

import ij.IJ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point3i;


public class Sphere {
	
	private static Point3i center = new Point3i(0, 0, 0);
	private int radius = 0;
	private static IntensityProjector projector = null;
	private Map<Point3i, Double> valuesCache = new HashMap<Point3i, Double>();
	
	public static double
//		poleOffsetPhi = 0,
//		poleOffsetTheta = 0,
		zeroMeridianOffset = 0;

	
	public Sphere(int radius) {
		setRadius(radius);
	}
	
		
	public static void setCenter(Point3i center) {
		Sphere.center = new Point3i(center.x - 1, center.y - 1, center.z);
	}

	
	public static Point3i getCenter() {
		return new Point3i(center.x + 1, center.y + 1, center.z);
	}
	
	
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	
	public int getRadius() {
		return radius;
	}
	
	
	public static void setIntensityProjector(IntensityProjector projector) {
		Sphere.projector = projector; 
	}

	
	public static void setZeroMeridianOffset(double degrees) {
		zeroMeridianOffset = Math.toRadians(degrees);
	}
	
	
	public static double getZeroMeridianOffset() {
		return Math.toDegrees(zeroMeridianOffset);
	}
	
	
	public int getVoxelCountAtEquator() {
		return voxelsOfPhi(Math.PI / 2, 0.00001).size();
	}
	
	
	public List<Point3i> voxelsOfPhi(double phi, double stepSizeInRadians) {
		Set<Point3i> set = new HashSet<Point3i>();
		List<Point3i> list = new ArrayList<Point3i>();
		for (double theta = 0; theta < 2*Math.PI; theta += stepSizeInRadians) {
			Point3i cartesian = sphericalToCartesianGrid(phi, theta);
			if (set.add(cartesian))
				list.add(cartesian);
		}
		return list;
	}
	
	
	public List<Point3i> voxelsOfTheta(double theta, double stepSizeInRadians) {
		Set<Point3i> set = new HashSet<Point3i>();
		List<Point3i> list = new ArrayList<Point3i>();
		for (double phi = 0; phi < 2*Math.PI; phi += stepSizeInRadians) {
			Point3i cartesian = sphericalToCartesianGrid(phi, theta);
			if (set.add(cartesian))
				list.add(cartesian);
		}
		return list;
	}

	
	public double getValueOfPhiTheta(double phi, double theta) {
		Point3i point = sphericalToCartesianGrid(phi, theta);
		Double value = valuesCache.get(point);
		if (value == null) {
			value = projector.maxAlongRay(center, point);
			valuesCache.put(point, value);
		}
		return value;
	}

	
	public Point3i sphericalToCartesianGrid(double phi, double theta) {
//		phi += Math.cos(theta) * poleOffsetPhi;
//		theta += poleOffsetTheta;
//		theta += Math.cos(theta) * poleOffsetPhi;
		theta += zeroMeridianOffset;
		final double radiusSinPhi = radius * Math.sin(phi);
		int x = (int)Math.round( radiusSinPhi * Math.cos(theta) + center.x );
		int y = (int)Math.round( radiusSinPhi * Math.sin(theta) + center.y );
		int z = (int)Math.round( radius * Math.cos(phi) + center.z );
		return new Point3i(x, y, z);
	}
	
	
	public Set<Point3i> getAllSurfaceVoxels() {
		IJ.showStatus("Computing all unit sphere surface voxels ...");
		double minStepSizeRad = (2*Math.PI / getVoxelCountAtEquator()) * 0.75;
		Set<Point3i> set = new HashSet<Point3i>();
		for (double phi = 0; phi <= Math.PI; phi += minStepSizeRad) {
			for (double theta = 0d; theta < 2*Math.PI; theta += minStepSizeRad) {
				Point3i point = sphericalToCartesianGrid(phi, theta);
				set.add(point);
			}
			IJ.showProgress(phi / Math.PI);
		}
		return set;
	}
	
	
//	public Point3d cartesianToSpherical(double x, double y, double z) {
//		x -= center.x;
//		y -= center.y;
//		z -= center.z;
//		double r = Math.sqrt( Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2) );
//		double phi = Math.acos(z / r);
//		double theta = Math.atan2(y, x);
//		if (theta < 0)
//			theta += 2 * Math.PI;
//		return new Point3d(phi, theta, r);
//	}
//	
//	
//	public void dirtyDebugCoordinateConversion() {
//		for (double phi = Math.toRadians(0.0); phi <= Math.toRadians(360.0); phi += initialStepSizeToBruteforceHemicircleRad * 500) {
//			for (double theta = Math.toRadians(0.0); theta <= Math.toRadians(360.0); theta += initialStepSizeToBruteforceHemicircleRad * 500) {
//				Point3d cartesian = sphericalToCartesianSubgrid(phi, theta);
//				Point3d spherical = cartesianToSpherical(cartesian.x, cartesian.y, cartesian.z);
//				Point3d turnedCartesian = sphericalToCartesianSubgrid(spherical.x, spherical.y);
//				IJ.log(String.format("%.2f, %.2f, %.2f   -->   %.2f, %.2f, %.2f", Math.toDegrees(phi), Math.toDegrees(theta), (float)radius, cartesian.x, cartesian.y, cartesian.z));
//				IJ.log(String.format("%.2f, %.2f, %.2f   -->   %.2f, %.2f, %.2f", Math.toDegrees(spherical.x), Math.toDegrees(spherical.y), spherical.z, turnedCartesian.x, turnedCartesian.y, turnedCartesian.z));
//				IJ.log("");
//			}
//		}
//	}
	
}
