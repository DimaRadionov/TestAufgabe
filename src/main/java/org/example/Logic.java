package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Logic {
    private JSONParser parser = new JSONParser();
    private List<String> functions = new ArrayList<>();
    private List<String> filters = new ArrayList<>();
    private List<String> cities = new ArrayList<>();
    private  List<String> citiesStartingWith = new ArrayList<>();
    private List<String> citiesEndingWith = new ArrayList<>();
    private List<String> citiesContainsOneChar = new ArrayList<>();
    private List<String> citiesContainsMultipleChars = new ArrayList<>();
    private List<Double> areas = new ArrayList<>();
    private List<Long> populations = new ArrayList<>();
    private List<String> namesList = new ArrayList<>();
    private List<String> valuesList = new ArrayList<>();
    private double totalPopulation = 0;
    private double totalArea = 0;

    // Reads a JSON file containing data and returns the "entries" array as a JSONArray
    public JSONArray getDataJson(String dataFilePath) throws IOException, ParseException {
        Object objData = parser.parse(new FileReader(dataFilePath));
        JSONObject dataFile = (JSONObject) objData;
        return (JSONArray) dataFile.get("entries");
    }

    // Reads a JSON file containing operations and returns the "operations" array as a JSONArray
    public JSONArray getOperationsJson(String operationsFilePath) throws IOException, ParseException {
        Object objOperations = parser.parse(new FileReader(operationsFilePath));
        JSONObject operationsFile = (JSONObject) objOperations;
        return (JSONArray) operationsFile.get("operations");
    }

    // Rounds a double value to two decimal places and returns it as a string
    public String roundValue(Double value) {
        DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        return df.format(value);
    }

    // Reads filter definitions from the operations file and adds them to the filters list
    public void getFilterFromOperations(String operationsFilePath) throws IOException, ParseException {
        for (Object item : getOperationsJson(operationsFilePath)) {
            JSONObject operation = (JSONObject) item;
            filters.add((String) operation.get("filter"));
        }
    }

    // Groups cities based on specified filter criteria from operations
    public void groupCities(String operationsFilePath, String dataFilePath) throws IOException, ParseException {
        // Extract functions and filters from operations file
        for (Object item : getOperationsJson(operationsFilePath)) {
           JSONObject operation = (JSONObject) item;
           functions.add((String) operation.get("function"));
       }
        // Extract city names from data file
       for (Object item : getDataJson(dataFilePath)) {
           JSONObject data = (JSONObject) item;
           cities.add((String) data.get("name"));
       }
        // Initialize filteredGroupsList for different filter criteria
       List<List<String>> filteredGroupsList = Arrays.asList(citiesStartingWith, citiesEndingWith, citiesContainsOneChar, citiesContainsMultipleChars);
        // Filter and add cities to their respective lists based on filter criteria
       for (int i = 0; i < filteredGroupsList.size(); i++) {
           int finalI = i;
           List<String> tempList = cities.stream().filter(x -> x.matches(filters.get(finalI))).toList();
           filteredGroupsList.get(i).addAll(tempList);
       }
    }

    // Calculates and returns the average population of cities starting with a certain filter
    public String avgPopulationFunction(String dataFilePath) throws IOException, ParseException {
        for (Object item : getDataJson(dataFilePath)) {
            if (citiesStartingWith.contains(((JSONObject) item).get("name"))) {
                totalPopulation += (Long) ((JSONObject) item).get("population");
            }
        }

        // Calculate and round the average population
        double avgPopulation = (totalPopulation) / (citiesStartingWith.size());
        return roundValue(avgPopulation);
    }

    // Calculates and returns the total area of cities ending with a certain filter
    public String totalAreaFunction(String dataFilePath) throws IOException, ParseException {
        for (Object item : getDataJson(dataFilePath)) {
            if (citiesEndingWith.contains(((JSONObject) item).get("name"))) {
                JSONObject extendedStats = (JSONObject) ((JSONObject) item).get("extendedStatistics");
                totalArea += (Double) extendedStats.get("area");
            }
        }

        // Round and return the total area
        return roundValue(totalArea);
    }

    // Finds and returns the minimum area value from cities containing one character
    public String minValueFromList(String dataFilePath) throws IOException, ParseException {
        for (Object item : getDataJson(dataFilePath)) {
            if (citiesContainsOneChar.contains(((JSONObject) item).get("name"))) {
                JSONObject extendedStats = (JSONObject) ((JSONObject) item).get("extendedStatistics");
                areas.add((Double) extendedStats.get("area"));
            }
        }

        // Sort areas and find the minimum value
        areas.sort(Double::compareTo);
        double minArea = areas.get(0);
        // Round and return the minimum area
        return roundValue(minArea);
    }

    // Finds and returns the maximum population value from cities containing multiple characters
    public String maxValueFromList(String dataFilePath) throws IOException, ParseException {
        for (Object item : getDataJson(dataFilePath)) {
            if (citiesContainsMultipleChars.contains(((JSONObject) item).get("name"))) {
                populations.add((Long) ((JSONObject) item).get("population"));
            }
        }

        // Find the maximum population value
        Long maxValue = populations.stream()
                .mapToLong(Long::longValue)
                .max().orElse(Long.MIN_VALUE);
        Double maxPopulations = maxValue.doubleValue();
        // Round and return the maximum population value
        return roundValue(maxPopulations);
    }

    // Retrieves function names and their corresponding values and stores them in lists
    public void functionsAndValuesToList(String dataFilePath, String operationsFilePath) throws IOException, ParseException {
        for (Object item : getOperationsJson(operationsFilePath)) {
            namesList.add((String) ((JSONObject) item).get("name"));
        }
        valuesList = Arrays.asList(avgPopulationFunction(dataFilePath), totalAreaFunction(dataFilePath), minValueFromList(dataFilePath), maxValueFromList(dataFilePath));
    }

    // Writes a JSONArray to a file in a specific format
    public void writeToFile(File file, JSONArray jsonArray) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            // Manually construct the JSON string with the desired order
            StringBuilder jsonBuilder = new StringBuilder("[");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);
                jsonBuilder.append(" {\n");
                jsonBuilder.append("  \"name\" : \"").append(obj.get("name")).append("\",\n");
                jsonBuilder.append("  \"roundedValue\" : \"").append(obj.get("roundedValue")).append("\"\n");
                jsonBuilder.append("}");
                if (i < jsonArray.size() - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append(" ]\n");

            // Write the manually constructed JSON string to the file
            fileWriter.write(jsonBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Creates and returns a JSONArray based on the stored function names and values
    public JSONArray createJson(){
        // Create a new JSON array
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < valuesList.size(); i++) {
            // Create a new JSON object
            JSONObject obj = new JSONObject();
            obj.put("name", namesList.get(i));
            obj.put("roundedValue", valuesList.get(i));
            jsonArray.add(obj);
        }
        return jsonArray;
    }
}