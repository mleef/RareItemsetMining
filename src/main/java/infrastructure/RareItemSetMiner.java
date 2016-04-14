package infrastructure;

import mining.FPItemSetMiner;
import mining.Item;
import mining.ItemSet;
import mining.ItemSetMiner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by mikkelKringelbach on 4/5/16.
 * Encapsulates streaming and mining logic
 */
public class RareItemSetMiner implements Serializable {

    //NOTE Can we assume it will always be string?
    private ItemSetMiner<String> itemSetMiner;

    public RareItemSetMiner() {
        this.itemSetMiner = new FPItemSetMiner<>(String.class);
    }

    private void runAnalysis(String inputFileName, String outputDir, int minThreshold, int maxThreshold,
                             int minSize, int maxSize) {

        // Setup the Spark contextBe
        SparkConf conf = new SparkConf().setAppName("edu.princeton.cos598e.rareitemset").setMaster("local");
        //JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
        JavaSparkContext context = new JavaSparkContext(conf);

        // Setup the miner
        FPItemSetMiner<String> miner = new FPItemSetMiner<>(String.class);

        // Perform the mappings
        JavaRDD<String> file = context.textFile(inputFileName);
        JavaRDD<String> stringJavaRDD = file.flatMap(NEW_LINE_SPLIT);
        JavaRDD<ItemSet<String>> itemSetJavaRDD = stringJavaRDD.map((Function<String, ItemSet<String>>) (s) -> itemSetFromLine(s, " "));
        itemSetJavaRDD.collect().forEach(miner::addItemSet);
        Set<ItemSet<String>> result = miner.mine(minThreshold, maxThreshold, minSize, maxSize);

        // Create a DStream that will connect to hostname:port, like localhost:9999
        // JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);

        // This is printing null right now because the mine is not implemented. But the tree is being printed so verify
        // the other output.
        for(ItemSet<String> items : result) {
            System.out.println(items);
        }
    }

    private void runGroceries(int minThreshold, int maxThreshold, int minSize, int maxSize) {
        // Setup the Spark contextBe
        SparkConf conf = new SparkConf().setAppName("edu.princeton.cos598e.rareitemset").setMaster("local");
        //JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
        JavaSparkContext context = new JavaSparkContext(conf);

        // Setup the miner
        FPItemSetMiner<String> miner = new FPItemSetMiner<>(String.class);

        // Perform the mappings
        JavaRDD<String> file = context.textFile("data/groceries.csv");
        JavaRDD<String> stringJavaRDD = file.flatMap(NEW_LINE_SPLIT);
        JavaRDD<ItemSet<String>> itemSetJavaRDD = stringJavaRDD.map(s -> itemSetFromLine(s, ","));
        itemSetJavaRDD.collect().forEach(miner::addItemSet);
        Set<ItemSet<String>> result = miner.mine(minThreshold, maxThreshold, minSize, maxSize);

        // Create a DStream that will connect to hostname:port, like localhost:9999
        // JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);

        // This is printing null right now because the mine is not implemented. But the tree is being printed so verify
        // the other output.
        for(ItemSet<String> items : result) {
            System.out.println(items);
        }
    }

    private void runAnalysisSocket(int minThreshold, int maxThreshold, int minSize, int maxSize) {

        // Setup the Spark contextBe
        SparkConf conf = new SparkConf().setAppName("edu.princeton.cos598e.rareitemset").setMaster("local");
        JavaStreamingContext context = new JavaStreamingContext(conf, Durations.seconds(15));

        JavaReceiverInputDStream<String> lines = context.socketTextStream("localhost", 9999);

        // Setup the miner
        FPItemSetMiner<String> miner = new FPItemSetMiner<>(String.class);

        // Perform the mappings
        JavaDStream<ItemSet<String>> itemsets = lines.map((Function<String, ItemSet<String>>) (s) -> itemSetFromLine(s, " "));
        itemsets.foreachRDD((itemSetJavaRDD, time) -> {

            System.out.println("Testing...");
            itemSetJavaRDD.collect().forEach(miner::addItemSet);
            Set<ItemSet<String>> result = miner.mine(minThreshold, maxThreshold, minSize, maxSize);

            System.out.println("Result: " + result);
        });

        context.start();
        context.awaitTermination();
    }

    /**
     * Converts a string of the form "a b c d" into the ItemSet representing a transaction of a, b, c, and d.
     * @param s The string to be converted to an item set
     * @param delimiter Delimiter to split the string by
     * @return The item set corresponding to the string
     */
    private ItemSet<String> itemSetFromLine(String s, String delimiter) {
        String[] items = s.split(delimiter);

        ItemSet<String> itemSet = new ItemSet<>(String.class);

        for (String item : items) {
            itemSet.add(new Item<>(item, String.class));
        }

        return itemSet;
    }

    private static final FlatMapFunction<String, String> NEW_LINE_SPLIT = s -> Arrays.asList(s.split("\n"));

    public static void main(String[] args) {

        RareItemSetMiner rareItemSetMiner = new RareItemSetMiner();

//        rareItemSetMiner.runAnalysis(args[0], args[1], 0, 10, 2, 10);
//        rareItemSetMiner.runAnalysisSocket(0, 10);
        rareItemSetMiner.runGroceries(1, 10, 2, 3);

    }

}
