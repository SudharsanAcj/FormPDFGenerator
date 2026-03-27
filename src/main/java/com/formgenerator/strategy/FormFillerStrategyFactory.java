package com.formgenerator.strategy;

import com.formgenerator.domain.model.enums.AmcName;
import com.formgenerator.exception.AmcNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormFillerStrategyFactory {

    /** Spring auto-injects all FormFillerStrategy beans */
    private final List<FormFillerStrategy> strategies;

    private final Map<AmcName, FormFillerStrategy> registry = new EnumMap<>(AmcName.class);

    @PostConstruct
    void buildRegistry() {
        for (FormFillerStrategy strategy : strategies) {
            registry.put(strategy.getSupportedAmc(), strategy);
            log.info("Registered form-filler strategy for AMC: [{}] -> [{}]",
                    strategy.getSupportedAmc(), strategy.getClass().getSimpleName());
        }
    }

    public FormFillerStrategy getStrategy(AmcName amcName) {
        FormFillerStrategy strategy = registry.get(amcName);
        if (strategy == null) {
            throw new AmcNotFoundException(amcName.getCode());
        }
        return strategy;
    }

    public boolean hasStrategy(AmcName amcName) {
        return registry.containsKey(amcName);
    }
}
