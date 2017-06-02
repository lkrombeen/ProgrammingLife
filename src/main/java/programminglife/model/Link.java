package programminglife.model;

/**
 * Created by toinehartman on 17/05/2017.
 */
public class Link implements Edge {
    private int[] genomeSet;
    private Node start;
    private Node end;

    /**
     * Constructor for Link.
     * @param start the start (parent) of this Link
     * @param end the end (child) of this Link
     * @param genomes Thegenomes that flow through this Link.
     */
    public Link(Node start, Node end, int[] genomes) {
        this.start = start;
        this.end = end;
        this.genomeSet = genomes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getGenomes() {
        return this.genomeSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getStart() {
        return start;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("Link from [node: %s] to [node: %s]",
                this.start.toString(),
                this.end.toString());
    }
}
