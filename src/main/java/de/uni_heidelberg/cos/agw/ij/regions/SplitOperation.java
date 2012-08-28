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

import ij.IJ;
import ij.ImagePlus;

public class SplitOperation extends AbstractMapBasedMultiPointLocalOperation {

    private final String strategy;

    public SplitOperation(ImagePlus imp, final String strategy) {
        super(imp);
        this.strategy = strategy;
    }

    @Override
    public String getName() {
        return "Split";
    }

    @Override
    public void run() {
        for (int value : getSelectedValues()) {
            if (strategy == "Manual") {
                manual(value);
            } else if (strategy == "Watershed") {
                watershed(value);
            } else {
                return;
            }
        }
    }

    private void manual(int value) {
        ImagePlus localImp = getLocalSlab(value);
        localImp.show();
    }

    private void watershed(int value) {
        ImagePlus localImp = getLocalSlab(value);
//		IJ.setThreshold(localImp, 1, 65535);
        IJ.run(localImp, "Convert to Mask", "");
        IJ.run(localImp, "Invert LUT", "");
        IJ.run(localImp, "Invert", "");
        IJ.run(localImp, "Watershed", "");
        IJ.run(localImp, "Invert", "");
        localImp.show();
        IJ.run("Find Connected Regions",
               "allow_diagonal display_one_image regions_for_values_over=100"
                + "minimum_number_of_points=1 stop_after=-1");
        ImagePlus localCcImp = IJ.getImage();
        localCcImp.setTitle(localImp.getTitle());
        localImp.hide();
        localImp.close();
        localCcImp.hide();
        putLocalSlab(localCcImp, value);
        localCcImp.clone();
    }
}