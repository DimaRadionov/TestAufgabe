package org.example;

import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.IOException;

public class JsonCalculator extends Logic {
    public static void main(String[] args) throws IOException, ParseException {
        // Don't change this part
        if (args.length == 3) {
            // Path to the data file, e.g. data/data.xml
            final String DATA_FILE = args[0];
            // Path to the data file, e.g. operations/operations.xml
            final String OPERATIONS_FILE = args[1];
            // Path to the output file
            final String OUTPUT_FILE = args[2];

            // <your code here>
            Logic logic = new Logic();
            logic.getFilterFromOperations(OPERATIONS_FILE);
            logic.groupCities(OPERATIONS_FILE, DATA_FILE);
            logic.functionsAndValuesToList(DATA_FILE, OPERATIONS_FILE);
            logic.writeToFile(new File(OUTPUT_FILE), logic.createJson());
        } else {
            System.exit(1);
        }
    }
}