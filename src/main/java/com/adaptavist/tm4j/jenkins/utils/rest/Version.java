package com.adaptavist.tm4j.jenkins.utils.rest;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

public class Version {

	private static String URL_GET_VERSIONS = "{SERVER}/rest/api/2/project/{PROJ_ID}/versions?expand";

	
	public static Long getVersionIdByNameProjectId(String versionName, Long projectId, RestClient restClient) {

		Long releaseId = 0L;

		HttpResponse response = null;
		try {
			response = restClient.getHttpclient().execute(new HttpGet(restClient.getUrl() + "/flex/services/rest/latest/release?name=" + URLEncoder.encode(versionName, "utf-8") + "&project.id=" + projectId), restClient.getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			String string = null;
			try {
				string = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			
			try {
				JSONArray releaseArray = new JSONArray(string);
				List<Long> releaseIdList = new ArrayList<Long>();
				for(int i = 0; i < releaseArray.length(); i++) {
					Long id = releaseArray.getJSONObject(i).getLong("id");
					releaseIdList.add(id);
				}
				
				Collections.sort(releaseIdList);
				releaseId = releaseIdList.get(0);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
		} else {
			try {
				throw new ClientProtocolException("Unexpected response status: "
						+ statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
	
		return releaseId;
	}
	
	public static Map<Long, String> getVersionsByProjectID(Long projectID, RestClient restClient) {


		Map<Long, String> releases = new TreeMap<Long, String>();
		
		HttpResponse response = null;
		
		final String url = URL_GET_VERSIONS.replace("{SERVER}", restClient.getUrl()).replace("{PROJ_ID}", projectID+"");
		try {
			response = restClient.getHttpclient().execute(new HttpGet(url), restClient.getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			String string = null;
			try {
				string = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			
			try {
				JSONArray projArray = new JSONArray(string);
				for(int i = 0; i < projArray.length(); i++) {
					Long id = projArray.getJSONObject(i).getLong("id");
					String projName = projArray.getJSONObject(i).getString("name");
					releases.put(id, projName);
				}
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
		} else {
			
			try {
				throw new ClientProtocolException("Unexpected response status: "
						+ statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
		releases.put(-1L, "Unscheduled");
		return releases;
	}
}
