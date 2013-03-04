/*
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
package de.uni_heidelberg.cos.agw.ij;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Label;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ThreePointAngle implements PlugInFilter {

    private final String pluginName = "3 Point Angle";
    protected ImagePlus inputImp, xzImp, yzImp;
    private final int nPoints = 3;
    private final double[][] voxelPoints = new double[nPoints][4];
    private final double[][] calibratedPoints = new double[nPoints][4];
    protected final Button[] buttons = new Button[nPoints];
    protected final Label[] labels = new Label[nPoints];
    protected GenericDialogPlus dialog;
    private EventListener eventListener;
    protected ResultsTable table;
    private Calibration calibration;
    private String units;
    private String timeUnit;
    protected OrthogonalViewsPointRoi roi;

    @Override
    public int setup(String arg, ImagePlus imp) {
        inputImp = imp;
        return STACK_REQUIRED + DOES_ALL;
    }

    @Override
    public void run(ImageProcessor inputIp) {
        int[] ids = WindowManager.getIDList();
        for (int id : ids) {
            ImagePlus i = WindowManager.getImage(id);
            String title = i.getTitle();
            if (title.startsWith("XZ ")) {
                xzImp = i;
            } else if (title.startsWith("YZ ")) {
                yzImp = i;
            }
        }
        if (xzImp == null || yzImp == null) {
            IJ.error(pluginName, "Please start the orthogonal views first.");
            return;
        }

        roi = new OrthogonalViewsPointRoi(inputImp, xzImp, yzImp);
        dialog = new GenericDialogPlus(pluginName);
        for (int i = 0; i < nPoints; ++i) {
            Button button = new Button(String.format("Point %d", i + 1));
            Label label = new Label("n/a");
            Panel panel = new Panel();
            panel.add(button);
            panel.add(label);
            buttons[i] = button;
            labels[i] = label;
            dialog.addComponent(panel);
        }
        dialog.addButton("Show points", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                roi.update();
            }
        });
        dialog.setModalityType(Dialog.ModalityType.MODELESS);
        dialog.setOKLabel("Add");
        dialog.setCancelLabel("Exit");
        dialog.showDialog();
        eventListener = new EventListener(this);
        table = ResultsTable.getResultsTable();
        table.reset();
        table.setPrecision(1);
        calibration = inputImp.getCalibration();
        units = calibration.getUnit();
        timeUnit = calibration.getTimeUnit();
    }

    protected void setPoint(final double[] point, final int index) {
        final double[] voxelPoint = voxelPoints[index];
        for (int i = 0; i < voxelPoint.length; ++i) {
            voxelPoint[i] = point[i];
        }
        final double[] calibratedPoint = calibratedPoints[index];
        calibratedPoint[0] = calibration.getX(voxelPoint[0]);
        calibratedPoint[1] = calibration.getY(voxelPoint[1]);
        calibratedPoint[2] = calibration.getZ(voxelPoint[2]);
        calibratedPoint[3] = calibration.frameInterval * voxelPoint[3];
        updateLabel(index);
        roi.setPoint(index, point);
    }

    private double getDistance(final double[] from, final double[] to) {
        double sqDistance = 0;
        for (int i = 0; i < 3; ++i) {
            sqDistance += Math.pow(to[i] - from[i], 2);
        }
        return Math.sqrt(sqDistance);
    }

    private void updateLabel(final int index) {
        labels[index].setText(String.format("%s;      %s",
                pointToString(voxelPoints[index]), pointToString(calibratedPoints[index])));
        Color color = SystemColor.orange;
        for (int i = 0; i < labels.length; ++i) {
            Label label = labels[i];
            if (i == index && label.getBackground() == color) {
                color = SystemColor.red;
            } else if (label.getBackground() != SystemColor.control) {
                label.setBackground(SystemColor.control);
            }
        }
        labels[index].setBackground(color);
        dialog.pack();
    }

    private String pointToString(final double[] point) {
        return String.format("%06.1f, %06.1f, %06.1f, %06.1f", point[0], point[1], point[2], point[3]);
    }

    protected void addToTable() {
        table.incrementCounter();
        for (int i = 0; i < voxelPoints.length; ++i) {
            double[] voxelPoint = voxelPoints[i];
            double[] calibratedPoint = calibratedPoints[i];
            table.addValue(String.format("x%d (voxel)", i + 1), voxelPoint[0]);
            table.addValue(String.format("y%d (voxel)", i + 1), voxelPoint[1]);
            table.addValue(String.format("z%d (voxel)", i + 1), voxelPoint[2]);
            table.addValue(String.format("t%d (timepoint)", i + 1), voxelPoint[3]);
            table.addValue(String.format("x%d (%s)", i + 1, units), calibratedPoint[0]);
            table.addValue(String.format("y%d (%s)", i + 1, units), calibratedPoint[1]);
            table.addValue(String.format("z%d (%s)", i + 1, units), calibratedPoint[2]);
            table.addValue(String.format("t%d (%s)", i + 1, timeUnit), calibratedPoint[3]);
        }
        double distanceX1X2 = getDistance(calibratedPoints[0], calibratedPoints[1]);
        double distanceX1X3 = getDistance(calibratedPoints[0], calibratedPoints[2]);
        double distanceX2X3 = getDistance(calibratedPoints[1], calibratedPoints[2]);
        table.addValue(String.format("Distance p1-p2 (%s)", units), distanceX1X2);
        table.addValue(String.format("Distance p1-p3 (%s)", units), distanceX1X3);
        table.addValue(String.format("Distance p2-p3 (%s)", units), distanceX2X3);
        table.addValue("Angle1", getAngle());
        table.show("Results");
    }

    private double getAngle() {
        double distance = Double.MAX_VALUE;
        int closestToPoint3Index = -1;
        for (int i = 0; i < calibratedPoints.length - 1; ++i) {
            double nextDistance = getDistance(calibratedPoints[calibratedPoints.length - 1], calibratedPoints[i]);
            if (nextDistance < distance) {
                distance = nextDistance;
                closestToPoint3Index = i;
            }
        }
        final double[] p3 = calibratedPoints[2];
        final double[] p2 = calibratedPoints[closestToPoint3Index];
        final double[] p1 = calibratedPoints[closestToPoint3Index == 0 ? 1 : 0];
        double[] v32 = new double[3];
        double[] v31 = new double[3];
        for (int i = 0; i < 3; ++i) {
            v32[i] = p1[i] - p3[i];
            v31[i] = p1[i] - p2[i];
        }
        double scalarProduct = 0;
        double magnitude32 = 0;
        double magnitude31 = 0;
        for (int i = 0; i < v32.length; ++i) {
            scalarProduct += v32[i] * v31[i];
            magnitude32 += Math.pow(v32[i], 2);
            magnitude31 += Math.pow(v31[i], 2);
        }

        magnitude32 = Math.sqrt(magnitude32);
        magnitude31 = Math.sqrt(magnitude31);
        double angle = Math.acos(scalarProduct / (magnitude32 * magnitude31));
        angle = Math.toDegrees(angle);
//        angle -= 180d;
//        if (angle < 0) {
//            angle = -angle;
//        }
        return angle;
    }
}

class EventListener implements ActionListener, MouseListener, WindowListener {

    private final ThreePointAngle plugin;
    private Button addButton;
    private int pointIndex = -1;

    public EventListener(final ThreePointAngle plugin) {
        this.plugin = plugin;
        listen();
    }

    private void listen() {
        plugin.dialog.addWindowListener(this);
        plugin.inputImp.getCanvas().addMouseListener(this);
        plugin.xzImp.getCanvas().addMouseListener(this);
        plugin.yzImp.getCanvas().addMouseListener(this);
        for (Button button : plugin.buttons) {
            button.addActionListener(this);
        }
        addButton = plugin.dialog.getButtons()[0];
        for (ActionListener l : addButton.getListeners(ActionListener.class)) {
            addButton.removeActionListener(l);
        }
        addButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Button button = (Button) ev.getSource();
        if (button == addButton) {
            plugin.addToTable();
            return;
        }

        for (int i = 0; i < plugin.buttons.length; ++i) {
            if (button == plugin.buttons[i]) {
                if (i == pointIndex) {
                    button.setBackground(SystemColor.control);
                    pointIndex = -1;
                } else {
                    if (pointIndex != -1) {
                        plugin.buttons[pointIndex].setBackground(SystemColor.control);
                    }
                    pointIndex = i;
                    button.setBackground(SystemColor.orange);
                }
                return;
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (pointIndex == -1) {
            return;
        }
        final int x = Integer.parseInt(plugin.yzImp.getTitle().substring(3));
        final int y = Integer.parseInt(plugin.xzImp.getTitle().substring(3));
        final int z = plugin.inputImp.getZ();
        final int t = plugin.inputImp.getT();
        plugin.setPoint(new double[]{x, y, z, t}, pointIndex);
        plugin.buttons[pointIndex].setBackground(SystemColor.control);
        pointIndex = -1;
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent ev) {
        plugin.inputImp.getCanvas().removeMouseListener(this);
        plugin.xzImp.getCanvas().removeMouseListener(this);
        plugin.yzImp.getCanvas().removeMouseListener(this);
        for (Button button : plugin.buttons) {
            button.removeActionListener(this);
        }
        addButton.removeActionListener(this);
        plugin.dialog.removeWindowListener(this);
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}