package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.vecmath.Point3i;
import java.util.List;

public class MergeOperation extends AbstractMapBasedMultiPointOperation {

    public MergeOperation(ImagePlus imp) {
        super(imp);
    }

    @Override
    public String getName() {
        return "Merge";
    }

    @Override
    public void run() {
        List<Integer> selectedValues = getSelectedValues();
        if (selectedValues.size() < 2) {
            return;
        }

        int mergedValue = selectedValues.get(0);
        for (int i = 1; i < selectedValues.size(); ++i) {
            int value = selectedValues.get(i);
            List<Point3i> pixels = intensityMap.remove(value);
            if (pixels == null) {
                continue;
            }

            ImageProcessor stackIp;
            for (Point3i pixel : pixels) {
                stackIp = stack.getProcessor(pixel.z);
                stackIp.putPixel(pixel.x, pixel.y, mergedValue);
            }
            intensityMap.get(mergedValue).addAll(pixels);
        }
        postRun();
    }
}
