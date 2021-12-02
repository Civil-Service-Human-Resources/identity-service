package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class frontendAssetConfig {

    @Value("${templates.assetCdn}")
    private String assetCdn;

    @Bean(name = "assetCdn")
    public AssetCdnService assetCdnService() {
        return () -> assetCdn;
    }
}
