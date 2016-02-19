
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
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
public class GETClient {

    private static int lamportClock = 1;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss z");
    private static TimeZone utcTime = TimeZone.getTimeZone("UTC");

    /*
     * Startup the get client. read the aggregation server and port number from
     * command line argument or user prompt.
     * then connect to aggregation server and get the aggregated feed file and
     * display it in a user friendly format
     */
    public static void main(String[] args) {

        GETClient gc = new GETClient();

        //read the command line to find the server name and port number (in URL format)
        try {
            if (args.length < 1) {
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter Aggregation Server URL: ");
                gc.request(bufferRead.readLine().trim());
            } else {
                gc.request(args[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Send a GET request for the ATOM feed
     *  Input: servername and port with implicit or explicit protocol information
     */
    public void request(String url) throws IOException {

        if (!url.matches("^http://.*$")) {
            url = "http://" + url;
        }
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Connection", "close");
        con.setRequestProperty("lamport-clock", lamportClock + "");

        con.setDoOutput(true);

        con.connect();

        //deal with the lamport clock
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

            int lamportServer = Integer.parseInt(con.getHeaderField("lamport-clock"));
            //System.out.println("Lamport Clock at Server : " + lamportServer);

            if (lamportServer > lamportClock) {
                lamportClock = lamportServer + 1;
            } else {
                lamportClock++;
            }
        }

        FeedProcessor fp = new FeedProcessor();
        int parseresult = fp.parseFeedDom(con.getInputStream());
        con.disconnect();

        //print result
        if (parseresult == HttpURLConnection.HTTP_OK) {
            printFeed(fp.getAtomFeed());
        } else {
            System.err.println("Feed parsing error!");
        }

    }

    /*
     * Print the Feed Content in Human Readable format
     * Input: AtomFeed
     */
    private void printFeed(AtomFeed af) {
        sdf.setTimeZone(utcTime);
        System.out.println("\nFeed: " + af.getTitle() + " - " + af.getSubTitle());
        System.out.println("Updated: " + sdf.format(af.getDate()));
        System.out.println("Link: " + af.getLink());
        System.out.println("Author: " + af.getAuthor());
        System.out.println("");

        if (!af.getEntries().isEmpty()) {
            Iterator entriesList = af.getEntries().iterator();

            //add entry from each server to the combined list
            int entrycount = 1;
            while (entriesList.hasNext()) {
                FeedEntry entry = (FeedEntry) entriesList.next();
                System.out.println("Title: " + entry.getTitle());
                System.out.println("Source: " + entry.getLink());
                System.out.println("Last Updated: " + sdf.format(entry.getDate()));
                System.out.println("-------------------------");
                System.out.println(entry.getSummary());
                System.out.println("");
            }
        } else {
            System.out.println("This feed do not contain any news items");
        }

    }
}
