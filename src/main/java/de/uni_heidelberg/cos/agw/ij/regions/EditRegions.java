/*
 * This file is part of the COS AGW ImageJ plugin bundle.
 * https://github.com/bhoeckendorf/cos-agw_ij
 * 
 * Copyright 2012 B. Hoeckendorf <b.hoeckendorf at web dot de>
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
		runDialog();
	}
	
	
	private void runDialog() {
		dialog = new GenericDialogPlus("Edit Regions");
		dialog.addMessage("Use the multipoint tool to\nselect the regions of interest.");
//		dialog.addButton("Split manual", this);
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
		if (!(source instanceof Button))
			return;
		String buttonLabel = ((Button)source).getLabel();
		if (buttonLabel == "Split manual")
			new SplitOperation(inputImp, "Manual").run();
		else if (buttonLabel == "Split watershed")
			new SplitOperation(inputImp, "Watershed").run();
		else if (buttonLabel == "Merge")
			new MergeOperation(inputImp).run();
		else if (buttonLabel == "Remove")
			new RemoveOperation(inputImp).run();
		else if (buttonLabel == "Clear selection")
			inputImp.setRoi(null, false);
	}

}