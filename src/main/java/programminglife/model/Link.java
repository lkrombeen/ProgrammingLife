package programminglife.model;

import java.util.Collection;
import java.util.Set;

/**
 * Created by toinehartman on 17/05/2017.
 */
public class Link implements Edge<Segment> {
    private Set<Genome> genomeSet;
    private Segment start;
    private Segment end;

    /**
     * Constructor for Link.
     * @param start the start (parent) of this Link
     * @param end the end (child) of this Link
     * @param genomes Thegenomes that flow through this Link.
     */
    public Link(Segment start, Segment end, Set<Genome> genomes) {
        this.start = start;
        this.end = end;
        this.genomeSet = genomes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Genome> getGenomes() {
        return this.genomeSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Segment getStart() {
        return start;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Segment getEnd() {
        return end;
    }
}
