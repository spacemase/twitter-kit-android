/*
 * Copyright (C) 2015 Twitter, Inc.
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
 *
 */

package com.twitter.sdk.android.tweetui.internal;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;
import android.widget.ImageView;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class OverlayImageViewTest extends AndroidTestCase {
    public static final int[] TEST_STATE = new int[]{0, 0};
    public static final int TEST_HEIGHT = 2;
    public static final int TEST_WIDTH = 4;

    public void testOnDraw() {
        final OverlayImageView overlayImageView = new OverlayImageView(getContext());
        final OverlayImageView.Overlay overlay = Mockito.mock(OverlayImageView.Overlay.class);
        final Canvas canvas = new Canvas();
        overlayImageView.overlay = overlay;
        overlayImageView.draw(canvas);

        Mockito.verify(overlay).draw(canvas);
    }

    public void testDrawableStateChanged() {
        final OverlayImageView overlayImageView = new OverlayImageView(getContext());
        final OverlayImageView.Overlay overlay = Mockito.mock(OverlayImageView.Overlay.class);
        overlayImageView.overlay = overlay;
        overlayImageView.drawableStateChanged();

        Mockito.verify(overlay).setDrawableState(Matchers.any(int[].class));
    }

    public void testOnMeasure() {
        final OverlayImageView overlayImageView = new OverlayImageView(getContext());
        final OverlayImageView.Overlay overlay = Mockito.mock(OverlayImageView.Overlay.class);
        overlayImageView.overlay = overlay;
        overlayImageView.measure(0, 0);

        Mockito.verify(overlay).setDrawableBounds(Matchers.anyInt(), Matchers.anyInt());
    }

    public void testOnSizeChanged() {
        final OverlayImageView overlayImageView = new OverlayImageView(getContext());
        final OverlayImageView.Overlay overlay = Mockito.mock(OverlayImageView.Overlay.class);
        overlayImageView.overlay = overlay;
        overlayImageView.onSizeChanged(TEST_WIDTH, TEST_HEIGHT, 0, 0);

        Mockito.verify(overlay).setDrawableBounds(TEST_WIDTH, TEST_HEIGHT);
    }

    public void testSetOverlayDrawable() {
        final OverlayImageView overlayImageView = new OverlayImageView(getContext());
        final OverlayImageView.Overlay overlay = Mockito.mock(OverlayImageView.Overlay.class);
        overlayImageView.overlay = overlay;
        final Drawable drawable = Mockito.mock(Drawable.class);
        overlayImageView.setOverlayDrawable(drawable);

        Mockito.verify(overlay).cleanupDrawable(overlayImageView);
        Assert.assertNotNull(overlayImageView.overlay);
        Assert.assertEquals(drawable, overlayImageView.overlay.drawable);
    }

    public void testSetOverlayDrawable_nullDrawable() {
        final OverlayImageView overlayImageView = new OverlayImageView(getContext());
        final OverlayImageView.Overlay overlay = Mockito.mock(OverlayImageView.Overlay.class);
        overlayImageView.overlay = overlay;
        overlayImageView.setOverlayDrawable(null);

        Mockito.verifyNoMoreInteractions(overlay);
    }

    public void testOverlayDraw() {
        final Drawable drawable = Mockito.mock(Drawable.class);
        final OverlayImageView.Overlay overlay = new OverlayImageView.Overlay(drawable);
        final Canvas canvas = new Canvas();
        overlay.draw(canvas);

        Mockito.verify(drawable).draw(canvas);
    }

    public void testOverlaySetDrawableState() {
        final Drawable drawable = Mockito.mock(Drawable.class);
        Mockito.when(drawable.isStateful()).thenReturn(true);
        final OverlayImageView.Overlay overlay = new OverlayImageView.Overlay(drawable);
        overlay.setDrawableState(TEST_STATE);

        Mockito.verify(drawable).isStateful();
        Mockito.verify(drawable).setState(TEST_STATE);
    }

    public void testOverlaySetDrawableState_drawableNotStateful() {
        final Drawable drawable = Mockito.mock(Drawable.class);
        Mockito.when(drawable.isStateful()).thenReturn(false);
        final OverlayImageView.Overlay overlay = new OverlayImageView.Overlay(drawable);
        overlay.setDrawableState(TEST_STATE);

        Mockito.verify(drawable).isStateful();
        Mockito.verifyNoMoreInteractions(drawable);
    }

    public void testOverlaySetDrawableBounds() {
        final Drawable drawable = Mockito.mock(Drawable.class);
        final OverlayImageView.Overlay overlay = new OverlayImageView.Overlay(drawable);
        overlay.setDrawableBounds(TEST_WIDTH, TEST_HEIGHT);

        Mockito.verify(drawable).setBounds(0, 0, TEST_WIDTH, TEST_HEIGHT);
    }

    public void testCleanupDrawable() {
        final ImageView imageView = Mockito.mock(ImageView.class);
        final Drawable drawable = Mockito.mock(Drawable.class);
        final OverlayImageView.Overlay overlay = new OverlayImageView.Overlay(drawable);
        overlay.cleanupDrawable(imageView);

        Mockito.verify(imageView).unscheduleDrawable(drawable);
    }
}
