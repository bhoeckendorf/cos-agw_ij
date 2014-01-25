package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.vecmath.Point3i;
import java.util.ArrayList;
import java.util.List;

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
