package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class Staging implements Serializable {

    /**
     * stagedFiles.
     */
    private TreeMap<String, String> stagedFiles;
    /**
     * removedFiles.
     */
    private ArrayList<String> removedFiles;
    /**
     * String of current branch.
     */
    private String currentBranch;

    /**
     * staging init.
     */
    public Staging() {
        stagedFiles = new TreeMap<>();
        removedFiles = new ArrayList<>();
        currentBranch = "master";
    }

    /**
     * gets current Branch.
     * @param name
     */
    public void setCurrentBranch(String name) {
        currentBranch = name;
    }

    /**
     * sets current branch.
     * @return
     */
    public String getCurrentBranch() {
        return currentBranch;
    }

    /**
     * clears.
     */
    public void clear() {
        stagedFiles = new TreeMap<>();
        removedFiles = new ArrayList<>();
    }

    /**
     * adds to stage.
     * @param name
     * @param hash
     */
    public void addtoStage(String name, String hash) {
        stagedFiles.put(name, hash);
    }

    /**
     * adds to removed.
     * @param name
     */
    public void addtoRemoved(String name) {
        removedFiles.add(name);
    }

    /**
     * gets staged Files.
     * @return TreeMap
     */
    public TreeMap<String, String> getStagedFiles() {
        return stagedFiles;
    }

    /**
     * gets removed files.
     * @return ArrayList
     */
    public ArrayList<String> getRemovedFiles() {
        return removedFiles;
    }
}
