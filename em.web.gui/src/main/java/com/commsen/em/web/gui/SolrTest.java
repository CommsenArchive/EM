package com.commsen.em.web.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class SolrTest {

	public static void main(String[] args) throws IOException {
		search("commsen");
	}
	
	public static void search(String query) throws IOException {
        URL u = new URL("http://search.maven.org/solrsearch/select?q=" + query + "&rows=100&wt=json");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream(), StandardCharsets.UTF_8))) {

            Gson gson = new Gson();
            JsonReader jsonReader =  gson.newJsonReader(br);
            JsonParser parser = new JsonParser();
			JsonObject jsonObj = (JsonObject) parser.parse(jsonReader);

			JsonArray docs = jsonObj.getAsJsonObject("response").getAsJsonArray("docs");
			
			System.out.println(docs);

            
//            jo = jo.getJSONObject("response");
//            int count = jo.getInt("numFound");
//
//            List<Artifact> artifacts = new ArrayList<>(count);
//
//            JSONArray docs = jo.getJSONArray("docs");
//
//            for (int i = 0; i < count; i++) {
//                JSONObject jsonArtifact = docs.getJSONObject(i);
//
//                artifacts.add(new Artifact(jsonArtifact.getString("g"), jsonArtifact.getString("a"), jsonArtifact.getString("latestVersion")));
//
//            }
//
//            return artifacts;
        }
    }
}
