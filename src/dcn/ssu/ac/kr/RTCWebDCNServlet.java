package dcn.ssu.ac.kr;

import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.*;


import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

import com.google.appengine.api.datastore.KeyFactory;


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
	
	private String make_pc_config(String stunServer) {
		if(stunServer != null) {
			return "STUN " + stunServer;
		} else {
			return "STUN stun.l.google.com:19302";
		}
	}
	
	DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String room_key = req.getParameter("r");
		String debug = req.getParameter("debug");
		debug = "true";
		String stunServer = req.getParameter("ss");
		if(room_key == null) {
			room_key = get_random(8);
			String str = "/?r=" + room_key;

			if(debug != null) {
				str += ("&debug=" + debug);
			}
			if(stunServer != null) {
				str += "&ss=" + stunServer;
			}
			resp.sendRedirect("/?r=" + room_key);
			return;
		}
		
		String user = null;
		Integer initiator = 0;
		
		Entity room = null;
		try {
			room = dataStore.get(KeyFactory.createKey("Room", room_key));
			if(!debug.equals("full")) {
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
				room.setProperty("NoU", "0");
				RoomManagement.addUser(room, user);
				if(!debug.equals("loopback")) {
					initiator = 0;
				} else {
					RoomManagement.addUser(room, user);
					initiator = 0;
				}
					
			}
			
		}
		
		
		String room_link = "https://localhost:8888/?r=" + room_key;
		if(debug != null) {
			room_link += ("&debug=" + debug);
		}
		if(stunServer != null) {
			room_link += "&ss=" + stunServer;
		}
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		String token = channelService.createChannel(room_key + "/" + user);
		String pc_config = make_pc_config(stunServer);
		String lst_user = "";
		ArrayList<String> otherUser = RoomManagement.getOtherUser(room, user);
		for(int i = 0; i < otherUser.size(); i++){
			if(otherUser.get(i) != null){
				lst_user += otherUser.get(i);
				if( (i + 1) < otherUser.size())
					lst_user+= " ";
			}
		}
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
		index = index.replaceAll("\\{\\{ lst_user \\}\\}", lst_user);
		resp.setContentType("text/html");
		resp.getWriter().write(index);

	}
	
	
}
