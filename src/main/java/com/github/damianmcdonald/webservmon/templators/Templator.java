package com.github.damianmcdonald.webservmon.templators;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class Templator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Templator.class);

    @Autowired
    private Configuration freemarkerConfig;

    public String getMergedTemplate(final HashMap model, final String templateFile) {
        LOGGER.debug(">>> Entering method");
        LOGGER.info(String.format(
                ">>> Beginning template merge with parameters: model=%s, templateFile=%s",
                model.keySet().stream()
                        .map(key -> String.format("%s = %s", key, model.get(key)))
                        .collect(Collectors.joining(", ", "{", "}")),
                 templateFile
        )
        );
        try {
            final Template t = freemarkerConfig.getTemplate(templateFile);
            final String mergedTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            writeTemplateToFile(mergedTemplate);
            return FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
        } catch (Exception ex) {
            LOGGER.error(">>> An error has ocurred.", ex);
            throw new RuntimeException((ex.getMessage()));
        }
    }
    
    private void writeTemplateToFile(final String mergedTemplate) {
        LOGGER.debug(">>> Entering method");
        BufferedWriter writer = null;
        final String fileExtension = (mergedTemplate.toLowerCase().contains("html")) ? ".html" : ".txt";
        try {
            final File tempFile = File.createTempFile(Integer.toString(new Random().nextInt()), fileExtension);
            writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(mergedTemplate);
            LOGGER.info(String.format(">>> Merged template can be viewed at %s", tempFile));
            LOGGER.debug("<<< Exiting method");
        } catch (Exception ex) {
            LOGGER.error(">>> An error has ocurred.", ex);
        } finally {
            try {
                if (writer != null) writer.close();
                LOGGER.debug("<<< Exiting method");
            } catch (IOException ex) {
                LOGGER.error(">>> An error has ocurred.", ex);
            }
        }
    }
}
