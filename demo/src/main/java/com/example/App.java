package com.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.lucene.queryparser.classic.QueryParser;




public class App {

    // Per evitare dei warning di log di Lucene, che non sono rilevanti per il nostro caso d'uso.
    private static final Logger LUCENE_LOG;

    static {
        LUCENE_LOG = Logger.getLogger("org.apache.lucene.internal.vectorization");
        LUCENE_LOG.setLevel(Level.OFF);
    }

    
    public static void main(String[] args) throws Exception {
        
        Indexer indexer = new Indexer(Paths.get("IngegneriaDeiDatiHw3/demo/Indexes"));
       long start = System.nanoTime();
       indexer.indexFiles("IngegneriaDeiDatiHw3/demo/documents");
        

        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.println("Tempo di indicizzazione: "
                + seconds + " secondi");

        System.out.println("Sintassi:");
        System.out.println("  contenuto termine1 termine2     (ricerca per termini)");
        System.out.println("  contenuto \"frase esatta\"        (phrase query)");
        System.out.println("  nome nomefile.txt               (ricerca per titolo)");
        System.out.print("query> ");

        String line = new Scanner(System.in).nextLine().trim();
        
        QueryInterrogator interrogator = new QueryInterrogator(line, indexer);
        
        interrogator.interrogator();
        interrogator.close();
        indexer.close();
        //Evaluator evaluator = new Evaluator();
        //evaluator.valuta("IngegneriaDeiDatiHw3/demo");

    }


   
}