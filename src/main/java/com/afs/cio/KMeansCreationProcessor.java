package com.afs.cio;

import com.afs.cio.model.LsiVectorLog;
import com.afs.cio.model.RawLogInput;
import com.afs.cio.model.SearchCluster;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.bundle.ObjectBundle;
import de.lmu.ifi.dbs.elki.distance.distancefunction.CosineDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;

public class KMeansCreationProcessor extends BaseXmlProcessor<LsiVectorLog,SearchCluster> {

    protected int clusterCount = 0;

    protected int maxIterations = 0;

    Comparator<RawLogInput> termComparator = new Comparator<RawLogInput>() {
        @Override
        public int compare(RawLogInput o1, RawLogInput o2) {
            return o1.getTerm().compareTo(o2.getTerm());
        }
    };

    Comparator<SearchCluster> sizeComparator = new Comparator<SearchCluster>() {
        @Override
        public int compare(SearchCluster o1, SearchCluster o2) {
            return Integer.compare(o2.getMembers().size(),o1.getMembers().size());
        }
    };

    public static void main( String[] args )
    {
        KMeansCreationProcessor app = new KMeansCreationProcessor();
        app.cmdLine(args);
        app.run();
    }

    @Override
    protected void addCustomOptions(Options cmdLineOptions) {
        cmdLineOptions.addOption(Option.builder("count").hasArg().desc("target cluster count").type(Integer.class).build());
        cmdLineOptions.addOption(Option.builder("iterations").hasArg().desc("maximum number of iterations").type(Integer.class).build());
    }

    @Override
    protected void extractCustomOptionValues(CommandLine cmdLine) throws ParseException {
        if (cmdLine.hasOption("count")) {
            this.clusterCount = Integer.parseInt(cmdLine.getOptionValue("count"));
        }
        if (cmdLine.hasOption("iterations")) {
            this.maxIterations = Integer.parseInt(cmdLine.getOptionValue("iterations"));
        }
        super.extractCustomOptionValues(cmdLine);
    }

    protected KMeansCreationProcessor() {
        super(LsiVectorLog.class, SearchCluster.class);
    }
    @Override
    public List<SearchCluster> process(List<LsiVectorLog> inputItems) {
        // Prep for K-Means execution
        // Adapter to load data from an existing array.
        double[][] data = new double[inputItems.size()][];
        String[] labels = new String[inputItems.size()];
        int index = 0;
        for (LsiVectorLog logRecord : inputItems) {
            data[index] = new double[logRecord.vector.size()];
            // TODO - attach index number (ID) to label and cross-reference to original log Record
            labels[index] = Integer.toString(index);
            for (int vecIndex = 0; vecIndex < logRecord.vector.size(); vecIndex++) {
                data[index][vecIndex] = logRecord.vector.get(vecIndex);
            }
            index++;
        }
        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, labels);
        // Create a database (which may contain multiple relations!)
        Database db = new StaticArrayDatabase(dbc, null);
        // Load the data into the database (do NOT forget to initialize...)
        db.initialize();

        // K-means should be used with squared Euclidean (least squares):
        CosineDistanceFunction dist = CosineDistanceFunction.STATIC;
        // Default initialization, using global random:
        // To fix the random seed, use: new RandomFactory(seed);
        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);

        // Setup textbook k-means clustering:
        if (this.clusterCount == 0) {
            this.clusterCount = inputItems.size()/5;
        }
        KMeansLloyd<NumberVector> km = new KMeansLloyd<>(dist, clusterCount, maxIterations, init);

        // Run the algorithm:
        long startTime = System.currentTimeMillis();
        Clustering<KMeansModel> c = km.run(db);
        long runTime = System.currentTimeMillis() - startTime;
        List<SearchCluster> results = new ArrayList<>();
        for (Cluster eachCluster : c.getToplevelClusters()) {
            SearchCluster resultCluster = new SearchCluster();
            resultCluster.setLabel(eachCluster.getNameAutomatic());
            resultCluster.setMembers(new ArrayList<>());
            for (DBIDIter iter = eachCluster.getIDs().iter(); iter.valid(); iter.advance()) {
                ObjectBundle bundle = db.getBundle(iter);
                String label = (String)bundle.data(0,2);
                resultCluster.getMembers().add(inputItems.get(Integer.parseInt(label)));
                DBID id = DBIDUtil.deref(iter); // Materialize only if you need to!
            }
            RawLogInput label = chooseLabel(resultCluster.getMembers());
            resultCluster.setLabel(label.getTerm());
            // Sort by query term so like terms are together
            Collections.sort(resultCluster.getMembers(),termComparator);
            results.add(resultCluster);
        }
        // sort by cluster size (biggest first)
        Collections.sort(results,sizeComparator);

        // Attach export attributes
        this.exportAttributes.put("clusterCount",Integer.toString(clusterCount));
        this.exportAttributes.put("iterations",Integer.toString(maxIterations));
        this.exportAttributes.put("runTimeMs",Long.toString(runTime));

        return results;
    }

    protected RawLogInput chooseLabel (List<RawLogInput> group) {
        CosineDistanceFunction dist = CosineDistanceFunction.STATIC;
        Map<LsiVectorLog,Double> distanceMap = new HashMap<>();
        for (RawLogInput candidateLabel : group) {
            LsiVectorLog lsiLog = (LsiVectorLog)candidateLabel;
            double aggregateDistance = 0.0;
            NumberVector candidateVector = createNumberVector(lsiLog);
            for (RawLogInput otherMember : group) {
                LsiVectorLog otherLsi = (LsiVectorLog)otherMember;
                if (candidateLabel != otherMember) {
                    aggregateDistance += dist.distance(candidateVector, createNumberVector(otherLsi));
                }
            }
            distanceMap.put(lsiLog,aggregateDistance);
        }
        // Choose first item as initial smallest distance
        LsiVectorLog bestLabel = distanceMap.entrySet().iterator().next().getKey();
        double bestDistance = distanceMap.get(bestLabel).doubleValue();
        for (Map.Entry<LsiVectorLog,Double> eachEntry : distanceMap.entrySet()) {
            if (eachEntry.getValue() < bestDistance) {
                bestDistance = eachEntry.getValue();
                bestLabel = eachEntry.getKey();
            }
        }
        return bestLabel;
    }

    private NumberVector createNumberVector(LsiVectorLog lsiVec) {
        double[] vector = new double[lsiVec.vector.size()];
        int index = 0;
        for (Double component : lsiVec.vector) {
            vector[index] = lsiVec.vector.get(index);
            index++;
        }
        return new DoubleVector(vector);
    }

}
