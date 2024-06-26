package data;


public class Node {

    private Integer id;
    private Float cx;
    private Float cy;
    private Float quantity;
    private Float timeStart;
    private Float timeEnd;
    private Float serviceTime;

    private Boolean visited;
    private Boolean depot;
    private Boolean dumpingSite;
    private Boolean nullNode;
    private Float visitedAt;
    private Float relatednessValue;

    public Node() {
        this.visited = false;
        this.depot = false;
        this.dumpingSite = false;
        this.nullNode = false;
    }

    public Node(Node node) {
        this.id = node.getId();
        this.cx = node.getCx();
        this.cy = node.getCy();
        this.quantity = node.getQuantity();
        this.timeStart = node.getTimeStart();
        this.timeEnd = node.getTimeEnd();
        this.serviceTime = node.getServiceTime();
        this.visited = node.getVisited();
        this.depot = node.getDepot();
        this.dumpingSite = node.getDumpingSite();
        this.nullNode = node.getNullNode();
        this.visitedAt = node.getVisitedAt();
        this.relatednessValue = node.getRelatednessValue();
    }

    public Float getRelatednessValue() {
        return relatednessValue;
    }

    public Boolean getVisited() {
        return visited;
    }

    public Boolean getDepot() {
        return depot;
    }

    public Boolean getDumpingSite() {
        return dumpingSite;
    }

    public Boolean getNullNode() {
        return nullNode;
    }

    public Boolean isNullNode() {
        return nullNode;
    }

    public Float getVisitedAt() {
        return visitedAt;
    }

    public void setVisitedAt(Float visitedAt) {
        this.visitedAt = visitedAt;
    }

    public void setNullNode(Boolean nullNode) {
        this.nullNode = nullNode;
    }

    public Boolean isDumpingSite() {
        return dumpingSite;
    }

    public void setDumpingSite(Boolean dumpingSite) {
        this.dumpingSite = dumpingSite;
    }

    public Boolean isDepot() {
        return depot;
    }

    public void setDepot(Boolean depot) {
        this.depot = depot;
    }

    public Boolean isVisited() {
        return visited;
    }

    public void setVisited(Boolean visited) {
        this.visited = visited;
    }

    public void setCx(Float cx) {
        this.cx = cx;
    }

    public void setCy(Float cy) {
        this.cy = cy;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public void setTimeStart(Float timeStart) {
        this.timeStart = timeStart;
    }

    public void setTimeEnd(Float timeEnd) {
        this.timeEnd = timeEnd;
    }

    public void setServiceTime(Float serviceTime) {
        this.serviceTime = serviceTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Float getCx() {
        return cx;
    }

    public Float getCy() {
        return cy;
    }

    public Float getQuantity() {
        return quantity;
    }

    public Float getTimeStart() {
        return timeStart;
    }

    public Float getTimeEnd() {
        return timeEnd;
    }

    public Float getServiceTime() {
        return serviceTime;
    }

    public boolean customerNode() {
        return !depot && !dumpingSite;
    }
}
