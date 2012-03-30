package dcn.ssu.ac.kr;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class DisconnectServlet  extends HttpServlet{
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		ChannelPresence presence = channelService.parsePresence(req);
		String key = presence.clientId();
		String str[] = key.split("/");
		String room_key = str[0];
		String user = str[1];

		try {
			Entity room = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("Room", room_key));
			String user1 = (String)room.getProperty("user1");
			String user2 = (String)room.getProperty("user2");
			if(user1.equals(user) || user2.equals(user)) {
				String otherUser = RoomManagement.getOtherUser(room, user);
				RoomManagement.removeUser(room, user);
				if(otherUser != null) {
					channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + otherUser, "BYE"));
				}
			}
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
