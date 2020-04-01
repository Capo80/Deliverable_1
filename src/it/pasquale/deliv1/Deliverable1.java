package it.pasquale.deliv1;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Deliverable1 {

	   private static String readAll(Reader rd) throws IOException {
		      StringBuilder sb = new StringBuilder();
		      int cp;
		      while ((cp = rd.read()) != -1) {
		         sb.append((char) cp);
		      }
		      return sb.toString();
		   }

	   public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
	      InputStream is = new URL(url).openStream();
	      try {
	         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	         String jsonText = readAll(rd);
	         JSONArray json = new JSONArray(jsonText);
	         return json;
	       } finally {
	         is.close();
	       }
	   }

	   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	      InputStream is = new URL(url).openStream();
	      try {
	         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	         String jsonText = readAll(rd);
	         JSONObject json = new JSONObject(jsonText);
	         return json;
	       } finally {
	         is.close();
	       }
	   }

	public static Vector<String> runCommand(Path directory, String... command) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder()
				.command(command)
				.directory(directory.toFile());
		Process p = pb.start();                                                                                                                                                   
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String s;
		Vector<String> toReturn = new Vector<>();
		while ((s = stdInput.readLine()) != null) {
				//System.out.println(s);
		        toReturn.add(s);
		}
		return toReturn;
	}
	
	  
	public static void main(String[] args) throws IOException, JSONException {
			 
		
		//Modified projName
		String projName ="STDCXX";
		Integer j = 0, i = 0, total = 1;
		//Get JSON API for closed bugs w/ AV in the project
		do {
			 //Only gets a max of 1000 at a time, so must do this multiple times if tickets >1000
	         j = i + 1000;
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	                + projName + "%22AND(%22status%22=%22closed%22OR"
	                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key&startAt="
	                + i.toString() + "&maxResults=" + j.toString();
	         //System.out.println(url);
	         JSONObject json = readJsonFromUrl(url);
	         JSONArray issues = json.getJSONArray("issues");
	         total = json.getInt("total");
	         for (; i < total && i < j; i++) {
	            //Iterate through each ticket
	            String key = issues.getJSONObject(i%1000).get("key").toString();
	    		Path dir = Paths.get("/home/capo80/Desktop/apache_repo/stdcxx");
	    		Vector<String> output  = new Vector<>();
	    		try {
	    			output = runCommand(dir, "git", "log", "--grep="+key, "--date=iso-strict");
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (InterruptedException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    		//System.out.println(key);
	    		Pattern date = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
	    		String last_commit = "1800-00-00";
	    		for (int s = 0; s < output.size(); s++ ) {
	    			Matcher m = date.matcher(output.get(s));
	    			if (m.find()) {
	    			    if (last_commit.compareTo(m.group(0)) < 0)
	    			    	last_commit = m.group(0);
	    					//System.out.println(m.group(0));
	    					// s now contains "BAR"
	    			}
	    		}
	    		System.out.println(key + ", " + last_commit);
	         }  
	      } while (i < total);
	      return;
	   }

	 

}
