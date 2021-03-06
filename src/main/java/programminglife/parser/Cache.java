package programminglife.parser;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import programminglife.gui.Alerts;
import programminglife.utility.Console;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A class for managing persistent data. It can open one cache, which contains the information for one gfa file.
 */
public final class Cache {
    private static final String CACHE_FOLDER = "caches";
    private static final String CACHE_EXTENSION = ".db.desoxyribonucleinezuur";

    private static final String SEQUENCE_MAP_NAME = "sequenceMap";
    private static final String SEQUENCE_LENGTH_MAP_NAME = "sequenceLengthMap";
    private static final String NODE_ID_GENOMES_MAP = "nodeIdGenomesMap";
    private static final String NODE_ID_GENOMES_NUMBER_MAP = "nodeIdGenomesNumberMap";
    private static final String GENOME_ID_NAMES_MAP_NAME = "genomeIdNamesMap";
    private static final String GENOME_NAMES_ID_MAP_NAME = "genomeNamesIdMap";
    private static final String CHILDREN_ADJACENCY_MAP_NAME = "childrenNamesMap";
    private static final String PARENTS_ADJACENCY_MAP_NAME = "parentsNamesMap";
    private static final String NUMBER_OF_NODES_INT_NAME = "numberOfNodes";

    private final String dbFileName;
    private DB db;

    private Map<Integer, String> sequenceMap;
    private Map<Integer, Integer> sequenceLengthMap;
    private Map<Integer, int[]> nodeIdGenomesMap; // node id -> array of genome ids
    private Map<Integer, Integer> nodeIdGenomesNumberMap; // node id -> number of genomes
    private Map<Integer, String> genomeIdNamesMap; // genome id -> genome name
    private Map<String, Integer> genomeNamesIdMap; // genome name -> genome id
    private Map<Integer, int[]> childrenAdjacencyMap;
    private Map<Integer, int[]> parentsAdjacencyMap;

    private Atomic.Integer numberOfNodes;

    private LinkedList<Integer> currentParentChildren;
    private int currentParentID;

    /**
     * Create the Cache and initialize the database.
     *
     * @param name The name of the {@link Cache}.
     *             Note: this method will append .db if it doesn't already end with that.
     */
    public Cache(String name) {
        this.dbFileName = toDBFile(name);
        Console.println("[%s] Setting up cache (%s)...", Thread.currentThread().getName(), this.dbFileName);

        try {
            Path dir = Paths.get(dbFileName).getParent();
            Files.createDirectories(dir);
            this.db = DBMaker.fileDB(new File(this.dbFileName))
                    .fileMmapEnable()
                    .fileMmapPreclearDisable()
                    .cleanerHackEnable()
                    .closeOnJvmShutdown()
                    .checksumHeaderBypass()
                    .make();
            this.initialize();
        } catch (IOException e) {
            Alerts.error("Cache file cannot be created at this time. "
                    + "Please run the application from a folder you have write permissions to.");
        }
    }

    /**
     * Initialize all collections in the cache.
     */
    private void initialize() {
        this.sequenceMap = getMap(db, SEQUENCE_MAP_NAME, Serializer.INTEGER, Serializer.STRING_ASCII);
        this.sequenceLengthMap = getMap(db, SEQUENCE_LENGTH_MAP_NAME, Serializer.INTEGER, Serializer.INTEGER);
        this.nodeIdGenomesMap = getMap(db, NODE_ID_GENOMES_MAP, Serializer.INTEGER, Serializer.INT_ARRAY);
        this.nodeIdGenomesNumberMap = getMap(db, NODE_ID_GENOMES_NUMBER_MAP, Serializer.INTEGER, Serializer.INTEGER);
        this.genomeIdNamesMap = getMap(db, GENOME_ID_NAMES_MAP_NAME, Serializer.INTEGER, Serializer.STRING_ASCII);
        this.genomeNamesIdMap = getMap(db, GENOME_NAMES_ID_MAP_NAME, Serializer.STRING_ASCII, Serializer.INTEGER);
        this.childrenAdjacencyMap = getMap(db, CHILDREN_ADJACENCY_MAP_NAME, Serializer.INTEGER, Serializer.INT_ARRAY);
        this.parentsAdjacencyMap = getMap(db, PARENTS_ADJACENCY_MAP_NAME, Serializer.INTEGER, Serializer.INT_ARRAY);

        this.numberOfNodes = db.atomicInteger(NUMBER_OF_NODES_INT_NAME).createOrOpen();

        this.currentParentID = -1;
        this.currentParentChildren = new LinkedList<>();
    }

