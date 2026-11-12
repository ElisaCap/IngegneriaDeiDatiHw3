
package com.example;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

public class Evaluator {

    public void valuta(String directory) throws Exception {

        Path base = Paths.get(directory);
        Path documenti = base.resolve("documents");

        // Controlla che la cartella dei documenti esista
        if (!Files.isDirectory(documenti)) {
            throw new IllegalArgumentException(
                    "Cartella documents non trovata: " + documenti);
        }

        // Legge le query
        Map<String, String> queries = new LinkedHashMap<>();

        for (String riga : Files.readAllLines(
                base.resolve("queries.csv"), StandardCharsets.UTF_8)) {

            riga = riga.replace("\uFEFF", "").trim();

            if (riga.isEmpty() || riga.startsWith("query_id"))
                continue;

            String[] campi = riga.split(",", -1);

            if (campi.length < 2)
                continue;

            String query = campi[1].trim();

            // Gestisce le virgolette del formato CSV
            if (query.startsWith("\"") && query.endsWith("\"")) {
                query = query.substring(1, query.length() - 1);
            }

            query = query.replace("\"\"", "\"");

            if (!query.isEmpty()) {
                queries.put(campi[0].trim(), query);
            }
        }

        // Legge il ground truth
        Map<String, Set<String>> rilevanti = new HashMap<>();

        for (String riga : Files.readAllLines(
                base.resolve("qrels.csv"), StandardCharsets.UTF_8)) {

            riga = riga.replace("\uFEFF", "").trim();

            if (riga.isEmpty() || riga.startsWith("query_id"))
                continue;

            String[] campi = riga.split(",", -1);

            if (campi.length < 2)
                continue;

            rilevanti.computeIfAbsent(
                    campi[0].trim(), k -> new HashSet<>())
                    .add(campi[1].trim());
        }

        // Conta i documenti
        long numeroDocumenti;

        try (Stream<Path> files = Files.walk(documenti)) {
            numeroDocumenti = files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                    .count();
        }

        System.out.println("Documenti da indicizzare: " + numeroDocumenti);
        System.out.println("Query generate: " + queries.size());

        // Crea l'indice Lucene
        Indexer indexer = new Indexer(base.resolve("index"));

        double sommaPrecision = 0;
        double sommaRecall = 0;
        int n = 0;

        try {
            // Indicizza tutti i documenti
            indexer.indexFiles(documenti.toString());

            // Esegue le query
            for (Map.Entry<String, String> entry : queries.entrySet()) {

                String id = entry.getKey();
                String query = entry.getValue();

                Set<String> attesi = rilevanti.getOrDefault(
                        id, Collections.emptySet());

                if (attesi.isEmpty()) {
                    System.out.println(
                            id + " - Nessun documento rilevante nel ground truth.");
                    continue;
                }

                System.out.println(
                        "\nQuery in esecuzione: " + id + " -> " + query);

                QueryInterrogator qi =
                        new QueryInterrogator(query, indexer);

                List<String> risultati;

                try {
                    risultati = qi.cerca(
                            Math.max(1, qi.getReader().numDocs()));
                } finally {
                    qi.close();
                }

                Set<String> recuperati = new HashSet<>(risultati);

                long corretti = recuperati.stream()
                        .filter(attesi::contains)
                        .count();

                double precision = recuperati.isEmpty()
                        ? 0
                        : (double) corretti / recuperati.size();

                double recall = (double) corretti / attesi.size();

                sommaPrecision += precision;
                sommaRecall += recall;
                n++;

                System.out.printf(
                        "%s | %-45s Precision: %.2f Recall: %.2f%n",
                        id, query, precision, recall);
            }

            // Risultati finali
            System.out.println("\n===== RISULTATI FINALI =====");

            System.out.printf("Precision media: %.3f%n",
                    n == 0 ? 0 : sommaPrecision / n);

            System.out.printf("Recall media: %.3f%n",
                    n == 0 ? 0 : sommaRecall / n);

            System.out.println("Query valutate: " + n);

        } finally {
            indexer.close();
        }
    }
}
