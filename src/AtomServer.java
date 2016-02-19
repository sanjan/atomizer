
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
public class AtomServer {

    private static final HashMap<String, Date> feedndate = new HashMap<String, Date>();
    private static final HashMap<String, List> feednentries = new HashMap<String, List>();

    public static void main(String[] args) throws IOException {

        int port = (args.length < 1) ? 4567 : Integer.parseInt(args[0]);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Handler(feedndate, feednentries));
        server.start();
        System.out.println("Atom Server ready. Listening on port: " + port);

//        Commented scheduled task because computationally expensive added removeOldEntries method instead
//        Timer time = new Timer(); // Instantiate Timer Object
//        FeedAvailabilityCheck fac = new FeedAvailabilityCheck(csCache, allentries, lamportClock); // Instantiate SheduledTask class
//        time.schedule(fac, 0, 1000); // Create Repetitively task for every 1 secs
    }
}

class Handler implements HttpHandler {

    private static HashMap<String, Date> feedndate;
    private static HashMap<String, List> feednentries;
    private static int lamportClock = 1;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
    private final TimeZone utcTime = TimeZone.getTimeZone("UTC");
    private Date lastUpdated = new Date();

    public Handler(HashMap<String, Date> fd, HashMap<String, List> fe) {
        feedndate = fd;
        feednentries = fe;
        sdf.setTimeZone(utcTime);
    }

    /*
     The handle method handles both PUT and GET requests to the server.
     and responds to the remote host with appropriate content.
     */
    @Override
    public void handle(HttpExchange xchg) throws IOException {

        String httpMethod = xchg.getRequestMethod();
        InetSocketAddress clientaddress = xchg.getRemoteAddress();

        System.out.println("Received " + httpMethod + " request from: "
                + clientaddress.getHostName() + ":" + clientaddress.getPort());

        Headers requestHeaders = xchg.getRequestHeaders();
        int remoteServerLamportClock = Integer.parseInt(requestHeaders.getFirst("lamport-clock"));

        //default responsecode is http 400
        int responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
        // response body
        StringBuilder responsebody = new StringBuilder();

        //Handle PUT (ContentServer)
        if (httpMethod.equals("PUT") && (xchg.getRequestURI().getPath().equals("/atom.xml"))) {

            //parse the http body cotent
            FeedProcessor feedproc = new FeedProcessor();
            responseCode = feedproc.parseFeedDom(xchg.getRequestBody());

            if (responseCode == HttpURLConnection.HTTP_OK) {
                lastUpdated = new Date();
                AtomFeed atomfeed = feedproc.getAtomFeed();
                String feedid = atomfeed.getFeedId();

                // response with 201 if feed is new
                if (!feedndate.containsKey(feedid)) {
                    responseCode = HttpURLConnection.HTTP_CREATED;
                }

                //add/update feed info
                feedndate.put(feedid, lastUpdated);
                feednentries.put(feedid, atomfeed.getEntries());

                responsebody.append("feed content received and processed successfully.");

                //update lamport clock
                if (remoteServerLamportClock > lamportClock) {
                    lamportClock = remoteServerLamportClock + 1;
                } else {
                    lamportClock++;
                }
                //System.out.println("Atom server clock now @ " + lamportClock);
            }

            // Handle GET (GETClient)
        } else if (httpMethod.equals("GET")) {

            //remove the feed entries submitted more than 15 seconds before
            removeOldEntries();

            //get the aggregated atom xml to be sent out to the client
            responsebody.append(generateXmlDom());
            responseCode = HttpURLConnection.HTTP_OK;

            if (remoteServerLamportClock > lamportClock) {
                lamportClock = remoteServerLamportClock + 1;
            } else {
                lamportClock++;
            }
            //System.out.println("Atom server clock now @ " + lamportClock);
        }

        // send out the response
        Headers responseHeaders = xchg.getResponseHeaders();
        responseHeaders.add("lamport-clock", lamportClock + "");
        xchg.sendResponseHeaders(responseCode, responsebody.length());
        OutputStream os = xchg.getResponseBody();
        os.write(responsebody.toString().getBytes());
        os.close();

        //write to file as a back up
        PrintWriter out = new PrintWriter("atom.xml");
        out.write(generateXmlDom());
        out.close();

    }