    /**
     * converts a name to the name that would be used for the cache.
     *
     * @param name The name to be converted
     * @return The converted name.
     */
    @NotNull
    static String toDBFile(String name) {
        if (name.toLowerCase().endsWith(".gfa")) {
            name = name.substring(0, name.length() - 4);
        }
        if (!name.toLowerCase().endsWith(CACHE_EXTENSION)) {
            name += CACHE_EXTENSION;
        }

        Path givenPath = Paths.get(name).toAbsolutePath();
        Path fileName = givenPath.getFileName();
        Path computedPath = givenPath
                .normalize()
                .resolveSibling(CACHE_FOLDER)
                .resolve(fileName)
                .toAbsolutePath();

        return computedPath.toString();
    }

    /**
     * Check whether a cache exists for file named name.
     *
     * @param name collection to check for
     * @return true iff a cache exists for the file, false iff otherwise.
     */
    static boolean hasCache(String name) {
        return Files.exists(Paths.get(toDBFile(name)));
    }

    /**
     * Get the HTreeMap cache for the cached sequence lengths.
     *
     * @return the HTreeMap cache for the sequence lengths.
     */
    private Map<Integer, Integer> getSequenceLengthMap() {
        return this.sequenceLengthMap;
    }

    /**
     * Get the HTreeMap cache for the cached sequences.
     *
     * @return the HTreeMap cache for the sequences.
     */
    private Map<Integer, String> getSequenceMap() {
        return this.sequenceMap;
    }

    private Map<Integer, int[]> getNodeIdGenomesMap() {
        return nodeIdGenomesMap;
    }

    public Map<Integer, Integer> getNodeIdGenomesNumberMap() {
        return nodeIdGenomesNumberMap;
    }

    /**
     * Get the HTreeMap cache for the cached genomes.
     *
     * @return the HTreeMap cache for the sequences.
     */
    private Map<Integer, String> getGenomeIdNamesMap() {
        return this.genomeIdNamesMap;
    }

    public Map<String, Integer> getGenomeNamesIdMap() {
        return genomeNamesIdMap;
    }

    public Map<Integer, int[]> getChildrenAdjacencyMap() {
        return this.childrenAdjacencyMap;
    }

    public Map<Integer, int[]> getParentsAdjacencyMap() {
        return this.parentsAdjacencyMap;
    }

    public int getNumberOfNodes() {
        return this.numberOfNodes.get();
    }

    /**
     * Get a disk-backed hashMap named name. If it doesn't exist, it is created using the provided serializers.
     *
     * @param db              the db to get the map from.
     * @param name            The name of the hashMap
     * @param keySerializer   The serializer for the keys
     * @param valueSerializer The serializer for th values
     * @param <K>             The type of the keys
     * @param <V>             The type of the values.
     * @return a disk-backed hashMap named name.
     */
    @NotNull
    private static <K, V> Map<K, V> getMap(DB db, String name,
                                           Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        if (db.exists(name)) {
            return db.get(name);
        } else {
            return db
                    .hashMap(name)
                    .keySerializer(keySerializer)
                    .valueSerializer(valueSerializer)
                    .create();
        }
    }

    /**
     * Close the database.
     */
    public void close() {
        if (!this.db.isClosed()) {
            Console.println("[%s] Closing MapDB...", Thread.currentThread().getName());
            this.db.close();
        }
    }

    /**
     * Get the sequence for the node with NodeId.
     *
     * @param nodeID ID of the node to get the sequence for.
     * @return the sequence.
     */
    @NotNull
    public String getSequence(int nodeID) {
        if (getSequenceMap().containsKey(nodeID)) {
            return getSequenceMap().get(nodeID);
        } else {
            throw new NoSuchElementException(String.format("No sequence is cached for node %d", nodeID));
        }
    }

    /**
     * Set the sequence for the node with NodeId.
     *
     * @param nodeID   ID of the node to set the sequence for.
     * @param sequence new sequence.
     */
    public void setSequence(int nodeID, String sequence) {
        if (getSequenceMap().put(nodeID, sequence) == null) {
            this.numberOfNodes.incrementAndGet();
        }
        getSequenceLengthMap().put(nodeID, sequence.length());
    }

