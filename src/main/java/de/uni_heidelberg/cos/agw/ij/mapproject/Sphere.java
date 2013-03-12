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
import javax.vecmath.Point3i;
import net.imglib2.Localizable;
import net.imglib2.realtransform.AffineTransform3D;

public class Sphere {

    private double[] origin = {0, 0, 0};
    private double[] temp = {0, 0, 0};
    private double radius = 0;
    private AffineTransform3D poleAxisLonTransform = new AffineTransform3D();
    private AffineTransform3D poleAxisLatTransform = new AffineTransform3D();
    private AffineTransform3D poleAxisTransform = poleAxisLonTransform.concatenate(poleAxisLatTransform);
    private AffineTransform3D zeroMeridianTransform = new AffineTransform3D();

    public void setOrigin(final double[] point) {
        origin = point.clone();
    }

    public void setorigin(final Localizable point) {
        origin = new double[point.numDimensions()];
        point.localize(origin);
    }

    public void setRadius(final double radius) {
        this.radius = radius;
    }

    public void setPoleAxisLatAngle(final double degrees) {
        poleAxisLatTransform.rotate(1, Math.toRadians(degrees)); //rotY(Math.toRadians(degrees));
        poleAxisTransform = poleAxisLonTransform.concatenate(poleAxisLatTransform);
    }

    public void setPoleAxisLonAngle(final double degrees) {
        poleAxisLonTransform.rotate(0, Math.toRadians(degrees)); //rotX(Math.toRadians(degrees));
        poleAxisTransform = poleAxisLonTransform.concatenate(poleAxisLatTransform);
    }

    public void setZeroMeridian(final double degrees) {
        zeroMeridianTransform.rotate(2, Math.toRadians(degrees)); //rotZ(Math.toRadians(degrees));
    }

    public int getVoxelCountAtEquator() {
        Set<Point3i> set = new HashSet<Point3i>();
        double[] cartesian = new double[3];
        for (double theta = 0; theta < 2 * Math.PI; theta += 0.000005) {
            getCartesian(Math.PI / 2, theta, radius, false, cartesian);
            set.add(new Point3i((int) Math.round(cartesian[0]), (int) Math.round(cartesian[1]), (int) Math.round(cartesian[2])));
        }
        return set.size();
    }

    public void getCartesian(final double lon, final double lat, final double radius, final boolean doTransform, final double[] output) {
        //TODO: why reverse lat & lon
        final double radiusSinLon = radius * Math.sin(lat);
        output[0] = radiusSinLon * Math.cos(lon);
        output[1] = radiusSinLon * Math.sin(lon);
        output[2] = radius * Math.cos(lat);
        if (doTransform) {
            zeroMeridianTransform.apply(output, temp);
            poleAxisTransform.apply(temp, output);
            for (int i = 0; i < 3; ++i) {
                output[i] += origin[i];
            }
        }
    }
}