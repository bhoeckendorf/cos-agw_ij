package de.uni_heidelberg.cos.agw.ij.regions;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditRegions implements PlugInFilter, ActionListener {

    private ImagePlus inputImp;
    private GenericDialogPlus dialog;

    @Override
    public int setup(String args, ImagePlus imp) {
        inputImp = imp;
        return DOES_8G + DOES_16;
    }

    public void run(ImagePlus imp) {
        inputImp = imp;
        run(inputImp.getProcessor());
    }

    @Override
    public void run(ImageProcessor inputIp) {
        AbstractMapBasedOperation.clearIntensityMap();
        runDialog();
    }

    private void runDialog() {
        dialog = new GenericDialogPlus("Edit Regions");
        dialog.addMessage("Use the multipoint tool to\n"
                + "select the regions of interest.");
        // dialog.addButton("Split manual", this);
        dialog.addButton("Split watershed", this);
        dialog.addButton("Merge", this);
        dialog.addButton("Remove", this);
        dialog.addButton("Clear selection", this);
        dialog.setModal(false);
        dialog.showDialog();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button)) {
            return;
        }
        String buttonLabel = ((Button) source).getLabel();
        if ("Split manual".equals(buttonLabel)) {
            new SplitOperation(inputImp, "Manual").run();
        } else if ("Split watershed".equals(buttonLabel)) {
            new SplitOperation(inputImp, "Watershed").run();
        } else if ("Merge".equals(buttonLabel)) {
            new MergeOperation(inputImp).run();
        } else if ("Remove".equals(buttonLabel)) {
            new RemoveOperation(inputImp).run();
        } else if ("Clear selection".equals(buttonLabel)) {
            inputImp.setRoi(null, false);
        } else {
            IJ.log("Edit Regions: Warning! No suitable handler found for event "
                    + event.toString());
        }
    }
}