package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class FileSender {

	public int sendFiles(String url, String username, String password, List<File> files) {
		try {
			HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .basicAuth(username, password)
					  .field("parameter", "value")
					  .field("file", files.get(0))
					  .asJson();
			return jsonResponse.getStatus();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
