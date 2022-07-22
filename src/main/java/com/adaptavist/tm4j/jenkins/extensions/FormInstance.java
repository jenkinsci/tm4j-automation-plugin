package com.adaptavist.tm4j.jenkins.extensions;

import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class FormInstance implements Serializable, Describable<FormInstance> {

    private String value;
    private String serverAddress;
    private String username;
    private String password;
    private String cloudAddress;
    private String jwt;

    @DataBoundConstructor
    public FormInstance(String value, String serverAddress, String username, String password, String cloudAddress, String jwt) {
        this.value = value;
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
        this.cloudAddress = cloudAddress;
        this.jwt = jwt;
    }

    public FormInstance() { }

    public String getValue() {
        return value;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCloudAddress() {
        return cloudAddress;
    }

    public String getJwt() {
        return jwt;
    }


    public void setValue(String value) {
        this.value = value;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCloudAddress(String cloudAddress) {
        this.cloudAddress = cloudAddress;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public Descriptor<FormInstance> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(this.getClass());
    }

}
