import data.Data;

import java.time.LocalTime;
import java.util.*;

/**
 * This main class is the entry point for the optimizer.
 */
public class Main {

    public static void main(String[] args) {
        boolean trace = false;
        parseData(trace);
    }

    /**
     * Parsing the data, then solving it with the greedy initialization and the ALNS afterwards.
     *
     * @param trace - if set to true, traces will appear
     */
    public static void parseData(boolean trace) {

        for (int i = 1; i < 11; i++) {
            LocalTime start = LocalTime.now();
            if (trace) System.out.println("Iteration " + i + " started at " + start.toString());
            Parser parser = new Parser();
            parser.addPath("path_goes_here");
            parser.setFolder();
            // TODO: IF PARSING THE SOLOMON INSTANCES, SET IT TO TRUE, FALSE OTHERWISE
            List<Data> dataList = parser.parseInstances(true);
            Solver solver = new Solver(dataList);
            Logger logger;
            for (Data data : dataList) {
                System.out.println("Solving " + data.getInfo());
                logger = new Logger();
                logger.setPath("path_goes_here" + data.getInfo() + "_" + i + ".txt");
                solver.initGreedy(data, logger);
                try {
                    solver.ALNS(data, logger);
                } catch (IndexOutOfBoundsException exception) {
                    logger.log(exception.getLocalizedMessage());
                    logger.writeFile();
                    if (trace) System.out.println(data.getInfo() + " failed");
                }
                logger.writeFile();
            }
            LocalTime end = LocalTime.now();
            if (trace) System.out.println("Iteration " + i + " ended at " + end.toString());
        }
    }

}