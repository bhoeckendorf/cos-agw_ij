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

import java.util.HashSet;
import java.util.Set;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

public class Sphere {

    private Point3i origin = new Point3i(0, 0, 0);
    private double radius = 0;
    private Transform3D poleAxisLatTransform = new Transform3D();
    private Transform3D poleAxisLonTransform = new Transform3D();
    private Transform3D zeroMeridianTransform = new Transform3D();
    private Transform3D translation = new Transform3D();

    public void setOrigin(final Point3i point) {
        origin.x = point.x - 1;
        origin.y = point.y - 1;
        origin.z = point.z;
        translation.setTranslation(new Vector3d(origin.x, origin.y, origin.z));
    }

    public void setRadius(final double radius) {
        this.radius = radius;
    }

    public void setPoleAxisLatAngle(final double degrees) {
        poleAxisLatTransform.rotY(Math.toRadians(degrees));
    }

    public void setPoleAxisLonAngle(final double degrees) {
        poleAxisLonTransform.rotX(Math.toRadians(degrees));
    }

    public void setZeroMeridian(final double degrees) {
        zeroMeridianTransform.rotZ(Math.toRadians(degrees));
    }

    public int getVoxelCountAtEquator() {
        Set<Point3i> set = new HashSet<Point3i>();
        for (double theta = 0; theta < 2 * Math.PI; theta += 0.000005) {
            Point3i cartesian = getCartesianGrid(Math.PI / 2, theta, radius, false);
            set.add(cartesian);
        }
        return set.size();
    }

    public Point3d getCartesian(final double lon, final double lat, final double radius) {
        final double radiusSinLon = radius * Math.sin(lon);
        double x = radiusSinLon * Math.cos(lat);
        double y = radiusSinLon * Math.sin(lat);
        double z = radius * Math.cos(lon);
        return new Point3d(x, y, z);
    }

    public Point3i getCartesianGrid(final double lon, final double lat, final double radius, final boolean doTransform) {
        Point3d point = getCartesian(lat, lon, radius);
        if (doTransform) {
            zeroMeridianTransform.transform(point);
            poleAxisLonTransform.transform(point);
            poleAxisLatTransform.transform(point);
            translation.transform(point);
        }
        return new Point3i((int) Math.round(point.x), (int) Math.round(point.y), (int) Math.round(point.z));
    }
}
