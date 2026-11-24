
package simester1.practice11.homework;

import java.util.*;

abstract class Transport {
    private final String name;
    private final double speedKmH;

    public Transport(String name, double speedKmH) {
        this.name = name;
        this.speedKmH = speedKmH;
    }

    public String getName() {
        return name;
    }

    public double getSpeedKmH() {
        return speedKmH;
    }

    public double calculateTimeHours(int distanceKm) {
        return distanceKm / speedKmH;
    }

    public abstract double calculateCostUah(int distanceKm);
}

class Bus extends Transport {

    public Bus() {
        super("Bus", 80.0);
    }

    @Override
    public double calculateCostUah(int distanceKm) {
        return distanceKm * 2.5;
    }
}

class Train extends Transport {

    public Train() {
        super("Train", 100.0);
    }

    @Override
    public double calculateCostUah(int distanceKm) {
        return distanceKm * 1.8 + 150.0;
    }
}

class DeliveryTruck extends Transport {

    public DeliveryTruck() {
        super("DeliveryTruck", 70.0);
    }

    @Override
    public double calculateCostUah(int distanceKm) {
        return distanceKm * 4.0 + 300.0;
    }
}

class TripOrder {
    private final String orderId;
    private final String fromCity;
    private final String toCity;
    private final Transport transport;

    public TripOrder(String orderId, String fromCity,
                     String toCity, Transport transport) {
        this.orderId = orderId;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.transport = transport;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getFromCity() {
        return fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public Transport getTransport() {
        return transport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TripOrder)) {
            return false;
        }

        TripOrder other = (TripOrder) o;
        return Objects.equals(orderId, other.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderId);
    }
}

class RouteDispatcher {
    private final ArrayList<String> cities;
    private final int[][] distances;
    private final HashSet<String> closedCities;
    private final ArrayList<TripOrder> orders;

    public RouteDispatcher(ArrayList<String> cities,
                           int[][] distances,
                           HashSet<String> closedCities) {

        this.cities = new ArrayList<>(cities);
        this.closedCities = new HashSet<>(closedCities);
        this.orders = new ArrayList<>();

        this.distances = new int[distances.length][];

        for (int i = 0; i < distances.length; i++) {
            this.distances[i] = distances[i].clone();
        }
    }

    public void addOrder(TripOrder order) {
        orders.add(order);
    }

    public int getDistance(String from, String to) {
        int i = cities.indexOf(from);
        int j = cities.indexOf(to);

        if (i == -1 || j == -1) {
            return 0;
        }

        return distances[i][j];
    }

    public HashSet<String> getDirectNeighbors(String city) {
        HashSet<String> neighbors = new HashSet<>();
        int idx = cities.indexOf(city);

        if (idx == -1) {
            return neighbors;
        }

        for (int i = 0; i < cities.size(); i++) {
            if (distances[idx][i] > 0) {
                neighbors.add(cities.get(i));
            }
        }

        return neighbors;
    }

    public HashSet<String> getCommonNeighbors(String cityA,
                                              String cityB) {
        HashSet<String> result = getDirectNeighbors(cityA);
        result.retainAll(getDirectNeighbors(cityB));

        return result;
    }

    public HashSet<String> getExclusiveNeighbors(String cityA,
                                                 String cityB) {
        HashSet<String> result = getDirectNeighbors(cityA);
        result.removeAll(getDirectNeighbors(cityB));

        return result;
    }

    public int filterInvalidOrders() {
        HashSet<String> seenIds = new HashSet<>();
        Iterator<TripOrder> it = orders.iterator();

        int removed = 0;

        while (it.hasNext()) {
            TripOrder order = it.next();

            String reason = null;

            if (!seenIds.add(order.getOrderId())) {
                reason = "ДУБЛІКАТ";
            } else if (closedCities.contains(order.getFromCity())
                    || closedCities.contains(order.getToCity())) {
                reason = "МІСТО ЗАКРИТЕ";
            } else if (getDistance(order.getFromCity(),
                    order.getToCity()) <= 0) {
                reason = "НЕМАЄ ДОРОГИ";
            }

            if (reason != null) {
                System.out.printf(
                        "  ❌ [%-13s] %s (%s -> %s) — видалено!%n",
                        reason,
                        order.getOrderId(),
                        order.getFromCity(),
                        order.getToCity()
                );

                it.remove();
                removed++;
            }
        }

        return removed;
    }

