package br.jus.trf1.dspace.clientrest.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private static final String urlBase = "https://demo.dspace.org/rest";
	private static final String TOKEN = null;

	public static void main(String[] args) throws Exception {
		// retrieveBitStreams("1b7dc5e9-2ea7-4b6a-b301-1f879b3a9890");
		//login("admin@dspace.org", "dspace");
		JSONArray arrayCommunities = getCommunities();
		for (int i = 0; i < arrayCommunities.length(); i++) {
			JSONObject object = arrayCommunities.getJSONObject(i);
			String uuidCommunity = object.getString("uuid");
			JSONArray arrayCollections = getCollections(uuidCommunity);
			LOGGER.info("======= Communities =======");
			LOGGER.info(uuidCommunity);

			for (int j = 0; j < arrayCollections.length(); j++) {
				JSONObject collection = arrayCollections.getJSONObject(j);
				String uuidCollection = collection.getString("uuid");
				LOGGER.info("======= Collections =======");
				LOGGER.info(uuidCollection);
				JSONArray arrayItems = getItems(uuidCollection);

				for (int k = 0; k < arrayItems.length(); k++) {
					JSONObject items = arrayItems.getJSONObject(k);
					String uuidItems = items.getString("uuid");
					LOGGER.info("======= Items =======");
					LOGGER.info(uuidItems);
					JSONArray arrayBitStreams = getBitStreams(uuidItems);
					LOGGER.info("======= BitStreams =======");
					for (int l = 0; l < arrayBitStreams.length(); l++) {
						JSONObject bitStreams = arrayBitStreams
								.getJSONObject(l);
						String uuidBitStreams = bitStreams.getString("uuid");

						LOGGER.info(uuidBitStreams);
						LOGGER.info("======= Data =======");
						LOGGER.info(getBitStream(uuidBitStreams).toString());
					}
				}
			}

		}

	}

	private static void retrieveBitStreams(String uuidBitStream)
			throws IOException {
		if (uuidBitStream != null && uuidBitStream != "") {
			JSONObject bitStream = getBitStream(uuidBitStream);
			String filename = bitStream.getString("name");
			URL website = new URL(urlBase + "/bitstreams/" + uuidBitStream
					+ "/retrieve");
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(filename);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}

	}

	private static JSONArray getBitStreams(String uuidItem) throws IOException {
		JSONArray arrayBitstreams;
		if (uuidItem != null && uuidItem != "") {
			arrayBitstreams = getJSonArray("/items/" + uuidItem + "/bitstreams");
		} else {
			arrayBitstreams = getJSonArray("/bitstreams/");
		}

		return arrayBitstreams;
	}

	private static JSONObject getBitStream(String uuidBitStream)
			throws IOException {
		JSONObject bitStream = null;
		if (uuidBitStream != null && uuidBitStream != "") {
			bitStream = getJSonObject("/bitstreams/" + uuidBitStream);
		}
		return bitStream;
	}

	private static JSONObject getJSonObject(String endpoint) throws IOException {
		LOGGER.debug("getJSonObject: {}", endpoint);
		String object = getJSON(endpoint);
		return new JSONObject(object);
	}

	private static JSONArray getItems(String uuidCollection) throws IOException {
		JSONArray arrayItems;
		if (uuidCollection != null && uuidCollection != "") {
			arrayItems = getJSonArray("/collections/" + uuidCollection
					+ "/items");
		} else {
			arrayItems = getJSonArray("/items/");
		}

		return arrayItems;
	}

	private static JSONObject getItem(String uuidItem) throws IOException {
		JSONObject item = null;
		if (uuidItem != null && uuidItem != "") {
			item = getJSonObject("/items/" + uuidItem);
		}
		return item;
	}

	private static JSONArray getCollections(String uuidCommunity)
			throws IOException {
		JSONArray arrayCollections;
		if (uuidCommunity != null && uuidCommunity != "") {
			arrayCollections = getJSonArray("/communities/" + uuidCommunity
					+ "/collections");
		} else {
			arrayCollections = getJSonArray("/collections/");
		}
		return arrayCollections;
	}

	private static JSONObject getCollection(String uuidCollection)
			throws IOException {
		JSONObject collection = null;
		if (uuidCollection != null && uuidCollection != "") {
			collection = getJSonObject("/collections/" + uuidCollection);
		}
		return collection;
	}

	private static JSONArray getCommunities() throws IOException {

		return getJSonArray("/communities");
	}

	private static JSONObject getCommunity(String uuidCommunity)
			throws IOException {
		JSONObject collection = null;
		if (uuidCommunity != null && uuidCommunity != "") {
			return getJSonObject("/communities/" + uuidCommunity);
		}
		return collection;
	}

	private static JSONArray getJSonArray(String endPoint) throws IOException {
		String communities = getJSON(endPoint);
		JSONArray arrayCommunities = getJSONArray(communities);
		return arrayCommunities;
	}

	private static JSONArray getJSONArray(String textContents) {
		JSONArray array = new JSONArray(textContents);
		return array;
	}

	private static String getJSON(String url) throws IOException {
		UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException(
				"json", "application/json",
				"https://demo.dspace.org/rest/communities");
		String mime = mimeType.getMimeType();
		Document doc = Jsoup.connect(urlBase + url)
				.header("Content-Type", mime).ignoreContentType(true).get();
		String textContents = doc.text();
		return textContents;
	}

	private static String login(String user, String password) throws Exception {
		UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException(
				"json", "application/json",
				"https://demo.dspace.org/rest/communities");
		String mime = mimeType.getMimeType();
		Document doc = Jsoup.connect(urlBase + "/login")
				.header("Content-Type", mime)
				.ignoreContentType(true)
				.requestBody("{\"email\":\"admin@dspace.org\", \"password\":\"dspace\"}")
				.post();
		String token = doc.text();
		LOGGER.debug("Autenticação OK. Token: {}", token);
		return token;

	}

	private static void logout() throws Exception {
		if (TOKEN != null && TOKEN != "") {
			UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException(
					"json", "application/json",
					"https://demo.dspace.org/rest/communities");
			String mime = mimeType.getMimeType();
			Response response = Jsoup.connect(urlBase + "/logout")
					.header("Content-Type", mime)
					.header("rest-dspace-token", TOKEN)
					.ignoreContentType(true)
					.method(Method.POST)
					.execute();
			if (response.statusCode() == 400) {
				throw new Exception("Token inválido");
			}
			if (response.statusCode() != 200) {
				throw new Exception("Falha ao realizar logout");
			}
			
		}

	}

	private static void deleteJSON(String url) throws Exception {
		if (TOKEN != null && TOKEN != "") {
			UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException(
					"json", "application/json",
					"https://demo.dspace.org/rest/communities");
			String mime = mimeType.getMimeType();
			Response response = Jsoup.connect(urlBase + url)
					.header("Content-Type", mime)
					.header("rest-dspace-token", TOKEN).ignoreContentType(true)
					.method(Method.DELETE).execute();
			if (response.statusCode() != 200) {
				throw new Exception();
			}
		}
	}
}
