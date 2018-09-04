package com.adaptavist.tm4j.jenkins.utils;

import jenkins.model.Jenkins;

public class Permissions {
	public static void checkAdminPermission() {
		Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
	}
}
