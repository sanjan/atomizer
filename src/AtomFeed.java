
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
public class AtomFeed {

    private String title, subtitle, link, author, id;
    private Date updatedate;
    private final List<FeedEntry> entries;

    public AtomFeed() {
        entries = new ArrayList<FeedEntry>();
    }

    public AtomFeed(String tt, String sub, String lnk, String date, String auth, String uid) {

        title = tt;
        subtitle = sub;
        link = lnk;
        Calendar cl = javax.xml.bind.DatatypeConverter.parseDateTime(date);
        updatedate = cl.getTime();
        author = auth;
        id = uid;
        entries = new ArrayList<FeedEntry>();

    }

    public void setTitle(String t) {
        title = t;
    }

    public String getTitle() {
        return title;
    }

    public void setSubTitle(String t) {
        subtitle = t;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public void setFeedId(String t) {
        id = t;
    }

    public String getFeedId() {
        return id;
    }

    public void setLink(String t) {
        link = t;
    }

    public String getLink() {
        return link;
    }

    public void setDate(String t) {
        Calendar cl = javax.xml.bind.DatatypeConverter.parseDateTime(t);
        updatedate = cl.getTime();
    }

    public Date getDate() {
        return updatedate;
    }

    public void setAuthor(String t) {
        author = t;
    }

    public String getAuthor() {
        return author;
    }

    public void addEntry(FeedEntry e) {
        entries.add(e);
    }

    public List getEntries() {
        return entries;
    }

}
