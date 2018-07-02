package com.adaptavist.tm4j.jenkins;

import org.apache.commons.lang.StringUtils;

public class ConfigurationValidator {

	public static boolean validateTm4JConfiguration(RestClient restClient) {

		String url = restClient.getUrl();
		String userName = restClient.getUserName();
		String password = restClient.getPassword();

		if (StringUtils.isBlank(url)) {
			return false;
		}

		if (StringUtils.isBlank(userName)) {
			return false;
		}

		if (StringUtils.isBlank(password)) {
			return false;
		}

		if (!(url.trim().startsWith("https://") || url.trim().startsWith("http://"))) {
			return false;
		}

		if (!ServerInfo.findServerAddressIsValidTm4JURL(restClient)) {
			return false;
		}

		boolean validCreds = ServerInfo.validateCredentials(restClient);
		if (!validCreds) {
			return false;
		}

		return true;
	}
}
