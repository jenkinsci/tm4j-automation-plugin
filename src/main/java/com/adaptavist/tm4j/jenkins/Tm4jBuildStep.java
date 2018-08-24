package com.adaptavist.tm4j.jenkins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class Tm4jBuildStep extends Builder {

    private final String task;

    @DataBoundConstructor
    public Tm4jBuildStep(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    @Extension
    public static class Tm4jBuildStepDescriptor extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "TM4J Build Step";
        }
    }
}
