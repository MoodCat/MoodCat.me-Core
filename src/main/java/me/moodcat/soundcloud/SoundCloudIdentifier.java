package me.moodcat.soundcloud;

import me.moodcat.soundcloud.models.MeModel;

/**
 * Identify Soundcloud logins.
 */
public class SoundCloudIdentifier extends SoundCloudAPIConnector {

    private static final String ME_ENDPOINT = "me.json";

    private static final String TOKEN_PARAM = "oauth_token";

    /**
     * The /me resource allows you to get information about the authenticated user and easily access
     * his or her related subresources like tracks, followings, followers, groups and so on.
     * 
     * @param token
     *            A valid OAuth token
     * @return the me response
     */
    public MeModel getMe(final String token) throws SoundCloudException {
        return perform(invocation -> invocation.path(ME_ENDPOINT)
            .queryParam(TOKEN_PARAM, token)
            .request()
            .get(MeModel.class));
    }

}
