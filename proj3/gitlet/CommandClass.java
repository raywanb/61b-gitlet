package gitlet;

import java.io.File;
import java.io.Serializable;

import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.TreeSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;

/**
 * All commands are here.
 *
 * @author Ray Wan
 */
public class CommandClass {

    /**
     * file addresses.
     */
    private static String commit = ".gitlet/commit/";
    /**
     * file addresses.
     */
    private static String branches = ".gitlet/branches/";
    /**
     * file addresses.
     */
    private static String blob = ".gitlet/blob/";
    /**
     * file addresses.
     */
    private static String head = ".gitlet/head.txt";
    /**
     * file addresses.
     */
    private static String staging = ".gitlet/staging";

    /**
     * file addresses.
     */
    static final File GITLET = new File(".gitlet");

    /**
     * file addresses.
     */
    static final File COMMIT = new File(commit);

    /**
     * file addresses.
     */
    static final File BRANCHES = new File(branches);

    /**
     * file addresses.
     */
    static final File BLOB = new File(blob);

    /**
     * file addresses.
     */
    static final File HEAD = new File(head);

    /**
     * file addresses.
     */
    private static Staging stage;

    /**
     * initalize command class.
     */
    public CommandClass() {
        File s = new File(staging);
        if (s.exists()) {
            stage = Utils.readObject(s, Staging.class);
        }

    }

    /**
     * git init.
     */
    public static void init() {
        if (GITLET.exists()) {
            System.err.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            GITLET.mkdir();
            COMMIT.mkdir();
            BLOB.mkdir();
            BRANCHES.mkdir();

            Commit initial = new Commit("initial commit", null, new TreeMap());
            File commitLoc = new File(COMMIT,
                    Utils.sha1(Utils.serialize(initial)));
            Utils.writeObject(commitLoc, initial);


            File master = Utils.join(BRANCHES, "master");
            Utils.writeContents(HEAD, Utils.sha1(Utils.serialize(initial)));
            Utils.writeContents(master, Utils.sha1(Utils.serialize(initial)));


            stage = new Staging();
            Utils.writeObject(new File(staging), stage);
        }
    }

    /**
     * remove.
     * @param name
     */
    public static void remove(String name) {
        boolean isStaged = stage.getStagedFiles().containsKey(name);
        Commit curr = currentCommit();
        boolean isTracked = false;
        Set<String> c = curr.getSavedFiles().keySet();
        ArrayList<String> committedFiles = new ArrayList<>(c);
        for (String f : committedFiles) {
            if (f.equals(name)) {
                isTracked = true;
            }
        }
        if (isTracked) {
            Utils.restrictedDelete(name);
            stage.addtoRemoved(name);
            if (isStaged) {
                stage.getStagedFiles().remove(name);
            }
            Utils.writeObject(new File(staging), stage);
        } else if (isStaged) {
            stage.getStagedFiles().remove(name);
            Utils.writeObject(new File(staging), stage);
        } else {
            System.out.print("No reason to remove the file.");
        }
    }

