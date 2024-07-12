package uk.gov.cshr.utils;

import uk.gov.cshr.domain.Identity;

import java.time.Instant;
import java.util.UUID;

public class DataUtils {

    public static final String EMAIL_TEMPLATE = "%s@example.org";
    public static final String PASSWORD = "password123";

    public static Identity createIdentity() {
        return createIdentity(null);
    }

    public static Identity createIdentity(String agencyTokenUid) {
        return createIdentity(UUID.randomUUID().toString(), UUID.randomUUID().toString(), agencyTokenUid);
    }

    public static Identity createIdentity(String uid, String emailPrefix, String agencyTokenUid) {
        return new Identity(uid, String.format(EMAIL_TEMPLATE, emailPrefix), PASSWORD, true, false, null, Instant.now(), false, false, agencyTokenUid);
    }
}
