package com.quickblox.sample.chat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

public class MultiUserChatTest {

	
	public void testMultiUserChat() throws Exception {
        ConnectionConfiguration config = new ConnectionConfiguration("localhost", 61222);
        //config.setDebuggerEnabled(true);
        //
        XMPPConnection consumerCon = new XMPPConnection(config);
        consumerCon.connect();
        consumerCon.login("consumer", "consumer");
        MultiUserChat consumerMuc = new MultiUserChat(consumerCon, "muc-test");
        consumerMuc.join("consumer");

        final ConsumerMUCMessageListener listener = new ConsumerMUCMessageListener();
        consumerMuc.addMessageListener(listener);

        XMPPConnection producerCon = new XMPPConnection(config);
        producerCon.connect();
        producerCon.login("producer", "producer");
        MultiUserChat producerMuc = new MultiUserChat(producerCon, "muc-test");
        producerMuc.join("producer");

        for (int i = 0; i < 10; i++) {
        	Log.i("Sending message: " , ""+ i);
            Message message = producerMuc.createMessage();
            message.setBody("Hello from producer, message # " + i);
            producerMuc.sendMessage(message);
        }
        Log.i("Sent all messages!","");

        

        Log.i("Consumer received - " , ""+ listener.getMessageCount());
    }
}

class ConsumerMUCMessageListener implements PacketListener {
    private int messageCount=0;

    public void processPacket(Packet packet) {
        if ( packet instanceof Message) {
            Log.i("Received message number : ", "" + messageCount++ );
        }
    }
    public int getMessageCount() {
        return messageCount;
    }
}
