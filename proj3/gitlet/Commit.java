package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.util.Date;


public class Commit implements Serializable {
    /**
     * string of date.
     */
    private final String dateFormat = "EEE MMM d HH:mm:ss yyyy Z";
    /**
     * string of time.
     */
    private final String timeStamp;
    /**
     * string of message.
     */
    private final String logMessage;
    /**
     * string of parent.
     */
    private final String parent;
    /**
     * string of parent2.
     */
    private String parent2;
    /**
     * boolean of if is merge.
     */
    private boolean isMerge = false;
    /**
     * Treemap of all files.
     */
    private final TreeMap<String, String> allFiles;

    /**
     * initialize commit.
     * @param s
     * @param tparent
     * @param tallFiles
     */
    @SuppressWarnings("unchecked")
    public Commit(String s, String tparent, TreeMap tallFiles) {
        logMessage = s;
        this.allFiles = tallFiles;
        if (tparent == null) {
            String dateString = new SimpleDateFormat(dateFormat)
                    .format(new Date(0));
            timeStamp = dateString;
        } else {
            String dateString = new SimpleDateFormat(dateFormat)
                    .format(new Date(System.currentTimeMillis()));
            timeStamp = dateString;
        }
        this.parent = tparent;
    }

    /**
     * initialize commit merge.
     * @param s
     * @param tparent
     * @param tparent2
     * @param tallFiles
     */
    @SuppressWarnings("unchecked")
    public Commit(String s, String tparent,
                  String tparent2,
                  TreeMap tallFiles) {
        logMessage = s;
        this.allFiles = tallFiles;
        this.parent = tparent;
        this.parent2 = tparent2;
        this.timeStamp = new SimpleDateFormat(dateFormat)
                .format(new Date(System.currentTimeMillis()));
        isMerge = true;
    }

    /**
     * get Date.
     * @return time
     */
    public String getDate() {
        return timeStamp;
    }

    /**
     * get Message.
     * @return message
     */

    public String getMessage() {
        return logMessage;
    }

    /**
     * get Parent.
     * @return parent
     */
    public String getParent() {
        return parent;
    }

    /**
     * get TreeMap.
     * @return files
     */
    public TreeMap<String, String> getSavedFiles() {
        return allFiles;
    }

    /**
     * get Parent 2.
     * @return parent2
     */
    public String getParent2() {
        return parent2;
    }

    /**
     * isMerge.
     * @return isMerge
     */
    public boolean isMerge() {
        return isMerge;
    }
}
