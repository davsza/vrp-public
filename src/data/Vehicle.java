package data;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {

    private Integer type;
    private Node departureNode;
    private Node arrivalNode;
    private Float capacity;
    private Integer maximumNumberOfStopsToVisit;
    private List<Node> route;
    private Float currentTime;

    private Integer maximumCapacity;
    private Integer id;
    private Boolean penaltyVehicle;
    private List<Float> arrivalTimes;

    public Vehicle() {
        this.route = new ArrayList<>();
        this.arrivalTimes = new ArrayList<>();
    }

    public Vehicle(Vehicle vehicle) {
        this.type = vehicle.getType();
        this.departureNode = vehicle.getDepartureNode();
        this.arrivalNode = vehicle.getArrivalNode();
        this.capacity = vehicle.getCapacity();
        this.maximumNumberOfStopsToVisit = vehicle.getMaximumNumberOfStopsToVisit();
        this.route = new ArrayList<>();
        for (Node node : vehicle.getRoute()) {
            Node copiedNode = new Node(node);
            this.route.add(copiedNode);
        }
        this.currentTime = vehicle.getCurrentTime();
        this.maximumCapacity = vehicle.getMaximumCapacity();
        this.id = vehicle.getId();
        this.penaltyVehicle = vehicle.isPenaltyVehicle();
        this.arrivalTimes = new ArrayList<>();
        for (Float arrivalTime : vehicle.getArrivalTimes()) {
            this.arrivalTimes.add(arrivalTime.floatValue());
        }
    }

    public boolean isEmpty() {
        for (Node node : route) {
            if (node.customerNode()) {
                return false;
            }
        }
        return true;
    }

    public List<Float> getArrivalTimes() {
        return arrivalTimes;
    }

    public List<Node> getRoute() {
        return route;
    }

    public Boolean isPenaltyVehicle() {
        return penaltyVehicle;
    }

    public void setPenaltyVehicle(Boolean penaltyVehicle) {
        this.penaltyVehicle = penaltyVehicle;
    }

    public Integer getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(Integer maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setDepartureNode(Node departureNode) {
        this.departureNode = departureNode;
    }

    public void setArrivalNode(Node arrivalNode) {
        this.arrivalNode = arrivalNode;
    }

    public void setCapacity(Float capacity) {
        this.capacity = capacity;
    }

    public void setMaximumNumberOfStopsToVisit(Integer maximumNumberOfStopsToVisit) {
        this.maximumNumberOfStopsToVisit = maximumNumberOfStopsToVisit;
    }

    public Integer getType() {
        return type;
    }

    public Node getDepartureNode() {
        return departureNode;
    }

    public Node getArrivalNode() {
        return arrivalNode;
    }

    public Float getCapacity() {
        return capacity;
    }

    public Integer getMaximumNumberOfStopsToVisit() {
        return maximumNumberOfStopsToVisit;
    }

    public Float getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Float currentTime) {
        this.currentTime = currentTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void initVehicle() {
        setCurrentTime((float) 0);
        setCapacity((float) 0);
        setPenaltyVehicle(false);
    }

    public float calculateTravelDistance(Data data) {
        if (penaltyVehicle) {
            return (2 * data.getMaximumTravelDistance()) * route.size();
        }
        if (isEmpty()) return 0;
        float travelDistance = 0;
        for (int i = 1; i < route.size(); i++) {
            float travelDistanceBetweenNodes = data.getDistanceBetweenNode(route.get(i - 1), route.get(i));
            travelDistance += travelDistanceBetweenNodes;
        }
        return travelDistance;
    }

    public String routeHash() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Node node : route) {
            String hash = Integer.toHexString(node.getId());
            stringBuilder.append(hash);
        }
        return stringBuilder.toString();
    }
}
