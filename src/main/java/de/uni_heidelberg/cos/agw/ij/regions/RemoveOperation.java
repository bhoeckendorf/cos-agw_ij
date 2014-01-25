package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.vecmath.Point3i;
import java.util.List;

public class RemoveOperation extends AbstractMapBasedMultiPointOperation {

    public RemoveOperation(ImagePlus imp) {
        super(imp);
    }

    @Override
    public String getName() {
        return "Remove";
    }

    @Override
    public void run() {
        List<Integer> selectedValues = getSelectedValues();
        for (int value : selectedValues) {
            run(value);
        }
    }

    public void run(int value) {
        List<Point3i> pixels = intensityMap.remove(value);
        if (pixels == null) {
            return;
        }

        int currentZ = pixels.get(0).z;
        ImageProcessor ip = stack.getProcessor(currentZ);
        for (Point3i pixel : pixels) {
            if (pixel.z != currentZ) {
                ip = stack.getProcessor(pixel.z);
                currentZ = pixel.z;
            }
            ip.putPixel(pixel.x, pixel.y, 0);
        }
        postRun();
    }
}
