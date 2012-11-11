package dcn.ssu.ac.kr;

import java.util.ArrayList;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Entity;


public class MessageManagement {
	public String token;
	public String msg;
	
	public static ArrayList<Entity> getSavedMessage(String _token) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Message").setFilter(new FilterPredicate("token", FilterOperator.EQUAL, _token));
		
		PreparedQuery pq = datastore.prepare(q);
		ArrayList<Entity> arrEntities = new ArrayList<Entity>();
		for(Entity e : pq.asIterable()) {
			arrEntities.add(e);
		}
		return arrEntities;
		
	}
	
	public static void sendSavedMessage(String _token) {
		ArrayList<Entity> arrEntities = MessageManagement.getSavedMessage(_token);
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		
		for(Entity e : arrEntities) {
			String msg = (String) e.getProperty("msg");
			
			channelService.sendMessage(new ChannelMessage(_token, msg));
			e.setProperty("msg", null);
		}
		
	}
	
	public static void deleteSavedMessage(String _token) {
		ArrayList<Entity> arrEntities = MessageManagement.getSavedMessage(_token);
		for(Entity e : arrEntities) {
			e.setProperty("msg", null);
		}
		
	}
	
	public static String makeToken(Entity room, String userName) {
		return room.getKey().getName() + "/" + userName;
	}
	
}
