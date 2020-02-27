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

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.twitter.sdk.android.tweetui.R;

import org.junit.Assert;
import org.mockito.Mockito;

public class VideoControlViewTest extends AndroidTestCase {
    static final int SECOND_IN_MS = 1000;
    static final int MINUTE_IN_MS = 60000;
    static final int TEST_BUFFER_PROGRESS = 32;
    VideoControlView videoControlView;

    public void setUp() throws Exception {
        super.setUp();
        videoControlView = new VideoControlView(getContext());
        videoControlView.onFinishInflate();
    }

    public void testInitialState() {
        Assert.assertTrue(videoControlView.getVisibility() == View.VISIBLE);
        Assert.assertNotNull(videoControlView.seekBar);
        Assert.assertNotNull(videoControlView.duration);
        Assert.assertNotNull(videoControlView.currentTime);
        Assert.assertNotNull(videoControlView.stateControl);
        Assert.assertNull(videoControlView.player);

        Assert.assertEquals(1000, videoControlView.seekBar.getMax());
        Assert.assertEquals(0, videoControlView.seekBar.getProgress());
        Assert.assertEquals(0, videoControlView.seekBar.getSecondaryProgress());

        Assert.assertEquals("0:00", videoControlView.duration.getText());
        Assert.assertEquals("0:00", videoControlView.currentTime.getText());
    }

    public void testCreateStateControlClickListener() {
        final VideoControlView.MediaPlayerControl player = Mockito.mock(VideoControlView.MediaPlayerControl.class);
        videoControlView.setMediaPlayer(player);

        final View.OnClickListener listener = videoControlView.createStateControlClickListener();

        Mockito.when(player.isPlaying()).thenReturn(false);
        listener.onClick(null);
        Mockito.verify(player).start();

        Mockito.when(player.isPlaying()).thenReturn(true);
        listener.onClick(null);
        Mockito.verify(player).pause();
    }

    public void testCreateProgressChangeListener() {
        final VideoControlView.MediaPlayerControl player = Mockito.mock(VideoControlView.MediaPlayerControl.class);
        videoControlView.setMediaPlayer(player);

        final SeekBar.OnSeekBarChangeListener listener = videoControlView.createProgressChangeListener();

        Mockito.when(player.getDuration()).thenReturn(MINUTE_IN_MS);
        listener.onProgressChanged(null, 500, true);
        Mockito.verify(player).seekTo(30000);
        Assert.assertEquals("0:30", videoControlView.currentTime.getText());
    }

    public void testCreateProgressChangeListener_fromUserFalse() {
        final VideoControlView.MediaPlayerControl player = Mockito.mock(VideoControlView.MediaPlayerControl.class);
        videoControlView.setMediaPlayer(player);

        final SeekBar.OnSeekBarChangeListener listener = videoControlView.createProgressChangeListener();

        Mockito.when(player.getDuration()).thenReturn(MINUTE_IN_MS);
        listener.onProgressChanged(null, 500, false);
        Mockito.verifyNoMoreInteractions(player);
    }

    public void testIsShowing() {
        Assert.assertTrue(videoControlView.isShowing());
    }

    public void testUpdateProgress() {
        final VideoControlView.MediaPlayerControl player = Mockito.mock(VideoControlView.MediaPlayerControl.class);
        Mockito.when(player.getCurrentPosition()).thenReturn(SECOND_IN_MS);
        Mockito.when(player.getDuration()).thenReturn(MINUTE_IN_MS);
        Mockito.when(player.getBufferPercentage()).thenReturn(50);
        videoControlView.setMediaPlayer(player);

        videoControlView.updateProgress();

        Assert.assertEquals(16, videoControlView.seekBar.getProgress());
        Assert.assertEquals(500, videoControlView.seekBar.getSecondaryProgress());

        Assert.assertEquals("1:00", videoControlView.duration.getText());
        Assert.assertEquals("0:01", videoControlView.currentTime.getText());
    }

    public void testSetDuration() {
        videoControlView.setDuration(SECOND_IN_MS);
        Assert.assertEquals("0:01", videoControlView.duration.getText());
    }

    public void testSetCurrentTime() {
        videoControlView.setCurrentTime(SECOND_IN_MS);
        Assert.assertEquals("0:01", videoControlView.currentTime.getText());
    }

    public void testSetSeekBarProgress() {
        videoControlView.setProgress(SECOND_IN_MS, MINUTE_IN_MS, TEST_BUFFER_PROGRESS);
        Assert.assertEquals(16, videoControlView.seekBar.getProgress());
        Assert.assertEquals(320, videoControlView.seekBar.getSecondaryProgress());
    }

    public void testSetSeekBarProgress_zeroDuration() {
        videoControlView.setProgress(SECOND_IN_MS, 0, TEST_BUFFER_PROGRESS);
        Assert.assertEquals(0, videoControlView.seekBar.getProgress());
        Assert.assertEquals(320, videoControlView.seekBar.getSecondaryProgress());
    }

    public void testSetPlayDrawable() {
        videoControlView.stateControl = Mockito.mock(ImageButton.class);
        videoControlView.setPlayDrawable();

        Mockito.verify(videoControlView.stateControl).setImageResource(R.drawable.tw__video_play_btn);
        Mockito.verify(videoControlView.stateControl).setContentDescription(getContext().getString(R.string.tw__play));
    }

    public void testSetPauseDrawable() {
        videoControlView.stateControl = Mockito.mock(ImageButton.class);
        videoControlView.setPauseDrawable();

        Mockito.verify(videoControlView.stateControl).setImageResource(R.drawable.tw__video_pause_btn);
        Mockito.verify(videoControlView.stateControl).setContentDescription(getContext().getString(R.string.tw__pause));
    }

    public void testSetReplayDrawable() {
        videoControlView.stateControl = Mockito.mock(ImageButton.class);
        videoControlView.setReplayDrawable();

        Mockito.verify(videoControlView.stateControl).setImageResource(R.drawable.tw__video_replay_btn);
        Mockito.verify(videoControlView.stateControl).setContentDescription(getContext().getString(R.string.tw__replay));
    }

    public void testSetMediaPlayer() {
        videoControlView.setMediaPlayer(mock(VideoControlView.MediaPlayerControl.class));
        Assert.assertNotNull(videoControlView.player);
    }
}
