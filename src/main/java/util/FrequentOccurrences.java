package util;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class FrequentOccurrences {
    public static void main(String[] args) {
        //String csvFile = args[0];
        String csvFile = "data/2013-02-27.1000.csv";
        BufferedReader br = null;
        String line;
        String csvSplitBy = ",";

        HashMap<String, Integer> counters = new HashMap<>();

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] pieces = line.split(csvSplitBy);
                /* line contains:
                00 start_ts,
                01 end_ts,
                02 src_ip,
                03 dst_ip,
                04 src_port,
                05 dst_port,
                06 protocol,
                07 tos,
                08 tcp_flags,
                09 packet_count,
                10 bytes,
                11 router_in_port,
                12 router_out_port
                */


                String key = pieces[2];

                if (!counters.containsKey(key))
                    counters.put(key, 0);
                counters.put(key, counters.get(key) + 1);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // roughly how many items to return
        int numReturns = 10;


        int N = counters.size();
        int[] counts = new int[N];
        int i = 0;
        for (String key : counters.keySet()) {
            counts[i++] = counters.get(key);

        }

        //Integer[] counts = (Integer[]) counters.values().toArray();
        Arrays.sort(counts);

        int lastIndex = N - numReturns;
        if (lastIndex < 0)
            lastIndex = 0;

        int threshold = counts[lastIndex];

        for (String key : counters.keySet()) {
            if (counters.get(key) >= threshold)
                System.out.printf("%4d occurrences: %s\n", counters.get(key), key);
        }
    }
}
