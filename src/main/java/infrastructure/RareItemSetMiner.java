package infrastructure;

import Structures.FPItemSetMiner;
import Structures.Item;
import Structures.ItemSet;
import Structures.ItemSetMiner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by mikkelKringelbach on 4/5/16.
 * Encapsulates streaming and mining logic
 */
public class RareItemSetMiner implements Serializable {


    // Can we assume it will always be string?
    ItemSetMiner<String> itemSetMiner;

    public RareItemSetMiner() {
        this.itemSetMiner = new FPItemSetMiner<>();
    }



    private void runAnalysis(String inputFileName, String outputDir) {

        // Setup the Spark context
        SparkConf conf = new SparkConf().setAppName("edu.princeton.cos598e.rareitemset").setMaster("local");
        //JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
        JavaSparkContext context = new JavaSparkContext(conf);

        // Setup the miner
        FPItemSetMiner<String> miner = new FPItemSetMiner<>();

        // Perform the mappings
        JavaRDD<String> file = context.textFile(inputFileName);
        JavaRDD<String> stringJavaRDD = file.flatMap(NEW_LINE_SPLIT);
        JavaRDD<ItemSet<String>> itemSetJavaRDD = stringJavaRDD.map(this::itemSetFromLine);
        itemSetJavaRDD.foreach(miner::addItemSet);
        Set<ItemSet<String>> result = miner.mine(0, 10);

        // Create a DStream that will connect to hostname:port, like localhost:9999
        // JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);

        System.out.println("Result: \n " + result);
    }



    private static final FlatMapFunction<String, String> NEW_LINE_SPLIT = s -> Arrays.asList(s.split("\n"));

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Please provide the input file full path as argument, and the output folder path");
            System.exit(0);
        }

        RareItemSetMiner rareItemSetMiner = new RareItemSetMiner();

        rareItemSetMiner.runAnalysis(args[0], args[1]);

    }

    private ItemSet<String> itemSetFromLine(String s) {
        String[] items = s.split(" ");

        ItemSet<String> itemSet = new ItemSet<>(String.class);

        for (String item : items) {
            itemSet.add(new Item<>(item, String.class));
        }

        return itemSet;
    }

}
