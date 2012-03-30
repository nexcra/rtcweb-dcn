package dcn.ssu.ac.kr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;


@SuppressWarnings("serial")
public class MessageServlet extends HttpServlet{


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
		
//		
//		ChannelPresence presence = channelService.parsePresence(req);
//		String key = presence.clientId();
//		String str[] = key.split("/");
//		String user = str[1];
		try {
			Entity room = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("Room", room_key));
			String user = req.getParameter("u");
			String otherUser = RoomManagement.getOtherUser(room, user);
			if(otherUser != null) {
				if(otherUser.equals(user)) {
					message = message.replace("\"OFFER\"", "\"ANSWER\",\n   \"answererSessionId\" : \"1\"");
					message = message.replace("a=crypto:0 AES_CM_128_HMAC_SHA1_32", "a=xrypto:0 AES_CM_128_HMAC_SHA1_32");
				}
				ChannelService channelService = ChannelServiceFactory.getChannelService();
				channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + otherUser, message));
			}
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
