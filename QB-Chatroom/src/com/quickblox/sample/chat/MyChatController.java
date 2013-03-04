package com.quickblox.sample.chat;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

import com.quickblox.module.chat.QBChat;

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * This class implements simple chat feature. Two users can send messages to each other.
 * First user (you) should be logged in mobile device, but other one can use any mobile
 * or desktop XMPP chat client, e.g. Adium, iChat, etc.
 * <p/>
 * All chat logic can be implemented by yourself using
 * ASMACK library (https://github.com/Flowdalic/asmack/downloads) that is
 * Android wrapper for Java XMPP library (http://www.igniterealtime.org/projects/smack/).
 * <p/>
 * All specific documentation about SMACK you can find in
 * http://www.igniterealtime.org/builds/smack/docs/latest/documentation/
 *
 * @author <a href="mailto:oleg@quickblox.com">Oleg Soroka</a>
 */
public class MyChatController {

    // ================= QuickBlox ===== Step 8 =================
    // Get QuickBlox chat server domain.
    // There will be created connection with chat server below.
    public static final String CHAT_SERVER = QBChat.getChatServerDomain();

    private XMPPConnection connection;

    private ConnectionConfiguration config;
    //private Chat chat;
    private MultiUserChat muc2;

    private String chatLogin;
    private String password;
    private String friendLogin;
    
	


    //private ChatManager chatManager;

    public MyChatController(String chatLogin, String password) {
        this.chatLogin = chatLogin;
        this.password = password;
		
    }
    
    
    public void startChat(String buddyLogin) {
        this.friendLogin = buddyLogin;
        new Thread(new Runnable() {
            @Override
            public void run() {
            	
            	// Chat action 1 -- create connection.
            	config = new ConnectionConfiguration(CHAT_SERVER);
                Connection connection = new XMPPConnection(config);
                Connection.DEBUG_ENABLED = true;
                try {
                	connection.connect();
                	 // Create a MultiUserChat using a Connection for a room
                	connection.login(chatLogin, password);
                	// Chat action 2 -- create chat manager.
                    //chatManager = connection.getChatManager();
                    muc2 = new MultiUserChat(connection, "vanatur-2013@muc.chat.quickblox.com");
                    // Discover whether user3@host.org supports MUC or not
                    
                    // The room service will decide the amount of history to send
                    muc2.join(chatLogin);
                    muc2.invite(friendLogin, "Welcome!");
                    Log.d("friendLogin ->",friendLogin);
                    // Set listener for outcoming messages.
                    //chatManager.addChatListener(chatManagerListener);
                    muc2.addMessageListener(packetListener);
                    addListenerToMuc(muc2);
                    
                    Message message = new Message(friendLogin + "@chat.quickblox.com");
                    message.setBody("Join me for a group chat!");
                    message.addExtension(new GroupChatInvitation("room@chat.example.com"));
                    connection.sendPacket(message);
                    
                    //muc2.addMessageListener(listener);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
                
                /*try {
                    connection.connect();
                    connection.login(chatLogin, password);

                    // Chat action 2 -- create chat manager.
                    chatManager = connection.getChatManager();

                    // Chat action 3 -- create chat.
                    //TODO my change chat = chatManager.createChat(friendLogin, messageListener);
                    Log.d("friendLogin ->",friendLogin);
                    chat = chatManager.getThreadChat("vanatur-2013@muc.chat.quickblox.com");

                    // Set listener for outcoming messages.
                    chatManager.addChatListener(chatManagerListener);

                } catch (XMPPException e) {
                    e.printStackTrace();
                }
                */
            }
        }).start();
    }
    
    
    	
    private void addListenerToMuc(MultiUserChat muc){
        if(null != muc){
            muc.addMessageListener(new PacketListener() {

                @Override
                public void processPacket(Packet packet) {
                    Log.i("processPacket", "receiving message");
                    }
            });
        }
    }    
    
    PacketListener packetListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
          Message message = (Message)packet;
          try {
			muc2.sendMessage(message);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          //System.out.println("got message " + message.toXML());
        }
	};  
    
    
    private PacketInterceptor packetInterceptor = new PacketInterceptor() {
		
		@Override
		public void interceptPacket(Packet packet) {
			 System.out.println("Sending message: " + packet.toString());
			 Message message = muc2.createMessage();
			 message.setBody("Hello from producer, message " +
			 		" ");
			 try {
				muc2.sendMessage(message);
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};

    private ChatManagerListener chatManagerListener = new ChatManagerListener() {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            // Set listener for incoming messages.
            chat.addMessageListener(messageListener);
            muc2.addMessageListener(packetListener);
        }
    };

    public void sendMessage(String message) {
        try {
            if (muc2 != null) {
            	muc2.sendMessage(message);
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private MessageListener messageListener = new MessageListener() {
        @Override
        public void processMessage(Chat chat, Message message) {
            // 'from' and 'to' fields contains senders ids, e.g.
            // 17792-1028@chat.quickblox.com/mac-167
            // 17744-1028@chat.quickblox.com/Smack
            String from = message.getFrom().split("@")[0];
            String to = message.getTo().split("@")[0];

            System.out.println(String.format(">>> Message received (from=%s, to=%s): %s",
                    from, to, message.getBody()));

            if (onMessageReceivedListener != null) {
                onMessageReceivedListener.onMessageReceived(message);
            }
            
            Log.d("chat.getThreadID() ->",chat.getThreadID());
        }
    };
    
    


    public static interface OnMessageReceivedListener {
        void onMessageReceived(Message message);
    }

    // Callback that performs when device retrieves incoming message.
    private OnMessageReceivedListener onMessageReceivedListener;

    public OnMessageReceivedListener getOnMessageReceivedListener() {
        return onMessageReceivedListener;
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener onMessageReceivedListener) {
        this.onMessageReceivedListener = onMessageReceivedListener;
    }
}

