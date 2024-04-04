import data.Data;
import data.FileSection;
import data.Node;
import data.Vehicle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Parser class for file parsing into correct form.
 */
public class Parser {

    /**
     * Path for the folder parsing from.
     */
    private String path;

    /**
     * List of the parsed and converted data objects to solve.
     */
    private final List<Data> data;

    /**
     * Exact folder object to parse the files from, set by the path.
     */
    private File folder;

    /**
     * Filesection object.
     */
    private FileSection section;

    public Parser() {
        this.path = "";
        this.data = new ArrayList<>();
        this.section = null;
    }

    void addPath(String path) {
        this.path = path;
    }

    void setFolder() {
        this.folder = new File(this.path);
    }

    public FileSection getSection() {
        return section;
    }

    public void setSection(FileSection section) {
        this.section = section;
    }

    /**
     * This method parses all the input files into the correct format and objects for the optimizer algorithm.
     * The files have to be passed in the correct format!
     *
     * @param solomon - if set to true, the Solomon type instances will be parsed, otherwise the Kim ones
     * @return - the data object list containing all the necessary data for the optimizer
     */
    public List<Data> parseInstances(boolean solomon) {
        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            Data data = new Data();
            Float[][] matrix = null;
            int size;
            int rowCount = 0;
            try {
                Scanner scanner = new Scanner(fileEntry);
                int idx = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (idx == 0) {
                        String[] datasetAndName = line.split(":");
                        data.setDataset(datasetAndName[0].strip());
                        data.setInfo(datasetAndName[1].strip());
                        String dataSetSize = datasetAndName[1].strip().split("_")[1];
                        size = getDataSetSize(dataSetSize, solomon);
                        matrix = new Float[size][size];
                        idx++;
                        continue;
                    } else if (line.contains("Nodes")) {
                        setSection(FileSection.NODES);
                        continue;
                    } else if (line.contains("Vehicles")) {
                        setSection(FileSection.VEHICLES);
                        continue;
                    } else if (line.contains("matrix")) {
                        setSection(FileSection.MATRIX);
                        continue;
                    }
                    if (getSection().equals(FileSection.NODES)) {
                        String[] nodeAttributes = line.split(" ");
                        Node node = new Node();
                        node.setId(data.getNodeListSize());
                        node.setCx(Float.parseFloat(nodeAttributes[0]));
                        node.setCy(Float.parseFloat(nodeAttributes[1]));
                        node.setQuantity(Float.parseFloat(nodeAttributes[2]));
                        node.setTimeStart(Float.parseFloat(nodeAttributes[3]));
                        node.setTimeEnd(Float.parseFloat(nodeAttributes[4]));
                        node.setServiceTime(Float.parseFloat(nodeAttributes[5]));
                        if (data.getNodeListSize() == 0) {
                            node.setDepot(true);
                        }
                        if (!node.isDepot() && node.getQuantity() > 1000) {
                            node.setDumpingSite(true);
                        }
                        data.addNode(node);
                    } else if (getSection().equals(FileSection.VEHICLES)) {
                        String[] vehicleAttributes = line.split(" ");
                        Vehicle vehicle = new Vehicle();
                        vehicle.setType(Integer.parseInt(vehicleAttributes[0]));
                        vehicle.setDepartureNode(data.getNodeOnIndex(Integer.parseInt(vehicleAttributes[1])));
                        vehicle.setArrivalNode(data.getNodeOnIndex(Integer.parseInt(vehicleAttributes[2])));
                        vehicle.setMaximumCapacity((int) Float.parseFloat(vehicleAttributes[3]));
                        vehicle.setMaximumNumberOfStopsToVisit((int) Float.parseFloat(vehicleAttributes[4]));
                        vehicle.setId(data.getFleet().size());
                        data.addVehicle(vehicle);
                    } else if (getSection().equals(FileSection.MATRIX)) {
                        String[] matrixRow = line.split(" ");
                        for (int i = 0; i < matrixRow.length; i++) matrix[rowCount][i] = Float.parseFloat(matrixRow[i]);
                        rowCount++;
                    }
                    idx++;
                }
                scanner.close();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            data.setMatrix(matrix);
            this.data.add(data);
        }
        return data;
    }

    private int getDataSetSize(String dataSetSize, boolean solomon) {
        if (dataSetSize.startsWith("0")) dataSetSize.substring(1);
        int size = Integer.parseInt(dataSetSize);
        return solomon ? size + 2 : size;
    }

}
