package dcn.ssu.ac.kr;

import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Random;

import javax.servlet.http.*;


import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.labs.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class RTCWebDCNServlet extends HttpServlet {
	
	private String get_random(int len) {
		Random r = new Random();
		String word = "";
		for(int i = 0; i < len; i ++) {
			word += Integer.toString(r.nextInt(10));
		}
		return word;
	}
	
	private String make_pc_config(String stunServer, String turnServer) {
		String server = "";
		String stun_config = "", turn_config = "";
		if(stunServer != null) {
			stun_config = "stun:" + stunServer;
		} else {
			stun_config = "stun:" + "stun.l.google.com:19302";
		}
		server = "{\'url\':stun_config}";
		if(turnServer != null) {
			turn_config = "turn:" + turnServer;
			server += "{\'url\':" + turn_config + ", \'credential\':\'\'}";
		}
		return "{\'iceServers\':" + server + "}";
	}
	
	DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String room_key = req.getParameter("r");
		String debug = req.getParameter("debug");
		debug = "true";
		String stunServer = req.getParameter("ss");
		String turnServer = req.getParameter("ts");
		if(room_key == null) {
			room_key = get_random(8);
			String str = "/?r=" + room_key;

			if(debug != null) {
				str += ("&debug=" + debug);
			}
			if(turnServer != null) {
				str += "&ts=" + turnServer;
			}			
			if(stunServer != null) {
				str += "&ss=" + stunServer;
			}
			resp.sendRedirect("/?r=" + room_key);
			return;
		}
		
		String user = null;
		Integer initiator = 0;
		
		Entity room;
		try {
			room = dataStore.get(KeyFactory.createKey("Room", room_key));
			if(RoomManagement.getOccupancy(room) == 1 && !debug.equals("full")) {
				user = get_random(8);
				RoomManagement.addUser(room, user);
				initiator = 1;
		} else {
			FileReader reader = new FileReader("full-template");
			CharBuffer buffer = CharBuffer.allocate(16384);
			reader.read(buffer);
			String index = new String(buffer.array());
			index = index.replaceAll("\\{\\{ room_key \\}\\}", room_key);
			resp.setContentType("text/html");
			resp.getWriter().write(index);
			return;
		}
		} catch (EntityNotFoundException e) {
			if(!debug.equals("full")) {
				user = get_random(8);
				room = new Entity("Room", room_key);
				RoomManagement.addUser(room, user);
				if(!debug.equals("loopback")) {
					initiator = 0;
				} else {
					RoomManagement.addUser(room, user);
					initiator = 1;
				}
					
			}
			
		}
		
		
		String room_link = "https://localhost:8888/?r=" + room_key;
		if(debug != null) {
			room_link += ("&debug=" + debug);
		}
		
		if(turnServer != null) {
			room_link += "&ts=" + turnServer;
		}
		
		if(stunServer != null) {
			room_link += "&ss=" + stunServer;
		}
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		String token = channelService.createChannel(room_key + "/" + user);
		String pc_config = make_pc_config(stunServer, turnServer);
		FileReader reader = new FileReader("index-template");
		CharBuffer buffer = CharBuffer.allocate(16384);
		reader.read(buffer);
		String index = new String(buffer.array());
		index = index.replaceAll("\\{\\{ room_link \\}\\}", room_link);
		index = index.replaceAll("\\{\\{ room_key \\}\\}", room_key);
		index = index.replaceAll("\\{\\{ initiator \\}\\}", Integer.toString(initiator));
		index = index.replaceAll("\\{\\{ token \\}\\}", token);
		index = index.replaceAll("\\{\\{ me \\}\\}", user);
		index = index.replaceAll("\\{\\{ pc_config \\}\\}", pc_config);

		resp.setContentType("text/html");
		resp.getWriter().write(index);

	}
	
	
}
