package com.finsight.api.service;

public interface CurrentUserService {
    /** The Auth0 <code>sub</code> claim of the caller, e.g. "auth0|123456". */
    String getSub();
}


