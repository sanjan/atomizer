
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Kondagamage Sanjan Chamara Grero
 *
 * @studentid A1204014
 *
 * @assignment Assignment 2 - News Aggregation Server
 *
 * @subject Distributed Systems
 *
 * @class Singapore - Trimester 1, 2014
 *
 */
public class FeedProcessor {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss z");
    TimeZone utcTime = TimeZone.getTimeZone("UTC");

    AtomFeed atomfeed = null;

    public int parseFeedDom(InputStream is) {
        DocumentBuilderFactory builderFactory
                = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            if (is.available() < 1) {
                return HttpURLConnection.HTTP_NO_CONTENT;
            }

            builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();

            //System.out.println("Root element :" + rootElement.getNodeName());
            if (!rootElement.getAttribute("xmlns").matches(".*www.w3.org/2005/Atom$")) {
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }

            String feedTitle = rootElement.getElementsByTagName("title").item(0).getTextContent();
            String feedSubTitle = rootElement.getElementsByTagName("subtitle").item(0).getTextContent();
            String feedLink = getNodeAttr("href", rootElement.getElementsByTagName("link").item(0));
            String feedDate = rootElement.getElementsByTagName("updated").item(0).getTextContent();
            String feedAuthor = rootElement.getElementsByTagName("name").item(0).getTextContent();
            String feedId = rootElement.getElementsByTagName("id").item(0).getTextContent();

            atomfeed = new AtomFeed(feedTitle, feedSubTitle, feedLink, feedDate, feedAuthor, feedId);

            // process entries
            NodeList entryList = doc.getElementsByTagName("entry");
            if (entryList.getLength() > 0) {
                //System.out.println("----------------------------");
                for (int entryindex = 0; entryindex < entryList.getLength(); entryindex++) {

                    Node entryNode = entryList.item(entryindex);

                    //System.out.println("\nCurrent Element :" + entryNode.getNodeName());
                    if (entryNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element entryElement = (Element) entryNode;
                        String etitle = entryElement.getElementsByTagName("title").item(0).getTextContent();
                        //System.out.println("title : " + etitle);

                        String elink = getNodeAttr("href", entryElement.getElementsByTagName("link").item(0));
                        //System.out.println("link : " + elink);

                        String eid = entryElement.getElementsByTagName("id").item(0).getTextContent();
                        //System.out.println("id : " + eid);

                        String edate = entryElement.getElementsByTagName("updated").item(0).getTextContent();
                        //System.out.println("updated : " + edate);

                        String esum = entryElement.getElementsByTagName("summary").item(0).getTextContent();
                        //System.out.println("summary : " + esum);

                        FeedEntry ent = new FeedEntry(etitle, elink, eid, edate, esum);
                        atomfeed.addEntry(ent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpURLConnection.HTTP_OK;
    }

    private String getNodeAttr(String attrName, Node node) {
        NamedNodeMap attrs = node.getAttributes();
        for (int y = 0; y < attrs.getLength(); y++) {
            Node attr = attrs.item(y);
            if (attr.getNodeName().equalsIgnoreCase(attrName)) {
                return attr.getNodeValue();
            }
        }
        return "";
    }

    public AtomFeed getAtomFeed() {
        return atomfeed;
    }

    // convert InputStream to String
    private String inputStream2String(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
}
