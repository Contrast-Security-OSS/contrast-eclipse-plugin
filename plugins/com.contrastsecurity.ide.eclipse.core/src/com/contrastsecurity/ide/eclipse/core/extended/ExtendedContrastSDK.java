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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.*;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.HttpMethod;
import com.contrastsecurity.ide.eclipse.core.UrlConstants;
import com.contrastsecurity.sdk.ContrastSDK;

import org.apache.commons.io.IOUtils;

public class ExtendedContrastSDK extends ContrastSDK {

    private static final int BAD_REQUEST = 400;
    private static final int SERVER_ERROR = 500;
	private Gson gson;
	private String restApiURL;

	public ExtendedContrastSDK(String user, String serviceKey, String apiKey, String restApiURL)
			throws IllegalArgumentException {
		super(user, serviceKey, apiKey, restApiURL);
		this.restApiURL = restApiURL;
		this.gson = new Gson();
	}

	public EventSummaryResource getEventSummary(String orgUuid, String traceId)
			throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String eventSummaryUrl = getEventSummaryUrl(orgUuid, traceId);
			is = makeRequest(HttpMethod.GET, eventSummaryUrl);
			reader = new InputStreamReader(is);
			EventSummaryResource resource = gson.fromJson(reader, EventSummaryResource.class);
			for (EventResource event : resource.getEvents()) {
				if (event.getCollapsedEvents() != null && !event.getCollapsedEvents().isEmpty()) {
					getCollapsedEventsDetails(event, orgUuid, traceId);
				} else {
					EventDetails eventDetails = getEventDetails(orgUuid, traceId, event);
					event.setEvent(eventDetails.getEvent());
				}
			}
			return resource;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(reader);
		}
	}

	private void getCollapsedEventsDetails(EventResource parentEvent, final String orgUuid, final String traceId)
			throws IOException, UnauthorizedException {
		for (EventResource event : parentEvent.getCollapsedEvents()) {
			EventDetails eventDetails = getEventDetails(orgUuid, traceId, event);
			event.setEvent(eventDetails.getEvent());
			event.setParent(parentEvent);
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

	private String getEventDetailsUrl(String orgUuid, String traceId, final EventResource event) {
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

	public RecommendationResource getRecommendation(String orgUuid, String traceId)
			throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String recommendationUrl = String.format(UrlConstants.RECOMMENDATION, orgUuid, traceId);
			is = makeRequest(HttpMethod.GET, recommendationUrl);
			reader = new InputStreamReader(is);
			return gson.fromJson(reader, RecommendationResource.class);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(reader);
		}
	}

	private String getTraceUrl(String orgUuid, String traceId) {
		return String.format("/ng/%s/traces/%s/story?expand=skip_links", orgUuid, traceId);
	}
	
	/**
	 * Gets a trace information using its UUID as reference.
	 * @param orgUuid Organization UUID.
	 * @param traceId Vulnerability UUID.
	 * @return A response that contains the requested trace information.
	 * @throws IOException
	 * @throws UnauthorizedException
	 */
	public TraceResponse getTraceByUuid(String orgUuid, String traceId) throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String url = String.format(UrlConstants.GET_TRACE, orgUuid, traceId);
			
			is = makeRequest(HttpMethod.GET, url);
			reader = new InputStreamReader(is);
			return gson.fromJson(reader, TraceResponse.class);
		}
		finally {
			 IOUtils.closeQuietly(is);
			 IOUtils.closeQuietly(reader);
		}
	}
	
	/**
	 * Changes the status for a list of vulnerabilities.
	 * @param orgUuid The UUID for the organization.
	 * @param request Object that contains the request body with the indicated vulnerabilities and their status data.
	 * @return A BaseResponse object that indicated whether the status change was successful.
	 * @throws IOException
	 * @throws UnauthorizedException
	 */
	public BaseResponse markStatus(String orgUuid, TraceStatusRequest request) throws IOException, UnauthorizedException {
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			String markUrl = String.format(UrlConstants.MARK_STATUS, orgUuid);
			is = makeRequest(HttpMethod.PUT, markUrl, request);
			reader = new InputStreamReader(is);
			return gson.fromJson(reader, BaseResponse.class);
		}
		finally {
			 IOUtils.closeQuietly(is);
			 IOUtils.closeQuietly(reader);
		}
	}
	
	public TagsResource getTagsByOrg(String orgUuid) throws IOException, UnauthorizedException {
        InputStream is = null;
        InputStreamReader reader = null;
        try {
            String tagsUrl = String.format(UrlConstants.ORG_TAGS, orgUuid);
            is = makeRequest(HttpMethod.GET, tagsUrl);
            reader = new InputStreamReader(is);
            return gson.fromJson(reader, TagsResource.class);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(reader);
        }
    }

    public TagsResource getTagsByTrace(String orgUuid, String traceId) throws IOException, UnauthorizedException {
        InputStream is = null;
        InputStreamReader reader = null;
        try {
            String tagsUrl = String.format(UrlConstants.TRACE_TAGS, orgUuid, traceId);
            is = makeRequest(HttpMethod.GET, tagsUrl);
            reader = new InputStreamReader(is);
            return gson.fromJson(reader, TagsResource.class);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(reader);
        }
    }

    public BaseResponse putTags(String orgUuid, TagsServersResource tagsServersResource) throws IOException, UnauthorizedException {
        InputStream is = null;
        InputStreamReader reader = null;
        try {
            String tagsUrl = String.format(UrlConstants.ORG_TAGS, orgUuid);
            is = makeRequest(HttpMethod.PUT, tagsUrl, tagsServersResource);
            reader = new InputStreamReader(is);
            return gson.fromJson(reader, BaseResponse.class);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(reader);
        }
    }

    public TagsResource deleteTag(String orgUuid, String traceId, String tag) throws IOException, UnauthorizedException {
        InputStream is = null;
        InputStreamReader reader = null;
        TagRequest tagRequest = new TagRequest(tag);
        try {
            String tagsUrl = String.format(UrlConstants.TRACE_TAGS_DELETE, orgUuid, traceId);
            is = makeRequest(HttpMethod.DELETE, tagsUrl, tagRequest);
            reader = new InputStreamReader(is);
            return gson.fromJson(reader, TagsResource.class);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(reader);
        }
    }
    
 // ------------------------ Utilities -----------------------------------------------
    private InputStream makeRequest(HttpMethod method, String path, Object body) throws IOException, UnauthorizedException {
        String url = restApiURL + path;
        HttpURLConnection connection = makeConnection(url, method.toString(), body);

        InputStream is = connection.getInputStream();
        int rc = connection.getResponseCode();
        if (rc >= BAD_REQUEST && rc < SERVER_ERROR) {
            IOUtils.closeQuietly(is);
            throw new UnauthorizedException(rc);
        }
        return is;
    }

    private HttpURLConnection makeConnection(String url, String method, Object body) throws IOException {

        HttpURLConnection connection = makeConnection(url, method);

        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        OutputStream os = connection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

        osw.write(gson.toJson(body));
        osw.flush();
        osw.close();
        os.close();

        return connection;
    }
	
}
