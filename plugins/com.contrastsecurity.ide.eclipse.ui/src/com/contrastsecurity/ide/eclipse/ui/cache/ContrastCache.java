/*******************************************************************************
 * Copyright (c) 2017 Contrast Security.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License.
 * 
 * The terms of the GNU GPL version 3 which accompanies this distribution
 * and is available at https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * Contributors:
 *     Contrast Security - initial API and implementation
 *******************************************************************************/
package com.contrastsecurity.ide.eclipse.ui.cache;

import com.contrastsecurity.models.EventSummaryResponse;
import com.contrastsecurity.models.HttpRequestResponse;
import com.contrastsecurity.models.RecommendationResponse;
import com.contrastsecurity.models.StoryResponse;
import com.contrastsecurity.models.TagsResponse;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class ContrastCache {
	private static final int MAX_CACHE_SIZE = 50;

	private ConcurrentLinkedHashMap<Key, EventSummaryResponse> eventSummaryResources = new ConcurrentLinkedHashMap.Builder<Key, EventSummaryResponse>()
			.initialCapacity(MAX_CACHE_SIZE / 2).maximumWeightedCapacity(MAX_CACHE_SIZE).build();
	private ConcurrentLinkedHashMap<Key, StoryResponse> storyResources = new ConcurrentLinkedHashMap.Builder<Key, StoryResponse>()
			.initialCapacity(MAX_CACHE_SIZE / 2).maximumWeightedCapacity(MAX_CACHE_SIZE).build();
	private ConcurrentLinkedHashMap<Key, HttpRequestResponse> httpRequestResources = new ConcurrentLinkedHashMap.Builder<Key, HttpRequestResponse>()
			.initialCapacity(MAX_CACHE_SIZE / 2).maximumWeightedCapacity(MAX_CACHE_SIZE).build();

	private ConcurrentLinkedHashMap<Key, RecommendationResponse> recommendationResources = new ConcurrentLinkedHashMap.Builder<Key, RecommendationResponse>()
			.initialCapacity(MAX_CACHE_SIZE / 2).maximumWeightedCapacity(MAX_CACHE_SIZE).build();

	private ConcurrentLinkedHashMap<Key, TagsResponse> tagsResources = new ConcurrentLinkedHashMap.Builder<Key, TagsResponse>()
			.initialCapacity(MAX_CACHE_SIZE / 2).maximumWeightedCapacity(MAX_CACHE_SIZE).build();

	public ConcurrentLinkedHashMap<Key, EventSummaryResponse> getEventSummaryResources() {
		return eventSummaryResources;
	}

	public ConcurrentLinkedHashMap<Key, StoryResponse> getStoryResources() {
		return storyResources;
	}

	public ConcurrentLinkedHashMap<Key, HttpRequestResponse> getHttpRequestResources() {
		return httpRequestResources;
	}

	public ConcurrentLinkedHashMap<Key, RecommendationResponse> getRecommendationResources() {
		return recommendationResources;
	}

	public ConcurrentLinkedHashMap<Key, TagsResponse> getTagsResources() {
		return tagsResources;
	}

	public void clear() {
		eventSummaryResources.clear();
		storyResources.clear();
		httpRequestResources.clear();
		recommendationResources.clear();
		tagsResources.clear();
	}
}
