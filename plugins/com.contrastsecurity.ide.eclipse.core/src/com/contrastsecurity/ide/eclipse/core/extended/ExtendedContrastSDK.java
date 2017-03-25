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
package com.contrastsecurity.ide.eclipse.core.extended;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.HttpMethod;
import com.contrastsecurity.sdk.ContrastSDK;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExtendedContrastSDK extends ContrastSDK {

	private Gson gson;

	public ExtendedContrastSDK() {
	}

	public ExtendedContrastSDK(String user, String serviceKey, String apiKey, String restApiURL)
			throws IllegalArgumentException {
		super(user, serviceKey, apiKey, restApiURL);
		this.gson = new Gson();
	}

	public ExtendedContrastSDK(String user, String serviceKey, String apiKey) {
		super(user, serviceKey, apiKey);
		this.gson = new Gson();
	}

	public EventSummaryResource getEventSummary(String orgUuid, String traceId) throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String eventSummaryUrl = getEventSummaryUrl(orgUuid, traceId);
			is = makeRequest(HttpMethod.GET, eventSummaryUrl);
			reader = new InputStreamReader(is);
			EventSummaryResource resource = gson.fromJson(reader, EventSummaryResource.class);
			for (EventResource event:resource.getEvents()) {
				EventDetails eventDetails = getEventDetails(orgUuid, traceId, event);
				event.setEvent(eventDetails.getEvent());
			}
			return resource;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(reader);
		}
	}

	public EventDetails getEventDetails(String orgUuid, String traceId, EventResource event)
			throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String eventDetailsUrl = getEventDetailsUrl(orgUuid, traceId, event);
			is = makeRequest(HttpMethod.GET, eventDetailsUrl);
			reader = new InputStreamReader(is);
			EventDetails resource = gson.fromJson(reader, EventDetails.class);
			return resource;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(reader);
		}
	}

	private String getEventDetailsUrl(String orgUuid, String traceId, EventResource event) {
		return String.format("/ng/%s/traces/%s/events/%s/details?expand=skip_links", orgUuid, traceId, event.getId());
	}

	private String getEventSummaryUrl(String orgUuid, String traceId) {
		return String.format("/ng/%s/traces/%s/events/summary?expand=skip_links", orgUuid, traceId);
	}

	public HttpRequestResource getHttpRequest(String orgUuid, String traceId)
			throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String httpRequestUrl = getHttpRequestUrl(orgUuid, traceId);
			is = makeRequest(HttpMethod.GET, httpRequestUrl);
			reader = new InputStreamReader(is);
			HttpRequestResource resource = gson.fromJson(reader, HttpRequestResource.class);
			return resource;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(reader);
		}
	}

	private String getHttpRequestUrl(String orgUuid, String traceId) {
		return String.format("/ng/%s/traces/%s/httprequest?expand=skip_links", orgUuid, traceId);
	}

	public StoryResource getStory(String orgUuid, String traceId) throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String traceUrl = getTraceUrl(orgUuid, traceId);
			is = makeRequest(HttpMethod.GET, traceUrl);
			reader = new InputStreamReader(is);

			String inputString = IOUtils.toString(is, "UTF-8");
			StoryResource story = this.gson.fromJson(inputString, StoryResource.class);
			JsonObject object = (JsonObject) new JsonParser().parse(inputString);
			JsonObject storyObject = (JsonObject) object.get("story");
			if (storyObject != null) {
				JsonArray chaptersArray = (JsonArray) storyObject.get("chapters");
				List<Chapter> chapters = story.getStory().getChapters();
				if (chapters == null) {
					chapters = new ArrayList<>();
				} else {
					chapters.clear();
				}
				for (int i = 0; i < chaptersArray.size(); i++) {
					JsonObject member = (JsonObject) chaptersArray.get(i);
					Chapter chapter = gson.fromJson(member, Chapter.class);
					chapters.add(chapter);
					JsonObject properties = (JsonObject) member.get("properties");
					if (properties != null) {
						Set<Entry<String, JsonElement>> entries = properties.entrySet();
						Iterator<Entry<String, JsonElement>> iter = entries.iterator();
						List<PropertyResource> propertyResources = new ArrayList<>();
						chapter.setPropertyResources(propertyResources);
						while (iter.hasNext()) {
							Entry<String, JsonElement> prop = iter.next();
							// String key = prop.getKey();
							JsonElement entryValue = prop.getValue();
							if (entryValue != null && entryValue.isJsonObject()) {
								JsonObject obj = (JsonObject) entryValue;
								JsonElement name = obj.get("name");
								JsonElement value = obj.get("value");
								if (name != null && value != null) {
									PropertyResource propertyResource = new PropertyResource();
									propertyResource.setName(name.getAsString());
									propertyResource.setValue(value.getAsString());
									propertyResources.add(propertyResource);
								}
							}
						}
					}

				}
			}
			return story;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(reader);
		}
	}

	private String getTraceUrl(String orgUuid, String traceId) {
		return String.format("/ng/%s/traces/%s/story?expand=skip_links", orgUuid, traceId);
	}
}
