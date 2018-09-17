package com.adaptavist.tm4j.jenkins.utils;

import static com.adaptavist.tm4j.jenkins.utils.Constants.*;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class Validator {

    public Validator validateProjectKey(String projectKey) throws Exception {
        if (isEmpty(projectKey))
            throw new Exception(PROJECT_KEY_IS_REQUIRED);
        return this;
    }

    public Validator validateTargetPath(String targetPath) throws Exception {
        if (isEmpty(targetPath))
            throw new Exception(FILE_TARGET_IS_REQUIRED);
        return this;
    }

    public Validator validateFilePath(String filePath) throws Exception {
        if (isEmpty(filePath))
            throw new Exception(FILE_PATH_IS_REQUIRED);
        return this;
    }

    public Validator validateFormat(String format) throws Exception {
        if (isEmpty(format))
            throw new Exception(FORMAT_IS_REQUIRED);
        return this;
    }

    public Validator validateServerAddress(String serverAddress) throws Exception {
        if (isEmpty(serverAddress))
            throw new Exception(SERVER_URL_IS_REQUIRED);
        return this;
    }
}
