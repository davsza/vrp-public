import data.*;

import javax.xml.crypto.NodeSetData;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class hold all the heuristics which are used during the optimizing process.
 * All the heuristics are implemented from "An Adaptive Large Neighborhood Search Heuristic for the Pickup and Delivery
 * Problem with Time Windows" by Stefan Ropke, David Pisinger. The heuristics will not contain any substantive
 * information, instead the appropriate chapter in the study.
 */
public class Heuristics {

    /**
     * Random object for random number generation.
     */
    private final Random random;

    /**
     * Solver object for the used methods.
     */
    private final Solver solver;

    /**
     * Constant object for the constants which are used by the heuristics.
     */
    private final Constants CONSTANTS;

    public Heuristics(Solver solver) {
        this.random = new Random();
        this.solver = solver;
        this.CONSTANTS = new Constants();
    }

    /**
     * Sums up the incoming float values for calculation possibility values
     *
     * @param floats - input values
     * @return - returns the sum of the values
     */
    public float sum(float... floats) {
        float sum = 0;
        for (float num : floats) {
            sum += num;
        }
        return sum;
    }

    /**
     * Deleting a random disposal from a random vehicle's route
     *
     * @param data        - data object (graph) to work with
     * @param nodesToSwap - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param logger      - logger object
     */
    private void deleteDisposal(Data data, List<Node> nodesToSwap, Logger logger) {

        LocalTime startTime = LocalTime.now();
        logger.log("deleteDisposal started at: " + startTime);
        long startNanoTime = System.nanoTime();

        List<NodeSwap> nodeSwapList = new ArrayList<>();
        List<Vehicle> feasibleVehicles = new ArrayList<>();
        for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty()).collect(Collectors.toList())) {
            int dumpingSites = 0;
            for (Node node : vehicle.getRoute()) {
                if (node.isDumpingSite()) {
                    dumpingSites++;
                }
            }
            if (dumpingSites > 1) {
                feasibleVehicles.add(vehicle);
            }
        }
        if (feasibleVehicles.size() == 0) {
            return;
        }

        for (Vehicle vehicle : feasibleVehicles) {
            for (int i = 0; i < vehicle.getRoute().size(); i++) {
                Node node = vehicle.getRoute().get(i);
                if (node.isDumpingSite()) {
                    NodeSwap nodeSwap = new NodeSwap(node, vehicle, 0, i, false);
                    nodeSwapList.add(nodeSwap);
                }
            }
        }

        int randomIndex = random.nextInt(nodeSwapList.size());
        NodeSwap nodeSwap = nodeSwapList.get(randomIndex);
        Vehicle vehicle = nodeSwap.getVehicle();
        Node dumpingSite = nodeSwap.getNode();
        int dumpingSiteIndex = nodeSwap.getIndex();

        nodeSwapList = nodeSwapList.stream().filter(nodeSwap1 -> nodeSwap1.getVehicle().equals(vehicle)).collect(Collectors.toList());

        if (vehicle.getRoute().get(dumpingSiteIndex + 1).isDepot()) {
            Node currentNode = dumpingSite;
            vehicle.getRoute().remove(currentNode);
            currentNode = vehicle.getRoute().get(dumpingSiteIndex - 1);
            while (!currentNode.isDumpingSite()) {
                nodesToSwap.add(currentNode);
                vehicle.getRoute().remove(currentNode);
                dumpingSiteIndex--;
                currentNode = vehicle.getRoute().get(dumpingSiteIndex - 1);
            }
        } else {
            int maximumCapacity = vehicle.getMaximumCapacity();
            int startingIndex = nodeSwapList.indexOf(nodeSwap) == 0 ? 1 : nodeSwapList.get(nodeSwapList.indexOf(nodeSwap) - 1).getIndex() + 1;
            float overallQuantity = 0;
            for (int i = startingIndex; i < nodeSwapList.get(nodeSwapList.indexOf(nodeSwap) + 1).getIndex(); i++) {
                Node node = vehicle.getRoute().get(i);
                if (!node.isDumpingSite()) {
                    overallQuantity += node.getQuantity();
                }
            }
            vehicle.getRoute().remove(dumpingSite);
            int numberOfNodesRemoved = 0;
            while (overallQuantity > maximumCapacity) {
                Node currentNode = vehicle.getRoute().get(dumpingSiteIndex - 1);
                overallQuantity -= currentNode.getQuantity();
                nodesToSwap.add(currentNode);
                vehicle.getRoute().remove(currentNode);
                numberOfNodesRemoved++;
                if (numberOfNodesRemoved % 2 == 0) {
                    dumpingSiteIndex--;
                }
            }
        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("deleteDisposal ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");
    }

    /**
     * Swapping to random disposal's places in the route of a random vehicle
     *
     * @param data        - data object (graph) to work with
     * @param nodesToSwap - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param logger      - logger object
     */
    private void swapDisposal(Data data, List<Node> nodesToSwap, Logger logger) {
        LocalTime startTime = LocalTime.now();
        logger.log("swapDisposal started at: " + startTime);
        long startNanoTime = System.nanoTime();

        List<NodeSwap> nodeSwapList = new ArrayList<>();
        int numberOfDisposalSites = 0;
        for (Node node : data.getNodeList()) if (node.isDumpingSite()) numberOfDisposalSites++;
        if (numberOfDisposalSites == 1) {
            return;
        }
        List<Vehicle> feasibleVehicles = new ArrayList<>();
        for (Vehicle vehicle : data.getFleet()) if (!vehicle.isEmpty()) feasibleVehicles.add(vehicle);
        for (Vehicle vehicle : feasibleVehicles) {
            for (int i = 0; i < vehicle.getRoute().size(); i++) {
                Node node = vehicle.getRoute().get(i);
                if (node.isDumpingSite()) {
                    NodeSwap nodeSwap = new NodeSwap(node, vehicle, 0, i, false);
                    nodeSwapList.add(nodeSwap);
                }
            }
        }

        int randomIndex = random.nextInt(nodeSwapList.size());
        NodeSwap nodeSwap = nodeSwapList.get(randomIndex);
        Vehicle vehicle = nodeSwap.getVehicle();
        Node dumpingSite = nodeSwap.getNode();
        int dumpingSiteIndex = nodeSwap.getIndex();

        List<Node> disposalSitesToSwapWith = new ArrayList<>();
        for (Node disposalSite : data.getNodeList())
            if (disposalSite.isDumpingSite() && (int) disposalSite.getId() != dumpingSite.getId())
                disposalSitesToSwapWith.add(disposalSite);
        randomIndex = random.nextInt(disposalSitesToSwapWith.size());
        Node disposalSiteToSwapWith = disposalSitesToSwapWith.get(randomIndex);

        vehicle.getRoute().set(dumpingSiteIndex, disposalSiteToSwapWith);
        float disposalTimeEnd = disposalSiteToSwapWith.getTimeEnd();
        float arrivalTimeAtPreviousNode, arrivalTimeAtNextNode, travelDistance, serviceTime, arrivalTimeAtDisposalSite;
        Node previousNode, nextNode;

        while (true) {
            previousNode = vehicle.getRoute().get(dumpingSiteIndex - 1);
            arrivalTimeAtPreviousNode = vehicle.getArrivalTimes().get(dumpingSiteIndex - 1);
            serviceTime = previousNode.getServiceTime();
            travelDistance = data.getDistanceBetweenNode(previousNode, disposalSiteToSwapWith);
            arrivalTimeAtDisposalSite = arrivalTimeAtPreviousNode + serviceTime + travelDistance;
            if (arrivalTimeAtDisposalSite <= disposalTimeEnd) {
                vehicle.getArrivalTimes().set(dumpingSiteIndex, Math.max(arrivalTimeAtDisposalSite, disposalSiteToSwapWith.getTimeStart()));
                solver.updateArrivalTimes(data);
                break;
            }
            nodesToSwap.add(previousNode);
            vehicle.getArrivalTimes().remove(dumpingSiteIndex - 1);
            vehicle.getRoute().remove(previousNode);
            dumpingSiteIndex--;
        }
        for (int i = dumpingSiteIndex + 1; i < vehicle.getRoute().size() - 1; i++) {
            nextNode = vehicle.getRoute().get(i);
            serviceTime = disposalSiteToSwapWith.getServiceTime();
            travelDistance = data.getDistanceBetweenNode(disposalSiteToSwapWith, nextNode);
            arrivalTimeAtNextNode = arrivalTimeAtDisposalSite + serviceTime + travelDistance;
            if (arrivalTimeAtNextNode <= nextNode.getTimeEnd()) {
                vehicle.getArrivalTimes().set(i, Math.max(arrivalTimeAtNextNode, nextNode.getTimeStart()));
                solver.updateArrivalTimes(data);
                continue;
            }
            nodesToSwap.add(nextNode);
            vehicle.getArrivalTimes().remove(i);
            vehicle.getRoute().remove(nextNode);
            i--;
        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("swapDisposal ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");
    }

    /**
     * Inserting a random disposal from a random vehicle's route
     *
     * @param data        - data object (graph) to work with
     * @param nodesToSwap - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param logger      - logger object
     */
    private void insertDisposal(Data data, List<Node> nodesToSwap, Logger logger) {
        LocalTime startTime = LocalTime.now();
        logger.log("inertDisposal started at: " + startTime);
        long startNanoTime = System.nanoTime();

        List<Vehicle> feasibleVehicles = new ArrayList<>();
        for (Vehicle vehicle : data.getFleet())
            if (!vehicle.isEmpty() && !vehicle.isPenaltyVehicle()) feasibleVehicles.add(vehicle);
        int randomIndex = random.nextInt(feasibleVehicles.size());
        Vehicle vehicleToInsertInto = feasibleVehicles.get(randomIndex);

        List<Node> disposalSitesToSwapWith = new ArrayList<>();
        for (Node disposalSite : data.getNodeList())
            if (disposalSite.isDumpingSite())
                disposalSitesToSwapWith.add(disposalSite);
        randomIndex = random.nextInt(disposalSitesToSwapWith.size());
        Node disposalSiteToInsert = disposalSitesToSwapWith.get(randomIndex);

        int index = vehicleToInsertInto.getRoute().size(); // lista merete, ezert indexbound lenne ha erre hivatkozunk de mivel beszurjuk index - 1-re a nodeot ezert beszuras utan jo lesz
        vehicleToInsertInto.getRoute().add(vehicleToInsertInto.getRoute().size() - 1, disposalSiteToInsert);

        Node currentNode;
        vehicleToInsertInto.getArrivalTimes().add(index, (float) 0);
        float arriveTimeAtPreviousNode, serviceTimeAtPreviousNode, travelDistance;
        while (true) {
            currentNode = vehicleToInsertInto.getRoute().get(index - 1);
            arriveTimeAtPreviousNode = vehicleToInsertInto.getArrivalTimes().get(index - 1);
            serviceTimeAtPreviousNode = currentNode.getServiceTime();
            travelDistance = data.getDistanceBetweenNode(currentNode, disposalSiteToInsert);
            if (arriveTimeAtPreviousNode + serviceTimeAtPreviousNode + travelDistance <= disposalSiteToInsert.getTimeEnd()) {
                vehicleToInsertInto.getArrivalTimes().set(index, arriveTimeAtPreviousNode + serviceTimeAtPreviousNode + travelDistance);
                break;
            }
            vehicleToInsertInto.getArrivalTimes().remove(index - 1);
            nodesToSwap.add(currentNode);
            vehicleToInsertInto.getRoute().remove(currentNode);
            index--;
        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("insertDisposal ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");
    }

    /**
     * See section 3.1.1.
     *
     * @param data        - data object (graph) to work with
     * @param p           - parameter
     * @param nodesToSwap - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param phi         - parameter
     * @param chi         - parameter
     * @param psi         - parameter
     * @param P           - parameter
     * @param logger      - logger object
     */
    private void relatedRemoval(Data data, int p, List<Node> nodesToSwap,
                                float phi, float chi, float psi, int P, Logger logger) {

        LocalTime startTime = LocalTime.now();
        logger.log("relatedRemoval started at: " + startTime);
        long startNanoTime = System.nanoTime();

        randomRemoval(data, 1, nodesToSwap, logger);

        int randomIndex;
        List<NodeSwap> nodeSwapList;
        NodeSwap bestNodeSwap, currentNodeSwap;

        while (nodesToSwap.size() < p) {
            nodeSwapList = new ArrayList<>();
            randomIndex = nodesToSwap.size() == 0 ? 0 : random.nextInt(nodesToSwap.size());
            Node nodeToCompare = nodesToSwap.get(randomIndex);
            data.calculateVisitingTime();

            List<Vehicle> feasibleVehicles = new ArrayList<>();
            for (Vehicle vehicle : data.getFleet()) if (!vehicle.isEmpty()) feasibleVehicles.add(vehicle);
            for (Vehicle vehicle : feasibleVehicles) {
                List<Node> feasibleNodes = new ArrayList<>();
                for (Node node : vehicle.getRoute())
                    if (!node.isDepot() && !node.isDumpingSite()) feasibleNodes.add(node);
                for (Node node : feasibleNodes) {
                    currentNodeSwap = new NodeSwap();
                    float relatedness = phi * data.getDistanceBetweenNode(nodeToCompare, node)
                            + chi * Math.abs(nodeToCompare.getVisitedAt() - node.getVisitedAt())
                            + psi * Math.abs(nodeToCompare.getQuantity() - node.getQuantity());
                    currentNodeSwap.setNode(node);
                    currentNodeSwap.setValue(relatedness);
                    currentNodeSwap.setVehicle(vehicle);

                    nodeSwapList.add(currentNodeSwap);
                }
            }

            nodeSwapList.sort(new Comparator<NodeSwap>() {
                @Override
                public int compare(NodeSwap o1, NodeSwap o2) {
                    return (Float.compare(o1.getValue(), o2.getValue()));
                }
            });

            double y = random.nextDouble();
            int index = (int) (Math.pow(y, P) * nodeSwapList.size());

            bestNodeSwap = nodeSwapList.get(index);
            nodesToSwap.add(bestNodeSwap.getNode());
            bestNodeSwap.getVehicle().getRoute().remove(bestNodeSwap.getNode());
        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("relatedRemoval ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");

    }

    /**
     * See section 3.1.2.
     *
     * @param data        - data object (graph) to work with
     * @param p           - parameter
     * @param nodesToSwap - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param logger      - logger object
     */
    private void randomRemoval(Data data, int p, List<Node> nodesToSwap, Logger logger) {
        LocalTime startTime = LocalTime.now();
        logger.log("randomRemoval started at: " + startTime);
        long startNanoTime = System.nanoTime();

        boolean found;
        int numberOfFeasibleNodesToRemove, index;
        List<Node> feasibleNodesToRemove = new ArrayList<>();

        while (nodesToSwap.size() < p) {
            for (Node node : data.getNodeList())
                if (!node.isDepot() && !node.isDumpingSite()) feasibleNodesToRemove.add(node);
            numberOfFeasibleNodesToRemove = feasibleNodesToRemove.size();
            index = random.nextInt(numberOfFeasibleNodesToRemove);
            Node nodeToRemove = feasibleNodesToRemove.get(index);
            for (Vehicle vehicle : data.getFleet()) {
                found = false;
                for (int i = 0; i < vehicle.getRoute().size(); i++) {
                    Node node = vehicle.getRoute().get(i);
                    if ((int) node.getId() == nodeToRemove.getId()) {
                        nodesToSwap.add(node);
                        vehicle.getRoute().remove(node);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("randomRemoval ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");
    }

    /**
     * See section 3.1.3.
     *
     * @param data        - data object (graph) to work with
     * @param p           - parameter
     * @param nodesToSwap - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param p_worst     - parameter
     * @param logger      - logger object
     */
    private void worstRemoval(Data data, int p, List<Node> nodesToSwap, int p_worst, Logger logger) {
        LocalTime startTime = LocalTime.now();
        logger.log("worstRemoval started at: " + startTime);
        long startNanoTime = System.nanoTime();

        float currentValue, initialValue = solver.getDataValue(data);
        int indexToRemoveFrom;
        List<NodeSwap> nodeSwapList = new ArrayList<>();
        Node nodeToRemove;
        NodeSwap bestNodeSwap, currentNodeSwap;
        Vehicle vehicleToRemoveFrom;

        for (Vehicle vehicle : data.getFleet()) {
            if (vehicle.isEmpty() || vehicle.isPenaltyVehicle()) {
                continue;
            }
            for (int i = 0; i < vehicle.getRoute().size(); i++) {
                Node node = vehicle.getRoute().get(i);
                currentNodeSwap = new NodeSwap();
                if (!node.isDepot() && !node.isDumpingSite()) {
                    if (vehicle.isPenaltyVehicle()) {
                        currentValue = initialValue - 2 * data.getMaximumTravelDistance();
                        currentNodeSwap.setNode(node);
                        currentNodeSwap.setValue(currentValue);
                        currentNodeSwap.setIndex(i);
                        currentNodeSwap.setVehicle(vehicle);

                        nodeSwapList.add(currentNodeSwap);
                    } else {
                        Node previousNode = vehicle.getRoute().get(i - 1);
                        Node nextNode = vehicle.getRoute().get(i + 1);

                        float distanceBetweenNodesBeforeRemoval = data.getDistanceBetweenNode(previousNode, node) + data.getDistanceBetweenNode(node, nextNode);

                        vehicle.getRoute().remove(node);

                        float distanceBetweenNodesAfterRemoval = data.getDistanceBetweenNode(previousNode, nextNode);

                        currentValue = initialValue - distanceBetweenNodesBeforeRemoval + distanceBetweenNodesAfterRemoval;

                        currentNodeSwap.setNode(node);
                        currentNodeSwap.setValue(currentValue);
                        currentNodeSwap.setIndex(i);
                        currentNodeSwap.setVehicle(vehicle);

                        nodeSwapList.add(currentNodeSwap);

                        vehicle.getRoute().add(i, node);
                    }
                }
            }
        }

        while (nodesToSwap.size() < p) {

            nodeSwapList.sort(new Comparator<NodeSwap>() {
                @Override
                public int compare(NodeSwap o1, NodeSwap o2) {
                    return (Float.compare(o2.getValue(), o1.getValue()));
                }
            });

            double y = random.nextDouble();
            int index = (int) (Math.pow(y, p_worst) * nodeSwapList.size());

            bestNodeSwap = nodeSwapList.get(index);
            vehicleToRemoveFrom = bestNodeSwap.getVehicle();
            nodeToRemove = bestNodeSwap.getNode();
            indexToRemoveFrom = bestNodeSwap.getIndex();
            vehicleToRemoveFrom.getRoute().remove(indexToRemoveFrom);
            nodeSwapList.remove(bestNodeSwap);
            nodesToSwap.add(nodeToRemove);

            Node previousNode = vehicleToRemoveFrom.getRoute().get(indexToRemoveFrom - 1);
            if (!previousNode.isDepot() && !previousNode.isDumpingSite()) {
                float distanceBeforeRemoval_ = data.getDistanceBetweenNode(previousNode, nodeToRemove);
                float distanceAfterRemoval_ = data.getDistanceBetweenNode(previousNode, vehicleToRemoveFrom.getRoute().get(indexToRemoveFrom));
                currentValue = initialValue - distanceBeforeRemoval_ + distanceAfterRemoval_;

                currentNodeSwap = nodeSwapList.stream().filter(nodeSwap -> nodeSwap.getNode().getId() == (int) previousNode.getId()).findFirst().get();
                if (currentValue > currentNodeSwap.getValue()) {
                    currentNodeSwap.setValue(currentValue);
                    currentNodeSwap.setVehicle(vehicleToRemoveFrom);
                    currentNodeSwap.setIndex(indexToRemoveFrom - 1);
                }
            }

            for (NodeSwap nodeSwap : nodeSwapList) {
                if (nodeSwap.getVehicle().equals(vehicleToRemoveFrom) && nodeSwap.getIndex() > indexToRemoveFrom) {
                    nodeSwap.setIndex(nodeSwap.getIndex() - 1);
                }
            }

            Node nextNode = vehicleToRemoveFrom.getRoute().get(indexToRemoveFrom);
            if (!nextNode.isDepot() && !nextNode.isDumpingSite()) {
                float distanceBeforeRemoval_ = data.getDistanceBetweenNode(nodeToRemove, nextNode);
                float distanceAfterRemoval_ = data.getDistanceBetweenNode(vehicleToRemoveFrom.getRoute().get(indexToRemoveFrom - 1), nextNode);
                currentValue = initialValue - distanceBeforeRemoval_ + distanceAfterRemoval_;

                currentNodeSwap = nodeSwapList.stream().filter(nodeSwap -> nodeSwap.getNode().getId() == (int) nextNode.getId()).findFirst().get();
                if (currentValue > currentNodeSwap.getValue()) {
                    currentNodeSwap.setValue(currentValue);
                    currentNodeSwap.setVehicle(vehicleToRemoveFrom);
                    currentNodeSwap.setIndex(indexToRemoveFrom);
                }
            }
        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("worstRemoval ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");
    }

    /**
     * See section 3.3.
     *
     * @param data             - data object (graph) to work with
     * @param p                - parameter
     * @param nodesToSwap      - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param heuristicWeights - the current weights of the heuristics based on the output of the past period
     * @param logger           - logger object
     */
    public void destroyNodes(Data data, int p, List<Node> nodesToSwap, HeuristicWeights heuristicWeights, Logger logger) {
        LocalTime startTime = LocalTime.now();
        long destroyStart = System.nanoTime();
        logger.log("Destroying nodes started at: " + startTime);
        logger.log("Removing " + p + " nodes");

        float sumOf = heuristicWeights.sumOfDestroy();
        float worstWeight = heuristicWeights.getWorstRemovalWeight() / sumOf;
        float randomWeight = heuristicWeights.getRandomRemovalWeight() / sumOf;
        float relatedWeight = heuristicWeights.getRelatedRemovalWeight() / sumOf;
        float deleteWeight = heuristicWeights.getDeleteDisposalWeight() / sumOf;
        float swapWeight = heuristicWeights.getSwapDisposalWeight() / sumOf;
        float insertWeight = heuristicWeights.getInsertDisposalWeight() / sumOf;
        double randomValue = random.nextDouble();

        if (randomValue < worstWeight) {
            heuristicWeights.setCurrentRemove(1);
            logger.log("Destroy method: worstRemoval");
            worstRemoval(data, p, nodesToSwap, CONSTANTS.getP_WORST(), logger);
        } else if (randomValue < worstWeight + randomWeight) {
            heuristicWeights.setCurrentRemove(2);
            logger.log("Destroy method: randomRemoval");
            randomRemoval(data, p, nodesToSwap, logger);
        } else if (randomValue < sum(worstWeight, randomWeight, relatedWeight)) {
            heuristicWeights.setCurrentRemove(3);
            logger.log("Destroy method: relatedRemoval");
            relatedRemoval(data, p, nodesToSwap, CONSTANTS.getPHI(), CONSTANTS.getCHI(), CONSTANTS.getPSI(), CONSTANTS.getP(), logger);
        } else if (randomValue < sum(worstWeight, randomWeight, relatedWeight, deleteWeight)) {
            heuristicWeights.setCurrentRemove(4);
            logger.log("Destroy method: deleteDisposal");
            deleteDisposal(data, nodesToSwap, logger);
        } else if (randomValue < sum(worstWeight, randomWeight, relatedWeight, deleteWeight, swapWeight)) {
            heuristicWeights.setCurrentRemove(5);
            logger.log("Destroy method: swapDisposal");
            swapDisposal(data, nodesToSwap, logger);
        } else if (randomValue < sum(worstWeight, randomWeight, relatedWeight, deleteWeight, swapWeight, insertWeight)) {
            heuristicWeights.setCurrentRemove(6);
            logger.log("Destroy method: insertDisposal");
            insertDisposal(data, nodesToSwap, logger);
        }

        LocalTime endTime = LocalTime.now();
        long destroyEnd = System.nanoTime();
        logger.log("Destroying nodes ended at: " + endTime + ", took " + ((destroyEnd - destroyStart) * 1e-9) + " seconds");
    }

    /**
     * See section 3.2.2.
     *
     * @param data        - data object (graph) to work with
     * @param nodesToSwap - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param p           - parameter
     * @param logger      - logger object
     */
    private void regretInsert(Data data, List<Node> nodesToSwap, int p, Logger logger) {

        LocalTime startTime = LocalTime.now();
        long startNanoTime = System.nanoTime();
        logger.log("regretInsert_" + (p == 2 || p == 3 ? p : "k") + " started at: " + startTime);

        float bestDiff, currentValue, diff, initialValue = solver.getDataValue(data);
        int indexToInsert;
        long totalInsertValidityCheck = 0, startNano, endNano;
        List<NodeSwap> nodeSwapList = new ArrayList<>();
        Node nodeToInsert;
        NodeSwap currentNodeSwap;
        Vehicle vehicleToInsertInto, penaltyVehicle = data.getPenaltyVehicle();

        for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty() && !vehicle.isPenaltyVehicle()).collect(Collectors.toList())) {
            boolean valid = solver.checkForValidity(data, vehicle);
            if (!valid) {
                System.out.println("Invalid vehicle at the start of regret");
            }
        }

        for (Node nodesToInsert : nodesToSwap) {
            NodeSwap customerNodeSwap = new NodeSwap(nodesToInsert);
            boolean checkedEmptyVehicle = false;
            for (Vehicle vehicle : data.getFleet()) {
                bestDiff = Float.MAX_VALUE;
                if (vehicle.isPenaltyVehicle()) {
                    currentNodeSwap = new NodeSwap(nodesToInsert);
                    diff = 2 * data.getMaximumTravelDistance();
                    currentNodeSwap.setVehicle(vehicle);
                    currentNodeSwap.setIndex(vehicle.getRoute().size());
                    currentNodeSwap.setFoundVehicleForNodeToInsert(true);
                    currentNodeSwap.setNode(nodesToInsert);
                    currentNodeSwap.setValue(diff);
                    customerNodeSwap.getRegretNodeSwapList().add(currentNodeSwap);
                    continue;
                }
                if (checkedEmptyVehicle) continue;
                checkedEmptyVehicle = vehicle.isEmpty();
                int vehicleRouteSize = vehicle.getRoute().size();
                currentNodeSwap = new NodeSwap(nodesToInsert);
                for (int i = 1; i < vehicleRouteSize - 1; i++) {
                    float previousNodeArrivalTime = vehicle.getArrivalTimes().get(i - 1);
                    Node previousNode = vehicle.getRoute().get(i - 1);
                    Node nextNode = vehicle.getRoute().get(i);
                    float serviceTimeAtPreviousNode = previousNode.getServiceTime();
                    float travelDistance = data.getDistanceBetweenNode(previousNode, nodesToInsert);
                    float arrivalTimeAtNode = previousNodeArrivalTime + serviceTimeAtPreviousNode + travelDistance;

                    if (arrivalTimeAtNode > nodesToInsert.getTimeEnd()) {
                        break;
                    }

                    if (nodesToInsert.getTimeStart() > nextNode.getTimeEnd()) {
                        continue;
                    }

                    float distanceBetweenNodesToInsert = data.getDistanceBetweenNode(previousNode, nextNode);

                    vehicle.getRoute().add(i, nodesToInsert);

                    float distanceBetweenNodesAfterInsert = data.getDistanceBetweenNode(previousNode, nodesToInsert) + data.getDistanceBetweenNode(nodesToInsert, nextNode);
                    startNano = System.nanoTime();
                    boolean valid = solver.checkForValidity(data, vehicle);
                    endNano = System.nanoTime();
                    totalInsertValidityCheck += (endNano - startNano);

                    if (valid) {
                        currentValue = initialValue - distanceBetweenNodesToInsert + distanceBetweenNodesAfterInsert;
                        diff = currentValue - initialValue;
                        if (diff < bestDiff) {
                            bestDiff = diff;
                            currentNodeSwap.setVehicle(vehicle);
                            currentNodeSwap.setIndex(i);
                            currentNodeSwap.setFoundVehicleForNodeToInsert(true);
                            currentNodeSwap.setNode(nodesToInsert);
                            currentNodeSwap.setValue(diff);
                        }
                    }
                    vehicle.getRoute().remove(nodesToInsert);
                }
                if (currentNodeSwap.getVehicle() != null) {
                    customerNodeSwap.getRegretNodeSwapList().add(currentNodeSwap);
                }
            }
            customerNodeSwap.sortRegretList();
            nodeSwapList.add(customerNodeSwap);
        }

        for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty() && !vehicle.isPenaltyVehicle()).collect(Collectors.toList())) {
            boolean valid = solver.checkForValidity(data, vehicle);
            if (!valid) {
                System.out.println("Invalid vehicle after regret values calculated");
            }
        }

        while (nodesToSwap.size() > 0) {

            nodeSwapList.sort(new Comparator<NodeSwap>() {
                @Override
                public int compare(NodeSwap o1, NodeSwap o2) {
                    return (Float.compare(o1.getNumberOfFeasibleVehiclesToInsertInto(), o2.getNumberOfFeasibleVehiclesToInsertInto()));
                }
            });
            int leastFeasibleVehicleInsert = nodeSwapList.get(0).getNumberOfFeasibleVehiclesToInsertInto();

            NodeSwap bestNodeSwap = new NodeSwap();

            if (leastFeasibleVehicleInsert < p) {
                List<NodeSwap> feasibleNodeSwaps = nodeSwapList
                        .stream()
                        .filter(nodeSwap -> nodeSwap.getNumberOfFeasibleVehiclesToInsertInto() == leastFeasibleVehicleInsert)
                        .collect(Collectors.toList());
                float worst = 0;
                float bestDataValue = Float.MAX_VALUE;
                for (NodeSwap nodeSwap : feasibleNodeSwaps) {
                    float bestValue = nodeSwap.getRegretNodeSwapList().get(0).getValue();
                    diff = nodeSwap.getRegretSum(leastFeasibleVehicleInsert);
                    if (diff > worst) {
                        worst = diff;
                        bestNodeSwap = nodeSwap;
                        bestDataValue = bestValue;
                    } else if (diff == worst && nodeSwap.getRegretNodeSwapList().get(0).getValue() < bestDataValue) {
                        worst = diff;
                        bestNodeSwap = nodeSwap;
                        bestDataValue = bestValue;
                    }
                }
            } else {
                float worst = 0;
                float bestDataValue = Float.MAX_VALUE;
                for (NodeSwap nodeSwap : nodeSwapList) {
                    float bestValue = nodeSwap.getRegretNodeSwapList().get(0).getValue();
                    diff = nodeSwap.getRegretSum(p);
                    if (diff > worst) {
                        worst = diff;
                        bestNodeSwap = nodeSwap;
                        bestDataValue = bestValue;
                    } else if (diff == worst && nodeSwap.getRegretNodeSwapList().get(0).getValue() < bestDataValue) {
                        worst = diff;
                        bestNodeSwap = nodeSwap;
                        bestDataValue = bestValue;
                    }
                }
            }

            vehicleToInsertInto = bestNodeSwap.getRegretNodeSwapList().get(0).getVehicle();
            nodeToInsert = bestNodeSwap.getRegretNodeSwapList().get(0).getNode();
            indexToInsert = bestNodeSwap.getRegretNodeSwapList().get(0).getIndex();

            if (bestNodeSwap.getRegretNodeSwapList().get(0).isFoundVehicleForNodeToInsert()) {
                vehicleToInsertInto.getRoute().add(indexToInsert, nodeToInsert);
                solver.updateArrivalTimesForVehicle(vehicleToInsertInto, data);
            } else {
                penaltyVehicle.getRoute().add(indexToInsert, nodeToInsert);
            }
            nodesToSwap.remove(nodeToInsert);
            nodeSwapList.remove(bestNodeSwap);

            for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty() && !vehicle.isPenaltyVehicle()).collect(Collectors.toList())) {
                boolean valid = solver.checkForValidity(data, vehicle);
                if (!valid) {
                    System.out.println("Invalid vehicle after regret insert");
                }
            }

            initialValue += bestNodeSwap.getRegretNodeSwapList().get(0).getValue();

            for (NodeSwap nodeSwap : nodeSwapList) {

                if (vehicleToInsertInto.isPenaltyVehicle()) {
                    break;
                }

                Vehicle finalVehicleToInsertInto = vehicleToInsertInto;
                List<NodeSwap> nodeSwapsWithSameVehicleList = nodeSwap.getRegretNodeSwapList()
                        .stream()
                        .filter(nodeSwap1 -> nodeSwap1.getVehicle().equals(finalVehicleToInsertInto))
                        .collect(Collectors.toList());

                if (nodeSwapsWithSameVehicleList.size() == 0) continue;

                NodeSwap selectedNodeSwap = nodeSwapsWithSameVehicleList.get(0);
                Node node = nodeSwap.getNode();

                bestDiff = Float.MAX_VALUE;
                selectedNodeSwap.setFoundVehicleForNodeToInsert(false);
                for (int i = 1; i < vehicleToInsertInto.getRoute().size() - 1; i++) {

                    float previousNodeArrivalTime = vehicleToInsertInto.getArrivalTimes().get(i - 1);
                    Node previousNode = vehicleToInsertInto.getRoute().get(i - 1);
                    Node nextNode = vehicleToInsertInto.getRoute().get(i);
                    float serviceTimeAtPreviousNode = previousNode.getServiceTime();
                    float travelDistance = data.getDistanceBetweenNode(previousNode, node);
                    float arrivalTimeAtNode = previousNodeArrivalTime + serviceTimeAtPreviousNode + travelDistance;

                    if (arrivalTimeAtNode > node.getTimeEnd()) {
                        break;
                    }

                    if (node.getTimeStart() > nextNode.getTimeEnd()) {
                        continue;
                    }

                    float distanceBetweenNodesToInsert = data.getDistanceBetweenNode(previousNode, nextNode);

                    vehicleToInsertInto.getRoute().add(i, node);

                    float distanceBetweenNodesAfterInsert = data.getDistanceBetweenNode(previousNode, node) + data.getDistanceBetweenNode(node, nextNode);

                    boolean valid = solver.checkForValidity(data, vehicleToInsertInto);

                    if (valid) {
                        currentValue = initialValue - distanceBetweenNodesToInsert + distanceBetweenNodesAfterInsert;
                        diff = currentValue - initialValue;
                        if (diff < bestDiff) {
                            bestDiff = diff;
                            selectedNodeSwap.setIndex(i);
                            selectedNodeSwap.setFoundVehicleForNodeToInsert(true);
                            selectedNodeSwap.setValue(diff);
                        }
                    }
                    vehicleToInsertInto.getRoute().remove(node);
                }

                if (!selectedNodeSwap.isFoundVehicleForNodeToInsert()) {
                    nodeSwap.getRegretNodeSwapList().remove(selectedNodeSwap);
                }

                nodeSwap.sortRegretList();
            }

            for (Vehicle vehicle2 : data.getFleet().stream().filter(vehicle3 -> !vehicle3.isEmpty() && !vehicle3.isPenaltyVehicle()).collect(Collectors.toList())) {
                boolean valid2 = solver.checkForValidity(data, vehicle2);
                if (!valid2) {
                    System.out.println("Invalid vehicle after regret values recalculated");
                }
            }

        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("regretInsert ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");
        logger.log("Validating the data took " + (totalInsertValidityCheck * 1e-9) + " seconds");

    }

    /**
     * See section 3.2.1.
     *
     * @param data        - data object (graph) to work with
     * @param nodesToSwap all the nodes from the graph which had been deleted by the destroyNodes method
     * @param logger      - logger object
     */
    private void greedyInsert(Data data, List<Node> nodesToSwap, Logger logger) {

        LocalTime startTime = LocalTime.now();
        logger.log("greedyInsert started at: " + startTime);
        long startNanoTime = System.nanoTime();

        float bestDiff, currentValue, diff, initialValue = solver.getDataValue(data);
        int indexToInsert;
        long totalInsertValidityCheck = 0, endNano, startNano;
        List<NodeSwap> nodeSwapList = new ArrayList<>();
        Node nodeToInsert;
        NodeSwap currentNodeSwap = null, bestNodeSwap;
        Vehicle vehicleToInsertInto, penaltyVehicle = data.getPenaltyVehicle();

        for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty() && !vehicle.isPenaltyVehicle()).collect(Collectors.toList())) {
            boolean valid = solver.checkForValidity(data, vehicle);
            if (!valid) {
                System.out.println("Invalid vehicle greedy elott");
            }
        }

        for (Node nodesToInsert : nodesToSwap) {
            bestDiff = Float.MAX_VALUE;
            boolean checkedEmptyVehicle = false;
            for (Vehicle vehicle : data.getFleet()) {
                if (vehicle.isPenaltyVehicle()) {
                    diff = 2 * data.getMaximumTravelDistance();
                    if (diff < bestDiff) {
                        bestDiff = diff;
                        currentNodeSwap = new NodeSwap(nodesToInsert, vehicle, diff, vehicle.getRoute().size(), true);
                    }
                    continue;
                }
                if (checkedEmptyVehicle && vehicle.isEmpty()) continue;
                checkedEmptyVehicle = vehicle.isEmpty();
                for (int i = 1; i < vehicle.getRoute().size() - 1; i++) {
                    float previousNodeArrivalTime = vehicle.getArrivalTimes().get(i - 1);
                    Node previousNode = vehicle.getRoute().get(i - 1);
                    Node nextNode = vehicle.getRoute().get(i);
                    float serviceTimeAtPreviousNode = previousNode.getServiceTime();
                    float travelDistance = data.getDistanceBetweenNode(previousNode, nodesToInsert);
                    float arrivalTimeAtNode = previousNodeArrivalTime + serviceTimeAtPreviousNode + travelDistance;
                    if (arrivalTimeAtNode > nodesToInsert.getTimeEnd()) {
                        break;
                    }
                    if (nodesToInsert.getTimeStart() > nextNode.getTimeEnd()) {
                        continue;
                    }
                    float distanceBetweenNodesToInsert = data.getDistanceBetweenNode(previousNode, nextNode);
                    vehicle.getRoute().add(i, nodesToInsert);
                    float distanceBetweenNodesAfterInsert = data.getDistanceBetweenNode(previousNode, nodesToInsert) + data.getDistanceBetweenNode(nodesToInsert, nextNode);
                    startNano = System.nanoTime();

                    boolean validSolution = solver.checkForValidity(data, vehicle);
                    endNano = System.nanoTime();
                    totalInsertValidityCheck += (endNano - startNano);
                    if (validSolution) {
                        currentValue = initialValue - distanceBetweenNodesToInsert + distanceBetweenNodesAfterInsert;
                        diff = currentValue - initialValue;
                        if (diff < bestDiff) {
                            bestDiff = diff;
                            currentNodeSwap = new NodeSwap(nodesToInsert, vehicle, diff, i, true);
                        }
                    }
                    vehicle.getRoute().remove(i);
                }
            }
            nodeSwapList.add(currentNodeSwap);
        }

        for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty() && !vehicle.isPenaltyVehicle()).collect(Collectors.toList())) {
            boolean valid = solver.checkForValidity(data, vehicle);
            if (!valid) {
                System.out.println("Invalid vehicle greedy ertekeket kiszamolasa utan");
            }
        }

        while (nodesToSwap.size() > 0) {

            nodeSwapList.sort(new Comparator<NodeSwap>() {
                @Override
                public int compare(NodeSwap o1, NodeSwap o2) {
                    return (Float.compare(o1.getValue(), o2.getValue()));
                }
            });

            bestNodeSwap = nodeSwapList.get(0);
            vehicleToInsertInto = bestNodeSwap.getVehicle();
            nodeToInsert = bestNodeSwap.getNode();
            indexToInsert = bestNodeSwap.getIndex();

            if (bestNodeSwap.isFoundVehicleForNodeToInsert()) {
                vehicleToInsertInto.getRoute().add(indexToInsert, nodeToInsert);
                solver.updateArrivalTimesForVehicle(vehicleToInsertInto, data);
            } else {
                penaltyVehicle.getRoute().add(indexToInsert, nodeToInsert);
            }
            nodesToSwap.remove(nodeToInsert);
            nodeSwapList.remove(0);

            for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty() && !vehicle.isPenaltyVehicle()).collect(Collectors.toList())) {
                boolean valid = solver.checkForValidity(data, vehicle);
                if (!valid) {
                    System.out.println("Invalid vehicle greedy beszuras utan");
                }
            }

            initialValue += bestNodeSwap.getValue();

            for (NodeSwap nodeSwap : nodeSwapList) {
                if (vehicleToInsertInto.isPenaltyVehicle()) {
                    break;
                }
                boolean foundBetterValue = false;
                if (nodeSwap.getVehicle().equals(vehicleToInsertInto)) {
                    Node node = nodeSwap.getNode();
                    bestDiff = Float.MAX_VALUE;
                    boolean checkedEmptyVehicle = false;
                    for (Vehicle vehicle : data.getFleet()) {
                        if (vehicle.isPenaltyVehicle()) {
                            diff = 2 * data.getMaximumTravelDistance();
                            if (diff < bestDiff) {
                                bestDiff = diff;
                                currentNodeSwap = new NodeSwap(node, vehicle, diff, vehicle.getRoute().size(), true);
                            }
                            continue;
                        }

                        if (checkedEmptyVehicle && vehicle.isEmpty()) continue;
                        checkedEmptyVehicle = vehicle.isEmpty();

                        for (int i = 1; i < vehicle.getRoute().size() - 1; i++) {
                            float previousNodeArrivalTime = vehicle.getArrivalTimes().get(i - 1);
                            Node previousNode = vehicle.getRoute().get(i - 1);
                            Node nextNode = vehicle.getRoute().get(i);
                            float serviceTimeAtPreviousNode = previousNode.getServiceTime();
                            float travelDistance = data.getDistanceBetweenNode(previousNode, node);
                            float arrivalTimeAtNode = previousNodeArrivalTime + serviceTimeAtPreviousNode + travelDistance;

                            if (arrivalTimeAtNode > node.getTimeEnd()) {
                                break;
                            }

                            if (node.getTimeStart() > nextNode.getTimeEnd()) {
                                continue;
                            }

                            float distanceBetweenNodesToInsert = data.getDistanceBetweenNode(previousNode, nextNode);

                            vehicle.getRoute().add(i, node);

                            float distanceBetweenNodesAfterInsert = data.getDistanceBetweenNode(previousNode, node) + data.getDistanceBetweenNode(node, nextNode);

                            boolean validSolution = solver.checkForValidity(data, vehicle);

                            if (validSolution) {
                                currentValue = initialValue - distanceBetweenNodesToInsert + distanceBetweenNodesAfterInsert;
                                diff = currentValue - initialValue;
                                if (diff < bestDiff) {
                                    bestDiff = diff;
                                    currentNodeSwap = new NodeSwap(node, vehicle, diff, i, true);
                                }
                            }

                            vehicle.getRoute().remove(i);
                        }
                    }
                    assert currentNodeSwap != null;
                    nodeSwap.setVehicle(currentNodeSwap.getVehicle());
                    nodeSwap.setValue(currentNodeSwap.getValue());
                    nodeSwap.setIndex(currentNodeSwap.getIndex());
                    nodeSwap.setModified(true);
                } else {
                    Node previousNode = vehicleToInsertInto.getRoute().get(indexToInsert - 1);
                    Node nextNode = vehicleToInsertInto.getRoute().get(indexToInsert + 1);
                    Node node = nodeSwap.getNode();

                    float distanceBetweenNodesToInsert = data.getDistanceBetweenNode(previousNode, nodeToInsert);

                    vehicleToInsertInto.getRoute().add(indexToInsert, node);

                    float distanceBetweenNodesAfterInsert = data.getDistanceBetweenNode(previousNode, node) + data.getDistanceBetweenNode(node, nodeToInsert);

                    boolean validSolution = solver.checkForValidity(data, vehicleToInsertInto);

                    if (validSolution) {
                        currentValue = initialValue - distanceBetweenNodesToInsert + distanceBetweenNodesAfterInsert;
                        diff = currentValue - initialValue;
                        if (diff < nodeSwap.getValue()) {
                            currentNodeSwap = new NodeSwap(node, vehicleToInsertInto, diff, indexToInsert, true);
                            foundBetterValue = true;
                        }
                    }

                    vehicleToInsertInto.getRoute().remove(indexToInsert);

                    distanceBetweenNodesToInsert = data.getDistanceBetweenNode(nodeToInsert, nextNode);

                    vehicleToInsertInto.getRoute().add(indexToInsert + 1, node);

                    distanceBetweenNodesAfterInsert = data.getDistanceBetweenNode(nodeToInsert, node) + data.getDistanceBetweenNode(node, nextNode);

                    validSolution = solver.checkForValidity(data, vehicleToInsertInto);

                    if (validSolution) {
                        currentValue = initialValue - distanceBetweenNodesToInsert + distanceBetweenNodesAfterInsert;
                        diff = currentValue - initialValue;
                        if ((diff < nodeSwap.getValue() && currentNodeSwap == null) || (currentNodeSwap != null && diff < currentNodeSwap.getValue())) {
                            currentNodeSwap = new NodeSwap(node, vehicleToInsertInto, diff, indexToInsert + 1, true);
                            foundBetterValue = true;
                        }
                    }

                    vehicleToInsertInto.getRoute().remove(indexToInsert + 1);

                    if (foundBetterValue) {
                        assert currentNodeSwap != null;
                        nodeSwap.setVehicle(currentNodeSwap.getVehicle());
                        nodeSwap.setValue(currentNodeSwap.getValue());
                        nodeSwap.setIndex(currentNodeSwap.getIndex());
                        nodeSwap.setModified(true);
                        currentNodeSwap = null;
                    }
                }
            }

            for (Vehicle vehicle : data.getFleet().stream().filter(vehicle -> !vehicle.isEmpty() && !vehicle.isPenaltyVehicle()).collect(Collectors.toList())) {
                boolean valid = solver.checkForValidity(data, vehicle);
                if (!valid) {
                    System.out.println("Invalid vehicle greedy ertekeket kiszamolasa utan");
                }
            }
        }

        LocalTime endTime = LocalTime.now();
        long endNanoTime = System.nanoTime();
        logger.log("greedyInsert ended at: " + endTime + ", took " + ((endNanoTime - startNanoTime) * 1e-9) + " seconds");
        logger.log("Validating the data took " + (totalInsertValidityCheck * 1e-9) + " seconds");

    }

    /**
     * See section 3.3.
     *
     * @param data             - data object (graph) to work with
     * @param nodesToSwap      - all the nodes from the graph which had been deleted by the destroyNodes method
     * @param heuristicWeights - the current weights of the heuristics based on the output of the past period
     * @param logger           - logger object
     */
    public void repairNodes(Data data, List<Node> nodesToSwap, HeuristicWeights heuristicWeights, Logger logger) {
        LocalTime startTime = LocalTime.now();
        long repairStart = System.nanoTime();
        logger.log("Repairing nodes started at: " + startTime);
        logger.log("Inserting " + nodesToSwap.size() + " nodes");

        float sumOf = heuristicWeights.sumOfRepair();
        float greedyWeight = heuristicWeights.getGreedyInsertWeight() / sumOf;
        float regret_2_Weight = heuristicWeights.getRegret_2_InsertWeight() / sumOf;
        float regret_3_Weight = heuristicWeights.getRegret_3_InsertWeight() / sumOf;
        float regret_K_Weight = heuristicWeights.getRegret_K_InsertWeight() / sumOf;
        float randomValue = random.nextFloat();
        if (randomValue < greedyWeight) {
            heuristicWeights.setCurrentInsert(1);
            logger.log("Insert method: greedyInsert");
            greedyInsert(data, nodesToSwap, logger);
        } else if (randomValue < sum(greedyWeight, regret_2_Weight)) {
            heuristicWeights.setCurrentInsert(2);
            logger.log("Insert method: regretInsert_2");
            regretInsert(data, nodesToSwap, 2, logger);
        } else if (randomValue < sum(greedyWeight, regret_2_Weight, regret_3_Weight)) {
            heuristicWeights.setCurrentInsert(3);
            logger.log("Insert method: regretInsert_3");
            regretInsert(data, nodesToSwap, 3, logger);
        } else if (randomValue < sum(greedyWeight, regret_2_Weight, regret_3_Weight, regret_K_Weight)) {
            heuristicWeights.setCurrentInsert(4);
            logger.log("Insert method: regretInsert_k");
            int customerNodeCount = (int) data.getNodeList().stream().filter(node -> !node.isDepot() && !node.isDumpingSite()).count();
            regretInsert(data, nodesToSwap, customerNodeCount, logger);
        }

        LocalTime endTime = LocalTime.now();
        long repairEnd = System.nanoTime();
        logger.log("Repairing nodes ended at: " + endTime + ", took " + ((repairEnd - repairStart) * 1e-9) + " seconds");

    }
}
