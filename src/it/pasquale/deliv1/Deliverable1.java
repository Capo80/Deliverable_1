package it.pasquale.deliv1;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Deliverable1 {

 	   //Pattern for date recognition
       final private static Pattern date = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");

	   final private static String dirPath ="/home/capo80/Desktop/apache_repo/stdcxx";
	   	
	   //Adds one month to a date (Assumes date is correcly formatted yyyy-mm)
	   private static String addOne(String date) {
		   
		   //System.out.println(date);
		   String[] splitted = date.split("-");
		   //System.out.println(splitted[1]);
		   int month = Integer.parseInt(splitted[1]);
		   int year = Integer.parseInt(splitted[0]);
		   
		   if (month != 12)
			   month++;
		   else {
			   year++;
			   month = 1;
		   }
		   String monthString = "";
		   if (month < 10)
			   monthString = "0"+String.valueOf(month);
		   else
			   monthString = String.valueOf(month);
		   return String.valueOf(year) + "-" + monthString;
		   
	   }
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
	
	private static void saveToCSV(Hashtable<String, Integer> commitInfo, String fileName) throws IOException {
		
		File newCSV = new File(fileName);
		if (!newCSV.exists())
			newCSV.createNewFile();
		
		FileWriter fw = new FileWriter(newCSV);
		
		fw.write("Date,Commits\n");
		
		Set<String> keys = commitInfo.keySet();
		for(String key: keys){
			fw.write(key + "," + String.valueOf(commitInfo.get(key)) + "\n");
		}
		
		fw.flush();
		fw.close();
		
		
	}
	//Function that uses git to to recover all commits
	//The results are saved in the commit info structured (assumed correctly initialized)
	private static void recoverCommit(Hashtable<String, Integer> commitInfo) throws IOException, InterruptedException {
		
		Pattern comPattern = Pattern.compile("commit");
		
		Path dir = Paths.get(dirPath);
		Vector<String> output  = new Vector<>();
		output = runCommand(dir, "git", "log", "--date=iso-strict");

		//System.out.println(key);
		String fixedCommit = "1800-00-00";
		for (int s = 0; s < output.size(); s++ ) {
    		//System.out.println(output.get(s));
			Matcher m = date.matcher(output.get(s));
			if (m.find()) {
			    if (fixedCommit.compareTo(m.group(0)) < 0)
			    	fixedCommit = m.group(0).subSequence(0, 7).toString();
					//System.out.println(m.group(0));
					
			}
			m = comPattern.matcher(output.get(s));
			if (m.find()) {
				if (fixedCommit.compareTo("1800-00-00") != 0) {
					commitInfo.put(fixedCommit, commitInfo.get(fixedCommit)+1);
				}
				fixedCommit = "1800-00-00";
			}
		}
		
	}
	
	
	//Function that uses git to recover all commit that match a specific ticket key
	//The results are saved in the commit info structured (assumed correctly initialized)
	private static void recoverCommitKeys(Hashtable<String, Integer> commitInfo, Vector<String> keys) throws IOException, InterruptedException {
		
		for (String key: keys) {
			Path dir = Paths.get(dirPath);
			Vector<String> output  = new Vector<>();
			output = runCommand(dir, "git", "log", "--grep="+key, "--date=iso-strict");

			//System.out.println(key);
			String fixedCommit = "1800-00-00";
			for (int s = 0; s < output.size(); s++ ) {
	    		//System.out.println(output.get(s));
				Matcher m = date.matcher(output.get(s));
				if (m.find()) {
				    if (fixedCommit.compareTo(m.group(0)) < 0)
				    	fixedCommit = m.group(0).subSequence(0, 7).toString();
						//System.out.println(m.group(0));
						
				}
			}
			System.out.println(fixedCommit);
			if (fixedCommit.compareTo("1800-00-00") != 0) {
				commitInfo.put(fixedCommit, commitInfo.get(fixedCommit)+1);
			}
		}
	}
	
	  
	public static void main(String[] args) throws IOException, JSONException {
			 
		//Recover first commit
		Vector<String> firstCommit = new Vector<>();
		try {
			firstCommit = runCommand(Paths.get(dirPath), "git",  "log","--max-parents=0", "HEAD", "--date=iso-strict");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Recover date of first commit (cut the days)
		String firstCommitDate = "";
		for (int s = 0; s < firstCommit.size(); s++ ) {
    		//System.out.println(firstCommit.get(s));
			Matcher m = date.matcher(firstCommit.get(s));
			if (m.find()) {
			    firstCommitDate = m.group(0).subSequence(0, 7).toString();
			}
		}
		
		System.out.println(firstCommitDate);
		
		//Recover last commit
		Vector<String> lastCommit = new Vector<>();
		try {
			lastCommit = runCommand(Paths.get(dirPath), "git",  "log","-1", "--date=iso-strict");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Recover date of last commit (cut the days)
		String lastCommitDate = "";
		for (int s = 0; s < lastCommit.size(); s++ ) {
    		//System.out.println(lastCommit.get(s));
			Matcher m = date.matcher(lastCommit.get(s));
			if (m.find()) {
			    lastCommitDate = m.group(0).subSequence(0, 7).toString();
			}
		}
		
		//System.out.println(lastCommitDate);
		
		//Initialize structure for the counting
		Hashtable<String, Integer> commitInfo = new Hashtable<String, Integer>();
		while (firstCommitDate.compareTo(lastCommitDate) <= 0) {
			commitInfo.put(firstCommitDate, 0);
			firstCommitDate = addOne(firstCommitDate);
			System.out.println(firstCommitDate);
		}
		Set<String> keys = commitInfo.keySet();
		for(String key: keys){
			System.out.println(key+": "+commitInfo.get(key));
		}
		
		//Modified projName
		String projName ="STDCXX";
		Integer j = 0, i = 0, total = 1;
		Vector<String> ticketKeys = new Vector<String>();
		//Get JSON API for closed bugs w/ AV in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if tickets >1000
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
					+ projName + "%22&fields=key&startAt="
	                + i.toString() + "&maxResults=" + j.toString();
			//System.out.println(url);
			JSONObject json = readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");
			for (; i < total && i < j; i++) {
				//Iterate through each ticket
	            ticketKeys.add(issues.getJSONObject(i%1000).get("key").toString());
	            
	         }  
		} while (i < total);
		//Set<String> keys = commitInfo.keySet();
		/*for(String key: ticketKeys){
			System.out.println(key);
		}*/
		
		try {
			recoverCommitKeys(commitInfo, ticketKeys);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		saveToCSV(commitInfo, "/home/capo80/Desktop/commitsKeys.csv");
		
		//reset structure for the counting
		keys = commitInfo.keySet();
		for(String key: keys){
			commitInfo.put(key, 0);
		}
		
		try {
			recoverCommit(commitInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		saveToCSV(commitInfo, "/home/capo80/Desktop/commits.csv");
		return;
	   }

	 

}
