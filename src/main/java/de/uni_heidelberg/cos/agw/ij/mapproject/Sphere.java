/*
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
package de.uni_heidelberg.cos.agw.ij.mapproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

public class Sphere {

    private final Point3i center = new Point3i(0, 0, 0);
    private final Vector3d vector = new Vector3d(center.x, center.y, center.z);
    private final int radius;
    private double poleOffset = 0,
            zeroMeridianOffset = 0;
    private final Matrix3d poleOffsetMatrix = new Matrix3d();
    private Transform3D transform;

    public Sphere(Point3i center, final int radius) {
        center.x = center.x - 1;
        center.y = center.y - 1;
        center.z = center.z;
        vector.x = center.x;
        vector.y = center.y;
        vector.z = center.z;
        this.radius = radius;
        transform = new Transform3D(poleOffsetMatrix, vector, 1d);
        poleOffsetMatrix.rotY(poleOffset);
    }

    public void setPoleOffset(double degrees) {
        poleOffset = Math.toRadians(degrees);
        poleOffsetMatrix.rotY(poleOffset);
        transform = new Transform3D(poleOffsetMatrix, vector, 1d);
    }

    public double getPoleOffset() {
        return Math.toDegrees(poleOffset);
    }

    public void setZeroMeridianOffset(double degrees) {
        zeroMeridianOffset = Math.toRadians(degrees);
    }

    public double getZeroMeridianOffset() {
        return Math.toDegrees(zeroMeridianOffset);
    }

    public int getVoxelCountAtEquator() {
        return voxelsOfPhi(Math.PI / 2, 0.00001).size();
    }

    private List<Point3i> voxelsOfPhi(double phi, double stepSizeInRadians) {
        Set<Point3i> set = new HashSet<Point3i>();
        List<Point3i> list = new ArrayList<Point3i>();
        for (double theta = 0; theta < 2 * Math.PI; theta += stepSizeInRadians) {
            Point3i cartesian = sphericalToCartesianGrid(phi, theta);
            if (set.add(cartesian)) {
                list.add(cartesian);
            }
        }
        return list;
    }

    private List<Point3i> voxelsOfTheta(double theta, double stepSizeInRadians) {
        Set<Point3i> set = new HashSet<Point3i>();
        List<Point3i> list = new ArrayList<Point3i>();
        for (double phi = 0; phi < 2 * Math.PI; phi += stepSizeInRadians) {
            Point3i cartesian = sphericalToCartesianGrid(phi, theta);
            if (set.add(cartesian)) {
                list.add(cartesian);
            }
        }
        return list;
    }

    public Point3i sphericalToCartesianGrid(final double phi, double theta) {
        theta += zeroMeridianOffset;
        final double radiusSinPhi = radius * Math.sin(phi);
        double x = radiusSinPhi * Math.cos(theta);
        double y = radiusSinPhi * Math.sin(theta);
        double z = radius * Math.cos(phi);

        Point3d point = new Point3d(x, y, z);
        transform.transform(point);

        return new Point3i((int) Math.round(point.x), (int) Math.round(point.y), (int) Math.round(point.z));
    }

    public Point3i[] sphericalToCartesianGrid(final double phi, double theta, final double[] radii) {
        theta += zeroMeridianOffset;
        final double sinPhi = Math.sin(phi);
        final double cosPhi = Math.cos(phi);
        final double sinTheta = Math.sin(theta);
        final double cosTheta = Math.cos(theta);
        Point3i[] points = new Point3i[radii.length];
        for (int i = 0; i < radii.length; ++i) {
            final double radius = radii[i];
            final double radiusSinPhi = radius * sinPhi;
            double x = radiusSinPhi * cosTheta;
            double y = radiusSinPhi * sinTheta;
            double z = radius * cosPhi;
            Point3d point = new Point3d(x, y, z);
            transform.transform(point);
            points[i] = new Point3i((int) Math.round(point.x), (int) Math.round(point.y), (int) Math.round(point.z));
        }
        return points;
    }
}
