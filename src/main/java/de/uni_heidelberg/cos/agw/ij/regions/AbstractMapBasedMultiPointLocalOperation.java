package de.uni_heidelberg.cos.agw.ij.regions;

import ij.ImagePlus;
import ij.ImageStack;

import javax.vecmath.Point3i;
import java.util.List;
import java.util.Map;

abstract public class AbstractMapBasedMultiPointLocalOperation extends AbstractMapBasedMultiPointOperation {

    private final int bitDepth;
    private Point3i boundsCorner = new Point3i(0, 0, 0);
    private int boundsWidth,
            boundsHeight,
            boundsDepth;

    public AbstractMapBasedMultiPointLocalOperation(ImagePlus imp) {
        super(imp);
        bitDepth = super.imp.getBitDepth();
    }

    protected final ImagePlus getLocalSlab(int value) {
        int[] bounds = getBoundsOfIntensity(value);
        boundsCorner.x = bounds[0];
        boundsCorner.y = bounds[2];
        boundsCorner.z = bounds[4];
        boundsWidth = 1 + bounds[1] - boundsCorner.x;
        boundsHeight = 1 + bounds[3] - boundsCorner.y;
        boundsDepth = 1 + bounds[5] - boundsCorner.z;

        ImageStack localStack = new ImageStack(boundsWidth, boundsHeight);
        for (int z = boundsCorner.z; z < boundsCorner.z + boundsDepth; ++z) {
            localStack.addSlice("", imp.getProcessor().createProcessor(boundsWidth, boundsHeight));
        }

        for (Point3i global : intensityMap.get(value)) {
            Point3i local = new Point3i();
            local.x = global.x - boundsCorner.x;
            local.y = global.y - boundsCorner.y;
            local.z = global.z - boundsCorner.z;
            localStack.setVoxel(local.x, local.y, local.z, 255);
        }

        ImagePlus localImp = new ImagePlus("Local " + value, localStack);
//		localImp.getProcessor().setLut(imp.getProcessor().getLut());
        return localImp;
    }

    protected final void putLocalSlab(ImagePlus imp, int value) {
        new RemoveOperation(super.imp).run(value);
        Map<Integer, List<Point3i>> localMap = getIntensityMap(imp);
        for (int localIntensity : localMap.keySet()) {
            int newGlobalValue = getFreeValue();
            List<Point3i> localPoints = localMap.get(localIntensity);
            for (Point3i point : localPoints) {
                point.x += boundsCorner.x;
                point.y += boundsCorner.y;
                point.z += boundsCorner.z - 1;
            }
            new AddOperation(super.imp).run(newGlobalValue, localPoints);
        }
    }

    protected final int[] getBoundsOfIntensity(int value) {
        List<Point3i> points = intensityMap.get(value);
        if (points == null || points.isEmpty()) {
            return new int[]{0, 0, 0, 0, 0, 0};
        }
        int minx = points.get(0).x;
        int miny = points.get(0).y;
        int minz = points.get(0).z;
        int maxx = minx;
        int maxy = miny;
        int maxz = minz;
        for (Point3i point : points) {
            if (point.x < minx) {
                minx = point.x;
            }
            if (point.x > maxx) {
                maxx = point.x;
            }
            if (point.y < miny) {
                miny = point.y;
            }
            if (point.y > maxy) {
                maxy = point.y;
            }
            if (point.z < minz) {
                minz = point.z;
            }
            if (point.z > maxz) {
                maxz = point.z;
            }
        }
        return new int[]{minx, maxx, miny, maxy, minz, maxz};
    }

    protected final int[] getBoundsOfIntensities(List<Integer> values) {
        if (values.isEmpty()) {
            return new int[]{0, 0, 0, 0, 0, 0};
        }
        int[] bounds = getBoundsOfIntensity(values.get(0));
        for (int i = 1; i < values.size(); ++i) {
            int[] nextBounds = getBoundsOfIntensity(values.get(i));
            for (int j = 0; j < bounds.length; ++j) {
                if (j == 0 || j == 2 || j == 4) {
                    if (nextBounds[j] < bounds[j]) {
                        bounds[j] = nextBounds[j];
                    }
                } else {
                    if (nextBounds[j] > bounds[j]) {
                        bounds[j] = nextBounds[j];
                    }
                }
            }
        }
        return bounds;
    }
}
