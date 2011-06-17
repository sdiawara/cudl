package cudl.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SessionFileCreator {
	private List<String> session = new ArrayList<String>() {
		{

			add("session = new Object();");
			add("connection =  new Object();");
			add("session.connection  = connection;");
			add("connection.local  =  new Object();");
			add("connection.remote  =  new Object();");
			add("connection.protocol  =  new Object();");
			add("connection.protocol.name  =  'isdnvn6';");
			add("connection.protocol.isdnvn6 =  new Object();");
			add("connection.protocol.version  =  '1.0';");
			add("connection.local.uri  =  'tel:0892683613';");
			add("connection.remote.uri  =  'tel:0400000400';");
			add("connection.connectionid  =  'CON1642389';");
			add("connection.protocol.isdnvn6['channel']  =  'vipB07_1';");
			add("connection.protocol.isdnvn6['evt']  =  't3.incoming';");
			add("connection.protocol.isdnvn6['infos'] = '153908500 255 2 1 255';");
			add("connection.protocol.isdnvn6['lastsup'] = '4700830E8307839098720051018A01338800';");
			add("connection.protocol.isdnvn6['local']  =  '140522806';");
			add("connection.protocol.isdnvn6['remote']  =  '153908500';");
			add("connection.protocol.isdnvn6['remote2']  =  '153908500';");
			add("connection.protocol.isdnvn6['state']  =  'incoming';");
			add("connection.channel = '1';");
		}
	};

	public File get3900DefaultSession() throws IOException {
		File sessionFile = new File("defaultSession");
		FileWriter fileWriter = new FileWriter(sessionFile);

		for (Iterator<String> iterator = session.iterator(); iterator.hasNext();) {
			String script = (String) iterator.next();
			String caller = InterpreterRequierement.connectionRemoteUri;
			String called = InterpreterRequierement.connectionLocalUri;
			String origin = InterpreterRequierement.connectionOrigin;

			if (caller != null && script.startsWith("connection.remote.uri")) {
				script = "connection.remote.uri ='tel:" + caller + "';";
			}

			if (called != null && script.startsWith("connection.local.uri")) {
				script = "connection.local.uri ='tel:" + called + "';";
			}

			if (origin != null
					&& script
							.startsWith("connection.protocol.isdnvn6['lastsup']")) {
				script = "connection.protocol.isdnvn6['lastsup']  ='4700830E8307839098720051018A01"
						+ origin + "8800';";
			}

			fileWriter.write(script);
		}
		fileWriter.close();
		return sessionFile;
	}
}
