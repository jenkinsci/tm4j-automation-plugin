package com.adaptavist.tm4j.jenkins.utils;

import hudson.Util;
import static com.adaptavist.tm4j.jenkins.utils.Constants.*;

public class Validator {

    public Validator validateProjectKey(String projectKey) throws Exception {
        if (Util.fixEmpty(projectKey) == null)
            throw new Exception(PROJECT_KEY_IS_REQUIRED);
        return this;
    }

    public Validator validateTargetPath(String targetPath) throws Exception {
        if (Util.fixEmpty(targetPath) == null)
            throw new Exception(FILE_TARGET_IS_REQUIRED);
        return this;
    }

    public Validator validateFilePath(String filePath) throws Exception {
        if (Util.fixEmpty(filePath) == null)
            throw new Exception(FILE_PATH_IS_REQUIRED);
        return this;
    }

    public Validator validateFormat(String format) throws Exception {
        if (Util.fixEmpty(format) == null)
            throw new Exception(FORMAT_IS_REQUIRED);
        return this;
    }

    public Validator validateServerAddress(String serverAddress) throws Exception {
        if (Util.fixEmpty(serverAddress) == null)
            throw new Exception(SERVER_URL_IS_REQUIRED);
        return this;
    }
}
