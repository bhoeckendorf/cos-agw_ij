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
package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3i;

public class AddOperation extends AbstractMapBasedOperation {

    public AddOperation(ImagePlus imp) {
        super(imp);
    }

    @Override
    public String getName() {
        return "Add";
    }

    @Override
    public void run() {
    }

    public void run(int value, List<Point3i> points) {
        for (Point3i point : points) {
            ImageProcessor ip = stack.getProcessor(point.z);
            ip.putPixel(point.x, point.y, value);
        }
        if (!intensityMap.containsKey(value)) {
            intensityMap.put(value, new ArrayList<Point3i>());
        }
        intensityMap.get(value).addAll(points);
    }
}
