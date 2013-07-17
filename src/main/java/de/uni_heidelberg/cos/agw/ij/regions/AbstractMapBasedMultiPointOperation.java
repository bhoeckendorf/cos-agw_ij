/**
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
package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.gui.Roi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point3i;

abstract public class AbstractMapBasedMultiPointOperation
        extends AbstractMapBasedOperation {

    public AbstractMapBasedMultiPointOperation(final ImagePlus imp) {
        super(imp);
    }

    protected final List<Point3i> getSelectedPoints() {
        List<Point3i> points = new ArrayList<Point3i>();
        Roi roi = imp.getRoi();
        if (roi == null) {
            return points;
        }

        float[] xs = roi.getFloatPolygon().xpoints;
        float[] ys = roi.getFloatPolygon().ypoints;
        int z = imp.getCurrentSlice();
        for (int i = 0; i < xs.length; ++i) {
            points.add(new Point3i(Math.round(xs[i]), Math.round(ys[i]), z));
        }
        return points;
    }

    protected final List<Integer> getSelectedValues() {
        Set<Integer> valuesSet = new HashSet<Integer>();
        for (Point3i point : getSelectedPoints()) {
            int value = stack.getProcessor(point.z).getPixel(point.x, point.y);
            if (value != 0) {
                valuesSet.add(value);
            }
        }
        List<Integer> valuesList = new ArrayList<Integer>(valuesSet);
        Collections.sort(valuesList);
        return valuesList;
    }
}