    /**
     * checkout.
     *
     * @param c
     * @param filename
     */
    public static void checkout(Commit c, String filename) {
        String targetHash = c.getSavedFiles().get(filename);
        if (targetHash != null) {
            File src = new File(blob + targetHash);
            File target = new File(filename);
            target.delete();
            Utils.writeContents(target, Utils.readContents(src));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /**
     * checkout commit.
     *
     * @param commitid
     * @param filename
     */
    public static void checkout(String commitid, String filename) {
        int maxLength = 5 * 8;
        if (commitid.length() == maxLength) {
            File f = new File(commit + commitid);
            if (f.exists()) {
                Commit c = Utils.readObject(
                        new File(commit + commitid), Commit.class);
                checkout(c, filename);
            } else {
                System.out.println("No commit with that id exists.");
            }
        } else if (commitid.length() < maxLength) {
            int length = commitid.length();
            List<String> allCommits = Utils.plainFilenamesIn(commit);
            for (String s : allCommits) {
                if (s.substring(0, length).equals(commitid)) {
                    File f = new File(commit + s);
                    if (f.exists()) {
                        Commit c = Utils.readObject(
                                new File(commit + s), Commit.class);
                        checkout(c, filename);
                        return;
                    }
                }
            }
            System.out.println("No commit with that id exists.");
        }
    }

    /**
     * checkout.
     *
     * @param name
     */
    public static void checkoutbranch(String name) {
        File f = new File(branches + name);
        if (!f.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        if (name.equals(stage.getCurrentBranch())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String newid = Utils.readContentsAsString(f);
        Commit newcommit = Utils.readObject(
                new File(commit + newid), Commit.class);
        Commit cur = currentCommit();
        TreeMap<String, String> curBlob = cur.getSavedFiles();
        TreeMap<String, String> newBlob = newcommit.getSavedFiles();

        List<String> workingDirFiles = Utils.plainFilenamesIn(".");

        for (String s : workingDirFiles) {
            if (!curBlob.containsKey(s) && newBlob.containsKey(s)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }

        for (String s : workingDirFiles) {
            if (!newBlob.containsKey(s) && curBlob.containsKey(s)) {
                Utils.restrictedDelete(new File(s));
            }
        }
        for (String s : newBlob.keySet()) {
            String blobId = newBlob.get(s);
            File src = new File(blob + blobId);
            File tar = new File(s);
            Utils.writeContents(tar, Utils.readContents(src));
        }
        stage.clear();
        stage.setCurrentBranch(name);

        Utils.writeObject(new File(staging), stage);

        Utils.writeContents(HEAD, getHash(newcommit));
    }

    /**
     * gets current branch.
     *
     * @return string
     */
    public static String getCurrentBranch() {
        return Utils.readContentsAsString(HEAD);
    }

    /**
     * get add.
     *
     * @param args
     */
    public static void add(String[] args) {
        validateNumArgs("add", args, 2);
        File addFile = new File(args[1]);

        if (addFile.exists()) {
            byte[] content = Utils.readContents(addFile);
            String hash = Utils.sha1(content);
            if (currentCommit().getSavedFiles().get(args[1]) != null
                    && currentCommit()
                    .getSavedFiles()
                    .get(args[1])
                    .equals(hash)) {
                if (stage.getRemovedFiles().contains(args[1])) {
                    stage.getRemovedFiles().remove(args[1]);
                    Utils.writeObject(new File(staging), stage);
                }
                return;
            }
            if (stage.getRemovedFiles() != null) {
                stage.getRemovedFiles().remove(args[1]);
            }
            Utils.writeContents(Utils.join(BLOB, hash), content);
            stage.addtoStage(args[1], hash);
            Utils.writeObject(new File(staging), stage);
        } else {
            System.out.println("File does not exist.");
        }

    }

    /**
     * commit.
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void commit(String[] args) {
        validateNumArgs("commit", args, 2);
        if (stage.getStagedFiles().size() == 0
                && stage.getRemovedFiles().size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (args[1].length() == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }
        TreeMap<String, String> commitFiles = (TreeMap<String, String>)
                currentCommit().getSavedFiles().clone();
        Set<String> s = stage.getStagedFiles().keySet();
        ArrayList<String> stagedFiles = new ArrayList<String>(s);
        for (String files : stagedFiles) {
            commitFiles.put(files, stage
                    .getStagedFiles()
                    .get(files));
        }

        for (String remove : stage.getRemovedFiles()) {
            commitFiles.remove(remove);
        }
        Commit newcommit = new Commit(args[1],
                getHash(currentCommit()), commitFiles);
        File loc = new File(commit
                + getHash(newcommit));
        String originalHead = Utils.readContentsAsString(HEAD);
        Utils.writeObject(loc, newcommit);
        Utils.writeContents(HEAD, getHash(newcommit));
        Utils.writeContents(new File(BRANCHES,
                stage.getCurrentBranch()),
                getHash(newcommit));
        stage.clear();
        Utils.writeObject(new File(staging), stage);
    }

    /**
     * validates the arguments.
     *
     * @param cmd
     * @param args
     * @param n
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }

    /**
     * branch.
     *
     * @param name
     */
    public static void branch(String name) {
        File target = new File(branches + name);
        if (!target.exists()) {
            String content = Utils.readContentsAsString(HEAD);
            Utils.writeContents(target, content);
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    /**
     * gets the hash of an object.
     *
     * @param ob
     * @return String
     */
    public static String getHash(Object ob) {
        byte[] intermediate = Utils.serialize((Serializable) ob);
        return Utils.sha1(intermediate);
    }

    /**
     * gets the current Commit.
     *
     * @return Commit
     */
    public static Commit currentCommit() {
        return Utils.readObject(new File(commit
                + Utils.readContentsAsString(HEAD)), Commit.class);
    }

    /**
     * gets the log.
     */
    public static void log() {
        Commit c = currentCommit();
        while (c != null) {
            if (c.isMerge()) {
                System.out.println("===");
                System.out.println("commit " + getHash(c));
                System.out.println("Merge: " + c.getParent().substring(0, 7)
                        + " " + c.getParent2().substring(0, 7));
                System.out.println("Date: " + c.getDate());
                System.out.println(c.getMessage());
                System.out.println();
                if (c.getParent() != null) {
                    File f = new File(commit + c.getParent());
                    c = Utils.readObject(f, Commit.class);
                } else {
                    break;
                }
            } else {
                System.out.println("===");
                System.out.println("commit " + getHash(c));
                System.out.println("Date: " + c.getDate());
                System.out.println(c.getMessage());
                System.out.println();
                if (c.getParent() != null) {
                    File f = new File(commit + c.getParent());
                    c = Utils.readObject(f, Commit.class);
                } else {
                    break;
                }
            }
        }
    }


    /**
     * global log.
     */
    public static void globalLog() {
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT);
        for (String s : allCommits) {
            File f = new File(commit + s);
            Commit c = Utils.readObject(f, Commit.class);
            System.out.println("===");
            System.out.println("commit " + getHash(c));
            System.out.println("Date: " + c.getDate());
            System.out.println(c.getMessage());
            System.out.println();
        }
    }

    /**
     * finds the commit with the message.
     *
     * @param message
     */
    public static void find(String message) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT);
        boolean hasMessage = false;
        for (String s : allCommits) {
            File f = new File(commit + s);
            Commit c = Utils.readObject(f, Commit.class);
            if (c.getMessage().equals(message)) {
                hasMessage = true;
                System.out.println(getHash(c));
            }
        }
        if (!hasMessage) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * removes the branch with the name.
     *
     * @param name
     */
    public static void rmbranch(String name) {
        if (name.equals(stage.getCurrentBranch())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File f = new File(branches + name);
        if (!f.exists()) {
            System.out.println(" A branch with that name does not exist.");
        } else {
            f.delete();
        }
    }


    /**
     * helper function.
     * @param name
     */
    public static void merge1(String name) {
        File f = new File(branches + name);
        if (!f.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (!stage.getRemovedFiles().isEmpty()
                || !stage.getStagedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (name.equals(stage.getCurrentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /**
     * merge helper 2.
     * @param curBlob
     * @param otherBlob
     * @param splitBlob
     * @param allFiles
     * @param mergeBlob
     * @param workingDirFiles
     * @return
     */
    public static boolean merge2(TreeMap<String, String> curBlob,
                                 TreeMap<String, String> otherBlob,
                                 TreeMap<String, String> splitBlob,
                                 Set<String> allFiles,
                                 TreeMap<String, String> mergeBlob,
                                 List<String> workingDirFiles) {
        boolean conflict = false;
        for (String s : workingDirFiles) {
            if (!curBlob.containsKey(s) && otherBlob.containsKey(s)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String s : allFiles) {
            String headH = curBlob.getOrDefault(s, "");
            String givenH = otherBlob.getOrDefault(s, "");
            String splitH = splitBlob.getOrDefault(s, "");
            if (headH.equals(splitH) && !headH.equals(givenH)) {
                File src = new File(blob + givenH);
                File tar = new File(s);
                if (src.exists() && givenH.length() > 0) {
                    Utils.writeContents(tar, Utils.readContents(src));
                    stage.addtoStage(s, givenH);
                    mergeBlob.put(s, givenH);
                } else {
                    Utils.restrictedDelete(tar);
                    stage.addtoRemoved(s);
                }
            } else if (!headH.equals(splitH)
                    && !headH.equals(givenH)
                    && !givenH.equals(splitH)) {
                conflict = true;
                String result = "<<<<<<< HEAD" + "\n";
                File f1 = new File(blob + headH);
                File f2 = new File(blob + givenH);
                File tar = new File(s);
                if (!headH.equals("")) {
                    if (f1.exists()) {
                        result += Utils.readContentsAsString(f1);
                    }
                }
                result += "=======\n";
                if (!givenH.equals("")) {
                    if (f2.exists()) {
                        result += Utils.readContentsAsString(f2);
                    }
                }
                result += ">>>>>>>" + "\n";
                Utils.writeContents(tar, result);
                stage.addtoStage(s, getHash(tar));
            } else {
                mergeBlob.put(s, headH);
            }
        }
        return conflict;
    }

    /**
     * merges the commits.
     *
     * @param name
     */
    public static void merge(String name) {
        File f = new File(branches + name);
        merge1(name);
        String otherloc = Utils.readContentsAsString(f);
        List<String> workingDirFiles = Utils.plainFilenamesIn(".");
        Commit cur = currentCommit();
        Commit other = Utils.readObject(new File(commit
                + otherloc), Commit.class);
        Commit ancestor = getAncestor(cur, other);
        LinkedHashSet<String> curAncestors = getHashLevelOrder(cur);
        LinkedHashSet<String> otherAncestors = getHashLevelOrder(other);
        if (otherAncestors.contains(getHash(cur))) {
            checkoutbranch(name);
            System.out.println("Current branch fast-forwarded.");
            return;
        } else if (curAncestors.contains(getHash(other))) {
            System.out.println("Given branch is an ancestor "
                   + "of the current branch.");
            return;
        }
        TreeMap<String, String> curBlob = cur.getSavedFiles();
        TreeMap<String, String> otherBlob = other.getSavedFiles();
        TreeMap<String, String> splitBlob = ancestor.getSavedFiles();
        TreeMap<String, String> mergeBlob = new TreeMap<>();
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(curBlob.keySet());
        allFiles.addAll(otherBlob.keySet());
        allFiles.addAll(splitBlob.keySet());
        boolean conflict = merge2(curBlob, otherBlob, splitBlob,
                allFiles, mergeBlob, workingDirFiles);
        for (String files : stage.getStagedFiles().keySet()) {
            mergeBlob.put(files, stage.getStagedFiles().get(files));
        }
        for (String r : stage.getRemovedFiles()) {
            mergeBlob.remove(r);
        }
        stage.clear();
        String message = "Merged " + name + " into "
                + stage.getCurrentBranch() + ".";
        Commit newCommit = new Commit(message, getHash(cur),
                getHash(other), mergeBlob);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        File cwrite = new File(commit + getHash(newCommit));
        Utils.writeObject(cwrite, newCommit);
        Utils.writeContents(HEAD, getHash(newCommit));

        File bwrite = new File(branches
                + stage.getCurrentBranch());
        Utils.writeContents(bwrite, getHash(newCommit));
        Utils.writeObject(new File(staging), stage);
    }

    /**
     * get status.
     */
    @SuppressWarnings("unchecked")
    public static void status() {
        if (GITLET.exists()) {
            List<String> br = Utils.plainFilenamesIn(BRANCHES);
            TreeSet<String> stagedFiles = new TreeSet(
                    stage.getStagedFiles().keySet());
            List<String> removed = stage.getRemovedFiles();
            TreeMap<String, String> modified = getModified();
            List<String> untracked = untracked();
            Collections.sort(untracked);
            Collections.sort(br);
            Collections.sort(removed);
            System.out.println("=== Branches ===");
            for (String s : br) {
                if (s.equals(stage.getCurrentBranch())) {
                    System.out.println("*" + s);
                } else {
                    System.out.println(s);
                }
            }
            System.out.println();
            System.out.println("=== Staged Files ===");
            for (String s : stagedFiles) {
                System.out.println(s);
            }
            System.out.println();
            System.out.println("=== Removed Files ===");
            for (String s : removed) {
                System.out.println(s);
            }
            System.out.println();
            System.out.println("=== Modifications Not "
                   + "Staged For Commit ===");
            System.out.println();
            System.out.println("=== Untracked Files ===");
            System.out.println();
        } else {
            System.out.println("Not in an initialized "
                  + "Gitlet directory.");
            return;
        }
    }

    /**
     * gets Modified.
     *
     * @return TreeMap
     */
    public static TreeMap<String, String> getModified() {
        Commit c = currentCommit();
        TreeMap<String, String> modified = new TreeMap<>();
        List<String> directory = Utils.plainFilenamesIn(new File("."));

        for (String s : directory) {
            if (c.getSavedFiles().containsKey(s)
                    && !c.getSavedFiles().get(s)
                            .equals(getHash(Utils
                                    .readContents(new File(s))))) {
                modified.put(s, "modified");
            }
        }

        for (String s : c.getSavedFiles().keySet()) {
            if (!directory.contains(s) && !stage
                    .getRemovedFiles()
                    .contains(s)) {
                modified.put(s, "deleted");
            }
        }

        return modified;
    }

    /**
     * gets untracked.
     *
     * @return List
     */
    public static List<String> untracked() {
        File f = new File(".");
        List<String> allFiles = Utils.plainFilenamesIn(f);
        List<String> result = new ArrayList<>();
        for (String s : allFiles) {
            if (!currentCommit().getSavedFiles().containsKey(s)
                    && !stage.getRemovedFiles().contains(s)
                    && !stage.getStagedFiles().containsKey(s)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * reset 2.
     *
     * @param id
     * @param f
     */
    public static void reset2(String id, File f) {
        Commit newcommit = Utils.readObject(f, Commit.class);
        Commit cur = currentCommit();
        TreeMap<String, String> curBlob = cur.getSavedFiles();
        TreeMap<String, String> newBlob = newcommit.getSavedFiles();

        List<String> workingDirFiles = Utils.plainFilenamesIn(".");

        for (String s : workingDirFiles) {
            if (!curBlob.containsKey(s) && newBlob.containsKey(s)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String s : workingDirFiles) {
            if (!newBlob.containsKey(s) && curBlob.containsKey(s)) {
                Utils.restrictedDelete(new File(s));
            }
        }
        for (String s : newBlob.keySet()) {
            String blobId = newBlob.get(s);
            File src = new File(blob + blobId);
            File tar = new File(s);
            Utils.writeContents(tar, Utils.readContents(src));
        }
        stage.clear();
        Utils.writeContents(new File(branches + stage.getCurrentBranch()),
                getHash(newcommit));
        Utils.writeObject(new File(staging), stage);
        Utils.writeContents(HEAD, getHash(newcommit));
    }


    /**
     * resets.
     *
     * @param id
     */
    public static void reset(String id) {
        int maxLength = 5 * 8;
        if (id.length() < maxLength) {
            int length = id.length();
            List<String> allFiles = Utils.plainFilenamesIn(COMMIT);
            boolean found = false;
            String get = "";
            for (String s : allFiles) {
                if (s.substring(0, length).equals(id)) {
                    get = s;
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("No commit with that id exists.");
                return;
            }
            File f = new File(commit + get);
            reset2(id, f);

        } else {
            File f = new File(commit + id);
            if (!f.exists()) {
                System.out.println("No commit with that id exists.");
                return;
            }
            reset2(id, f);
        }
    }

    /**
     * gets ancestors.
     *
     * @param head2
     * @param other
     * @return
     */
    public static Commit getAncestor(Commit head2, Commit other) {
        LinkedHashSet<String> headHash = getHashLevelOrder(head2);
        LinkedHashSet<String> otherHash = getHashLevelOrder(other);
        ArrayList<String> hHash = new ArrayList<>();
        hHash.addAll(headHash);
        for (int i = 0; i < hHash.size(); i++) {
            String s = hHash.get(i);
            if (otherHash.contains(s)) {
                return Utils.readObject(new File(commit
                        + s), Commit.class);
            }
        }
        return null;
    }


    /**
     * gets all Commits to init.
     *
     * @param c
     * @return LinkedHashSet
     */
    public static LinkedHashSet<String> getHashLevelOrder(Commit c) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Queue<Commit> queue = new LinkedList<>();
        queue.add(c);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Commit current = queue.remove();
                result.add(getHash(current));
                if (current.isMerge()) {
                    if (current.getParent() != null) {
                        queue.add(Utils.readObject(new File(commit
                                + current.getParent()), Commit.class));
                    }
                    if (current.getParent2() != null) {
                        queue.add(Utils.readObject(new File(commit
                                + current.getParent2()), Commit.class));
                    }
                } else {
                    if (current.getParent() != null) {
                        queue.add(Utils.readObject(new File(commit
                                + current.getParent()), Commit.class));
                    }
                }
            }
        }
        return result;
    }

    /**
     * accessors.
     * @return String
     */
    public String getCommit() {
        return commit;
    }
    /**
     * accessors.
     * @return String
     */
    public String getBranches() {
        return branches;
    }
    /**
     * accessors.
     * @return String
     */
    public String getBlob() {
        return blob;
    }
    /**
     * accessors.
     * @return String
     */
    public String getHead() {
        return head;
    }
    /**
     * accessors.
     * @return String
     */
    public String getStaging() {
        return staging;
    }
    /**
     * accessors.
     * @return Staging
     */
    public Staging getStage() {
        return stage;
    }
}
