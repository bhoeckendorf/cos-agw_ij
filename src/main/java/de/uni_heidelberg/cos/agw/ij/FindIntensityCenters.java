/*
 * This file is part of the COS AGW ImageJ plugin bundle.
 * https://github.com/bhoeckendorf/cos-agw_ij
 *
 * Copyright 2012 B. Hoeckendorf <b.hoeckendorf at web dot de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.uni_heidelberg.cos.agw.ij;

import de.uni_heidelberg.cos.agw.ij.util.Util;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
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
        if (dialog.wasCanceled()) {
            return;
        }

        final boolean doDisplayTable = dialog.getNextBoolean();
        final boolean doDisplayImage = dialog.getNextBoolean();
        final int pointRadius = (int) Math.round(dialog.getNextNumber());
        final int width = inputImp.getWidth();
        final int height = inputImp.getHeight();
        final int nplanes = inputImp.getStackSize();

        // Read intensity values from image, store in data structure.
        // Map<intensity, List<sumX, sumY, sumZ, n>>
        Map<Integer, List<Integer>> data = new HashMap<Integer, List<Integer>>();
        final ImageStack inputStack = inputImp.getImageStack();
        for (int z = 1; z <= nplanes; ++z) {
            final ImageProcessor ip = inputStack.getProcessor(z);
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    final int value = ip.getPixel(x, y);
                    if (value == 0) {
                        continue;
                    }
                    List<Integer> valueData = data.get(value);
                    if (valueData == null) {
                        final int nfields = 4;
                        valueData = new ArrayList<Integer>(nfields);
                        for (int i = 0; i < nfields; ++i) {
                            valueData.add(0);
                        }
                        data.put(value, valueData);
                    }
                    final int[] pointData = new int[]{x, y, z, 1};
                    for (int i = 0; i < pointData.length; ++i) {
                        valueData.set(i, valueData.get(i) + pointData[i]);
                    }
                }
            }
        }

        // Generate value, center map.
        Map<Integer, Point3i> pointMap = new HashMap<Integer, Point3i>();
        for (final int value : data.keySet()) {
            List<Integer> valueData = data.get(value);
            final int n = valueData.get(3);
            final Point3i point = new Point3i();
            point.x = (int) Math.round(valueData.get(0) / n);
            point.y = (int) Math.round(valueData.get(1) / n);
            point.z = (int) Math.round(valueData.get(2) / n);
            pointMap.put(value, point);
        }

        // Create and show an output image, if requested.
        if (doDisplayImage) {
            final ImageStack outputStack = new ImageStack(width, height);
            for (int z = 1; z <= nplanes; ++z) {
                outputStack.addSlice("", Util.newProcessor(inputImp, width, height));
            }

            for (final int value : pointMap.keySet()) {
                final Point3i point = pointMap.get(value);
                for (int z = point.z - pointRadius; z <= point.z + pointRadius; ++z) {
                    if (z < 1 || z > nplanes) {
                        continue;
                    }
                    final ImageProcessor ip = outputStack.getProcessor(z);
                    for (int x = point.x - pointRadius; x <= point.x
                            + pointRadius; ++x) {
                        for (int y = point.y - pointRadius; y <= point.y
                                + pointRadius; ++y) {
                            ip.putPixel(x, y, value);
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

                // ImageJ's first z is 1, not 0
                table.addValue("z", point.z - 1);
            }
            table.show("Intensity centers");
        }
    }
}
