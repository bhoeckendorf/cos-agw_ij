package de.uni_heidelberg.cos.agw.ij.util;

import ij.measure.Calibration;

import javax.vecmath.Point2d;
import javax.vecmath.Point2i;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

public class Util {

    public static String addToFilename(final String filename,
                                       final String addition) {
        final int dotIdx = filename.lastIndexOf(".");
        if (dotIdx < 0 || dotIdx < filename.length() - 6) {
            return filename + addition;
        }
        return filename.substring(0, dotIdx) + addition
                + filename.substring(dotIdx);
    }

    public static Point2i getGridPosition(final Point2d point) {
        Point2i pixel = new Point2i();
        pixel.x = (int) Math.round(point.x);
        pixel.y = (int) Math.round(point.y);
        return pixel;
    }

    public static Point2d calibratePoint(final Point2i point,
                                         final Calibration calibration) {
        Point2d calibratedPoint = new Point2d();
        calibratedPoint.x = point.x * calibration.pixelWidth;
        calibratedPoint.y = point.y * calibration.pixelHeight;
        return calibratedPoint;
    }

    public static Point2d calibratePoint(final Point2d point,
                                         final Calibration calibration) {
        Point2d calibratedPoint = new Point2d();
        calibratedPoint.x = point.x * calibration.pixelWidth;
        calibratedPoint.y = point.y * calibration.pixelHeight;
        return calibratedPoint;
    }

    public static Point2d getSubGridPosition(final Point2d point,
                                             final Calibration calibration) {
        Point2d pixel = new Point2d();
        pixel.x = point.x / calibration.pixelWidth;
        pixel.y = point.y / calibration.pixelHeight;
        return pixel;
    }

    public static Point2i getGridPosition(final Point2d point,
                                          final Calibration calibration) {
        Point2i pixel = new Point2i();
        pixel.x = (int) Math.round(point.x / calibration.pixelWidth);
        pixel.y = (int) Math.round(point.y / calibration.pixelHeight);
        return pixel;
    }

    public static Point3i getGridPosition(final Point3d point) {
        Point3i pixel = new Point3i();
        pixel.x = (int) Math.round(point.x);
        pixel.y = (int) Math.round(point.y);
        pixel.z = (int) Math.round(point.z);
        return pixel;
    }

    public static Point3d calibratePoint(final Point3i point,
                                         final Calibration calibration) {
        Point3d calibratedPoint = new Point3d();
        calibratedPoint.x = point.x * calibration.pixelWidth;
        calibratedPoint.y = point.y * calibration.pixelHeight;
        calibratedPoint.z = (point.z - 1) * calibration.pixelDepth;
        return calibratedPoint;
    }

    public static Point3d calibratePoint(final Point3d point,
                                         final Calibration calibration) {
        Point3d calibratedPoint = new Point3d();
        calibratedPoint.x = point.x * calibration.pixelWidth;
        calibratedPoint.y = point.y * calibration.pixelHeight;
        calibratedPoint.z = (point.z - 1) * calibration.pixelDepth;
        return calibratedPoint;
    }

    public static Point3d getSubGridPosition(final Point3d point,
                                             final Calibration calibration) {
        Point3d pixel = new Point3d();
        pixel.x = point.x / calibration.pixelWidth;
        pixel.y = point.y / calibration.pixelHeight;
        pixel.z = point.z / calibration.pixelDepth + 1;
        return pixel;
    }

    public static Point3i getGridPosition(final Point3d point,
                                          final Calibration calibration) {
        Point3i pixel = new Point3i();
        pixel.x = (int) Math.round(point.x / calibration.pixelWidth);
        pixel.y = (int) Math.round(point.y / calibration.pixelHeight);
        pixel.z = (int) Math.round(point.z / calibration.pixelDepth) + 1;
        return pixel;
    }
}
