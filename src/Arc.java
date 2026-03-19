public class Arc {

    private Localisation origine;
    private Localisation destination;
    private double distance;
    private String nomRue;

    public Arc(Localisation origine, Localisation destination, double distance, String nomRue) {
        this.origine = origine;
        this.destination = destination;
        this.distance = distance;
        this.nomRue = nomRue;
    }

    public Localisation getOrigine() {
        return origine;
    }

    public void setOrigine(Localisation origine) {
        this.origine = origine;
    }

    public Localisation getDestination() {
        return destination;
    }

    public void setDestination(Localisation destination) {
        this.destination = destination;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getNomRue() {
        return nomRue;
    }

    public void setNomRue(String nomRue) {
        this.nomRue = nomRue;
    }

    public double calculerPente() {
        return (origine.getAltitude() - destination.getAltitude()) / distance;
    }

    @Override
    public String toString() {
        return "Arc{" +
                "origine=" + origine.getId() +
                ", destination=" + destination.getId() +
                ", distance=" + distance +
                ", nomRue='" + nomRue + '\'' +
                '}';
    }
}