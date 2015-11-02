package com.spikeify.cron.utils;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlUtils {

	private UrlUtils() {
		// hide
	}

	public static String getFullUrl(String rootUrl, String relativeUrl) {

		if (rootUrl == null || rootUrl.trim().length() == 0) {
			throw new IllegalArgumentException("rootUrl must be given.");
		}

		URI root;
		try {
			if (rootUrl.endsWith("/")) {
				rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
			}

			root = new URI(rootUrl);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid root URL given.");
		}

		if (relativeUrl != null) {
			try {
				URI uri = new URI(relativeUrl);
				relativeUrl = uri.toString();

				if (uri.isAbsolute()) {
					return relativeUrl;
				}

				if (!relativeUrl.startsWith("/")) {
					relativeUrl = "/" + relativeUrl;
				}

				URI full = root.resolve(relativeUrl);
				return full.toString();
			}
			catch (URISyntaxException e) {
				throw new IllegalArgumentException("Invalid relative URL given.");
			}
		}

		return root.toString();
	}

	public static String composeUrl(String scheme, String domain, int port, String path) {
		return scheme + "://" + domain + (port > 0 && port != 80 && port != 443 ? ":" + port : "") + "/" + path;
	}
}
