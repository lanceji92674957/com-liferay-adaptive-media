/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.adaptive.media.image.content.transformer.internal;

import com.liferay.adaptive.media.AdaptiveMedia;
import com.liferay.adaptive.media.AdaptiveMediaException;
import com.liferay.adaptive.media.image.finder.ImageAdaptiveMediaFinder;
import com.liferay.adaptive.media.image.processor.ImageAdaptiveMediaAttribute;
import com.liferay.adaptive.media.image.processor.ImageAdaptiveMediaProcessor;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.net.URI;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Alejandro Tardín
 */
@RunWith(MockitoJUnitRunner.class)
public class HtmlContentTransformerImplTest {

	@Before
	public void setUp() throws AdaptiveMediaException, PortalException {
		_htmlContentTransformer.setDlAppLocalService(_dlAppLocalService);
		_htmlContentTransformer.setImageAdaptiveMediaFinder(_finder);
	}

	@Test
	public void testAlsoReplacesSeveralImagesInAMultilineString()
		throws Exception {

		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia =
			_createAdaptiveMedia(_ADAPTIVE_WIDTH, _ADAPTIVE_URL);

		Mockito.when(
			_finder.getAdaptiveMedia(Mockito.any())
		).thenAnswer(
			invocation -> Stream.of(adaptiveMedia)
		);

		Assert.assertEquals(
			_duplicateWithNewLine(_HTML_WITH_ADAPTIVE_PICTURE_TAG),
			_htmlContentTransformer.transform(
				_duplicateWithNewLine(_HTML_WITH_ADAPTABLE_PICTURES)));
	}

	@Test
	public void testAppliesSeveralMediaQueries() throws Exception {
		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia1 =
			_createAdaptiveMedia(_ADAPTIVE_MIN_WIDTH, _ADAPTIVE_URL_SMALL);

		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia2 =
			_createAdaptiveMedia(_ADAPTIVE_WIDTH, _ADAPTIVE_URL);

		Mockito.when(
			_finder.getAdaptiveMedia(Mockito.any())
		).thenReturn(
			Stream.of(adaptiveMedia1, adaptiveMedia2)
		);

		Assert.assertEquals(
			_HTML_ADAPTIVE_PICTURE_WITH_TWO_SOURCES,
			_htmlContentTransformer.transform(_HTML_ADAPTABLE_IMG));
	}

	@Test
	public void testReplacesTheAdaptableImagesWithTheAdaptivePictureTag()
		throws Exception {

		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia =
			_createAdaptiveMedia(_ADAPTIVE_WIDTH, _ADAPTIVE_URL);

		Mockito.when(
			_finder.getAdaptiveMedia(Mockito.any())
		).thenReturn(
			Stream.of(adaptiveMedia)
		);

		Assert.assertEquals(
			_HTML_WITH_ADAPTIVE_PICTURE_TAG,
			_htmlContentTransformer.transform(_HTML_WITH_ADAPTABLE_PICTURES));
	}

	@Test
	public void testReplacesTwoConsecutiveImageTags() throws Exception {
		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia =
			_createAdaptiveMedia(_ADAPTIVE_WIDTH, _ADAPTIVE_URL);

		Mockito.when(
			_finder.getAdaptiveMedia(Mockito.any())
		).thenAnswer(
			invocation -> Stream.of(adaptiveMedia)
		);

		Assert.assertEquals(
			_HTML_ADAPTIVE_PICTURE + _HTML_ADAPTIVE_PICTURE,
			_htmlContentTransformer.transform(
				_HTML_ADAPTABLE_IMG + _HTML_ADAPTABLE_IMG));
	}

	@Test
	public void testReturnsTheSameHTMLIfNoAdaptiveMediaImagesArePresent()
		throws Exception {

		Mockito.when(
			_finder.getAdaptiveMedia(Mockito.any())
		).thenReturn(
			Stream.empty()
		);

		Assert.assertEquals(
			_HTML_WITH_ADAPTABLE_PICTURES,
			_htmlContentTransformer.transform(_HTML_WITH_ADAPTABLE_PICTURES));
	}

	@Test
	public void testReturnsTheSameHTMLIfNoImagesArePresent() throws Exception {
		Assert.assertEquals(
			_HTML_WITHOUT_PICTURES,
			_htmlContentTransformer.transform(_HTML_WITHOUT_PICTURES));
	}

	@Test
	public void testReturnsTheSameHTMLIfThereAreNoAdaptableImagesPresent()
		throws Exception {

		Assert.assertEquals(
			_HTML_WITH_NO_ADAPTABLE_PICTURES,
			_htmlContentTransformer.transform(
				_HTML_WITH_NO_ADAPTABLE_PICTURES));
	}

