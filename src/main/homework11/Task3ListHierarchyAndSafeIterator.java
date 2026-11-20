package semester1.practice10.homework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Task3ListHierarchyAndSafeIterator {

    public static void demonstrateFailFastCrash(List<String> logs) {
        try {
            System.out.println("Спроба викликати logs.remove() всередині for-each...");

            for (String log : logs) {
                if (log != null && log.contains("CORRUPTED")) {
                    logs.remove(log);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Перехоплено ConcurrentModificationException!");
            System.out.println("Причина: modCount != expectedModCount.");
        }
    }

    public static int purgeLogsByKeywordSafely(List<String> logs, String keyword) {
        Iterator<String> it = logs.iterator();
        int removed = 0;

        while (it.hasNext()) {
            String log = it.next();

            if (log == null || log.trim().isEmpty() || log.contains(keyword)) {
                it.remove();
                removed++;
            }
        }

        return removed;
    }

    public static void enrichCriticalLogsWithListIterator(List<String> logs) {
        ListIterator<String> listIt = logs.listIterator();

        while (listIt.hasNext()) {
            String log = listIt.next();
            String trimmed = log.trim();

            listIt.set(trimmed);

            if (trimmed.startsWith("ERROR:")) {
                listIt.add("[ALERT-TRIGGERED]");
            }
        }
    }

    public static List<String> collectReverseOrder(List<String> logs) {
        List<String> reversed = new ArrayList<>();
        ListIterator<String> listIt = logs.listIterator(logs.size());

        while (listIt.hasPrevious()) {
            reversed.add(listIt.previous());
        }

        return reversed;
    }

    public static void main(String[] args) {
        List<String> rawLogs = Arrays.asList(
                "INFO: Auth OK",
                "CORRUPTED_PACKET_01",
                "  ERROR: DB Timeout  ",
                "DEBUG: Ping",
                "CORRUPTED_PACKET_02",
                null,
                "ERROR: Disk Full",
                "INFO: Logout"
        );

        System.out.println("================ ІЄРАРХІЯ LIST ТА БЕЗПЕЧНИЙ ITERATOR ================");
        System.out.println("Початковий потік логів (" + rawLogs.size() + " записів):");
        System.out.println(rawLogs);

        System.out.println("\n[1. ДЕМОНСТРАЦІЯ ПАСТКИ FAIL-FAST У ЦИКЛІ FOR-EACH]:");

        List<String> failFastLogs = new ArrayList<>(rawLogs);
        demonstrateFailFastCrash(failFastLogs);

        System.out.println("\n[2. ПОЛІМОРФНА ОБРОБКА ARRAYLIST<String> ЧЕРЕЗ ITERATOR]:");

        List<String> arrayLogs = new ArrayList<>(rawLogs);

        int arrayRemoved = purgeLogsByKeywordSafely(arrayLogs, "CORRUPTED");

        System.out.println("Безпечно видалено записів: " + arrayRemoved);
        System.out.println("Залишилося чистих логів: " + arrayLogs);

        enrichCriticalLogsWithListIterator(arrayLogs);

        System.out.println("Модифікація через ListIterator:");
        System.out.println(arrayLogs);

        System.out.println("\n[3. ПОЛІМОРФНА ОБРОБКА LINKEDLIST<String> ТИМИ САМИМИ МЕТОДАМИ]:");

        List<String> linkedLogs = new LinkedList<>(rawLogs);

        int linkedRemoved = purgeLogsByKeywordSafely(linkedLogs, "CORRUPTED");

        enrichCriticalLogsWithListIterator(linkedLogs);

        boolean identical = arrayLogs.equals(linkedLogs);

        System.out.println("Видалено записів у LinkedList: " + linkedRemoved);
        System.out.println("Результат LinkedList повністю збігається з ArrayList: "
                + identical + " (Розмір: " + linkedLogs.size() + ")");

        List<String> reversedLogs = collectReverseOrder(linkedLogs);

        System.out.println("Зворотний обхід через ListIterator:");
        System.out.println(reversedLogs);

        System.out.println("=====================================================================");
    }
}