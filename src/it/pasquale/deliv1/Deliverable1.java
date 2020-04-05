package it.pasquale.deliv1;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Deliverable1 {

	   private static final String MIN_DATE = "1800-00-00";
	   private static final String DATE_ISO_STRICT = "--date=iso-strict";
	
 	   //Pattern for date recognition
       private static final Pattern date = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
  	
	   private static String importRepository(String repoURL, String directory) throws InterruptedException {
		   
		   String repoName;
		   //Url must me in format https://../.../RepoName.git
		   String[] splitted = repoURL.split("/");
		   String repoGitName = splitted[splitted.length-1];
		   repoName = repoGitName.substring(0, repoGitName.length()-4);

		   
		   //The command will simply fail if the repository is already there
		   try {
				runCommand(Paths.get(directory), "git",  "clone", repoURL);
		   } catch (IOException e1) {
				e1.printStackTrace();
		   } 
		   
		   return repoName;
	   }
	   //Adds one month to a date (Assumes date is correcly formatted yyyy-mm)
	   private static String addOne(String date) {
		   
		   String[] splitted = date.split("-");
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
			   monthString = "0"+month;
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
	      try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
	         String jsonText = readAll(rd);
	         return new JSONArray(jsonText);
	       }
	   }

	   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	      InputStream is = new URL(url).openStream();
	      try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
	         String jsonText = readAll(rd);
	         return new JSONObject(jsonText);
	       } finally {
	         is.close();
	       }
	   }

	public static ArrayList<String> runCommand(Path directory, String... command) throws IOException {
		ProcessBuilder pb = new ProcessBuilder()
				.command(command)
				.directory(directory.toFile());
		Process p = pb.start();                                                                                                                                                   
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String s;
		ArrayList<String> toReturn = new ArrayList<String>();
		while ((s = stdInput.readLine()) != null) {
		        toReturn.add(s);
		}
		return toReturn;
	}
	
	private static void saveToCSV(HashMap<String, Integer> commitInfo, String fileName) throws IOException {
		
		File newCSV = new File(fileName);
		if (!newCSV.exists())
			newCSV.createNewFile();
		
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Date,Commits\n");
			
			Set<String> keys = commitInfo.keySet();
			for(String key: keys){
				fw.write(key + "," + commitInfo.get(key) + "\n");
			}
		}
		
		
	}
	//Function that uses git to to count all commits per month
	//The results are saved in the commit info structured (assumed correctly initialized)
	private static void countCommit(HashMap<String, Integer> commitInfo, String dirPath) throws IOException, InterruptedException {

		Pattern comPattern = Pattern.compile("commit");
		
		Path dir = Paths.get(dirPath);
		ArrayList<String> output  = new ArrayList<String>();
		output = runCommand(dir, "git", "log", DATE_ISO_STRICT);

		String fixedCommit = MIN_DATE;
		for (int s = 0; s < output.size(); s++ ) {
    		Matcher m = date.matcher(output.get(s));
			if (m.find() && fixedCommit.compareTo(m.group(0)) < 0)
			    fixedCommit = m.group(0).subSequence(0, 7).toString();
			m = comPattern.matcher(output.get(s));
			if (m.find()) {
				if (fixedCommit.compareTo(MIN_DATE) != 0) {
					commitInfo.put(fixedCommit, commitInfo.get(fixedCommit)+1);
				}
				fixedCommit = MIN_DATE;
			}
		}
		
	}
	//Function that uses git to recover all commit with keys per month
	//The results are saved in the commit info structured (assumed correctly initialized)
	private static void countCommitKeysOnce(HashMap<String, Integer> commitInfo, String dirPath) throws IOException, InterruptedException {
		
		Pattern comPattern = Pattern.compile("commit");
		Pattern keyPattern = Pattern.compile("STDCXX");
		
		Path dir = Paths.get(dirPath);
		ArrayList<String> output  = new ArrayList<String>();
		output = runCommand(dir, "git", "log", DATE_ISO_STRICT);

		String fixedCommit = MIN_DATE;
		boolean hasKey = false;
		for (int s = 0; s < output.size(); s++ ) {
    		Matcher m = date.matcher(output.get(s));
			if (m.find() && fixedCommit.compareTo(m.group(0)) < 0)
			    fixedCommit = m.group(0).subSequence(0, 7).toString();
					
			
			m = keyPattern.matcher(output.get(s));
			if (m.find()) {
				hasKey = true;
			}
			m = comPattern.matcher(output.get(s));
			if (m.find()) {
				if (hasKey && fixedCommit.compareTo(MIN_DATE) != 0) {
					commitInfo.put(fixedCommit, commitInfo.get(fixedCommit)+1);
				}
				fixedCommit = MIN_DATE;
				hasKey = false;
			}
		}

	}
	
	//Function that uses git to count all fixed tickets per month
	//The results are saved in the commit info structured (assumed correctly initialized)
	private static void countCommitKeys(HashMap<String, Integer> commitInfo, Vector<String> keys, String dirPath) throws IOException, InterruptedException {
		
		for (String key: keys) {
			Path dir = Paths.get(dirPath);
			ArrayList<String> output  = new ArrayList<String>();
			output = runCommand(dir, "git", "log", "--grep="+key, DATE_ISO_STRICT);

			String fixedCommit = MIN_DATE;
			for (int s = 0; s < output.size(); s++ ) {
	    		Matcher m = date.matcher(output.get(s));
				if (m.find() && fixedCommit.compareTo(m.group(0)) < 0)
				    fixedCommit = m.group(0).subSequence(0, 7).toString();
				
			}
			if (fixedCommit.compareTo(MIN_DATE) != 0) {
				commitInfo.put(fixedCommit, commitInfo.get(fixedCommit)+1);
			}
		}
	}
	
	  
	public static void main(String[] args) throws IOException, JSONException, InterruptedException {
		
		String dirPath = "/home/capo80/Desktop/apache_repo";
		
		String repoName = importRepository("https://github.com/apache/stdcxx.git", dirPath);
		
		dirPath += "/" + repoName;
		
		//Recover first commit
		ArrayList<String> firstCommit = new ArrayList<String>();
		try {
			firstCommit = runCommand(Paths.get(dirPath), "git",  "log","--max-parents=0", "HEAD", DATE_ISO_STRICT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Recover date of first commit (cut the days)
		String firstCommitDate = "";
		for (int s = 0; s < firstCommit.size(); s++ ) {
    		Matcher m = date.matcher(firstCommit.get(s));
			if (m.find()) {
			    firstCommitDate = m.group(0).subSequence(0, 7).toString();
			}
		}
		
		//Recover last commit
		ArrayList<String> lastCommit = new ArrayList<String>();
		try {
			lastCommit = runCommand(Paths.get(dirPath), "git",  "log","-1", DATE_ISO_STRICT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Recover date of last commit (cut the days)
		String lastCommitDate = "";
		for (int s = 0; s < lastCommit.size(); s++ ) {
    		Matcher m = date.matcher(lastCommit.get(s));
			if (m.find()) {
			    lastCommitDate = m.group(0).subSequence(0, 7).toString();
			}
		}
			
		//Initialize structure for the counting
		HashMap<String, Integer> commitInfo = new HashMap<String, Integer>();
		while (firstCommitDate.compareTo(lastCommitDate) <= 0) {
			commitInfo.put(firstCommitDate, 0);
			firstCommitDate = addOne(firstCommitDate);
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
			JSONObject json = readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");
			for (; i < total && i < j; i++) {
				//Iterate through each ticket
	            ticketKeys.add(issues.getJSONObject(i%1000).get("key").toString());
	            
	         }  
		} while (i < total);
	
		try {
			countCommitKeys(commitInfo, ticketKeys, dirPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		saveToCSV(commitInfo, "/home/capo80/Desktop/commitsKeys.csv");
		
		//reset structure for the counting
		Set<String> keys = commitInfo.keySet();
		for(String key: keys){
			commitInfo.put(key, 0);
		}
		
		try {
			countCommitKeysOnce(commitInfo, dirPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		saveToCSV(commitInfo, "/home/capo80/Desktop/commitsKeyOnce.csv");
		
		//reset structure for the counting
		keys = commitInfo.keySet();
		for(String key: keys){
			commitInfo.put(key, 0);
		}
				
		try {
			countCommit(commitInfo, dirPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		saveToCSV(commitInfo, "/home/capo80/Desktop/commits.csv");
		return;
	   }

	 

}
