package dcn.ssu.ac.kr;

import java.util.ArrayList;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

public class RoomManagement {
	public static ArrayList<String> getOtherUser(Entity room, String user) {
		String user1 = (String)room.getProperty("user1");
		String user2 = (String)room.getProperty("user2");
		String user3 = (String)room.getProperty("user3");
		ArrayList<String> arr =new ArrayList<String>();
		if(user1 != null)
			arr.add(user1);
		if(user2 != null)
			arr.add(user2);
		if(user3 != null)
			arr.add(user3);
		if(user.equals(user1)){
			arr.remove(user1);
		} else if(user.equals(user2))
			arr.remove(user2);
		else if(user.equals(user3)){
			arr.remove(user3);
		}
		return arr;
	}
	
	public static void removeUser(Entity room, String user) {
		String user1 = (String)room.getProperty("user1");
		String user2 = (String)room.getProperty("user2");
		if(user.equals(user2)) {
			user2 = null;
		}
		
		if(user.equals(user1)) {
			if(user2 != null) {
				user1 = user2;
				user2 = null;
			} else {
				user1 = null;
			}
		}
		room.setProperty("user1", user1);
		room.setProperty("user2", user2);
	}
	
	public static void addUser(Entity entity, String user) {
		if(entity.getProperty("user1") == null)
			entity.setProperty("user1", user);
		else if(entity.getProperty("user2") == null)
			entity.setProperty("user2", user);
		else if(entity.getProperty("user3") == null)
			entity.setProperty("user3", user);
		DatastoreServiceFactory.getDatastoreService().put(entity);
	}
	
	public static int getOccupancy(Entity entity) {
		int occupancy = 0;
		if(entity.getProperty("user1") != null)
			occupancy += 1;
		
		if(entity.getProperty("user2") != null)
			occupancy += 1;
		if(entity.getProperty("user3") != null)
			occupancy += 1;
		return occupancy;
	}
}
