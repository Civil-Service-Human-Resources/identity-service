package uk.gov.cshr.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Token;
import uk.gov.cshr.domain.TokenStatus;
import uk.gov.cshr.repository.TokenRepository;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;
import static uk.gov.cshr.domain.Token.extractTokenKey;

@Service
public class TokenStore implements org.springframework.security.oauth2.provider.token.TokenStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenStore.class);

    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    private TokenRepository tokenRepository;

    @Autowired
    public TokenStore(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Cacheable(cacheNames = "readAuthenticationCache", key = "#token.toString()")
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        LOGGER.info("1. readAuthentication");
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String tokenValue) {
        LOGGER.info("2. readAuthentication no cache");
        Token token = tokenRepository.findByTokenIdAndStatus(extractTokenKey(tokenValue), TokenStatus.ACTIVE);
        if (token != null) {
            return token.getAuthentication();
        }
        return null;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        LOGGER.info("3. storeAccessToken");
        Token storedToken = tokenRepository.findByTokenIdAndStatus(extractTokenKey(token.getValue()), TokenStatus.ACTIVE);
        if (storedToken == null) {
            storedToken = new Token(authenticationKeyGenerator.extractKey(authentication), token, authentication);
        } else {
            storedToken.setAuthentication(authentication);
        }
        tokenRepository.save(storedToken);
    }

    @Override
    @Cacheable(cacheNames = "readAccessTokenCache", key = "#tokenValue")
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        LOGGER.info("4. readAccessToken");

        Token token = tokenRepository.findByTokenIdAndStatus(extractTokenKey(tokenValue), TokenStatus.ACTIVE);
        if (token != null) {
            return token.getToken();
        }
        return null;
    }

    @Override
    @CacheEvict(cacheNames = "readAccessTokenCache", key = "#token.getValue()")
    public void removeAccessToken(OAuth2AccessToken token) {
        LOGGER.info("5. removeAccessToken");

        Token storedToken = tokenRepository.findByTokenIdAndStatus(extractTokenKey(token.getValue()), TokenStatus.ACTIVE);
        if (storedToken != null) {
            storedToken.setStatus(TokenStatus.REVOKED);
            tokenRepository.save(storedToken);
        }
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        LOGGER.info("6. storeRefreshToken");
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        return null;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return null;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        LOGGER.info("7. getAccessToken");

        Token storedToken = tokenRepository.findByAuthenticationIdAndStatus(authenticationKeyGenerator.extractKey(authentication), TokenStatus.ACTIVE);
        if (storedToken != null) {
            return storedToken.getToken();
        }
        return null;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        LOGGER.info("8. findTokensByClientIdAndUserName");

        Collection<Token> storedTokens = tokenRepository.findByClientIdAndUserName(clientId, userName);
        return storedTokens.stream()
                .map(Token::getToken)
                .collect(toSet());
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        LOGGER.info("9. findTokensByClientId");

        Collection<Token> storedTokens = tokenRepository.findByClientId(clientId);
        return storedTokens.stream()
                .map(Token::getToken)
                .collect(toSet());
    }
}