    /* This method will remove any feed that submitted data more than 15
     * seconds ago.
     */
    private void removeOldEntries() {

        for (Map.Entry<String, Date> feedMap : feedndate.entrySet()) {
            Date now = new Date();
            Date feedsubmitdate = feedMap.getValue();
            long timediff = (now.getTime() - feedsubmitdate.getTime());

            //delete feeds which are older than 15 seconds
            if (timediff > 15000) {
                System.out.println("Removing old feed: " + feedMap.getKey());
                feednentries.remove(feedMap.getKey());
                feedndate.remove(feedMap.getKey());
            }
        }
    }

    /*
     * This method will extract all entries from all the submitted feeds.
     * sort them by date. and returns a new atom formatted xml string with
     * latest 25 entries
     */
    private String generateXmlDom() {

        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
                + "\n<feed xml:lang=\"en-US\" xmlns=\"http://www.w3.org/2005/Atom\">"
                + "\n<id>http://www.abc.com/feed/atom</id>"
                + "\n<title>ABC News</title>"
                + "\n<subtitle>Latest news from ABC</subtitle>"
                + "\n<updated>" + sdf.format(lastUpdated).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2") + "</updated>"
                + "\n<link href=\"http://www.abc.com/feed/atom\" rel=\"self\">"
                + "\n</link>"
                + "\n<author>"
                + "\n<name>Sanjan Grero</name>"
                + "\n</author>");

        if (!feednentries.isEmpty()) {

            // combine all the entries into one list
            List<FeedEntry> allEntries = new ArrayList<FeedEntry>();

            for (Map.Entry<String, List> csMap : feednentries.entrySet()) {
                List csList = csMap.getValue();
                Iterator itCSList = csList.iterator();
                //add entry from each server to the combined list
                while (itCSList.hasNext()) {
                    FeedEntry singleentry = (FeedEntry) itCSList.next();
                    allEntries.add(singleentry);
                }
            }

            //sort the combined list by date (newest to oldest)
            Collections.sort(allEntries, new FeedEntryComparator());

            //append the 25 latest news entries to the atom xml
            Iterator itAllEntries = allEntries.iterator();
            int entrynum = 1;
            while (itAllEntries.hasNext()) {

                FeedEntry cEntry = (FeedEntry) itAllEntries.next();
                String currentEntry = "\n<entry>";

                currentEntry += "\n<title>" + cEntry.getTitle() + "</title>";
                //System.out.println("Entry [" + entrynum + "] Title: " + cEntry.getTitle());

                currentEntry += "\n<link href=\"" + cEntry.getLink() + "\"></link>";
                //System.out.println("Entry [" + entrynum + "] Link: " + cEntry.getLink());

                currentEntry += "\n<id>" + cEntry.getEntryId() + "</id>";
                //System.out.println("Entry [" + entrynum + "] Id: " + cEntry.getEntryId());

                String entrydate = sdf.format(cEntry.getDate()).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
                currentEntry += "\n<updated>" + entrydate + "</updated>";
                //System.out.println("Entry [" + entrynum + "] Updated: " + cEntry.getDate());

                currentEntry += "\n<summary>" + cEntry.getSummary() + "</summary>";
                // System.out.println("Entry [" + entrynum + "] Summary: " + cEntry.getSummary());
                //System.out.println();
                currentEntry += "\n</entry>";
                xml.append(currentEntry);

                //maximum entries are 25
                if (entrynum == 25) {
                    break;
                }

                entrynum++;

            }

        }
        xml.append("\n</feed>");
        //System.out.println(xml.toString());
        return xml.toString();
    }

}
