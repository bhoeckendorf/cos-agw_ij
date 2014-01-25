package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.gui.Roi;

import javax.vecmath.Point3i;
import java.util.*;

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
