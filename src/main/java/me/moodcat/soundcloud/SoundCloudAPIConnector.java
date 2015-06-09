package me.moodcat.soundcloud;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import lombok.SneakyThrows;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import java.net.URLEncoder;

/**
 * Connects to the SoundCloud API using its {@link #createClient()}.
 */
public abstract class SoundCloudAPIConnector {

	/**
	 * The charset that is used to encode urls.
	 */
	protected static final String URI_CHARSET = "UTF-8";

	/**
	 * The host-name able to be formatted with a sub-domain of SoundCloud.
	 */
	protected static final String SOUNDCLOUD_HOST_FORMAT_STRING = "https://%ssoundcloud.com";

	/**
	 * api.soundcloud.com takes care of all API calls.
	 */
	protected static final String SOUNDCLOUD_API = String.format(
			SOUNDCLOUD_HOST_FORMAT_STRING, "api.");

	/**
	 * soundcloud.com is the general host-name.
	 */
	protected static final String SOUNDCLOUD_HOST = String.format(
			SOUNDCLOUD_HOST_FORMAT_STRING, "");

	/**
	 * Our client-id in order to talk to SoundCloud.
	 */
	protected static final String CLIENT_ID = "b45b1aa10f1ac2941910a7f0d10f8e28";

	/**
	 * Obtain a HTTP-client to start a request.
	 *
	 * @return The HTTP-client.
	 */
	protected Client createClient() {
		return ResteasyClientBuilder.newBuilder().build();
	}

	/**
	 * Encode a String for URLs
	 * 
	 * @param url
	 *            string to be decoded
	 * @return decoded string
	 */
	@SneakyThrows
	protected static String encode(final String url) {
		return URLEncoder.encode(url, URI_CHARSET);
	}

	/**
	 * An invocation to the Soundcloud API
	 * 
	 * @param <T>
	 *            return type
	 */
	@FunctionalInterface
	interface Invocation<T> {

		/**
		 * Perform the call to the SoundCloud API.
		 *
		 * @param webTarget
		 *            target to interact with
		 * @return response
		 * @throws Exception
		 *             any exception that occurs
		 */
		T perform(WebTarget webTarget) throws Exception;
	}

	/**
	 * Perform a Http request to the Soundcloud API.
	 *
	 * @param invocation
	 *            Invocation that interacts with a {@link WebTarget}.
	 * @param <T>
	 *            Type of response
	 * @return return value of the request
	 * @throws SoundCloudException
	 *             If an error occurred
	 */
	protected <T> T perform(final Invocation<T> invocation)
			throws SoundCloudException {
		return perform(SOUNDCLOUD_API, invocation);
	}

	/**
	 * Perform a Http request to the Soundcloud API.
	 *
	 * @param host
	 *            The host to send the request to
	 * @param invocation
	 *            Invocation that interacts with a {@link WebTarget}.
	 * @param <T>
	 *            Type of response
	 * @return return value of the request
	 * @throws SoundCloudException
	 *             If an error occurred
	 */
	protected <T> T perform(final String host, final Invocation<T> invocation)
			throws SoundCloudException {
		Client client = createClient();
		try {
			return invocation.perform(client.target(host));
		} catch (Exception e) {
			throw new SoundCloudException(e.getMessage(), e);
		} finally {
			client.close();
		}
	}

}
