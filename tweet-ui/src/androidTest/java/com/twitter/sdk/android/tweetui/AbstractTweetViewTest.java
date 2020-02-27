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

package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.twitter.sdk.android.core.TwitterTestUtils;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.ImageValue;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.internal.AspectRatioFrameLayout;
import com.twitter.sdk.android.tweetui.testutils.TestFixtures;
import com.twitter.sdk.android.tweetui.testutils.TestUtils;

import org.junit.Assert;
import org.mockito.Mockito;

import java.util.Locale;

public abstract class AbstractTweetViewTest extends TweetUiTestCase {
    private static final double EPSILON = 0.01f;

    Context context;
    Resources resources;
    Locale defaultLocale;
    AbstractTweetView.DependencyProvider mockDependencyProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();
        resources = context.getResources();
        defaultLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);
        setUpMockDependencyProvider();
    }

    @Override
    protected void tearDown() throws Exception {
        TestUtils.setLocale(getContext(), defaultLocale);
        scrubClass(AbstractTweetViewTest.class);
        super.tearDown();
    }

    public Resources getResources() {
        return resources;
    }

    // constructor factories
    abstract AbstractTweetView createView(Context context, Tweet tweet);

    abstract AbstractTweetView createViewInEditMode(Context context, Tweet tweet);

    abstract AbstractTweetView createViewWithMocks(Context context, Tweet tweet);

    abstract AbstractTweetView createViewWithMocks(Context context, Tweet tweet,
            AbstractTweetView.DependencyProvider dependencyProvider);

    private void setUpMockDependencyProvider() {
        mockDependencyProvider = Mockito.mock(TestDependencyProvider.class);
        Mockito.when(mockDependencyProvider.getImageLoader()).thenReturn(TweetUi.getInstance().getImageLoader());
        Mockito.when(mockDependencyProvider.getTweetUi()).thenReturn(TweetUi.getInstance());
    }

    // initialization

    public void testInit() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final long tweetId = TestFixtures.TEST_TWEET.id;
        Assert.assertEquals(tweetId, view.getTweetId());
        Assert.assertEquals(TestFixtures.TEST_NAME, view.fullNameView.getText().toString());
        Assert.assertEquals(TestFixtures.TEST_FORMATTED_SCREEN_NAME, view.screenNameView.getText());
        Assert.assertEquals(TestFixtures.TEST_STATUS, view.contentView.getText().toString());
    }

    public void testInit_withEmptyTweet() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        // recycle so we're not relying on first time defaults, fields should clear
        view.setTweet(TestFixtures.EMPTY_TWEET);
        Assert.assertEquals(TestFixtures.EMPTY_TWEET.id, view.getTweetId());
        Assert.assertEquals(TestFixtures.EMPTY_STRING, view.fullNameView.getText().toString());
        Assert.assertEquals(TestFixtures.EMPTY_STRING, view.screenNameView.getText().toString());
        Assert.assertEquals(TestFixtures.EMPTY_STRING, view.contentView.getText().toString());
    }

    public void testInit_withNullTweet() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        // recycle so we're not relying on first time defaults, fields should clear
        view.setTweet(null);
        Assert.assertEquals(TestFixtures.EMPTY_TWEET.id, view.getTweetId());
        Assert.assertEquals(TestFixtures.EMPTY_STRING, view.fullNameView.getText().toString());
        Assert.assertEquals(TestFixtures.EMPTY_STRING, view.screenNameView.getText().toString());
        Assert.assertEquals(TestFixtures.EMPTY_STRING, view.contentView.getText().toString());
    }

    public void testInit_inEditMode() {
        TwitterTestUtils.resetTwitter();
        try {
            final AbstractTweetView view = createViewInEditMode(context, TestFixtures.TEST_TWEET);
            Assert.assertTrue(view.isInEditMode());
            Assert.assertTrue(view.isEnabled());
        } catch (Exception e) {
            Assert.fail("Must start TweetUi... IllegalStateException should be caught");
        } finally {
            TwitterTestUtils.resetTwitter();
        }
    }

    public void testIsTweetUiEnabled_withEditMode() {
        final AbstractTweetView view = createView(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertTrue(view.isTweetUiEnabled());
    }

    public void testIsTweetUiEnabled_inEditMode() {
        final AbstractTweetView view = createViewInEditMode(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertFalse(view.isTweetUiEnabled());
    }

    public void testIsTweetUiEnabled_tweetUiStarted() {
        final AbstractTweetView view = new TweetView(getContext(), TestFixtures.TEST_TWEET);
        Assert.assertTrue(view.isTweetUiEnabled());
        Assert.assertTrue(view.isEnabled());
    }

    // Tests Date formatting reliant string, manually sets english and restores original locale
    public void testGetContentDescription_emptyTweet() {
        final Locale originalLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        view.setTweet(TestFixtures.EMPTY_TWEET);
        Assert.assertEquals(getResources().getString(R.string.tw__loading_tweet), view.getContentDescription());
        TestUtils.setLocale(getContext(), originalLocale);
    }

    // Tests Date formatting reliant string, manually sets english and restores original locale
    public void testGetContentDescription_fullTweet() {
        final Locale originalLocale = TestUtils.setLocale(getContext(), Locale.ENGLISH);

        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertTrue(TweetUtils.isTweetResolvable(view.tweet));
        Assert.assertEquals(TestFixtures.TEST_CONTENT_DESCRIPTION, view.getContentDescription());

        TestUtils.setLocale(getContext(), originalLocale);
    }

    public void testSetTweetMediaClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);
        view.setTweetMediaClickListener((tweet, entity) -> { });

        Assert.assertNotNull(view.tweetMediaClickListener);
    }

    public void testSetTweetLinkClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);
        final TweetLinkClickListener linkClickListener = Mockito.mock(TweetLinkClickListener.class);
        view.setTweetLinkClickListener(linkClickListener);

        Assert.assertNotNull(view.tweetLinkClickListener);

        view.getLinkClickListener().onUrlClicked(TestFixtures.TEST_URL);
        Mockito.verify(linkClickListener).onLinkClick(TestFixtures.TEST_TWEET_LINK, TestFixtures.TEST_URL);
    }

    public void testSetHashtagLinkClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_HASHTAG);
        final TweetLinkClickListener linkClickListener = Mockito.mock(TweetLinkClickListener.class);
        view.setTweetLinkClickListener(linkClickListener);

        Assert.assertNotNull(view.tweetLinkClickListener);

        view.getLinkClickListener().onUrlClicked(TestFixtures.TEST_HASHTAG);
        Mockito.verify(linkClickListener).onLinkClick(TestFixtures.TEST_TWEET_HASHTAG, TestFixtures.TEST_HASHTAG);
    }

    public void testSetTweet_defaultClickListener() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET_LINK);

        Assert.assertNull(view.tweetLinkClickListener);
    }

    // Permalink click
    public void testSetTweet_permalink() {
        final AbstractTweetView view = createView(context, null);
        view.setTweet(TestFixtures.TEST_TWEET);
        Assert.assertEquals(TestFixtures.TEST_PERMALINK_ONE, view.getPermalinkUri().toString());
    }

    // permalinkUri should be null so the permalink launcher will be a NoOp
    public void testSetTweet_nullTweetPermalink() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        view.setTweet(null);
        Assert.assertNull(view.getPermalinkUri());
    }

    public void testSetTweet_updatePermalink() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        Assert.assertEquals(TestFixtures.TEST_PERMALINK_ONE, view.getPermalinkUri().toString());
        view.setTweet(TestFixtures.TEST_PHOTO_TWEET);
        Assert.assertEquals(TestFixtures.TEST_PERMALINK_TWO, view.getPermalinkUri().toString());
    }

    public void testGetAspectRatio_withNullMediaEntity() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity mediaEntity = null;
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), EPSILON);
    }

    public void testGetAspectRatio_withNullImageValue() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final ImageValue imageValue = null;
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(imageValue), EPSILON);
    }

    public void testGetAspectRatio_mediaEntityWithNullSizes() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity mediaEntity = TestFixtures.createMediaEntityWithPhoto(null);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), EPSILON);
    }

    public void testGetAspectRatio_mediaEntityWithEmptySizes() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);
        final MediaEntity.Sizes sizes = new MediaEntity.Sizes(null, null, null, null);
        final MediaEntity mediaEntity = TestFixtures.createMediaEntityWithPhoto(sizes);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO, view.getAspectRatio(mediaEntity), EPSILON);
    }

    public void testGetAspectRatio_mediaEntityWithZeroDimension() {
        final AbstractTweetView view = createView(context, TestFixtures.TEST_TWEET);

        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                            view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(0, 0)),
                            EPSILON);
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                            view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(100, 0)),
                            EPSILON);
        Assert.assertEquals(BaseTweetView.DEFAULT_ASPECT_RATIO,
                            view.getAspectRatio(TestFixtures.createMediaEntityWithPhoto(0, 100)),
                            EPSILON);
    }

    public void testSetTweetMedia_handlesNullPicasso() {
        Mockito.when(mockDependencyProvider.getImageLoader()).thenReturn(null);

        final AbstractTweetView tweetView =
                createViewWithMocks(context, TestFixtures.TEST_TWEET, mockDependencyProvider);

        try {
            tweetView.setTweetMedia(Mockito.mock(Tweet.class));
        } catch (NullPointerException e) {
            Assert.fail("Should have handled null error image");
        }
    }

    public void testRender_forSinglePhotoEntity() {
        final AbstractTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_PHOTO_TWEET);

        Assert.assertEquals(View.VISIBLE, tweetView.mediaContainer.getVisibility());
        Assert.assertEquals(View.VISIBLE, tweetView.tweetMediaView.getVisibility());
        Assert.assertEquals(View.GONE, tweetView.mediaBadgeView.getVisibility());
    }

    public void testRender_forMultiplePhotoEntities() {
        final AbstractTweetView tweetView = createViewWithMocks(context, null);
        tweetView.setTweet(TestFixtures.TEST_MULTIPLE_PHOTO_TWEET);

        Assert.assertEquals(View.VISIBLE, tweetView.mediaContainer.getVisibility());
        Assert.assertEquals(View.VISIBLE, tweetView.tweetMediaView.getVisibility());
        Assert.assertEquals(View.GONE, tweetView.mediaBadgeView.getVisibility());
    }

    public void testRender_rendersVineCard() {
        final AbstractTweetView view = createViewWithMocks(context, null);
        final Card sampleVineCard = TestFixtures.sampleValidVineCard();
        final Tweet tweetWithVineCard = TestFixtures.createTweetWithVineCard(
                TestFixtures.TEST_TWEET_ID, TestFixtures.TEST_USER,
                TestFixtures.TEST_STATUS, sampleVineCard);

        view.setTweet(tweetWithVineCard);

        Assert.assertEquals(TestFixtures.TEST_NAME, view.fullNameView.getText().toString());
        Assert.assertEquals(TestFixtures.TEST_FORMATTED_SCREEN_NAME, view.screenNameView.getText());
        Assert.assertEquals(TestFixtures.TEST_STATUS, view.contentView.getText().toString());
        Assert.assertEquals(View.VISIBLE, view.mediaContainer.getVisibility());
        Assert.assertEquals(View.VISIBLE, view.mediaBadgeView.getVisibility());
        Assert.assertEquals(View.VISIBLE, view.tweetMediaView.getVisibility());
    }

    public void testClearMedia() {
        final AbstractTweetView view = createViewWithMocks(context, null);
        view.mediaContainer = Mockito.mock(AspectRatioFrameLayout.class);

        view.clearTweetMedia();

        Mockito.verify(view.mediaContainer).setVisibility(View.GONE);
    }
}

