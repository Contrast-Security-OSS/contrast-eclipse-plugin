package com.contrastsecurity.ide.eclipse.ui.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.cache.ContrastCache;
import com.contrastsecurity.ide.eclipse.ui.cache.Key;
import com.contrastsecurity.models.EventSummaryResponse;
import com.contrastsecurity.models.HttpRequestResponse;
import com.contrastsecurity.models.StoryResponse;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;


public class ContrastCacheTest {

	/**
	 * Organization UUID. Required to run when testing retrieval of an event.
	 */
	private static String ORGANIZATION_UUID;
	/**
	 * Trace (vulnerability) UUID. Required to run when testing retrieval of an
	 * event.
	 */
	private static String TRACE_ID;

	Key key;

	private ContrastCache contrastCache;

	@BeforeClass
	public static void initRequiredParams() {
		ORGANIZATION_UUID = System.getProperty("organizationId");
		TRACE_ID = System.getProperty("traceId");
	}

	@Before
	public void init() {
		contrastCache = ContrastUIActivator.getContrastCache();
		key = new Key(ORGANIZATION_UUID, TRACE_ID);
	}

	@Test
	public void getEventSummaryResourcesTest() {

		EventSummaryResponse eventSummaryResource = new EventSummaryResponse();

		ConcurrentLinkedHashMap<Key, EventSummaryResponse> eventSummaryResources = contrastCache
			.getEventSummaryResources();

		eventSummaryResources.put(key, eventSummaryResource);

		assertTrue(contrastCache.getEventSummaryResources().size() == 1);

		EventSummaryResponse eventSummaryResourceNew = eventSummaryResources.get(key);
		assertEquals(eventSummaryResource, eventSummaryResourceNew);
	}

	@Test
	public void getStoryResourcesTest() {

		StoryResponse storyResource = new StoryResponse();

		ConcurrentLinkedHashMap<Key, StoryResponse> storyResources = contrastCache.getStoryResources();

		storyResources.put(key, storyResource);

		assertTrue(contrastCache.getStoryResources().size() == 1);

		StoryResponse storyResourceNew = storyResources.get(key);

		assertEquals(storyResource, storyResourceNew);

	}

	@Test
	public void getHttpRequestResourcesTest() {

		HttpRequestResponse httpRequestResource = new HttpRequestResponse();

		ConcurrentLinkedHashMap<Key, HttpRequestResponse> httpRequestResources = contrastCache
			.getHttpRequestResources();

		httpRequestResources.put(key, httpRequestResource);

		assertTrue(contrastCache.getHttpRequestResources().size() == 1);

		HttpRequestResponse httpRequestResourceNew = httpRequestResources.get(key);

		assertEquals(httpRequestResource, httpRequestResourceNew);

	}

	@Test
	public void clearTest() {

		EventSummaryResponse eventSummaryResource = new EventSummaryResponse();
		ConcurrentLinkedHashMap<Key, EventSummaryResponse> eventSummaryResources = contrastCache
			.getEventSummaryResources();
		eventSummaryResources.put(key, eventSummaryResource);

		StoryResponse storyResource = new StoryResponse();
		ConcurrentLinkedHashMap<Key, StoryResponse> storyResources = contrastCache.getStoryResources();
		storyResources.put(key, storyResource);

		HttpRequestResponse httpRequestResource = new HttpRequestResponse();
		ConcurrentLinkedHashMap<Key, HttpRequestResponse> httpRequestResources = contrastCache
			.getHttpRequestResources();
		httpRequestResources.put(key, httpRequestResource);

		contrastCache.clear();
		assertTrue(contrastCache.getEventSummaryResources().isEmpty());
		assertTrue(contrastCache.getStoryResources().isEmpty());
		assertTrue(contrastCache.getHttpRequestResources().isEmpty());

	}

}
