package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;

public class GeographicToSphericalTransform implements InvertibleRealTransform {

    private final double HALF_PI = 0.5d * Math.PI;
    private final double TWO_PI = 2d * Math.PI;
    private final double[] temp = new double[3];
    private final InverseRealTransform inverse;

    public GeographicToSphericalTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        geographicToSpherical(source[0], source[1], source[2], target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        geographicToSpherical(source[0], source[1], source[2], temp);
        for (int i = 0; i < temp.length; ++i) {
            target[i] = (float) temp[i];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        geographicToSpherical(source.getDoublePosition(0), source.getDoublePosition(1), source.getDoublePosition(2), temp);
        target.setPosition(temp);
    }

    @Override
    public void applyInverse(final double[] source, final double[] target) {
        sphericalToGeographic(target[0], target[1], target[2], source);
    }

    @Override
    public void applyInverse(final float[] source, final float[] target) {
        sphericalToGeographic(target[0], target[1], target[2], temp);
        for (int i = 0; i < temp.length; ++i) {
            source[i] = (float) temp[i];
        }
    }

    @Override
    public void applyInverse(final RealPositionable source, final RealLocalizable target) {
        sphericalToGeographic(target.getDoublePosition(0), target.getDoublePosition(1), target.getDoublePosition(2), temp);
        source.setPosition(temp);
    }

    private void geographicToSpherical(final double radius, final double latitude, final double longitude, final double[] target) {
        target[0] = radius;

        target[1] = HALF_PI - latitude;
        while (target[1] < 0) {
            if (target[1] > -Math.PI) {
                target[1] = -target[1];
                break;
            }
            target[1] += TWO_PI;
        }
        while (target[1] > Math.PI) {
            if (target[1] < TWO_PI) {
                target[1] = Math.PI - (target[1] - Math.PI);
                break;
            }
            target[1] -= TWO_PI;
        }

        target[2] = Math.PI - longitude;
        while (target[2] < 0) {
            target[2] += TWO_PI;
        }
        while (target[2] >= TWO_PI) {
            target[2] -= TWO_PI;
        }
    }

    private void sphericalToGeographic(final double radius, final double polar, final double azimuth, final double[] target) {
        target[0] = radius;
        target[1] = HALF_PI - polar;
        target[2] = Math.PI - azimuth;
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
    public InvertibleRealTransform inverse() {
        return inverse;
    }

    @Override
    public InvertibleRealTransform copy() {
        return this;
    }
}
