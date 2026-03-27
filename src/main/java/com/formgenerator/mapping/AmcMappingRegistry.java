package com.formgenerator.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.formgenerator.domain.model.enums.AmcName;
import com.formgenerator.exception.AmcMappingNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AmcMappingRegistry {

    private final Map<AmcName, AmcFieldMapping> registry = new HashMap<>();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @PostConstruct
    public void loadMappings() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:amc-mappings/*.yaml");

        if (resources.length == 0) {
            log.warn("No AMC field mapping YAML files found in classpath:amc-mappings/");
            return;
        }

        for (Resource resource : resources) {
            try {
                AmcFieldMapping mapping = yamlMapper.readValue(resource.getInputStream(), AmcFieldMapping.class);
                AmcName amcName = AmcName.fromCode(mapping.getAmcName());
                registry.put(amcName, mapping);
                log.info("Loaded AMC field mapping for [{}] from [{}]", amcName, resource.getFilename());
            } catch (Exception e) {
                log.error("Failed to load AMC mapping from [{}]: {}", resource.getFilename(), e.getMessage(), e);
            }
        }
        log.info("AMC mapping registry loaded with {} entries", registry.size());
    }

    public AmcFieldMapping getMapping(AmcName amcName) {
        AmcFieldMapping mapping = registry.get(amcName);
        if (mapping == null) {
            throw new AmcMappingNotFoundException(amcName.getCode());
        }
        return mapping;
    }

    public boolean hasMapping(AmcName amcName) {
        return registry.containsKey(amcName);
    }
}
