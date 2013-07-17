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
package de.uni_heidelberg.cos.agw.ij.regions;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.Button;
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