package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import javax.vecmath.Point3i;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class AbstractMapBasedOperation implements Operation {

    protected static Map<Integer, List<Point3i>> intensityMap =
            new HashMap<Integer, List<Point3i>>();
    protected final ImagePlus imp;
    protected final ImageStack stack;

    public AbstractMapBasedOperation(final ImagePlus imp) {
        this.imp = imp;
        stack = this.imp.getImageStack();
        if (intensityMap.isEmpty()) {
            intensityMap = getIntensityMap(this.imp);
        }
    }

    @Override
    abstract public String getName();

    @Override
    abstract public void run();

    protected void postRun() {
        imp.setRoi(null, false);
        imp.updateAndDraw();
    }

    protected final Map<Integer, List<Point3i>> getIntensityMap(ImagePlus imp) {
        Map<Integer, List<Point3i>> map = new HashMap<Integer, List<Point3i>>();
        final ImageStack stack = imp.getStack();
        for (int z = 1; z <= stack.getSize(); ++z) {
            ImageProcessor ip = stack.getProcessor(z);
            for (int y = 0; y < ip.getHeight(); ++y) {
                for (int x = 0; x < ip.getWidth(); ++x) {
                    final int value = ip.getPixel(x, y);
                    if (value == 0) {
                        continue;
                    }
                    List<Point3i> points = map.get(value);
                    if (points == null) {
                        points = new ArrayList<Point3i>();
                        map.put(value, points);
                    }
                    Point3i point = new Point3i(x, y, z);
                    points.add(point);
                }
            }
        }
        return map;
    }

    protected final int getFreeValue() {
        int value = 1;
        while (intensityMap.containsKey(value)) {
            value++;
        }
        return value;
    }

    public static final void clearIntensityMap() {
        intensityMap.clear();
    }
}
