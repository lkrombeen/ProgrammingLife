package programminglife.parser;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import programminglife.model.*;
import programminglife.model.exception.UnknownTypeException;

import java.io.File;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by toinehartman on 16/05/2017.
 */
public class GraphParserTest implements Observer {
    private static String TEST_PATH, TEST_FAULTY_PATH;

    private String linkLine, nodeLine;
    private GraphParser graphParser, faultyGraphParser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        DataManager.initialize("test");

        TEST_PATH = new File(GenomeGraphTest.class.getResource("/test.gfa").toURI()).getAbsolutePath();
        TEST_FAULTY_PATH = new File(
                GenomeGraphTest.class.getClass().getResource("/test-faulty.gfa").toURI()
        ).getAbsolutePath();
    }

    @Before
    public void setUp() throws Exception {
        File testFile = new File(TEST_PATH);
        graphParser = new GraphParser(testFile);

        File faultyTestFile = new File(TEST_FAULTY_PATH);
        faultyGraphParser = new GraphParser(faultyTestFile);

        linkLine = "L\t34\t+\t35\t+\t0M";
        nodeLine = "S\t6\tC\t*\tORI:Z:TKK_04_0031.fasta\tCRD:Z:TKK_04_0031.fasta\tCRDCTG:Z:7000000219691771\tCTG:Z:7000000219691771\tSTART:Z:3039";
    }

    @Test(expected = UnknownTypeException.class)
    public void faultyParseTest() throws Exception {
        faultyGraphParser.parse();
    }

    @Test
    public void parseTest() throws Exception {
        graphParser.parse();
        GenomeGraph graph = graphParser.getGraph();

        assertEquals(8, graph.size());
    }

    @Test
    public void parseLinkTest() {
         graphParser.parseLink(linkLine);
    }

    @Test
    public void parseSegmentTest() {
        graphParser.parseSegment(nodeLine);
        Graph g = graphParser.getGraph();

        assertTrue(g.contains(6));
        assertEquals("C", DataManager.getSequence(6));
        assertEquals(0, g.getParents(6).size());
        assertEquals(0, g.getChildren(6).size());
    }

    @Test
    public void runTestSuccess() {
        graphParser.addObserver(this);
        graphParser.run();
    }

    @Test(expected = UnknownTypeException.class)
    public void runTestFailure() throws Throwable {
        try {
            faultyGraphParser.addObserver(this);
            faultyGraphParser.run();
            fail();
        } catch (RuntimeException re) {
            throw re.getCause();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof GraphParser) {
            if (arg instanceof GenomeGraph) {
                GenomeGraph graph = (GenomeGraph) arg;

                assertEquals(new File(TEST_PATH).getName(), graph.getID());
                assertEquals("GTC", DataManager.getSequence(8));
            } else if (arg instanceof Exception) {
                throw new RuntimeException((Exception) arg);
            }
        }
    }
}
