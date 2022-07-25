package com.adaptavist.tm4j.jenkins.casc;

import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class ConfigurationAsCodeTest {

    @Rule
    public JenkinsConfiguredWithCodeRule r = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode("casc/configuration-as-code.yml")
    public void should_support_configuration_as_code_multiInstance() {
        Tm4jGlobalConfiguration tm4jGlobalConfiguration = r.getInstance().getExtensionList(Tm4jGlobalConfiguration.class).get(Tm4jGlobalConfiguration.class);
        assert tm4jGlobalConfiguration != null;
        List<Instance> formInstances = tm4jGlobalConfiguration.getJiraInstances();
        assertEquals(2, formInstances.size());
    }

    @Test
    @ConfiguredWithCode("casc/configuration-as-code-single.yml")
    public void should_support_configuration_as_code_singleInstance() {
        Tm4jGlobalConfiguration tm4jGlobalConfiguration = r.getInstance().getExtensionList(Tm4jGlobalConfiguration.class).get(Tm4jGlobalConfiguration.class);
        assert tm4jGlobalConfiguration != null;
        List<Instance> formInstances = tm4jGlobalConfiguration.getJiraInstances();
        assertEquals(1, formInstances.size());
    }
}
