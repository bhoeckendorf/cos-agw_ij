package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.RealTransform;

public class EllipticCylindricalToCartesianTransform extends EllipticToCartesianTransform {

    @Override
    public void apply(double[] source, double[] target) {
        super.apply(source, target);
        target[2] = source[2];
    }

    @Override
    public void apply(float[] source, float[] target) {
        super.apply(source, target);
        target[2] = source[2];
    }

    @Override
    public void apply(RealLocalizable source, RealPositionable target) {
        super.apply(source, target);
        target.setPosition(source.getDoublePosition(2), 2);
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
}
