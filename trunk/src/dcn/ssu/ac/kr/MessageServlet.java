package dcn.ssu.ac.kr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import dcn.ssu.ac.kr.message.Message;


@SuppressWarnings("serial")
public class MessageServlet extends HttpServlet{

	Message mROAPMessage;

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
			Entity room = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("Room", room_key));
			
			String sender = req.getParameter("sender");
			String receiver = req.getParameter("receiver");
			
			mROAPMessage = new Message(message, sender, receiver);
			ArrayList<String> otherUser = RoomManagement.getOtherUser(room, sender);
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			
			if(otherUser.size() > 0) {
				//how to determine this is the first time to return a list of ongoing participants???
				if(receiver == null) { 		//the first offer from newcomer
					if(mROAPMessage.isOffer()) {	//new client join and send offer by creating peerconnection
						//return list of current participants
						String listOfParticipants = "PARTICIPANTS ";
						for(String u : otherUser) {
							listOfParticipants += u + " ";
						}
						listOfParticipants = listOfParticipants.trim();
						//response the newcomer with a list of participants
						channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + sender, listOfParticipants));		
						//forward the offer to the first initiator
						channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + otherUser.get(0), mROAPMessage.getFullMessage()));		

					}

				} else {
					channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + receiver, mROAPMessage.getFullMessage()));
				}
			}

		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}

	}
}
