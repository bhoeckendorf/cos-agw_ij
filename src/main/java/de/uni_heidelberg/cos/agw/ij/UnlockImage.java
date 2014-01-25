package de.uni_heidelberg.cos.agw.ij;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class UnlockImage implements PlugInFilter {

    @Override
    public int setup(String args, ImagePlus imp) {
        try {
            if (imp.isLocked()) {
                imp.unlock();
            }
        } catch (NullPointerException e) {
        }
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
    }
}
