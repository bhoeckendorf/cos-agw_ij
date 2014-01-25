package de.uni_heidelberg.cos.agw.ij;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.measure.Calibration;
import net.imglib2.RealPoint;

import java.util.ArrayList;
import java.util.List;

public class OrthogonalViewsPointRoi {

    private ImagePlus[] images; //inputImp, xzImp, yzImp
    private Overlay[] overlays;
    private double anisotropy;
    private List<RealPoint> points;

    public OrthogonalViewsPointRoi(final ImagePlus imp) {
        images = new ImagePlus[3];
        images[0] = imp;
        int[] ids = WindowManager.getIDList();
        for (int id : ids) {
            ImagePlus i = WindowManager.getImage(id);
            String title = i.getTitle();
            if (title.startsWith("XZ ")) {
                images[1] = i;
            } else if (title.startsWith("YZ ")) {
                images[2] = i;
            }
        }
        overlays = new Overlay[]{new Overlay(), new Overlay(), new Overlay()};
        anisotropy = getAnisotropy();
        points = new ArrayList<RealPoint>();
        points.add(new RealPoint(new double[]{0, 0, 0, 0}));
        points.add(new RealPoint(new double[]{0, 0, 0, 0}));
        points.add(new RealPoint(new double[]{0, 0, 0, 0}));
    }

    public OrthogonalViewsPointRoi(final ImagePlus imp, final ImagePlus xz, final ImagePlus yz) {
        images = new ImagePlus[3];
        images[0] = imp;
        images[1] = xz;
        images[2] = yz;
        overlays = new Overlay[]{new Overlay(), new Overlay(), new Overlay()};
        anisotropy = getAnisotropy();
        points = new ArrayList<RealPoint>();
        points.add(new RealPoint(new double[]{0, 0, 0, 0}));
        points.add(new RealPoint(new double[]{0, 0, 0, 0}));
        points.add(new RealPoint(new double[]{0, 0, 0, 0}));
    }

    public void addPoint(final double[] point) {
        points.add(new RealPoint(point));
        update();
    }

    public void removePoint(final int index) {
        points.remove(index);
        update();
    }

    public void setPoint(final int index, final double[] point) {
        points.set(index, new RealPoint(point));
        update();
    }

    public void update() {
        PointRoi[] rois = getRois();
        for (int i = 0; i < overlays.length; ++i) {
            Overlay overlay = overlays[i];
            overlay.clear();
            overlay.add(rois[i]);
            images[i].setOverlay(overlay);
        }
    }

    public PointRoi[] getRois() {
        float[][] points = getArray();
        PointRoi[] rois = new PointRoi[3];
        rois[0] = new PointRoi(points[0].clone(), points[1].clone(), points.length);
        rois[1] = new PointRoi(points[0].clone(), correctForAnisotropy(points[2].clone()), points.length);
        rois[2] = new PointRoi(correctForAnisotropy(points[2].clone()), points[1].clone(), points.length);
        return rois;
    }

    private float[][] getArray() {
        final int nPoints = points.size();
        final int nDimensions = 3;
        float[][] array = new float[nDimensions][nPoints];
        try {
            double[] position = new double[points.get(0).numDimensions()];
            for (int p = 0; p < nPoints; ++p) {
                points.get(p).localize(position);
                for (int d = 0; d < nDimensions; ++d) {
                    array[d][p] = (float) position[d];
                }
            }
        } catch (NullPointerException ex) {
        }
        return array;
    }

    private float[] correctForAnisotropy(final float[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = (float) (anisotropy * array[i]);
        }
        return result;
    }

    private double getAnisotropy() {
        Calibration cal = images[0].getCalibration();
        return cal.pixelDepth / cal.pixelWidth;
    }
}