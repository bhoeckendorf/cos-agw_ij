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
package de.uni_heidelberg.cos.agw.ij.mapproject;

import de.uni_heidelberg.cos.agw.imglib2.LineIntensityProjector;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.real.binary.RealBinaryOperation;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

public abstract class Projection<T extends NumericType<T> & NativeType<T>> {

    protected final LineIntensityProjector<T> projector;
    protected final RealTransform transform;
    protected final T element;

    public Projection(final RealTransform transform, final LineIntensityProjector<T> projector, final T element) {
        this.transform = transform;
        this.projector = projector;
        this.element = element;
    }

    abstract public String getName();

    abstract public Img<T> project(final double innerRadius, final double outerRadius, final double planePosition, final double scale, final int nProjections, final RealBinaryOperation operation);

    protected Img<T> getOutputImg(final int width, final int height, final int depth) {
        return new ArrayImgFactory<T>().create(new int[]{width, height, depth}, element);
    }
}
