package org.kie.server.router.identity;

import io.undertow.security.idm.IdentityManager;

public interface IdentityService extends IdentityManager {

    default String id() {
        return "default";
    }

    default void addKieServerInstance(String kieServerInstanceId, String password) throws Exception {
        throw new UnsupportedOperationException();
    }

    default void removeKieServerInstance(String kieServerInstanceId) {
        throw new UnsupportedOperationException();
    }
}
