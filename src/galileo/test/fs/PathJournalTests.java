
package galileo.test.fs;

import java.io.File;
import java.io.IOException;

import galileo.dataset.feature.Feature;
import galileo.fs.PathJournal;
import galileo.graph.FeaturePath;

import org.junit.Test;

import static org.junit.Assert.*;

public class PathJournalTests {

    private static String journal = "/tmp/pathjournal";
    private static String index = "/tmp/pathjournal.index";

    public PathJournalTests() {
        removeJournal();
    }

    private void removeJournal() {
        new File(journal).delete();
        new File(index).delete();
    }

    @Test
    public void testCreation() throws Exception {
        removeJournal();

        FeaturePath<String> fp = new FeaturePath<>();
        fp.add(new Feature("humidity", 24.3f));
        fp.add(new Feature("temperature", 4.1f));
        fp.add(new Feature("wind", 8.0f));
        fp.add(new Feature("snow", 0.32f));
        fp.addPayload("/a/b/c/d");

        FeaturePath<String> fp2 = new FeaturePath<>();
        fp.add(new Feature("humidity", 84.3f));
        fp.add(new Feature("temperature", 43.1f));
        fp.add(new Feature("wind", 1.0f));
        fp.add(new Feature("snow", 0.0f));
        fp.addPayload("/a/b/c/test");

        PathJournal pj = new PathJournal("/tmp/pathjournal");
        pj.start();
        pj.persistPath(fp);
        pj.persistPath(fp2);

        System.out.println("=======");
    }
}
