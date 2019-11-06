package uk.gov.cshr.AppInsightsHelper;

import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

@Component
public class Helper {
    public void initAppInsighsFilter(MockMvc mockMvc) throws IllegalAccessException {
        Filter[] filters = (Filter[]) FieldUtils.readField(mockMvc, "filters", true);
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].getClass().getSimpleName().equals("PatternMappingFilterProxy")) {
                WebRequestTrackingFilter filter = (WebRequestTrackingFilter) FieldUtils.readField(filters[i],
                        "delegate", true);
                FieldUtils.writeField(filter, "appName", "test", true);
                filter.init(new FilterConfig() {
                    @Override
                    public String getFilterName() {
                        return null;
                    }
                    @Override
                    public ServletContext getServletContext() {
                        return null;
                    }
                    @Override
                    public String getInitParameter(String name) {
                        return null;
                    }
                    @Override
                    public Enumeration<String> getInitParameterNames() {
                        return null;
                    }
                });
            }
        }
    }
}
