package gitlet;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Ray Wan
 */
public class Main {
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        CommandClass c = new CommandClass();
        switch (args[0]) {
        case "init":
            c.init();
            break;
        case "add":
            c.add(args);
            break;
        case "commit":
            c.commit(args);
            break;
        case "rm":
            c.remove(args[1]);
            break;
        case "log":
            c.log();
            break;
        case "global-log":
            c.globalLog();
            break;
        case "find":
            c.find(args[1]);
            break;
        case "status":
            c.status();
            break;
        case "checkout":
            checkout(args);
            break;
        case "branch":
            c.branch(args[1]);
            break;
        case "rm-branch":
            c.rmbranch(args[1]);
            break;
        case "reset":
            c.reset(args[1]);
            break;
        case "merge":
            c.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    /**
     * checkout.
     *
     * @param args
     */
    public static void checkout(String[] args) {
        CommandClass c = new CommandClass();
        if (args.length != 2 && args.length != 3
                && args.length != 4) {
            System.out.println("Incorrect Operands");
        } else if ((args.length == 4
                && !args[2].equals("--")) || (args.length == 3
                && !args[1].equals("--"))) {
            System.out.println("Incorrect Operands");
        } else {
            if (args.length == 3) {
                c.checkout(c.currentCommit(), args[2]);
            } else if (args.length == 4) {
                c.checkout(args[1], args[3]);
            } else if (args.length == 2) {
                c.checkoutbranch(args[1]);
            }
        }
    }
}