    public void printReport() {
        int totalDistance = 0;
        double totalCost = 0;

        System.out.println("\n--- 3. Схвалені рейси (залишилось "
                + orders.size() + ") ---");

        for (TripOrder order : orders) {
            int distance = getDistance(
                    order.getFromCity(),
                    order.getToCity()
            );

            Transport transport = order.getTransport();

            double time = transport.calculateTimeHours(distance);
            double cost = transport.calculateCostUah(distance);

            totalDistance += distance;
            totalCost += cost;

            System.out.printf(Locale.US,
                    "  ✅ %s | %s -> %-6s | %d км | %-13s | Час: %.2f год | Вартість: %.2f грн%n",
                    order.getOrderId(),
                    order.getFromCity(),
                    order.getToCity(),
                    distance,
                    transport.getName(),
                    time,
                    cost
            );
        }

        System.out.println("\n--- Підсумок ---");

        System.out.printf(Locale.US,
                "Загальна відстань: %d км | Загальна вартість перевезень: %.2f грн%n",
                totalDistance,
                totalCost
        );
    }

    public int getOrdersCount() {
        return orders.size();
    }

    public HashSet<String> getClosedCities() {
        return new HashSet<>(closedCities);
    }
}

public class Task4MatrixAndCityNetwork {

    public static void main(String[] args) {

        ArrayList<String> cities = new ArrayList<>(
                Arrays.asList("Київ", "Львів", "Одеса", "Дніпро")
        );

        int[][] distances = {
                {0, 540, 480, 450},
                {540, 0, 790, 0},
                {480, 790, 0, 460},
                {450, 0, 460, 0}
        };

        HashSet<String> closedCities = new HashSet<>();
        closedCities.add("Одеса");

        RouteDispatcher dispatcher = new RouteDispatcher(
                cities, distances, closedCities
        );

        dispatcher.addOrder(
                new TripOrder("ORD-1", "Київ", "Львів", new Train())
        );

        dispatcher.addOrder(
                new TripOrder("ORD-1", "Київ", "Львів", new Bus())
        );

        dispatcher.addOrder(
                new TripOrder("ORD-2", "Київ", "Одеса", new Bus())
        );

        dispatcher.addOrder(
                new TripOrder("ORD-3", "Львів", "Дніпро", new DeliveryTruck())
        );

        dispatcher.addOrder(
                new TripOrder("ORD-4", "Київ", "Дніпро", new DeliveryTruck())
        );

        System.out.println("================ 🚚 ЛОГІСТИЧНИЙ ДИСПЕТЧЕР: МАТРИЦЯ ТА МЕРЕЖА 🚚 ================");

        System.out.println("\n--- 1. Аналіз мережі доріг (2D-масив -> HashSet) ---");

        HashSet<String> kyivNeighbors =
                dispatcher.getDirectNeighbors("Київ");

        HashSet<String> lvivNeighbors =
                dispatcher.getDirectNeighbors("Львів");

        System.out.println("Прямі сусіди міста Київ ["
                + kyivNeighbors.size() + "]: " + kyivNeighbors);

        System.out.println("Прямі сусіди міста Львів ["
                + lvivNeighbors.size() + "]: " + lvivNeighbors);

        System.out.println("Спільні сусіди Київ ∩ Львів: "
                + dispatcher.getCommonNeighbors("Київ", "Львів"));

        System.out.println("Тільки з Києва (Київ \\ Львів): "
                + dispatcher.getExclusiveNeighbors("Київ", "Львів"));

        System.out.println("\n--- 2. Фільтрація заявок через Iterator.remove() ---");

        System.out.println("Закриті міста: "
                + dispatcher.getClosedCities());

        int removed = dispatcher.filterInvalidOrders();

        dispatcher.printReport();

        System.out.println("Видалено заявок: " + removed
                + " | Схвалено: " + dispatcher.getOrdersCount());

        System.out.println("=================================================================================");
    }
}
