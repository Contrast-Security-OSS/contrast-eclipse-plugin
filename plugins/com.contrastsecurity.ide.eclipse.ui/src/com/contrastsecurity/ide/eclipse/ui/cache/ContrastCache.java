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

import com.contrastsecurity.ide.eclipse.core.extended.EventSummaryResource;
import com.contrastsecurity.ide.eclipse.core.extended.HttpRequestResource;
import com.contrastsecurity.ide.eclipse.core.extended.StoryResource;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class ContrastCache {
	private static final int MAX_CACHE_SIZE = 50;

	private ConcurrentLinkedHashMap<Key, EventSummaryResource> eventSummaryResources =
			 new ConcurrentLinkedHashMap.Builder<Key, EventSummaryResource>()
			.initialCapacity(MAX_CACHE_SIZE / 2)
			.maximumWeightedCapacity(MAX_CACHE_SIZE)
			.build();
	private ConcurrentLinkedHashMap<Key, StoryResource> storyResources =
			 new ConcurrentLinkedHashMap.Builder<Key, StoryResource>()
			.initialCapacity(MAX_CACHE_SIZE / 2)
			.maximumWeightedCapacity(MAX_CACHE_SIZE)
			.build();
	private ConcurrentLinkedHashMap<Key, HttpRequestResource> httpRequestResources =
			 new ConcurrentLinkedHashMap.Builder<Key, HttpRequestResource>()
			.initialCapacity(MAX_CACHE_SIZE / 2)
			.maximumWeightedCapacity(MAX_CACHE_SIZE)
			.build();
	public ConcurrentLinkedHashMap<Key, EventSummaryResource> getEventSummaryResources() {
		return eventSummaryResources;
	}
	public ConcurrentLinkedHashMap<Key, StoryResource> getStoryResources() {
		return storyResources;
	}
	public ConcurrentLinkedHashMap<Key, HttpRequestResource> getHttpRequestResources() {
		return httpRequestResources;
	}
	public void clear() {
		eventSummaryResources.clear();
		storyResources.clear();
		httpRequestResources.clear();
	}
}
