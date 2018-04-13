package uk.gov.cshr.service.security;

import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;

public class WebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {

    @Override
    protected SecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, FilterInvocation fi) {
        WebSecurityExpressionRoot root = (WebSecurityExpressionRoot) super.createSecurityExpressionRoot(authentication, fi);
        root.setDefaultRolePrefix(""); //remove the prefix ROLE_
        return root;
    }
}
