package com.easemob.push.admin;

import java.io.File;
import java.lang.reflect.Constructor;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.NodeType;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.provider.EventProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemsProvider;
import org.jivesoftware.smackx.pubsub.provider.PubSubProvider;
import org.jivesoftware.smackx.pubsub.provider.SubscriptionProvider;
import org.jivesoftware.smackx.pubsub.provider.SubscriptionsProvider;
import org.jivesoftware.smackx.search.UserSearch;


import android.os.Build;
import android.util.Log;

public class PubSubPubClient {

    private static final String userName = "admin";
    private static final String password = "thepushbox";
    
    
    private ConnectionConfiguration connectionConfig = null;
    	
	private void initFeatures(XMPPConnection connection) {
	        ServiceDiscoveryManager.setIdentityName("easemob");
	        ServiceDiscoveryManager.setIdentityType("phone");
	        ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
	        if (sdm == null) {
	            sdm = new ServiceDiscoveryManager(connection);
	        }
	        
	        sdm.addFeature("http://jabber.org/protocol/disco#info");
	        // sdm.addFeature("jabber:iq:privacy");
	        sdm.addFeature("http://jabber.org/protocol/caps");
	        sdm.addFeature("urn:xmpp:avatar:metadata");
	        sdm.addFeature("urn:xmpp:avatar:metadata+notify");
	        sdm.addFeature("urn:xmpp:avatar:data");
	        sdm.addFeature("http://jabber.org/protocol/nick");
	        sdm.addFeature("http://jabber.org/protocol/nick+notify");
	        sdm.addFeature("http://jabber.org/protocol/muc");
	        sdm.addFeature("http://jabber.org/protocol/muc#rooms");

	}
	
	private void configure(ProviderManager pm) {
	    Constructor<?>[] c = ReconnectionManager.class.getConstructors();
	       
	    // Service Discovery # Items
	    pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
	    // Service Discovery # Info
	    pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

	    // Privacy
	    //pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
	    // Delayed Delivery only the new version
	    pm.addExtensionProvider("delay", "urn:xmpp:delay", new DelayInfoProvider());

	    // Service Discovery # Items
	    pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
	    // Service Discovery # Info
	    pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

	    // Chat State
	    ChatStateExtension.Provider chatState = new ChatStateExtension.Provider();
	    pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", chatState);
	    pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates",
	        chatState);
	    pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", chatState);
	    pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", chatState);
	    pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", chatState);
	    // capabilities
	    //pm.addExtensionProvider(CapsExtension.NODE_NAME, CapsExtension.XMLNS, new CapsExtensionProvider());

	    //Pubsub
	    pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub", new PubSubProvider());
	    pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub", new ItemsProvider());
	    pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub", new ItemsProvider());
	    pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub", new ItemProvider());

	    pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub#event", new ItemsProvider());
	    pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub#event", new ItemProvider());
	    pm.addExtensionProvider("event", "http://jabber.org/protocol/pubsub#event", new EventProvider());
	    
	    //Make sure NS (easemob:pubsub) is consistent with the NS on the sender.ie.
	    //Make sure NS (easemob:pubsub) is consistent with the NS on the sender.ie.
	    //SimplePayload payload = new SimplePayload("content", "easemob:push", "<entry xmlns='easemob:pubsub'>" + message +"</entry>");
	    pm.addExtensionProvider("entry", "easemob:pubsub", new EntryProvider());
	    
	    pm.addExtensionProvider("subscriptions", "http://jabber.org/protocol/pubsub", new SubscriptionsProvider());
	    pm.addExtensionProvider("subscription", "http://jabber.org/protocol/pubsub", new SubscriptionProvider());
	   
