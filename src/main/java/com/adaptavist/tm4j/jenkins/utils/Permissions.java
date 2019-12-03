package com.adaptavist.tm4j.jenkins.utils;

import jenkins.model.Jenkins;

public class Permissions {
    public static void checkAdminPermission() {
        Jenkins.getInstanceOrNull().checkPermission(Jenkins.ADMINISTER);
    }
}
