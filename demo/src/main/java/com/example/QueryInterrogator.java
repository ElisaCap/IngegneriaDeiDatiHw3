package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
 
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
/**
 * Legge query da console, interroga l'indice creato da Indexer e stampa i risultati.
 *
 * Sintassi della query:
 *     <campo> <termine | "frase esatta"> ...
 *
 *  - campo = "nome" (campo nome_file) oppure "contenuto" (campo contenuto)
 *  - i termini senza virgolette sono opzionali (BooleanQuery con clausole SHOULD)
 *  - le sequenze tra virgolette sono phrase query
 *
 * Esempi:
 *     nome rapporto finale
 *     contenuto ingegneria "dati strutturati"
 *     contenuto "laurea magistrale"
 *
 * Uso: java QueryInterrogator <directory_indice>
 * Per uscire: scrivere "exit".
 */
public class QueryInterrogator {
 private IndexReader reader; 
 private IndexSearcher searcher; 
 private QueryParser queryParser;
 private Query query;
 private Indexer indexer;
public QueryInterrogator(String query, Indexer indexer) throws ParseException, IOException{
    this.indexer=indexer;
    this.reader = DirectoryReader.open(this.indexer.getDirectory());
    this.searcher = new IndexSearcher(reader);
    this.queryParser = new QueryParser("titolo", this.indexer.getConfig().getAnalyzer());
    this.query=this.queryParser.parse(query);
}


public void interrogator() throws IOException {

    TopDocs hits = searcher.search(query, 10);

    StoredFields storedFields = searcher.storedFields();

    System.out.println(
        "Numero di documenti trovati: " + hits.totalHits.value()
    );

    for (ScoreDoc scoreDoc : hits.scoreDocs) {

        Document doc = storedFields.document(scoreDoc.doc);

        System.out.println("-------------------------");
        System.out.println("Titolo: " + doc.get("titolo"));
    //    System.out.println("Contenuto: " + doc.get("contenuto"));
        System.out.println("Punteggio: " + scoreDoc.score);
    }
}


public void close() {
    try {
        this.reader.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}





}