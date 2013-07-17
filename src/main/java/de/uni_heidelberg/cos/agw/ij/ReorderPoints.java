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
package de.uni_heidelberg.cos.agw.ij;

import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.util.Arrays;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.collection.KDTree;
import net.imglib2.collection.RealPointSampleList;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.neighborsearch.InverseDistanceWeightingInterpolatorFactory;
import net.imglib2.interpolation.neighborsearch.NearestNeighborInterpolatorFactory;
import net.imglib2.neighborsearch.KNearestNeighborSearch;
import net.imglib2.neighborsearch.KNearestNeighborSearchOnKDTree;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

// TODO: Some of this only seems to work when using DoubleType.
public class ReorderPoints<I extends RealType<I> & NativeType<I>, C extends RealType<C> & NativeType<C>, M extends IntegerType<M> & NativeType<M>> implements PlugIn {

    private String pluginName = "Reorder Points";

    @Override
    public void run(String arg) {
        int[] params = showDialog();
        if (params == null) {
            return;
        }

        Img<I> source = ImageJFunctions.wrap(WindowManager.getImage(params[0]));
        RandomAccessibleInterval<C>[] ras = new RandomAccessibleInterval[params[4] != 1 ? 3 : 2];
        ras[0] = ImageJFunctions.wrap(WindowManager.getImage(params[2]));
        ras[1] = ImageJFunctions.wrap(WindowManager.getImage(params[3]));
        if (ras.length == 3) {
            ras[2] = ImageJFunctions.wrap(WindowManager.getImage(params[4]));
        }

        Img<M> mask = null;
        if (params[1] != 1) {
            mask = ImageJFunctions.wrap(WindowManager.getImage(params[1]));
        }

        final InterpolatorFactory<DoubleType, NearestNeighborSearch<DoubleType>> interpolation =
                params[5] == 0
                ? new NearestNeighborInterpolatorFactory()
                : new InverseDistanceWeightingInterpolatorFactory<DoubleType>();
        final int nn = params[6];

        RandomAccessibleInterval<DoubleType> output;
        if (mask != null) {
            output = compute(mask, source, ras, interpolation, nn);
        } else {
            output = compute(source, ras, interpolation, nn);
        }
        ImageJFunctions.show(output);
    }

    private int[] showDialog() {
        int[] ids = WindowManager.getIDList();
        if (ids.length < 3) {
            IJ.error(pluginName, "At least 3 images must be open.");
            return null;
        }
        String[] titles = new String[ids.length];
        String[] optionalTitles = new String[ids.length + 1];
        for (int i = 0; i < ids.length; ++i) {
            String title = WindowManager.getImage(ids[i]).getTitle();
            titles[i] = title;
            optionalTitles[i] = title;
        }
        optionalTitles[ids.length] = "*None*";

        String[] interpolations = {"Nearest Neighbor", "Linear"};

        GenericDialog dialog = new GenericDialog(pluginName);
        dialog.addChoice("Source", titles, titles[0]);
        dialog.addChoice("Mask", optionalTitles, optionalTitles[optionalTitles.length - 1]);
        dialog.addChoice("x", titles, titles[0]);
        dialog.addChoice("y", titles, titles[0]);
        dialog.addChoice("z", optionalTitles, optionalTitles[optionalTitles.length - 1]);
        dialog.addChoice("Interpolation", interpolations, interpolations[1]);
        dialog.addNumericField("Neighbors for interpolation", 10, 0);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return null;
        }

        int sourceId = ids[dialog.getNextChoiceIndex()];

        int maskId = 1;
        try {
            maskId = ids[dialog.getNextChoiceIndex()];
        } catch (ArrayIndexOutOfBoundsException ex) {
        }

        int xId = ids[dialog.getNextChoiceIndex()];
        int yId = ids[dialog.getNextChoiceIndex()];

        int zId = 1;
        try {
            zId = ids[dialog.getNextChoiceIndex()];
        } catch (ArrayIndexOutOfBoundsException ex) {
        }

        int interpolationIdx = dialog.getNextChoiceIndex();
        int nn = (int) Math.round(dialog.getNextNumber());

