
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

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
public class FeedAvailabilityCheck extends TimerTask {

    private final HashMap<String, Date> feedndate;
    private final HashMap<String, List> feednentries;
    private int lamportClock;
    // Add your task here

    public FeedAvailabilityCheck(HashMap<String, Date> c, HashMap<String, List> all, int lc) {
        feedndate = c;
        feednentries = all;
        lamportClock = lc;
    }

    public void run() {

        for (Map.Entry<String, Date> feedMap : feedndate.entrySet()) {
            Date now = new Date(); // initialize date
            Date feedsubmitdate = feedMap.getValue();
            long timediff = (now.getTime() - feedsubmitdate.getTime()) / 1000;

            //delete feeds which are older than 15 seconds
            if (timediff > 15) {
                System.out.println("Removing old feed :" + feedMap.getKey()); // Display current time
                //System.out.println("Before:map 1 size: " + feedndate.size() + ", map2 size:" + feednentries.size());
                feednentries.remove(feedMap.getKey());
                feedndate.remove(feedMap.getKey());
                lamportClock++;
                System.out.println("clock now @ " + lamportClock);
                //System.out.println("After: map 1 size: " + feedndate.size() + ", map2 size:" + feednentries.size());
            }
        }

    }
}
