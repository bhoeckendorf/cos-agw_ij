package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealTransform;

// ToDo: Rotation produces ArrayIndexOutOfBoundsException if not constructed with 3D transform.
public class PositionableRealTransform implements RealPositionable, RealTransform {

    protected final double[] translation, tempSource, tempTarget, tempTransform;
    protected final AffineTransform3D rotX = new AffineTransform3D();
    protected final AffineTransform3D rotY = new AffineTransform3D();
    protected final AffineTransform3D rotZ = new AffineTransform3D();
    private final RealTransform transform;

    public PositionableRealTransform(final RealTransform transform) {
        this.transform = transform;
        final int numTargetDimensions = this.transform.numTargetDimensions();
        translation = new double[numTargetDimensions];
        tempSource = new double[this.transform.numSourceDimensions()];
        tempTarget = new double[numTargetDimensions];
        tempTransform = new double[numTargetDimensions];
    }

    @Override
    public void apply(final double[] source, final double[] target) {
        transform.apply(source, target);
        applyRotationTranslation(target);
    }

    @Override
    public void apply(final float[] source, final float[] target) {
        transform.apply(source, target);
        applyRotationTranslation(target);
    }

    @Override
    public void apply(final RealLocalizable source, final RealPositionable target) {
        source.localize(tempSource);
        transform.apply(tempSource, tempTarget);
        applyRotationTranslation(tempTarget);
        target.setPosition(tempTarget);
    }

    private void applyTranslation(final double[] target) {
        for (int d = 0; d < numDimensions(); ++d) {
            target[d] += translation[d];
        }
    }

    private void applyRotation(final double[] target) {
        rotZ.apply(target, tempTransform);
        rotY.apply(tempTransform, target);
        rotX.apply(target, tempTransform);
        System.arraycopy(tempTransform, 0, target, 0, numDimensions());
    }

    public void rotate(final int axis, final double radians) {
        switch (axis) {
            case 0:
                rotX.rotate(axis, radians);
                break;
            case 1:
                rotY.rotate(axis, radians);
                break;
            case 2:
                rotZ.rotate(axis, radians);
                break;
            default:
                break;
        }
    }

    private void applyRotationTranslation(final double[] target) {
        applyRotation(target);
        applyTranslation(target);
    }

    private void applyRotationTranslation(final float[] target) {
        for (int d = 0; d < target.length; ++d) {
            tempTarget[d] = target[d];
        }
        applyRotationTranslation(tempTarget);
        for (int d = 0; d < target.length; ++d) {
            target[d] = (float) tempTarget[d];
        }
    }

    @Override
    public int numSourceDimensions() {
        return transform.numSourceDimensions();
    }

    @Override
    public int numTargetDimensions() {
        return transform.numTargetDimensions();
    }

    @Override
    public RealTransform copy() {
        return this;
    }

    @Override
    public void move(final float distance, final int d) {
        translation[d] += distance;
    }

    @Override
    public void move(final double distance, final int d) {
        translation[d] += distance;
    }

    @Override
    public void move(final RealLocalizable localizable) {
        localizable.localize(tempTarget);
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] += tempTarget[d];
        }
    }

    @Override
    public void move(final float[] distance) {
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] += distance[d];
        }
    }

    @Override
    public void move(final double[] distance) {
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] += distance[d];
        }
    }

    @Override
    public void setPosition(final RealLocalizable localizable) {
        localizable.localize(translation);
    }

    @Override
    public void setPosition(final float[] position) {
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] = position[d];
        }
    }

    @Override
    public void setPosition(final double[] position) {
        System.arraycopy(translation, 0, position, 0, numDimensions());
    }

    @Override
    public void setPosition(final float position, final int d) {
        translation[d] = position;
    }

    @Override
    public void setPosition(final double position, final int d) {
        translation[d] = position;
    }

    @Override
    public void fwd(final int d) {
        translation[d] += 1;
    }

    @Override
    public void bck(final int d) {
        translation[d] -= 1;
    }

    @Override
    public void move(final int distance, final int d) {
        translation[d] += distance;
    }

    @Override
    public void move(final long distance, final int d) {
        translation[d] += distance;
    }

    @Override
    public void move(final Localizable localizable) {
        localizable.localize(tempTarget);
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] += tempTarget[d];
        }
    }

    @Override
    public void move(final int[] distance) {
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] += distance[d];
        }
    }

    @Override
    public void move(final long[] distance) {
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] += distance[d];
        }
    }

    @Override
    public void setPosition(final Localizable localizable) {
        localizable.localize(translation);
    }

    @Override
    public void setPosition(final int[] position) {
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] = position[d];
        }
    }

    @Override
    public void setPosition(final long[] position) {
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] = position[d];
        }
    }

    @Override
    public void setPosition(final int position, final int d) {
        translation[d] = position;
    }

    @Override
    public void setPosition(final long position, final int d) {
        translation[d] = position;
    }

    @Override
    public int numDimensions() {
        return transform.numTargetDimensions();
    }
}
