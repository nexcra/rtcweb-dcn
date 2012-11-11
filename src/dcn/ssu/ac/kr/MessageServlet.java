package dcn.ssu.ac.kr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class MessageServlet extends HttpServlet{

	private void onMessage(Entity room, String userName, String message) {
		String token = MessageManagement.makeToken(room, userName);
		if(RoomManagement.isConnected(room, userName)) {
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			channelService.sendMessage(new ChannelMessage(token, message));
			System.out.println("Delivered message to " + token);
		} else {
			DatastoreServiceFactory.getDatastoreService().put( new Entity(token, message));
		}
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
		  InputStream inputStream = req.getInputStream();
		  if (inputStream != null) {
		   bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		   char[] charBuffer = new char[128];
		   int bytesRead = -1;
		   while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
		    stringBuilder.append(charBuffer, 0, bytesRead);
		   }
		  } else {
		   stringBuilder.append("");
		  }
		} catch (IOException ex) {
		  throw ex;
		} finally {
		  if (bufferedReader != null) {
		   try {
		    bufferedReader.close();
		   } catch (IOException ex) {
		    throw ex;
		   }
		  }
		}
		String message = stringBuilder.toString();
		String room_key = req.getParameter("r");
		
		
		try {
			JSONObject jsonMessage = new JSONObject(message);
			
			Entity room = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("Room", room_key));
			String user = req.getParameter("u");
			String otherUser = (String)RoomManagement.getOtherUser(room, user).getProperty("name");
			System.out.println("User: " + user + " Other User: " + otherUser);
			if(jsonMessage.get("type").equals("bye")) {
				RoomManagement.removeUser(room, user);
				System.out.println("User " + user + " quit from room " + room_key);
			}
			
			if(otherUser != null) {
				if(otherUser.equals(user)) {
					message = message.replace("\"offer\"", "\"answer\"");
					message = message.replace("a=ice-options:google-ice\\r\\n", "");
				}
				onMessage(room, otherUser, message);
			}
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
