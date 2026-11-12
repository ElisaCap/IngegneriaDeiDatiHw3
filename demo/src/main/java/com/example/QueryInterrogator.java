
package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Legge query da console, interroga l'indice creato da Indexer e stampa i risultati.
 */
public class QueryInterrogator {

    private IndexReader reader;
    private IndexSearcher searcher;
    private QueryParser queryParser;
    private Query query;
    private Indexer indexer;

    // Indica quale indice è stato interrogato
    private String campo;

    public QueryInterrogator(String query, Indexer indexer) throws ParseException, IOException {

        this.indexer = indexer;

        String queryModificata = modificaStringa(query);

        String keyword = query.trim().split("\\s+", 2)[0].toLowerCase();

        if (keyword.equals("nome")) {
            campo = "titolo";
        } else if (keyword.equals("contenuto")) {
            campo = "contenuto";
        } else {
            throw new IllegalArgumentException("Query non valida: usa 'nome' o 'contenuto'.");
        }

        Directory directory = this.indexer.getDirectory(campo);
        Analyzer analyzer = this.indexer.getAnalyzer(campo);

        this.reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
        this.queryParser = new QueryParser(campo, analyzer);

        try {
            this.query = this.queryParser.parse(queryModificata);
        } catch (ParseException | RuntimeException e) {
            reader.close();
            throw e;
        }
    }

    // Stampa i risultati della ricerca
    public void interrogator() throws IOException {

        TopDocs hits = searcher.search(query, 10);

        StoredFields storedFields = searcher.storedFields();

        System.out.println("Numero di documenti trovati: " + hits.totalHits.value());

        for (ScoreDoc scoreDoc : hits.scoreDocs) {

            Document doc = storedFields.document(scoreDoc.doc);

            System.out.println("-------------------------");
            System.out.println("Titolo: " + nomeFile(doc));
            System.out.println("Punteggio: " + scoreDoc.score);
        }
    }

    // Rende la query dell'utente compatibile con Lucene, aggiungendo il prefisso del campo e gestendo le virgolette.
    public String modificaStringa(String line) {

        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Query vuota.");
        }

        String[] parts = line.trim().split("\\s+", 2);

        if (parts.length < 2) {
            throw new IllegalArgumentException("Query non valida.");
        }

        String keyword = parts[0].toLowerCase();
        String testo = parts[1].trim();
        String luceneQuery;

        if (testo.isEmpty()) {
            throw new IllegalArgumentException("Query senza termini.");
        }

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
            throw new IllegalArgumentException("Query non valida: usa 'nome' o 'contenuto'.");
        }

        return luceneQuery;
    }

    // Trasforma "t1 t2" in "campo:t1 campo:t2" (termini opzionali)
    private static String perTermini(String campo, String testo) {

        StringBuilder sb = new StringBuilder();

        for (String t : testo.split("\\s+")) {
            sb.append(campo).append(":").append(QueryParser.escape(t)).append(" ");
        }

        return sb.toString().trim();
    }

    // Recupera il nome del file dal documento Lucene
    private String nomeFile(Document doc) {

        if (campo.equals("titolo")) {
            return doc.get("titolo");
        }

        String id = doc.get("id");
        return Paths.get(id).getFileName().toString();
    }


    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> cerca(int k) throws IOException {
        List<String> risultati = new ArrayList<>();

        if (k <= 0) {
            return risultati;
        }

        TopDocs hits = searcher.search(query, k);
        StoredFields storedFields = searcher.storedFields();

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = storedFields.document(scoreDoc.doc);
            risultati.add(nomeFile(doc));
        }
        return risultati;
    }






    public IndexReader getReader() {
        return reader;
    }

    public void setReader(IndexReader reader) {
        this.reader = reader;
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    public void setSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }

    public QueryParser getQueryParser() {
        return queryParser;
    }

    public void setQueryParser(QueryParser queryParser) {
        this.queryParser = queryParser;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }
}
