package com.example;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;




public class App {


        private static final Logger LUCENE_LOG;

    static {
        LUCENE_LOG = Logger.getLogger("org.apache.lucene.internal.vectorization");
        LUCENE_LOG.setLevel(Level.OFF);
    }

    public static void main(String[] args) throws IOException, ParseException {

        Indexer indexer = new Indexer(Paths.get("IngegneriaDeiDatiHw3/demo/Indexes"));
        indexer.idexFiles("IngegneriaDeiDatiHw3/demo/documents");

        System.out.println("Sintassi:");
        System.out.println("  contenuto termine1 termine2     (ricerca per termini)");
        System.out.println("  contenuto \"frase esatta\"        (phrase query)");
        System.out.println("  nome nomefile.txt               (ricerca per titolo)");
        System.out.print("query> ");

        String line = new Scanner(System.in).nextLine().trim();
        String[] parts = line.split("\\s+", 2);

        if (parts.length < 2) {
            System.out.println("Query non valida.");
            indexer.close();
            return;
        }

        String keyword = parts[0].toLowerCase();
        String testo = parts[1].trim();
        String luceneQuery;

        if (keyword.equals("contenuto")) {
            if (testo.length() > 1 && testo.startsWith("\"") && testo.endsWith("\"")) {
                // contenuto "frase" -> phrase query
                luceneQuery = "contenuto:" + testo;
            } else {
                // contenuto t1 t2 -> query per termini
                luceneQuery = perTermini("contenuto", testo);
            }
        } else if (keyword.equals("nome")) {
            // nome file.txt -> ricerca sul campo titolo
            luceneQuery = perTermini("titolo", testo);
        } else {
            System.out.println("Query non valida: usa 'nome' o 'contenuto'.");
            indexer.close();
            return;
        }

        QueryInterrogator interrogator = new QueryInterrogator(luceneQuery, indexer);
        interrogator.interrogator();

        interrogator.close();
        indexer.close();
    }


    // Trasforma "t1 t2" in "campo:t1 campo:t2" (termini opzionali)
    private static String perTermini(String campo, String testo) {
        StringBuilder sb = new StringBuilder();
        for (String t : testo.split("\\s+")) {
            sb.append(campo).append(":").append(QueryParser.escape(t)).append(" ");
        }
        return sb.toString().trim();
    }
}