package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;

public class GeographicToCartesianTransform extends SphericalToCartesianTransform {

    private final double[] temp0 = new double[3];
    private final double[] temp1 = new double[3];
    private final GeographicToSphericalTransform geographicToSpherical;
    private final InverseRealTransform inverse;

    public GeographicToCartesianTransform() {
        geographicToSpherical = new GeographicToSphericalTransform();
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        geographicToSpherical.apply(source, temp0);
        super.apply(temp0, target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        for (int i = 0; i < temp0.length; ++i) {
            temp0[i] = source[i];
        }
        geographicToSpherical.apply(temp0, temp1);
        super.apply(temp1, temp0);
        for (int i = 0; i < temp0.length; ++i) {
            target[i] = (float) temp0[i];
        }
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        source.localize(temp0);
        geographicToSpherical.apply(temp0, temp1);
        super.apply(temp1, temp0);
        target.setPosition(temp0);
    }

    @Override
    public void applyInverse(final double[] source, final double[] target) {
        geographicToSpherical.applyInverse(temp0, target);
        super.applyInverse(source, temp0);
    }

    @Override
    public void applyInverse(final float[] source, final float[] target) {
        for (int i = 0; i < temp0.length; ++i) {
            temp0[i] = target[i];
        }
        geographicToSpherical.applyInverse(temp1, temp0);
        super.applyInverse(temp0, temp1);
        for (int i = 0; i < temp0.length; ++i) {
            source[i] = (float) temp0[i];
        }
    }

    @Override
    public void applyInverse(final RealPositionable source, final RealLocalizable target) {
        target.localize(temp0);
        geographicToSpherical.applyInverse(temp1, temp0);
        super.applyInverse(temp0, temp1);
        source.setPosition(temp0);
    }

    @Override
    public int numSourceDimensions() {
        return geographicToSpherical.numSourceDimensions();
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