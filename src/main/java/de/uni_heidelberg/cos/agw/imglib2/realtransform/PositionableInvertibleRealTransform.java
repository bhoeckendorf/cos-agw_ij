package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;

// ToDo: Rotation produces ArrayIndexOutOfBoundsException if not constructed with 3D transform.
public class PositionableInvertibleRealTransform extends PositionableRealTransform implements InvertibleRealTransform {

    private final InvertibleRealTransform transform;
    private final InverseRealTransform inverse;

    public PositionableInvertibleRealTransform(final InvertibleRealTransform transform) {
        super(transform);
        this.transform = transform;
        inverse = new InverseRealTransform(this);
    }

    @Override
    public void applyInverse(final double[] source, final double[] target) {
        applyInverseRotationTranslation(target);
        transform.applyInverse(source, target);
    }

    @Override
    public void applyInverse(final float[] source, final float[] target) {
        applyInverseRotationTranslation(target);
        transform.applyInverse(source, target);
    }

    @Override
    public void applyInverse(final RealPositionable source, final RealLocalizable target) {
        target.localize(tempTarget);
        applyInverseRotationTranslation(tempTarget);
        transform.applyInverse(tempSource, tempTarget);
        source.setPosition(tempSource);
    }

    private void applyInverseTranslation(final double[] target) {
        for (int d = 0; d < numDimensions(); ++d) {
            target[d] -= translation[d];
        }
    }

    private void applyInverseRotation(final double[] target) {
        rotX.applyInverse(tempTransform, target);
        rotY.applyInverse(target, tempTransform);
        rotZ.applyInverse(tempTransform, target);
        System.arraycopy(tempTransform, 0, target, 0, numDimensions());
    }

    private void applyInverseRotationTranslation(final double[] target) {
        applyInverseTranslation(target);
        applyInverseRotation(target);
    }

    private void applyInverseRotationTranslation(final float[] target) {
        for (int d = 0; d < target.length; ++d) {
            tempTarget[d] = target[d];
        }
        applyInverseRotationTranslation(tempTarget);
        for (int d = 0; d < target.length; ++d) {
            target[d] = (float) tempTarget[d];
        }
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
