package com.adaptavist.tm4j.jenkins.extensions;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import java.io.Serializable;

public class JiraInstance implements Serializable, Describable<JiraInstance> {

    private String value;
    private String serverAddress;
    private String username;
    private Secret password;
    private Secret jwt;

    @DataBoundConstructor
    public JiraInstance(String value) {
        this.value = value;
    }

    public JiraInstance() { }

    @CheckForNull
    public String getValue() {
        return value;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getUsername() {
        return username;
    }

    public Secret getPassword() {
        return password;
    }

    public Secret getJwt() {
        return jwt;
    }


    public void setValue(String value) {
        this.value = value;
    }

    @DataBoundSetter
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    @DataBoundSetter
    public void setPassword(Secret password) {
        this.password = password;
    }


    @DataBoundSetter
    public void setJwt(Secret jwt) {
        this.jwt = jwt;
    }

    @Override
    public Descriptor<JiraInstance> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(this.getClass());
    }
}
