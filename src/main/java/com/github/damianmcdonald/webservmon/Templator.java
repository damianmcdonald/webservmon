package com.github.damianmcdonald.webservmon;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;

@Component
public class Templator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Templator.class);

    @Autowired
    private Configuration freemarkerConfig;

    public String getMergedTemplate(final HashMap model,final String templateFile) {
        try {
            final Template t = freemarkerConfig.getTemplate(templateFile);
            final String mergedTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            LOGGER.debug(String.format("Merged template %s with following model:", templateFile));
            model.forEach((k, v) -> LOGGER.debug(String.format("Key %s : Value %s", k, v)));
            LOGGER.trace(String.format("Dumping the merged template: %n %s", mergedTemplate));
            return FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
        } catch (Exception ex) {
            ex.getMessage();
            LOGGER.error("An error has ocurred.", ex);
            throw new RuntimeException((ex.getMessage()));
        }
    }
}
