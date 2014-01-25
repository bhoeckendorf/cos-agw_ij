package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;

public class CylindricalToCartesianTransform extends PolarToCartesianTransform {

    private final InverseRealTransform inverse;

    public CylindricalToCartesianTransform() {
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        super.apply(source, target);
        target[2] = source[2];
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        super.apply(source, target);
        target[2] = source[2];
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        super.apply(source, target);
        target.setPosition(source.getDoublePosition(2), 2);
    }

    @Override
    public void applyInverse(final double[] source, final double[] target) {
        super.applyInverse(source, target);
        source[2] = target[2];
    }

    @Override
    public void applyInverse(final float[] source, final float[] target) {
        super.applyInverse(source, target);
        source[2] = target[2];
    }

    @Override
    public void applyInverse(final RealPositionable source, final RealLocalizable target) {
        super.applyInverse(source, target);
        source.setPosition(target.getDoublePosition(2), 2);
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