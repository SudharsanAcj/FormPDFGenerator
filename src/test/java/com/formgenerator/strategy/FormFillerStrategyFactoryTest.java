package com.formgenerator.strategy;

import com.formgenerator.domain.model.enums.AmcName;
import com.formgenerator.exception.AmcNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FormFillerStrategyFactoryTest {

    private FormFillerStrategyFactory factory;
    private FormFillerStrategy blackrockStrategy;

    @BeforeEach
    void setUp() {
        blackrockStrategy = mock(FormFillerStrategy.class);
        when(blackrockStrategy.getSupportedAmc()).thenReturn(AmcName.BLACKROCK);

        factory = new FormFillerStrategyFactory(List.of(blackrockStrategy));
        factory.buildRegistry();
    }

    @Test
    void getStrategy_registeredAmc_returnsStrategy() {
        FormFillerStrategy result = factory.getStrategy(AmcName.BLACKROCK);
        assertThat(result).isSameAs(blackrockStrategy);
    }

    @Test
    void getStrategy_unregisteredAmc_throwsAmcNotFoundException() {
        assertThatThrownBy(() -> factory.getStrategy(AmcName.HDFC))
                .isInstanceOf(AmcNotFoundException.class)
                .hasMessageContaining("HDFC");
    }

    @Test
    void hasStrategy_registeredAmc_returnsTrue() {
        assertThat(factory.hasStrategy(AmcName.BLACKROCK)).isTrue();
    }

    @Test
    void hasStrategy_unregisteredAmc_returnsFalse() {
        assertThat(factory.hasStrategy(AmcName.HDFC)).isFalse();
    }
}
