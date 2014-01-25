package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.RealTransform;

public class AzimuthalEquidistantToSphericalTransform implements RealTransform, Interval {

    private final double outerRadius, radiusInverval, maxPlanarRadius;
    private final double[] temp = new double[3];
    private final double[] planarCartesian = new double[2];
    private final double[] planarPolar = new double[2];
    private final long[] azimuthalEquidistantDimensions = new long[3];
    private final PolarToCartesianTransform polarToCartesianTransform;

    public AzimuthalEquidistantToSphericalTransform(final double innerRadius, final double outerRadius, final double standardRadiusOffset, final double scale) {
        this.outerRadius = outerRadius;
        radiusInverval = this.outerRadius - innerRadius + 1;
        final double stdRadius = innerRadius + standardRadiusOffset * radiusInverval;
        azimuthalEquidistantDimensions[0] = Math.round(scale * stdRadius * Math.PI); // 0-2PI, excl. 2PI
        azimuthalEquidistantDimensions[1] = azimuthalEquidistantDimensions[0];
        azimuthalEquidistantDimensions[2] = Math.round(scale * radiusInverval);

        polarToCartesianTransform = new PolarToCartesianTransform();
        planarCartesian[0] = 0.5d * azimuthalEquidistantDimensions[0];
        planarCartesian[1] = 0.5d * azimuthalEquidistantDimensions[1];
        polarToCartesianTransform.applyInverse(planarPolar, planarCartesian);
        maxPlanarRadius = planarPolar[0];
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        azimuthalEquidistantToSpherical(source[0], source[1], source[2], target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        azimuthalEquidistantToSpherical(source[0], source[1], source[2], temp);
        for (int d = 0; d < temp.length; ++d) {
            target[d] = (float) temp[d];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        azimuthalEquidistantToSpherical(source.getDoublePosition(0), source.getDoublePosition(1), source.getDoublePosition(2), temp);
        target.setPosition(temp);
    }

    // ToDo: The += Math.PI correction of polar and azimuth should not be required.
    private void azimuthalEquidistantToSpherical(final double x, final double y, final double z, final double[] target) {
        target[0] = outerRadius - z * radiusInverval / azimuthalEquidistantDimensions[2];
        planarCartesian[0] = x - 0.5d * azimuthalEquidistantDimensions[0];
        planarCartesian[1] = y - 0.5d * azimuthalEquidistantDimensions[1];
        polarToCartesianTransform.applyInverse(planarPolar, planarCartesian);
        target[1] = planarPolar[0] / maxPlanarRadius * Math.PI + Math.PI;
        target[2] = planarPolar[1] + Math.PI;
    }

    @Override
    public int numSourceDimensions() {
        return 3;
    }

    @Override
    public int numTargetDimensions() {
        return 3;
    }

    @Override
    public RealTransform copy() {
        return this;
    }

    @Override
    public long min(final int d) {
        return 0;
    }

    @Override
    public void min(final long[] min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min[d] = 0;
        }
    }

    @Override
    public void min(final Positionable min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min.setPosition(0, d);
        }
    }

    @Override
    public long max(final int d) {
        return azimuthalEquidistantDimensions[d] - 1;
    }

    @Override
    public void max(final long[] max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max[d] = azimuthalEquidistantDimensions[d] - 1;
        }
    }

    @Override
    public void max(final Positionable max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max.setPosition(azimuthalEquidistantDimensions[d] - 1, d);
        }
    }

    @Override
    public void dimensions(final long[] dimensions) {
        System.arraycopy(azimuthalEquidistantDimensions, 0, dimensions, 0, azimuthalEquidistantDimensions.length);
    }

    @Override
    public long dimension(final int d) {
        return azimuthalEquidistantDimensions[d];
    }

    @Override
    public double realMin(final int d) {
        return 0;
    }

    @Override
    public void realMin(final double[] min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min[d] = 0;
        }
    }

    @Override
    public void realMin(final RealPositionable min) {
        for (int d = 0; d < numDimensions(); ++d) {
            min.setPosition(0, d);
        }
    }

    @Override
    public double realMax(final int d) {
        return azimuthalEquidistantDimensions[d] - 1;
    }

    @Override
    public void realMax(final double[] max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max[d] = azimuthalEquidistantDimensions[d] - 1;
        }
    }

    @Override
    public void realMax(final RealPositionable max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max.setPosition(azimuthalEquidistantDimensions[d] - 1, d);
        }
    }

    @Override
    public int numDimensions() {
        return 3;
    }
}
