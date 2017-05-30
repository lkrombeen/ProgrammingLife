package programminglife.model;

import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by marti_000 on 25-4-2017.
 */
public class Segment extends Rectangle implements Node {
    private int id;
    private boolean drawDimensionsUpToDate = false;
    private GenomeGraph graph;

    /**
     * Constructor for a node with an id.
     * @param id int.
     * @param graph The {@link GenomeGraph}.
     */
    public Segment(int id, GenomeGraph graph) {
        this(id, null, graph);
    }

    /**
     * Constructor for a node with and id and sequence.
     * @param id int.
     * @param sequence String.
     * @param graph The {@link GenomeGraph}.
     */
    public Segment(int id, String sequence, GenomeGraph graph) {
        this.id = id;
        this.graph = graph;
        if (sequence != null) {
            DataManager.setSequence(id, sequence);
        }
    }

    /**
     * Getter for the sequence.
     * @return the sequence of base pairs
     */
    public String getSequence() {
        return DataManager.getSequence(this.id);
    }

    /**
     * Set the sequence of base pairs of the {@link Segment}.
     * @param sequence A {@link String} representing the base pairs
     */
    public void setSequence(String sequence) {
        DataManager.setSequence(this.id, sequence);
        this.drawDimensionsUpToDate = false;
    }

    /**
     * getter for the center of the right border.
     * @return XYCoordinate.
     */
    public XYCoordinate getRightBorderCenter() {
        if (!drawDimensionsUpToDate) {
            setDrawDimensions();
        }
        return this.getCenter().add(this.getSize().getX() >> 1, 0);
    }

    /**
     * getter for the center of the left border.
     * @return XYCoordinate.
     */
    public XYCoordinate getLeftBorderCenter() {
        if (!drawDimensionsUpToDate) {
            setDrawDimensions();
        }
        return this.getCenter().add(-(this.getSize().getX() >> 1), 0);
    }

    /**
     * getter for the center.
     * @return XYCoordinate.
     */
    public XYCoordinate getCenter() {
        if (!drawDimensionsUpToDate) {
            setDrawDimensions();
        }
        return this.getLocation().add(this.getSize().multiply(0.5));
    }

    /**
     * Getter for the id.
     * @return int.
     */
    public int getIdentifier() {
        return this.id;
    }

    @Override
    public Collection<? extends Edge> getChildEdges() {
        Collection<Link> result = new HashSet<>();
        for (Node node : graph.getChildren(this.id)) {
            result.add(new Link(this, node, graph.getGenomes(node)));
        }
        return result;
    }

    @Override
    public Collection<? extends Edge> getParentEdges() {
        Collection<Link> result = new HashSet<>();
        for (Node node : graph.getParents(this.id)) {
            result.add(new Link(node, this, graph.getGenomes(this)));
        }
        return result;
    }

    @Override
    public Collection<? extends Node> getChildren() {
        return new HashSet<>(graph.getChildren(this.id));
    }

    @Override
    public Collection<? extends Node> getParents() {
        return new HashSet<>(graph.getParents(this.id));
    }

    @Override
    public Collection<Genome> getGenomes() {
        return this.graph.getGenomes(this);
    }

    /**
     * toString method for the node.
     * @return the {@link String} representation of a {@link Segment}
     */
    @Override
    public String toString() {
        String sequence = this.getSequence();
        return String.format("Segment<%d>[s(%d):%s]",
                this.getIdentifier(),
                sequence.length(),
                StringUtils.abbreviate(sequence, 11));
    }

    /**
     * Get a {@link XYCoordinate} representing the size of the {@link Segment}.
     * @return The size of the {@link Segment}
     */
    public XYCoordinate getSize() {
        if (!drawDimensionsUpToDate) {
            setDrawDimensions();
        }
        return new XYCoordinate((int) this.getWidth(), (int) this.getHeight());
    }

    /**
     * Set the size {@link XYCoordinate} of the {@link Segment}.
     * @param size The {@link XYCoordinate} representing the size
     */
    void setSize(XYCoordinate size) {
        this.setWidth(size.getX());
        this.setHeight(size.getY());
        this.drawDimensionsUpToDate = true;
    }

    /**
     * Getter for top left corner of a {@link Segment}.
     * @return {@link XYCoordinate} with the values of the top left corner.
     */
    public XYCoordinate getLocation() {
        return new XYCoordinate((int) this.getX(), (int) this.getY());
    }

    /**
     * Set an {@link XYCoordinate} representing the location of the {@link Segment}.
     * @param location The {@link XYCoordinate}
     */
    public void setLocation(XYCoordinate location) {
        this.setX(location.getX());
        this.setY(location.getY());
    }

    /**
     * getter for the width coordinate.
     * @return XYCoordinate.
     */
    public XYCoordinate getWidthCoordinate() {
        if (!drawDimensionsUpToDate) {
            setDrawDimensions();
        }
        return new XYCoordinate((int) this.getWidth(), 0);
    }

    /**
     * getter for the height coordinate.
     * @return XYCoordinate.
     */
    public XYCoordinate getHeightCoordinate() {
        if (!drawDimensionsUpToDate) {
            setDrawDimensions();
        }
        return new XYCoordinate(0, (int) this.getHeight());
    }

    /**
     * Setter for the dimension of the node.
     */
    private void setDrawDimensions() {
        int segmentLength = this.getSequenceLength();
        int width, height;

        width = 10 + (int) Math.pow(segmentLength, 1.0 / 2);
        height = 10;

        this.setSize(new XYCoordinate(width, height));
        this.drawDimensionsUpToDate = true;
    }

    /**
     * get the length of the sequence of this segment.
     * @return the length of the sequence of this segment
     */
    public int getSequenceLength() {
        return DataManager.getSequenceLength(this.getIdentifier());
    }
}