	@Test
	public void testSupportsImageTagsWithNewLineCharacters() throws Exception {
		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia =
			_createAdaptiveMedia(_ADAPTIVE_WIDTH, _ADAPTIVE_URL);

		Mockito.when(
			_finder.getAdaptiveMedia(Mockito.any())
		).thenAnswer(
			invocation -> Stream.of(adaptiveMedia)
		);

		Assert.assertEquals(
			_HTML_ADAPTIVE_PICTURE.replace("src=", "\nsrc="),
			_htmlContentTransformer.transform(
				_HTML_ADAPTABLE_IMG.replace("src=", "\nsrc=")));
	}

	@Test
	public void testTheAttributeIsCaseInsensitive() throws Exception {
		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia =
			_createAdaptiveMedia(_ADAPTIVE_WIDTH, _ADAPTIVE_URL);

		Mockito.when(
			_finder.getAdaptiveMedia(Mockito.any())
		).thenReturn(
			Stream.of(adaptiveMedia)
		);

		Assert.assertEquals(
			_HTML_WITH_ADAPTIVE_PICTURE_TAG,
			_htmlContentTransformer.transform(
				StringUtil.toLowerCase(_HTML_WITH_ADAPTABLE_PICTURES)));
	}

	private AdaptiveMedia<ImageAdaptiveMediaProcessor> _createAdaptiveMedia(
		int imageWidth, String url) {

		AdaptiveMedia<ImageAdaptiveMediaProcessor> adaptiveMedia = Mockito.mock(
			AdaptiveMedia.class);

		Mockito.when(
			adaptiveMedia.getAttributeValue(
				ImageAdaptiveMediaAttribute.IMAGE_WIDTH)
		).thenReturn(
			Optional.of(imageWidth)
		);

		Mockito.when(
			adaptiveMedia.getURI()
		).thenReturn(
			URI.create(url)
		);

		return adaptiveMedia;
	}

	private String _duplicateWithNewLine(String text) {
		return text + StringPool.NEW_LINE + text;
	}

	private static final int _ADAPTIVE_MIN_WIDTH = 1986;

	private static final String _ADAPTIVE_URL = "http://very.adaptive.com";

	private static final String _ADAPTIVE_URL_SMALL =
		"http://small.very.adaptive.com";

	private static final int _ADAPTIVE_WIDTH = 1989;

	private static final long _FILE_ENTRY_ID = 1989L;

	private static final String _HTML_ADAPTABLE_IMG =
		"<img data-fileEntryId=\"" + _FILE_ENTRY_ID + "\" src=\"adaptable\"/>";

	private static final String _HTML_ADAPTABLE_IMG_WITHOUT_ATTR =
		"<img src=\"adaptable\"/>";

	private static final String _HTML_ADAPTIVE_PICTURE =
		"<picture><source media=\"(max-width:" + _ADAPTIVE_WIDTH + "px)\" " +
			"srcset=\"" + _ADAPTIVE_URL + "\"/>" +
				_HTML_ADAPTABLE_IMG_WITHOUT_ATTR + "</picture>";

	private static final String _HTML_ADAPTIVE_PICTURE_WITH_TWO_SOURCES =
		"<picture><source media=\"(max-width:" + _ADAPTIVE_MIN_WIDTH +
			"px)\" srcset=\"" + _ADAPTIVE_URL_SMALL + "\"/><source media=\"" +
				"(max-width:" + _ADAPTIVE_WIDTH + "px) and (min-width:" +
					_ADAPTIVE_MIN_WIDTH + "px)\" srcset=\"" + _ADAPTIVE_URL +
						"\"/>" + _HTML_ADAPTABLE_IMG_WITHOUT_ATTR +
							"</picture>";

	private static final String _HTML_WITH_ADAPTABLE_PICTURES =
		"<div><div>" + _HTML_ADAPTABLE_IMG + "</div></div><br/>";

	private static final String _HTML_WITH_ADAPTIVE_PICTURE_TAG =
		"<div><div>" + _HTML_ADAPTIVE_PICTURE + "</div></div><br/>";

	private static final String _HTML_WITH_NO_ADAPTABLE_PICTURES =
		"<div><div><img src=\"no.adaptable\"/></div></div>";

	private static final String _HTML_WITHOUT_PICTURES =
		"<div><div>some <a>stuff</a></div></div>";

	@Mock
	private DLAppLocalService _dlAppLocalService;

	@Mock
	private ImageAdaptiveMediaFinder _finder;

	private final HtmlContentTransformerImpl _htmlContentTransformer =
		new HtmlContentTransformerImpl();

}