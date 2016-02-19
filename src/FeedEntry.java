
import java.util.Calendar;
import java.util.Date;

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
public class FeedEntry {

    private String title, link, id, summary;
    private Date updatedate;
    //private DateFormat dateFormatterAtomPubDate;

    public FeedEntry(String tt, String lnk, String uid, String date, String sum) {

        title = tt;
        link = lnk;
        id = uid;

        //  dateFormatterAtomPubDate= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        Calendar cl = javax.xml.bind.DatatypeConverter.parseDateTime(date);
        updatedate = cl.getTime();
        summary = sum;
    }

    public void setTitle(String t) {
        title = t;
    }

    public String getTitle() {
        return title;
    }

    public void setEntryId(String t) {
        id = t;
    }

    public String getEntryId() {
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

    public void setSummary(String t) {
        summary = t;
    }

    public String getSummary() {
        return summary;
    }

}
