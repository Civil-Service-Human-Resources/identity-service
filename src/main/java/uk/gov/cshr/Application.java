package uk.gov.cshr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.service.notify.NotificationClient;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Bean(name = "loginAttemptCache")
    public Map<String, Integer> loginAttemptCache() {
        return new HashMap<>();
    }

    @Bean
    public NotificationClient notificationClient(@Value("${govNotify.key}") String key) {
        return new NotificationClient(key);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}