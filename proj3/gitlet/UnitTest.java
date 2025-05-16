package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Ray Wan
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
        Commit c = new Commit("hello", null, new TreeMap());
        assertEquals(c.getMessage(), "hello");
        assertEquals(c.getParent(), null);
        assertEquals(c.getSavedFiles(), new TreeMap<>());
    }

}


