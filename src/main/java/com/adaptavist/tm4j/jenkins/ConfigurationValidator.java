package com.adaptavist.tm4j.jenkins;

import org.apache.commons.lang.StringUtils;

public class ConfigurationValidator {

	public static boolean validateTm4JConfiguration(RestClient restClient) {

		boolean status = false;
		String url = restClient.getUrl();
		String userName = restClient.getUserName();
		String password = restClient.getPassword();

		if (StringUtils.isBlank(url)) {
			return status;
		}

		if (StringUtils.isBlank(userName)) {
			return status;
		}

		if (StringUtils.isBlank(password)) {
			return status;
		}

		if (!(url.trim().startsWith("https://") || url
				.trim().startsWith("http://"))) {
			return status;
		}

		String tm4j = URLValidator.validateURL(url);

		if (!tm4j.startsWith("http")) {
			return status;
		}

		if (!ServerInfo.findServerAddressIsValidTm4JURL(restClient)) {
			return status;
		}

		boolean validCreds = ServerInfo.validateCredentials(restClient);
		if (!validCreds) {
			return status;
		}

		status = true;
		return status;
	}
}
