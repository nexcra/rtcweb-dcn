package dcn.ssu.ac.kr;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class ConnectServlet  extends HttpServlet{
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		ChannelPresence presence = channelService.parsePresence(req);
		String key = presence.clientId();
		String str[] = key.split("/");
		System.out.println("User " + str[1] + " connected to room " + str[0]);
		try {
			Entity room = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("Room", str[0]));
			if(RoomManagement.hasUser(room, str[1])) {
				RoomManagement.setConnected(room, str[1]);
				MessageManagement.sendSavedMessage(MessageManagement.makeToken(room, str[1]));
			}
			
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("ConnectServlet - EntityNotFound:" + e.getMessage());
		}
		
	}
}
