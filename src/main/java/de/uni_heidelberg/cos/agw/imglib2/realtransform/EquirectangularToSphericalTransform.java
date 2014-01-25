package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.RealTransform;

public class EquirectangularToSphericalTransform implements RealTransform, Interval {

    private final double outerRadius, radiusInverval;
    private final double TWO_PI = 2d * Math.PI;
    private final long[] equirectangularDimensions = new long[3];
    private final double[] temp = new double[3];

    public EquirectangularToSphericalTransform(final double innerRadius, final double outerRadius, final double standardRadiusOffset, final double scale) {
        this.outerRadius = outerRadius;
        radiusInverval = this.outerRadius - innerRadius + 1;
        final double stdRadius = innerRadius + standardRadiusOffset * radiusInverval;
        equirectangularDimensions[0] = Math.round(scale * stdRadius * TWO_PI); // 0-2PI, excl. 2PI
        equirectangularDimensions[1] = Math.round(scale * stdRadius * Math.PI) + 1; // 0-PI, incl. PI
        equirectangularDimensions[2] = Math.round(scale * radiusInverval);
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        equirectangularToSpherical(source[0], source[1], source[2], target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        equirectangularToSpherical(source[0], source[1], source[2], temp);
        for (int d = 0; d < temp.length; ++d) {
            target[d] = (float) temp[d];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        equirectangularToSpherical(source.getDoublePosition(0), source.getDoublePosition(1), source.getDoublePosition(2), temp);
        target.setPosition(temp);
    }

    private void equirectangularToSpherical(final double x, final double y, final double z, final double[] target) {
        target[0] = outerRadius - z * radiusInverval / equirectangularDimensions[2];
        target[1] = y / (equirectangularDimensions[1] - 1) * Math.PI; // 0-PI, incl. PI
        target[2] = x / equirectangularDimensions[0] * TWO_PI; // 0-2PI, excl. 2PI
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
        return equirectangularDimensions[d] - 1;
    }

    @Override
    public void max(final long[] max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max[d] = equirectangularDimensions[d] - 1;
        }
    }

    @Override
    public void max(final Positionable max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max.setPosition(equirectangularDimensions[d] - 1, d);
        }
    }

    @Override
    public void dimensions(final long[] dimensions) {
        System.arraycopy(equirectangularDimensions, 0, dimensions, 0, equirectangularDimensions.length);
    }

    @Override
    public long dimension(final int d) {
        return equirectangularDimensions[d];
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
        return equirectangularDimensions[d] - 1;
    }

    @Override
    public void realMax(final double[] max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max[d] = equirectangularDimensions[d] - 1;
        }
    }

    @Override
    public void realMax(final RealPositionable max) {
        for (int d = 0; d < numDimensions(); ++d) {
            max.setPosition(equirectangularDimensions[d] - 1, d);
        }
    }

    @Override
    public int numDimensions() {
        return 3;
    }
}
