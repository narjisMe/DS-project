import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Graph {

	//ATTRIBUT ?
	//TODO
    private Map<Long, Localisation> localisationsParId;
    private Map<Long, List<Arc>> arcsSortants;

    public Graph(String localisations, String roads)  {
        //TODO
        localisationsParId = new HashMap<>();
        arcsSortants = new HashMap<>();

        try {
            BufferedReader brLocalisations = new BufferedReader(new FileReader(localisations));
            String ligne = brLocalisations.readLine();

            while ((ligne = brLocalisations.readLine()) != null) {
                String[] parties = ligne.split(",", 5);

                long id = Long.parseLong(parties[0].trim());
                String nom = parties[1].trim();
                double latitude = Double.parseDouble(parties[2].trim());
                double longitude = Double.parseDouble(parties[3].trim());
                double altitude = Double.parseDouble(parties[4].trim());

                Localisation localisation = new Localisation(id, nom, latitude, longitude, altitude);
                localisationsParId.put(id, localisation);
                arcsSortants.put(id, new ArrayList<>());
            }

            brLocalisations.close();

            BufferedReader brRoads = new BufferedReader(new FileReader(roads));
            ligne = brRoads.readLine();

            while ((ligne = brRoads.readLine()) != null) {
                String[] parties = ligne.split(",", 4);

                long idOrigine = Long.parseLong(parties[0].trim());
                long idDestination = Long.parseLong(parties[1].trim());
                double distance = Double.parseDouble(parties[2].trim());
                String nomRue = parties[3].trim();

                Localisation origine = localisationsParId.get(idOrigine);
                Localisation destination = localisationsParId.get(idDestination);

                Arc arc = new Arc(origine, destination, distance, nomRue);
                arcsSortants.get(idOrigine).add(arc);
            }

            brRoads.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Localisation[] determinerZoneInondee(long[] idsOrigin,double epsilon) {
        //TODO
        if (idsOrigin == null || idsOrigin.length == 0) {
            return new Localisation[0];
        }

        List<Localisation> resultat = new ArrayList<>();
        Set<Long> visites = new HashSet<>();
        Queue<Long> file = new ArrayDeque<>();

        for (long id : idsOrigin) {
            if (localisationsParId.containsKey(id) && visites.add(id)) {
                file.add(id);
                resultat.add(localisationsParId.get(id));
            }
        }

        while (!file.isEmpty()) {
            long idCourant = file.poll();

            for (Arc arc : arcsSortants.getOrDefault(idCourant, Collections.emptyList())) {
                Localisation voisin = arc.getDestination();

                if (voisin == null) {
                    continue;
                }

                if (arc.calculerPente() >= -epsilon) {
                    long idVoisin = voisin.getId();
                    if (visites.add(idVoisin)) {
                        file.add(idVoisin);
                        resultat.add(voisin);
                    }
                }
            }
        }

        return resultat.toArray(new Localisation[0]);
    }

    public Deque<Localisation> trouverCheminLePlusCourtPourContournerLaZoneInondee(long idOrigin, long idDestination, Localisation[] floodedZone) {
		//TODO
        Deque<Localisation> chemin = new ArrayDeque<>();

        if (!localisationsParId.containsKey(idOrigin) || !localisationsParId.containsKey(idDestination)) {
            return chemin;
        }

        Set<Long> zonesInondees = new HashSet<>();
        if (floodedZone != null) {
            for (Localisation loc : floodedZone) {
                if (loc != null) {
                    zonesInondees.add(loc.getId());
                }
            }
        }

        if (zonesInondees.contains(idOrigin) || zonesInondees.contains(idDestination)) {
            return chemin;
        }

        Queue<Long> file = new ArrayDeque<>();
        Set<Long> visites = new HashSet<>();
        Map<Long, Long> parent = new HashMap<>();

        file.add(idOrigin);
        visites.add(idOrigin);

        while (!file.isEmpty()) {
            long courant = file.poll();

            if (courant == idDestination) {
                break;
            }

            for (Arc arc : arcsSortants.getOrDefault(courant, Collections.emptyList())) {
                Localisation voisin = arc.getDestination();

                if (voisin == null) {
                    continue;
                }

                long idVoisin = voisin.getId();

                if (zonesInondees.contains(idVoisin)) {
                    continue;
                }

                if (visites.add(idVoisin)) {
                    parent.put(idVoisin, courant);
                    file.add(idVoisin);
                }
            }
        }

        if (!visites.contains(idDestination)) {
            return chemin;
        }

        LinkedList<Localisation> temp = new LinkedList<>();
        long courant = idDestination;
        temp.addFirst(localisationsParId.get(courant));

        while (courant != idOrigin) {
            Long precedent = parent.get(courant);
            if (precedent == null) {
                return new ArrayDeque<>();
            }
            courant = precedent;
            temp.addFirst(localisationsParId.get(courant));
        }

        chemin.addAll(temp);
        return chemin;
    }

    public Map<Localisation,Double> determinerChronologieDeLaCrue(long[] idsOrigin, double vWaterInit,double k) {
        //TODO
        Map<Localisation,Double> tFlood = new LinkedHashMap<>();

        if (idsOrigin == null || idsOrigin.length == 0) {
            return tFlood;
        }

        Map<Long, Double> meilleurTemps = new HashMap<>();
        PriorityQueue<double[]> file = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));

        for (long id : idsOrigin) {
            if (localisationsParId.containsKey(id)) {
                double[] etatInitial = new double[3];
                etatInitial[0] = id;
                etatInitial[1] = 0.0;
                etatInitial[2] = vWaterInit;
                meilleurTemps.put(id, 0.0);
                file.add(etatInitial);
            }
        }

        while (!file.isEmpty()) {
            double[] etat = file.poll();
            long idCourant = (long) etat[0];
            double tempsCourant = etat[1];
            double vitesseCourante = etat[2];

            if (tempsCourant > meilleurTemps.getOrDefault(idCourant, Double.POSITIVE_INFINITY)) {
                continue;
            }

            Localisation source = localisationsParId.get(idCourant);
            if (!tFlood.containsKey(source)) {
                tFlood.put(source, tempsCourant);
            }

            for (Arc arc : arcsSortants.getOrDefault(idCourant, Collections.emptyList())) {
                Localisation destination = arc.getDestination();
                if (destination == null) {
                    continue;
                }

                double pente = arc.calculerPente();
                double nouvelleVitesse = vitesseCourante + (k * pente);

                if (nouvelleVitesse <= 0) {
                    continue;
                }

                double tempsParcours = arc.getDistance() / nouvelleVitesse;
                double nouveauTemps = tempsCourant + tempsParcours;
                long idDestination = destination.getId();

                if (nouveauTemps < meilleurTemps.getOrDefault(idDestination, Double.POSITIVE_INFINITY)) {
                    meilleurTemps.put(idDestination, nouveauTemps);

                    double[] nouvelEtat = new double[3];
                    nouvelEtat[0] = idDestination;
                    nouvelEtat[1] = nouveauTemps;
                    nouvelEtat[2] = nouvelleVitesse;
                    file.add(nouvelEtat);
                }
            }
        }

        return tFlood;
    }

    public Deque<Localisation> trouverCheminDEvacuationLePlusCourt(long idOrigin, long idEvacuation, double vVehicule, Map<Localisation,Double> tFlood) {
        //TODO
        Deque<Localisation> chemin = new ArrayDeque<>();

        if (!localisationsParId.containsKey(idOrigin) || !localisationsParId.containsKey(idEvacuation) || vVehicule <= 0) {
            return chemin;
        }

        Localisation origine = localisationsParId.get(idOrigin);
        Double tempsInondationOrigine = tFlood.get(origine);
        if (tempsInondationOrigine != null && 0.0 >= tempsInondationOrigine) {
            return chemin;
        }

        Map<Long, Double> meilleurTemps = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();
        PriorityQueue<double[]> file = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));

        meilleurTemps.put(idOrigin, 0.0);
        file.add(new double[]{idOrigin, 0.0});

        while (!file.isEmpty()) {
            double[] etat = file.poll();
            long idCourant = (long) etat[0];
            double tempsCourant = etat[1];

            if (tempsCourant > meilleurTemps.getOrDefault(idCourant, Double.POSITIVE_INFINITY)) {
                continue;
            }

            if (idCourant == idEvacuation) {
                break;
            }

            for (Arc arc : arcsSortants.getOrDefault(idCourant, Collections.emptyList())) {
                Localisation destination = arc.getDestination();
                if (destination == null) {
                    continue;
                }

                double tempsParcours = arc.getDistance() / vVehicule;
                double tempsArrivee = tempsCourant + tempsParcours;

                Double tempsInondationDestination = tFlood.get(destination);
                if (tempsInondationDestination != null && tempsArrivee >= tempsInondationDestination) {
                    continue;
                }

                long idDestination = destination.getId();

                if (tempsArrivee < meilleurTemps.getOrDefault(idDestination, Double.POSITIVE_INFINITY)) {
                    meilleurTemps.put(idDestination, tempsArrivee);
                    parent.put(idDestination, idCourant);
                    file.add(new double[]{idDestination, tempsArrivee});
                }
            }
        }

        if (!meilleurTemps.containsKey(idEvacuation)) {
            return chemin;
        }

        LinkedList<Localisation> temp = new LinkedList<>();
        long courant = idEvacuation;
        temp.addFirst(localisationsParId.get(courant));

        while (courant != idOrigin) {
            Long precedent = parent.get(courant);
            if (precedent == null) {
                return new ArrayDeque<>();
            }
            courant = precedent;
            temp.addFirst(localisationsParId.get(courant));
        }

        chemin.addAll(temp);
        return chemin;
    }
}
