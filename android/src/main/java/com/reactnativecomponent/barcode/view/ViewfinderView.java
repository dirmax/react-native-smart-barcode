/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reactnativecomponent.barcode.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.reactnativecomponent.barcode.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;


public final class ViewfinderView extends View implements ResultPointCallback
{
    private static final long ANIMATION_DELAY = 50L;
    private static final int OPAQUE = 0xFF;
    private int resultPointColor = Color.parseColor("#ffffff");
    private final Paint paint;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;


    public ViewfinderView(Context context)
    {
        super(context);
        paint = new Paint();
        possibleResultPoints = new HashSet<ResultPoint>(5);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) return;

        Collection<ResultPoint> currentPossible = possibleResultPoints;
        Collection<ResultPoint> currentLast = lastPossibleResultPoints;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new HashSet<ResultPoint>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(OPAQUE);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentPossible) {
                canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 8.0f, paint);
            }
        }
        if (currentLast != null) {
            paint.setAlpha(OPAQUE / 2);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentLast) {
                canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 4.0f, paint);
            }
        }

        postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);

    }

    public void drawViewfinder()
    {
        invalidate();
    }

    /**
     * Set color of possible points
     * @param color
     */
    public void setPossiblePointsColor(String color)
    {
        this.resultPointColor = Color.parseColor(color);
    }

    public void foundPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

}