    /**
     * Get the sequence length for the node with NodeId.
     *
     * @param nodeID ID of the node to get the sequence length for.
     * @return the length of the sequence.
     */
    public int getSequenceLength(int nodeID) {
        if (getSequenceLengthMap().containsKey(nodeID)) {
            return getSequenceLengthMap().get(nodeID);
        } else {
            throw new NoSuchElementException(String.format("No sequence length is cached for node %d", nodeID));
        }
    }

    /**
     * Set the Genomes through a specific Node.
     *
     * @param nodeID    the ID of the Node
     * @param genomeIDs an Array of IDs of Genomes
     */
    public void setGenomes(int nodeID, int[] genomeIDs) {
        this.getNodeIdGenomesMap().put(nodeID, genomeIDs);
        this.getNodeIdGenomesNumberMap().put(nodeID, genomeIDs.length);
    }

    /**
     * Get the Genomes through a specific Node.
     *
     * @param nodeID the ID of the Node
     * @return an Array of Genome IDs
     */
    public int[] getGenomes(int nodeID) {
        return this.getNodeIdGenomesMap().get(nodeID);
    }

    /**
     * Get the name of a Genome based on its index.
     *
     * @param genomeID the index (0-based) of the Genome in the GFA header
     * @return the name of the Genome
     */
    @NotNull
    public String getGenomeName(int genomeID) {
        if (getGenomeIdNamesMap().containsKey(genomeID)) {
            return getGenomeIdNamesMap().get(genomeID);
        } else {
            throw new NoSuchElementException(String.format("No name is cached for genome %d", genomeID));
        }
    }

    /**
     * Get the ID of a Genome based on its name.
     *
     * @param genomeName the name of the Genome
     * @return the index of the Genome in the GFA header
     */
    public int getGenomeID(String genomeName) {
        if (getGenomeNamesIdMap().containsKey(genomeName)) {
            return getGenomeNamesIdMap().get(genomeName);
        } else {
            throw new NoSuchElementException(String.format("No ID is cached for genome %s", genomeName));
        }
    }

    /**
     * Add the name of a Genome, index is previous one + 1.
     *
     * @param genomeName the name of the Genome to add
     */
    public void addGenomeName(String genomeName) {
        int index = getGenomeIdNamesMap().size();
        getGenomeIdNamesMap().put(index, genomeName);
        getGenomeNamesIdMap().put(genomeName, index);
    }

    /**
     * Completely remove a database. This cannot be undone.
     *
     * @throws IOException when something strange happens during deletion
     */
    public void removeDB() throws IOException {
        Console.println("[%s] Removing database %s", Thread.currentThread().getName(), this.dbFileName);
        close();
        Files.deleteIfExists(Paths.get(this.dbFileName));
    }

    /**
     * Remove a cache file.
     *
     * @param name the name of the file to remove
     * @return true if the file was deleted
     * @throws IOException when strange things happen
     */
    public static boolean removeDB(String name) throws IOException {
        return Files.deleteIfExists(Paths.get(toDBFile(name)));
    }

    /**
     * Rolls back non-persistent changes in database.
     *
     * @throws IOException when something strange happens during deletion
     */
    public void rollback() throws IOException {
        // TODO find a way to handle a partially complete cache
        // Just removing the cache is the best solution for now
        this.removeDB();
    }

    public LinkedList<Integer> getCurrentParentChildren() {
        return this.currentParentChildren;
    }

    public void setCurrentParentChildren(LinkedList<Integer> currentParentChildren) {
        this.currentParentChildren = currentParentChildren;
    }

    public int getCurrentParentID() {
        return this.currentParentID;
    }

    public void setCurrentParentID(int currentParentID) {
        this.currentParentID = currentParentID;
    }

    /**
     * Get Node IDs belonging to a Genome.
     *
     * @param genomeID the ID of the Genome to look up
     * @return a {@link Collection} of IDs
     */
    public Collection<Integer> getGenomeNodeIDs(int genomeID) {
        Set<Integer> nodeIDs = new TreeSet<>();
        for (Map.Entry<Integer, int[]> entry : this.getNodeIdGenomesMap().entrySet()) {
            if (ArrayUtils.contains(entry.getValue(), genomeID)) {
                nodeIDs.add(entry.getKey());
            }
        }
        return nodeIDs;
    }
}
