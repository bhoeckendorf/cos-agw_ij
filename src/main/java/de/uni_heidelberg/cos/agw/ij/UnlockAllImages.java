package de.uni_heidelberg.cos.agw.ij;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class UnlockAllImages implements PlugInFilter {

    @Override
    public int setup(String string, ImagePlus ip) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        UnlockImage unlockImage = new UnlockImage();
        for (int id : WindowManager.getIDList()) {
            unlockImage.setup("", WindowManager.getImage(id));
        }
    }
}