	    //vCard
	    ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new org.jivesoftware.smackx.provider.VCardProvider());
	 
	    // MUC User
	    pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
	    // MUC Admin
	    pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
	    // MUC Owner
	    pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
	    
	    // Group Chat Invitations
	    pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());
	    // Data Forms
	    pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
	    
	    
	    // Offline Message Requests
	    pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
	    // Offline Message Indicator
	    pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
	    
	    // FileTransfer
	    pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
	    pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());

	    pm.addIQProvider("open","http://jabber.org/protocol/ibb", new OpenIQProvider());
	    pm.addIQProvider("data","http://jabber.org/protocol/ibb", new DataPacketProvider());
	    pm.addIQProvider("close","http://jabber.org/protocol/ibb", new CloseIQProvider());
	    
	 // User Search
	    pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
	    
	    }

	
	private void initConnectionConfig() {
	    configure(ProviderManager.getInstance());
	    if (connectionConfig == null) {
            //http://stackoverflow.com/questions/1334291/how-to-handle-add-request-in-smack-api
            Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);
            SASLAuthentication.supportSASLMechanism("PLAIN");
            XMPPConnection.DEBUG_ENABLED = true;
            
            //otherwise we may get 'Timeout getting VCard information: request-timeout(408) Timeout getting VCard information'
            SmackConfiguration.setPacketReplyTimeout(30000);
            
            //set the heatbeat interval to 5 minutes
            //SmackConfiguration.setKeepAliveInterval(180000);
            
            connectionConfig = new ConnectionConfiguration("push.easemob.com", 5222, "ac2");
            connectionConfig.setRosterLoadedAtLogin(false);
            //NOTE: Setting to true or false has no effect on whether or not we can receive presence events from roster friends.
            connectionConfig.setSendPresence(false);
            connectionConfig.setReconnectionAllowed(false);
            connectionConfig.setCompressionEnabled(true);

            /*
             * connConfig.setSecurityMode(SecurityMode.required);
             * connConfig.setSASLAuthenticationEnabled(false);
             */
            
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                connectionConfig.setTruststoreType("AndroidCAStore");
                connectionConfig.setTruststorePassword(null);
                connectionConfig.setTruststorePath(null);
            } else {
                connectionConfig.setTruststoreType("BKS");
                String path = System.getProperty("javax.net.ssl.trustStore");
                if (path == null)
                    path = System.getProperty("java.home") + File.separator + "etc"
                        + File.separator + "security" + File.separator
                        + "cacerts.bks";
                connectionConfig.setTruststorePath(path);
            }
        }

	    
	}
	
	
	private XMPPConnection createXmppConnection() {
	    try {
	        initConnectionConfig();
	        XMPPConnection conn = new XMPPConnection(connectionConfig);
	        // For debugging
            XMPPConnection.DEBUG_ENABLED = true;
            conn.connect();
            initFeatures(conn);
            conn.login("admin", "thepushbox");
            return conn;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public boolean createPubSubNode(String node) {
	    XMPPConnection conn = createXmppConnection();
	    if (conn == null) {
	        return false;
	    }
        try {
            // Create the topic
            ConfigureForm f = new ConfigureForm(FormType.submit);
            // Set some params for the topic node according to your requirement
            f.setPersistentItems(true);
            f.setPresenceBasedDelivery(false);
            f.setDeliverPayloads(true);
            f.setAccessModel(AccessModel.open);
            f.setPublishModel(PublishModel.open);
            f.setMaxItems(-1);
            f.setSubscribe(true);

            PubSubManager mgr = new PubSubManager(conn, "pubsub.ac2");
            Node n = mgr.createNode(node, f);
            
            Log.d("pubsub", "pubsub node created:" + node);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
	}
	
	public boolean deletePubSubNode(String node) {
        XMPPConnection conn = createXmppConnection();
        if (conn == null) {
            return false;
        }
        try {
            
            PubSubManager mgr = new PubSubManager(conn, "pubsub.ac2");
            mgr.deleteNode(node);
            
            Log.d("pubsub", "pubsub node delete:" + node);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
	
	public boolean listPubSubNode(String node) {
        XMPPConnection conn = createXmppConnection();
        if (conn == null) {
            return false;
        }
        try {
            PubSubManager mgr = new PubSubManager(conn, "pubsub.ac2");
            mgr.deleteNode(node);            
            Log.d("pubsub", "pubsub node deleted:" + node);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
	
	public boolean publishNotification(String node, String message) {
        XMPPConnection conn = createXmppConnection();
        if (conn == null) {
            return false;
        }
        try {
            PubSubManager mgr = new PubSubManager(conn, "pubsub.ac2");
            Node n = mgr.getNode(node);
                        
            SimplePayload payload = new SimplePayload("content", "easemob:push",
                    "<entry xmlns='easemob:pubsub'>" + message +"</entry>");


            PayloadItem payloadItem = new PayloadItem(null, payload);
            ((LeafNode) n).publish(payloadItem);
            Log.d("pubsub", "publish notification on node:" + node + " msg:" + message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
	
	public boolean publishMsg(String node, String message) {
        XMPPConnection conn = createXmppConnection();
        if (conn == null) {
            return false;
        }
        message = "[sub:msg:"  + message + "]";
        
        try {
            PubSubManager mgr = new PubSubManager(conn, "pubsub.ac2");
            Node n = mgr.getNode(node);
            
            // Now publish something            
            SimplePayload payload = new SimplePayload("content", "easemob:push",
                    "<entry xmlns='easemob:pubsub'>" + message +"</entry>");
            
            PayloadItem payloadItem = new PayloadItem(null, payload);
            ((LeafNode) n).publish(payloadItem);
            Log.d("pubsub", "publish message on node:" + node + " msg:" + message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
	}

}