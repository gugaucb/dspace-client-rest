package br.jus.trf1.dspace.clientrest.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	private static final String UUID = "id";
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	// private static final String urlBase = "https://demo.dspace.org/rest";
	private static final String urlBase = "http://172.16.3.48:8080/rest";

	private static String TOKEN = null;

	public static void main(String[] args) throws Exception {
		// retrieveBitStreams("139815");
		login("dspace", "Gol2Reudspace");
		// status();
		 deleteItem("195592");
		//teste();

		// logout();
		// listAllOBject();
		// console();

	}

	private static void console() throws Exception {

		Scanner input = new Scanner(System.in);
		System.out.print("Command: ");
		String line = input.nextLine();
		while (!line.equals("q")) {

			// create Options object
			CommandLine cmd = command(line);
			Option option = cmd.getOptions()[0];
			String command = option.getValue();
			switch (command) {
			case "help":
				help();
				break;
			case "signin":
				if (cmd.getArgs().length == 2) {
					login(cmd.getArgs()[0], cmd.getArgs()[1]);
				}
				break;
			case "signout":
				logout();
				break;
			default:
				help();
			}
			System.out.print("Command: ");
			line = input.nextLine();
		}
	}

	private static void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("dspace-cli", getOptions());
	}

	private static CommandLine command(String line) throws ParseException {
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		String[] args = line.split(" ");
		CommandLine cmd = parser.parse(options, args);
		return cmd;
	}

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(Option.builder("c").hasArgs().argName("login:password").longOpt("command").build());
		return options;
	}

	private static void listAllOBject() throws IOException {
		JSONArray arrayCommunities = getCommunities();
		for (int i = 0; i < arrayCommunities.length(); i++) {
			JSONObject object = arrayCommunities.getJSONObject(i);
			String uuidCommunity = object.getInt(UUID) + "";
			JSONArray arrayCollections = getCollections(uuidCommunity);
			LOGGER.info("======= Communities =======");
			LOGGER.info(uuidCommunity);

			for (int j = 0; j < arrayCollections.length(); j++) {
				JSONObject collection = arrayCollections.getJSONObject(j);
				String uuidCollection = collection.getInt(UUID) + "";
				LOGGER.info("======= Collections =======");
				LOGGER.info(uuidCollection);
				JSONArray arrayItems = getItems(uuidCollection);

				for (int k = 0; k < arrayItems.length(); k++) {
					JSONObject items = arrayItems.getJSONObject(k);
					String uuidItems = items.getInt(UUID) + "";
					LOGGER.info("======= Items =======");
					LOGGER.info(uuidItems);
					JSONArray arrayBitStreams = getBitStreams(uuidItems);
					LOGGER.info("======= BitStreams =======");
					for (int l = 0; l < arrayBitStreams.length(); l++) {
						JSONObject bitStreams = arrayBitStreams.getJSONObject(l);
						String uuidBitStreams = bitStreams.getInt(UUID) + "";

						LOGGER.info(uuidBitStreams);
						LOGGER.info("======= Data =======");
						LOGGER.info(getBitStream(uuidBitStreams).toString());
					}
				}
			}

		}
	}

	private static void teste() throws Exception {

		JSONArray arrayItems = getItems("112");
		LOGGER.info("======= Items " + arrayItems.length() + " =======");
		for (int k = 0; k < arrayItems.length(); k++) {
			JSONObject items = arrayItems.getJSONObject(k);
			String uuidItems = items.getInt(UUID) + "";
			if (!uuidItems.equals("57968")) {
				LOGGER.info("Deleting {}", uuidItems);
				deleteItem(uuidItems);
				LOGGER.info("Deleted {}", uuidItems);
			}
		}
	}

	private static void retrieveBitStreams(String uuidBitStream) throws IOException {
		if (uuidBitStream != null && uuidBitStream != "") {
			JSONObject bitStream = getBitStream(uuidBitStream);
			String filename = bitStream.getString("name");
			URL website = new URL(urlBase + "/bitstreams/" + uuidBitStream + "/retrieve");
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

	private static JSONObject getBitStream(String uuidBitStream) throws IOException {
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
			arrayItems = getJSonArray("/collections/" + uuidCollection + "/items?limit=1000000");
		} else {
			arrayItems = getJSonArray("/items/?limit=1000000");
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

	private static void deleteItem(String uuidItem) throws Exception {
		if (uuidItem != null && uuidItem != "") {
			deleteJSON("/items/" + uuidItem);
		}
	}

	private static JSONArray getCollections(String uuidCommunity) throws IOException {
		JSONArray arrayCollections;
		if (uuidCommunity != null && uuidCommunity != "") {
			arrayCollections = getJSonArray("/communities/" + uuidCommunity + "/collections");
		} else {
			arrayCollections = getJSonArray("/collections/");
		}
		return arrayCollections;
	}

	private static JSONObject getCollection(String uuidCollection) throws IOException {
		JSONObject collection = null;
		if (uuidCollection != null && uuidCollection != "") {
			collection = getJSonObject("/collections/" + uuidCollection);
		}
		return collection;
	}

	private static JSONArray getCommunities() throws IOException {

		return getJSonArray("/communities");
	}

	private static JSONObject getCommunity(String uuidCommunity) throws IOException {
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
		UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException("json", "application/json",
				"https://demo.dspace.org/rest/communities");
		String mime = mimeType.getMimeType();
		Document doc = Jsoup.connect(urlBase + url).header("Content-Type", mime).ignoreContentType(true)
				.timeout(6000000).get();
		String textContents = doc.text();
		return textContents;
	}

	private static String login(String user, String password) throws Exception {
		UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException("json", "application/json",
				"https://demo.dspace.org/rest/communities");
		String mime = mimeType.getMimeType();
		Document doc = Jsoup.connect(urlBase + "/login").header("Content-Type", mime).ignoreContentType(true)
				.timeout(6000000).requestBody("{\"email\":\"" + user + "\", \"password\":\"" + password + "\"}").post();
		TOKEN = doc.text();
		LOGGER.debug("Autenticação OK. Token: {}", TOKEN);
		return TOKEN;

	}

	private static void logout() throws Exception {
		if (TOKEN != null && TOKEN != "") {
			LOGGER.debug("Token {}", TOKEN);
			UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException("json", "application/json",
					"https://demo.dspace.org/rest/communities");
			String mime = mimeType.getMimeType();
			Connection connection = Jsoup.connect(urlBase + "/logout").header("Content-Type", mime)
					.header("rest-dspace-token", TOKEN).userAgent("Mozilla").ignoreContentType(true)
					.method(Method.POST);
			Response response = connection.execute();
			if (response.statusCode() == 400) {
				throw new Exception("Token inválido");
			}
			if (response.statusCode() != 200) {
				throw new Exception("Falha ao realizar logout");
			}
			LOGGER.info("Logout OK");

		}

	}

	private static void status() throws Exception {
		if (TOKEN != null && TOKEN != "") {
			LOGGER.debug("Token {}", TOKEN);
			UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException("json", "application/json",
					"https://demo.dspace.org/rest/communities");
			String mime = mimeType.getMimeType();
			Connection connection = Jsoup.connect(urlBase + "/test").header("Content-Type", mime)
					.header("rest-dspace-token", TOKEN).userAgent("Mozilla").ignoreContentType(true).method(Method.GET);
			Response response = connection.execute();
			if (response.statusCode() == 400) {
				throw new Exception("Token inválido");
			}
			if (response.statusCode() != 200) {
				throw new Exception("Falha ao realizar logout");
			}
			LOGGER.info("Test OK");

		}

	}

	private static void deleteJSON(String url) throws Exception {
		if (TOKEN != null && TOKEN != "") {
			UnsupportedMimeTypeException mimeType = new UnsupportedMimeTypeException("json", "application/json",
					"https://demo.dspace.org/rest/communities");
			String mime = mimeType.getMimeType();
			Response response = Jsoup.connect(urlBase + url).header("Content-Type", mime)
					.header("rest-dspace-token", TOKEN).ignoreContentType(true).method(Method.DELETE).execute();
			if (response.statusCode() != 200) {
				throw new Exception();
			}
		}
	}
}