        int[] params = {sourceId, maskId, xId, yId, zId, interpolationIdx, nn};
        return params;
    }

    public RandomAccessibleInterval<DoubleType> compute(final IterableInterval<I> source, final RandomAccessibleInterval<C>[] coordinates, final InterpolatorFactory<DoubleType, NearestNeighborSearch<DoubleType>> interpolation, final int nn) {
        for (int i = 0; i < coordinates.length; ++i) {
            compareDimensions(source, coordinates[i]);
        }
        RealPointSampleList<DoubleType> points = getPoints(source, coordinates);
        return getRandomAccessibleInterval(points, interpolation, nn);
    }

    public RandomAccessibleInterval<DoubleType> compute(final IterableInterval<M> mask, final RandomAccessibleInterval<I> source, final RandomAccessibleInterval<C>[] coordinates, final InterpolatorFactory<DoubleType, NearestNeighborSearch<DoubleType>> interpolation, final int nn) {
        compareDimensions(source, mask);
        for (int i = 0; i < coordinates.length; ++i) {
            compareDimensions(source, coordinates[i]);
        }
        RealPointSampleList<DoubleType> points = getPoints(mask, source, coordinates);
        return getRandomAccessibleInterval(points, interpolation, nn);
    }

    private RealPointSampleList<DoubleType> getPoints(IterableInterval<I> source, RandomAccessible<C>[] coordinates) {
        final int nDimensions = coordinates.length;
        RealPointSampleList<DoubleType> points = new RealPointSampleList<DoubleType>(nDimensions);
        Cursor<I> sourceCur = source.localizingCursor();
        RandomAccess<C>[] ras = new RandomAccess[nDimensions];
        for (int i = 0; i < nDimensions; ++i) {
            ras[i] = coordinates[i].randomAccess();
        }
        double[] p = new double[nDimensions];
        while (sourceCur.hasNext()) {
            sourceCur.fwd();
            for (int i = 0; i < nDimensions; ++i) {
                RandomAccess<C> ra = ras[i];
                ra.setPosition(sourceCur);
                p[i] = ra.get().getRealDouble();
            }
            for (int i = 0; i < nDimensions; ++i) {
                if (p[i] != 0d) {
                    points.add(new RealPoint(p), new DoubleType(sourceCur.get().getRealDouble()));
                    break;
                }
            }
        }
        return points;
    }

    private RealPointSampleList<DoubleType> getPoints(IterableInterval<M> mask, RandomAccessible<I> source, RandomAccessible<C>[] coordinates) {
        final int nDimensions = coordinates.length;
        RealPointSampleList<DoubleType> points = new RealPointSampleList<DoubleType>(nDimensions);
        Cursor<M> maskCur = mask.localizingCursor();
        RandomAccess<I> sourceRa = source.randomAccess();
        RandomAccess<C>[] ras = new RandomAccess[nDimensions];
        for (int i = 0; i < nDimensions; ++i) {
            ras[i] = coordinates[i].randomAccess();
        }
        double[] p = new double[nDimensions];
        while (maskCur.hasNext()) {
            maskCur.fwd();
            if (maskCur.get().getInteger() > 0) {
                sourceRa.setPosition(maskCur);
                points.add(new RealPoint(p), new DoubleType(sourceRa.get().getRealDouble()));
            }
        }
        return points;
    }

    private <T extends Type<T>> RandomAccessibleInterval<T> getRandomAccessibleInterval(final RealPointSampleList<T> points, final InterpolatorFactory<T, NearestNeighborSearch<T>> interpolation, final int nn) {
        KNearestNeighborSearch<T> knnSearch = new KNearestNeighborSearchOnKDTree<T>(
                new KDTree<T>(points), Math.min(nn, (int) points.size()));
        RealRandomAccessible<T> realRandomAccessible = Views.interpolate(knnSearch, interpolation);
        RandomAccessible<T> randomAccessible = Views.raster(realRandomAccessible);

        final double[] bounds = new double[points.numDimensions()];
        final long[] min = new long[bounds.length];
        final long[] max = new long[bounds.length];
        points.realMin(bounds);
        for (int i = 0; i < bounds.length; ++i) {
            min[i] = Math.round(bounds[i]);
        }
        points.realMax(bounds);
        for (int i = 0; i < bounds.length; ++i) {
            max[i] = Math.round(bounds[i]);
        }

        return Views.interval(randomAccessible, min, max);
    }

    private void compareDimensions(final Interval i1, final Interval i2) {
        final long[] i1dims = new long[i1.numDimensions()];
        i1.dimensions(i1dims);
        final long[] i2dims = new long[i2.numDimensions()];
        i2.dimensions(i2dims);
        if (!Arrays.equals(i1dims, i2dims)) {
            String msg = "Dimensions of source and coordinate images do not match.";
            IJ.error(pluginName, msg);
            throw new IllegalArgumentException(msg);
        }
    }
}