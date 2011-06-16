package cudl.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class RemoteFileAccess {
	public static File getRemoteFile(String url, String fileName)
			throws IOException {
		System.err.println("remotefilename " + url + fileName);
		if (null == url)
			return null;
		URL url2 = new URL(url + fileName);
		BufferedReader in = new BufferedReader(new InputStreamReader(url2
				.openStream()));
		File file = new File("tempremotefile");
		FileWriter fileWriter = new FileWriter(file);
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			fileWriter.append(inputLine + "\n");
		}
		in.close();
		fileWriter.close();

		return file;
	}
}
