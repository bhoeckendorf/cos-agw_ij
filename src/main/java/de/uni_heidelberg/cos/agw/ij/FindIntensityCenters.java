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

import de.uni_heidelberg.cos.agw.ij.util.Util;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 * Computes the average 2D or 3D position of discrete intensities in an image;
 * intended to be used on connected components. Result can be displayed as plot
 * in a new image, as a table, or both. Entries in the table can be pixel grid
 * positions or calibrated positions.
 *
 * @todo Center of Mass computation using the connected components as mask for
 * raw intensity values.
 */
public class FindIntensityCenters implements PlugInFilter {

    private ImagePlus inputImp = null;
    private Map<Integer, Point3d> centerMap = null;
    private final String outputTitleAddition = "-centers";

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
        dialog.addCheckbox("Calibrate table", true);
        dialog.addCheckbox("Display image", false);
        dialog.addNumericField("Point radius", 0, 0);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        final boolean doDisplayTable = dialog.getNextBoolean();
        final boolean doCalibrateTable = dialog.getNextBoolean();
        final boolean doDisplayImage = dialog.getNextBoolean();
        final int pointRadius = (int) Math.round(dialog.getNextNumber());

        centerMap = getCenterMap(inputImp);
        if (doDisplayTable) {
            ResultsTable resultsTable;
            String title = Util.addToFilename(inputImp.getTitle(),
                                              outputTitleAddition) + " - in ";
            if (!doCalibrateTable) {
                resultsTable = getResultsTable(centerMap);
                title += "pixels";
            } else {
                resultsTable = getResultsTable(centerMap, inputImp.getCalibration());
                title += inputImp.getCalibration().getUnits();
            }
            resultsTable.show(title);
        }
        if (doDisplayImage) {
            int[] dimensions = new int[]{inputImp.getWidth(),
                inputImp.getHeight(), inputImp.getStackSize()};
            ImagePlus outputImp = getPlot(centerMap, pointRadius, dimensions);
            outputImp.show();
        }
    }

    /**
     * Returns a map holding value, center pairs in pixel grid positions.
     *
     * @param imp input
     * @return value, center map
     */
    private Map<Integer, Point3d> getCenterMap(final ImagePlus imp) {
        // Read intensity values from image, store in data structure.
        // Map<intensity, List<sumX, sumY, sumZ, n>>
        Map<Integer, List<Integer>> data = new HashMap<Integer, List<Integer>>();
        final ImageStack stack = imp.getImageStack();
        for (int z = 1; z <= stack.getSize(); ++z) {
            final ImageProcessor ip = stack.getProcessor(z);
            for (int y = 0; y < imp.getHeight(); ++y) {
                for (int x = 0; x < imp.getWidth(); ++x) {
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
        Map<Integer, Point3d> centerMap = new HashMap<Integer, Point3d>();
        for (final int value : data.keySet()) {
            List<Integer> valueData = data.get(value);
            final int n = valueData.get(3);
            final Point3d center = new Point3d();
            center.x = (double) valueData.get(0) / n;
            center.y = (double) valueData.get(1) / n;
            center.z = (double) valueData.get(2) / n;
            centerMap.put(value, center);
        }

        return centerMap;
    }

    /**
     * Returns an image showing the center positions. Center positions are drawn
     * as squares with a size given as pointRadius.
     *
     * @param imp input
     * @param pointRadius the radius of drawn center positions
     * @return image showing center positions
     */
    public ImagePlus getPlot(final ImagePlus imp, final int pointRadius) {
        if (imp != inputImp) {
            inputImp = imp;
            centerMap = null;
        }
        if (centerMap == null) {
            centerMap = getCenterMap(imp);
        }
        int[] dimensions = new int[]{imp.getWidth(), imp.getHeight(), imp.getStackSize()};
        ImagePlus plotImp = getPlot(centerMap, pointRadius, dimensions);
        return plotImp;
    }

    /**
     * @see FindIntensityCenters#getPlot(ij.ImagePlus, int)
     * @param pointMap value, center map as produced by
     * {@link #getCenterMap(ij.ImagePlus)}
     * @param pointRadius the radius of drawn center positions
     * @param dimensions the dimensions of the output image {x, y, z}, should be
     * identical to the ones of input image
     * @return image showing center positions
     */
    private ImagePlus getPlot(final Map<Integer, Point3d> pointMap,
            final int pointRadius, final int[] dimensions) {
        final ImageStack outputStack = new ImageStack(dimensions[0], dimensions[1]);
        for (int z = 1; z <= dimensions[2]; ++z) {
            outputStack.addSlice("", Util.newProcessor(inputImp, outputStack.getWidth(), outputStack.getHeight()));
        }

        for (final int value : pointMap.keySet()) {
            final Point3i point = Util.getGridPosition(pointMap.get(value));
            for (int z = point.z - pointRadius; z <= point.z + pointRadius; ++z) {
                if (z < 1 || z > dimensions[2]) {
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

        ImagePlus plotImp = new ImagePlus("", outputStack);
        plotImp.setTitle(Util.addToFilename(inputImp.getTitle(), outputTitleAddition));
        Util.copyLutAndCalibration(inputImp, plotImp);
        return plotImp;
    }

    /**
     * Returns a ResultsTable holding the value and center positions.
     *
     * @param imp input
     * @param doCalibrate whether the center positions should be calibrated to
     * real world dimensions or remain as pixel grid positions
     * @return table
     */
    public ResultsTable getResultsTable(final ImagePlus imp, final boolean doCalibrate) {
        if (imp != inputImp) {
            inputImp = imp;
            centerMap = null;
        }
        if (centerMap == null) {
            centerMap = getCenterMap(imp);
        }
        if (!doCalibrate) {
            return getResultsTable(centerMap);
        } else {
            return getResultsTable(centerMap, imp.getCalibration());
        }
    }

    /**
     * @see ResultsTable#getResultsTable(ij.ImagePlus, boolean). Table shows
     * non-calibrated pixel grid positions
     *
     * @param pointMap value, center map as produced by
     * {@link #getCenterMap(ij.ImagePlus)}
     * @return table
     */
    private ResultsTable getResultsTable(final Map<Integer, Point3d> pointMap) {
        ResultsTable table = new ResultsTable();
        table.setPrecision(2);
        List<Integer> values = new ArrayList<Integer>(pointMap.keySet());
        Collections.sort(values);
        for (int value : values) {
            table.incrementCounter();
            Point3d point = pointMap.get(value);
            table.addValue("Value", value);
            table.addValue("x", point.x);
            table.addValue("y", point.y);

            // ImageJ's first z is 1, not 0
            table.addValue("z", point.z - 1);
        }
        return table;
    }

    /**
     * @see ResultsTable#getResultsTable(ij.ImagePlus, boolean). Table shows
     * calibrated positions.
     *
     * @param pointMap value, center map as produced by
     * {@link #getCenterMap(ij.ImagePlus)}
     * @return table
     */
    private ResultsTable getResultsTable(final Map<Integer, Point3d> pointMap, final Calibration calibration) {
        ResultsTable table = new ResultsTable();
        table.setPrecision(2);
        List<Integer> values = new ArrayList<Integer>(pointMap.keySet());
        Collections.sort(values);
        for (int value : values) {
            table.incrementCounter();
            Point3d point = Util.calibratePoint(pointMap.get(value), calibration);
            table.addValue("Value", value);
            table.addValue("x", point.x);
            table.addValue("y", point.y);

            // ImageJ's first z is 1, not 0,
            // which in this case is handled by Util.calibratePoint
            table.addValue("z", point.z);
        }
        return table;
    }
}
