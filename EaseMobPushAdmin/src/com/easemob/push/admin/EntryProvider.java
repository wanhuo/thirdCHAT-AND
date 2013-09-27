package com.easemob.push.admin;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.xmlpull.v1.XmlPullParser;


//This is to handle <entry> element 
//packet example: <message from="pubsub.domain" to="guest@domain/EaseMob" id="appkey__guest@domain__D8T2c"><event xmlns="http://jabber.org/protocol/pubsub#event"><items node="appkey"><item id="adb62b37-6578-4bde-b74e-19c7e6678d1438"><entry xmlns="http://jabber.org/protocol/pubsub">Send from EaseMob. message 2. hello!Sent On:11:18:45:002</entry></item></items></event><delay xmlns="urn:xmpp:delay" stamp="2013-03-20T03:17:10.700Z"/></message>
public class EntryProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser)
            throws Exception {
        String id = parser.getAttributeValue(null, "id");
        String node = parser.getAttributeValue(null, "node");
        String elem = parser.getName();

        int tag = parser.next();

        if (tag == XmlPullParser.END_TAG) {
            return new Item(id, node);
        } else {
            String payloadElemName = parser.getName();
            String payloadNS = parser.getNamespace();

            if (ProviderManager.getInstance().getExtensionProvider(
                    payloadElemName, payloadNS) == null) {
                boolean done = false;
                StringBuilder payloadText = new StringBuilder();

                while (!done) {
                    if (tag == XmlPullParser.END_TAG
                            && parser.getName().equals(elem))
                        done = true;
                    else if (!((tag == XmlPullParser.START_TAG) && parser
                            .isEmptyElementTag()))
                        payloadText.append(parser.getText());

                    if (!done)
                        tag = parser.next();
                }
                // return new PayloadItem<SimplePayload>("eee", new SimplePayload("content", "easemob:push", payloadText.toString()));
                return new Entry(payloadText.toString());
            } else {
                return new PayloadItem<PacketExtension>(id, node,
                        PacketParserUtils.parsePacketExtension(payloadElemName,
                                payloadNS, parser));
            }
        }
    }

}
