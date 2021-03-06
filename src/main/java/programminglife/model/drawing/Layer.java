package programminglife.model.drawing;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A layer of {@link DrawableNode DrawableNodes}. Multiple Layers are used to lay out the graph.
 *
 * @see SubGraph#layout()
 * @see SubGraph#findLayers()
 */
public class Layer implements Iterable<DrawableNode>, Comparable<Double> {
    private static final double DUMMY_Y_OFFSET = 5;
    private double x;
    private final List<DrawableNode> nodes;

    private static final int LINE_PADDING = 30;

    /**
     * Default empty constructor.
     */
    Layer() {
        this.x = 0;
        this.nodes = new ArrayList<>();
    }

    /**
     * Getter for the width of a layer.
     *
     * @return double of the width.
     */
    public double getWidth() {
        double width = 0;
        for (DrawableNode node : nodes) {
            if (node.getWidth() > width) {
                width = node.getWidth();
            }
        }
        return width;
    }

    /**
     * Add a {@link DrawableNode} to this Layer.
     *
     * @param node the node to add.
     */
    public void add(DrawableNode node) {
        this.nodes.add(node);
    }

    /**
     * Get an iterator over the {@link DrawableNode DrawableNodes} in this Layer.
     *
     * @return an iterator over the {@link DrawableNode DrawableNodes} in this Layer.
     */
    @NotNull
    @Override
    public Iterator<DrawableNode> iterator() {
        return nodes.iterator();
    }

    /**
     * sort the {@link DrawableNode DrawableNodes} in this layer according to the order of the {@link Comparator} c.
     *
     * @param subGraph   The subGraph that the neighbour layer is part of.
     * @param neighbour  The neighbouring layer that is used to sort this one.
     * @param hasParents Whether the neighbouring layer contains the parents (true)
     *                   or the children (false) of this layer.
     */
    void sort(SubGraph subGraph, Layer neighbour, boolean hasParents) {
        Map<DrawableNode, Double> averages = new LinkedHashMap<>();

        for (DrawableNode n : nodes) {
            Collection<DrawableNode> neighbourCollection;
            if (hasParents) {
                neighbourCollection = subGraph.getParents(n);
            } else {
                neighbourCollection = subGraph.getChildren(n);
            }

            int size = neighbourCollection.size();
            double sum = 0; // double to avoid integer division when putting the averages in the map.
            for (DrawableNode p : neighbourCollection) {
                int index = neighbour.indexOf(p);
                if (index >= 0) {
                    sum += index;
                } else {
                    size--; // one of the parents / children is not in the neighbour,
                    // we shouldn't count it towards the size
                }
            }

            // avoid division by 0.
            if (size != 0) {
                averages.put(n, sum / size);
            } else {
                averages.put(n, -1.0);
            }
        }

        nodes.sort((o1, o2) -> {
            double epsilon = 1e-10;
            double difference = averages.get(o2) - averages.get(o1);
            if (difference < -epsilon) {
                return 1;
            } else if (difference > epsilon) {
                return -1;
            } else if (o1.getGenomes().size() != o2.getGenomes().size()) {
                return o2.getGenomes().size() - o1.getGenomes().size();
            } else {
                return o2.getIdentifier() - o1.getIdentifier();
            }
        });
    }

    /**
     * Checks whether a node is in this layer.
     *
     * @param node {@link DrawableNode} to check for its presence.
     * @return {@link boolean} true if it is in the layer, false otherwise.
     */
    public boolean contains(DrawableNode node) {
        return nodes.contains(node);
    }

    /**
     * Get the size of the layer.
     *
     * @return {@link int} the size of the layer.
     */
    public int size() {
        return this.nodes.size();
    }

    /**
     * Get the index of the a {@link DrawableNode} node in the layer.
     *
     * @param node {@link DrawableNode} to get the index of.
     * @return the index of the node, or -1 if this Layer does not contain the node.
     */
    private int indexOf(DrawableNode node) {
        return nodes.indexOf(node);
    }

    /**
     * Setter of the location to draw at.
     *
     * @param y double of the y location.
     * @param zoomLevel double of the zoomLevel.
     */
    void setDrawLocations(double y, double zoomLevel) {

        for (DrawableNode node : nodes) {
            if (node instanceof DrawableDummy) {
                node.setLocation(x, y + (DUMMY_Y_OFFSET * zoomLevel));
            } else {
                node.setLocation(x, y);
            }
            y += LINE_PADDING * zoomLevel;
        }
    }

    /**
     * setter for the size of a layer.
     *
     * @param scale double of the scale to be set.
     */
    public void setSize(double scale) {
        for (DrawableNode node : nodes) {
            node.setDrawDimensions(scale);
        }
    }

    /**
     * Creates of toString of the layer.
     *
     * @return String of the layer.
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Layer[Segments: ");
        for (DrawableNode n : nodes) {
            stringBuilder.append(n.toString()).append(", ");
        }
        return stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append("]").toString();
    }

    public List<DrawableNode> getNodes() {
        return this.nodes;
    }

    public double getX() {
        return x;
    }

    void setX(double x) {
        this.x = x;
    }

    @Override
    public int compareTo(@NotNull Double o) {
        return Double.compare(this.x, o);
    }

    public double getY() {
        return this.nodes.get(0).getLocation().getY();
    }
}
