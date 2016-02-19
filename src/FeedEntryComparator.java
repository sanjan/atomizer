
import java.util.Comparator;

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
public class FeedEntryComparator implements Comparator<FeedEntry> {

    @Override
    public int compare(FeedEntry o1, FeedEntry o2) {
        return o2.getDate().compareTo(o1.getDate());
    }

}
