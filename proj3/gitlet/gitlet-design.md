# Gitlet Project Design Document

**Name**: Ray Wan

## Classes and Data Structures

### Main.java
This class is the entry point of the program. It implements methods to set up persistance and support each command of the program.

### Fields
1. File CWD // maybe don't need this
2. File .gitlet
3. File blob
4. File commit
5. File stagingArea
6. File branches //don't know if I actually need this.

### Commit.java
This class sets up the commit function for Gitlet. It has functions like advance, getMessage, getTimestamp.

### Fields
1. String message: the commit message of the commit.
2. Date timeStamp: timestamp of the commit.
3. Commit parent: points to the parent of this commit.
4. List trackedFiles: list of trackedFiles in the working directory.

## Algorithms

###Main.java
1. main(String[] args): The entry point of the program. Determines which method to call according to user input. Exits the entire program if it senses an error in the arguments.
2. init(): Sets up the file directory of gitlet.
3. add(): Stage the files to commit. Create pointers to the files in the Staging Folder.
4. commit(): calls the commit class to commit the files.
5. merge(): merge two branches of commit together by deleting the head files and creating only one of them.
6. rm(): remove the file from the staging folder.
7. log(): create the log output by going back the commit folders and displaying each commit.
8. find():  Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the commit command above.
9. status(): Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal. An example of the exact format it should follow is as follows.
10. checkout(): reverts the files in the working directory to a previous state (don't really know how to do this atm).
11. generateSHA(): generate the SHA function for the files.
12. getWorkingDirectory(): get the current working directory of the user.
13. branch(): creates a new branch in the HEAD folder.


###Commit.java
1. advance(): advance the pointer of the HEAD to the new commit, and save the files into BLOB.
2. clearStaging(): clear files of the staging area.
3. getMessage(): get commit messages.
4. getTimeStamp(): get the timestamp of the commit
5. unTrack(): untracks a file in the staging area.


##Persistence

### Commits
1. We store the commit inside the .gitlet folder. This commits points to the blobs that are inside the blob folder.

### Blobs
1. We store the history of the tracked files inside the blob folder. Each commit points to a new blob folder (current implementation).

### HEAD
1. We store the current head of the branches inside the head folder. We are storing the SHA-1 hash. We are also storing all the heads of the branches(if there are more than 1).


## DESIGN DIAGRAM
![Design Diagram](gitlet-design.png)