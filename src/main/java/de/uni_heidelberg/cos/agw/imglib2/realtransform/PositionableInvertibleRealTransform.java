/**
 * This file is part of the COS AGW ImageJ plugin bundle.
 * https://github.com/bhoeckendorf/cos-agw_ij
 *
 * Copyright 2012, 2013  B. Hoeckendorf <b.hoeckendorf at web dot de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni_heidelberg.cos.agw.imglib2.realtransform;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.InvertibleRealTransform;

public class PositionableInvertibleRealTransform implements RealPositionable, InvertibleRealTransform {

    private final double[] translation, tempSource, tempTarget, tempTransform;
    private final AffineTransform3D rotX = new AffineTransform3D();
    private final AffineTransform3D rotY = new AffineTransform3D();
    private final AffineTransform3D rotZ = new AffineTransform3D();
    private final InvertibleRealTransform transform;
    private final InverseRealTransform inverse;

    public PositionableInvertibleRealTransform(final InvertibleRealTransform transform) {
        this.transform = transform;
        final int numTargetDimensions = this.transform.numTargetDimensions();
        translation = new double[numTargetDimensions];
        tempSource = new double[this.transform.numSourceDimensions()];
        tempTarget = new double[numTargetDimensions];
        tempTransform = new double[numTargetDimensions];
        inverse = new InverseRealTransform(this);
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

    private void applyTranslation(final double[] target) {
        for (int d = 0; d < numDimensions(); ++d) {
            target[d] += translation[d];
        }
    }

    private void applyInverseTranslation(final double[] target) {
        for (int d = 0; d < numDimensions(); ++d) {
            target[d] -= translation[d];
        }
    }

    private void applyRotation(final double[] target) {
        rotZ.apply(target, tempTransform);
        rotY.apply(tempTransform, target);
        rotX.apply(target, tempTransform);
        for (int d = 0; d < numDimensions(); ++d) {
            target[d] = tempTransform[d];
        }
    }

    private void applyInverseRotation(final double[] target) {
        rotX.applyInverse(tempTransform, target);
        rotY.applyInverse(target, tempTransform);
        rotZ.applyInverse(tempTransform, target);
        for (int d = 0; d < numDimensions(); ++d) {
            target[d] = tempTransform[d];
        }
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
    public int numSourceDimensions() {
        return transform.numSourceDimensions();
    }

    @Override
    public int numTargetDimensions() {
        return transform.numTargetDimensions();
    }

    @Override
    public InvertibleRealTransform inverse() {
        return inverse;
    }

    @Override
    public InvertibleRealTransform copy() {
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
        for (int d = 0; d < numDimensions(); ++d) {
            translation[d] = position[d];
        }
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
