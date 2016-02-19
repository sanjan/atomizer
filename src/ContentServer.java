
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
public class ContentServer {

    private static int lamportClock = 1;

    /*
     startup the contentserver, read 2 arguments from command line
     for file and the url.
     parse the file content and submit the atom feed to aggregation sever.
     detect read any modification to the news content file and republish the feed
     */
    public static void main(String[] args) {
        ContentServer cs = new ContentServer();
        try {

            if (args.length == 0) {
                System.out.println("Usage:java ContentServer <atom server url> <news content file> ");
                System.out.println("Example:java ContentServer http://www.atomserver.com/atom.xml news_content.txt ");
                System.exit(0);
            }
            String publishURL = args[0];
            File file = new File(args[1]);
            long lastModTime = file.lastModified();

            //publish the first time
            cs.publish(publishURL, cs.txt2XML(file));

            //wait and publish any changes to news content
            while (true) {

                if (lastModTime != file.lastModified()) {
                    System.out.println("News Content Modification Detected. Publishing to Aggregation Server\n"
                            + "========================================================");
                    cs.publish(publishURL, cs.txt2XML(file));
                    lastModTime = file.lastModified();
                }

                Thread.sleep(2000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     Generate a atom formatted xml string by reading a text file content
     Input: file
     Output: atom feed string
     */
    public String txt2XML(File file) throws IOException {
        BufferedReader b = null;
        b = new BufferedReader(new FileReader(file));
        String line = b.readLine();
        if (line == null) {
            throw new IOException("Empty file");
        }

        int entrycount = 0;
        String xmlContent = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                + "<feed xml:lang=\"en-US\" xmlns=\"http://www.w3.org/2005/Atom\">";

        while (line != null) {

            line = line.trim();

            if (line.length() == 0) {
                line = b.readLine();
                continue;
            }

            if (line.matches("^#.*$")) {
                line = b.readLine();
                continue;
            } else if (line.matches("^entry$")) {
                if (entrycount == 0) {
                    xmlContent += "\n<entry>";
                    entrycount++;
                } else {
                    xmlContent += "\n</entry>\n<entry>";
                }
                line = b.readLine();
                continue;
            }

            String[] result = line.split(":");

            if ((result[0].equals("title")) && (result.length == 2)) {

                xmlContent += "\n<title>" + result[1] + "</title>";

            } else if (result[0].equals("subtitle")) {
                xmlContent += "\n<subtitle>" + result[1] + "</subtitle>";
            } else if (result[0].equals("link")) {

                xmlContent += "\n<link href=\"" + result[1] + "\"></link>";

            } else if (result[0].equals("updated")) {
                xmlContent += "\n<updated>";
                for (int i = 1; i < result.length; i++) {
                    xmlContent += result[i] + ":";
                }
                xmlContent = xmlContent.substring(0, xmlContent.length() - 1);
                xmlContent += "</updated>";
            } else if (result[0].equals("author")) {
                xmlContent += "\n<author>\n<name>" + result[1] + "</name>\n</author>";
            } else if (result[0].equals("id")) {
                xmlContent += "\n<id>" + result[1] + ":" + result[2] + ":" + result[3] + "</id>";
            } else if (result[0].equals("summary")) {
                xmlContent += "\n<summary>" + result[1] + "</summary>";

            }
            line = b.readLine();
        }
        xmlContent += "\n</entry>\n</feed>";
        return xmlContent;
    }

    /*
     publish the atom xml format string to aggregation server
     Input: URL string and atom-xml string
     */
    public void publish(String url, String xmlContent) throws IOException {
        int responsecode = -1;
        int counter = 1;
        if (!url.matches("^http://.*$")) {
            url = "http://" + url;
        }
        url += "/atom.xml";
        while (responsecode != 200) {
            try {
                System.out.println("Connection attempt [" + counter + "] to: " + url);
                URL urlObj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                con.setRequestMethod("PUT");
                con.setRequestProperty("User-Agent", "ATOMClient/1/0");
                con.setRequestProperty("Content-Type", "application/atom+xml");
                con.setRequestProperty("Content-Length", xmlContent.length() + "");
                con.setRequestProperty("lamport-clock", lamportClock + "");
                con.setRequestProperty("Connection", "close");

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(xmlContent);
                wr.flush();
                wr.close();

                responsecode = con.getResponseCode();

                System.out.println("Response Code : " + responsecode + " " + con.getResponseMessage());

                //deal with the lamport clock
                if (responsecode == 200) {

                    int lamportServer = Integer.parseInt(con.getHeaderField("lamport-clock"));
                    //System.out.println("Lamport Clock at Server : " + lamportServer);

                    if (lamportServer > lamportClock) {
                        lamportClock = lamportServer++;
                    } else {
                        lamportClock++;
                    }
                }

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine + "\n");
                }

                //print result
                System.out.println("Received response: " + response.toString());
                in.close();
                con.disconnect();
                //replace responsecode to make life easy
                if (responsecode == 201) {
                    responsecode = 200;
                }

            } catch (IOException e) {

                if (counter == 5) {
                    System.out.println("WARN: Maximum retry attempts reached.\n");
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ee) {
                    System.err.println("Interrupted Exception occured while sleeping");
                }
                counter++;

                System.out.println(e.getMessage());
            }

        }
    }
}
