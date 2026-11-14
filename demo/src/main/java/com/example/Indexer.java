
package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Indicizza tutti i file .txt contenuti in una directory.
 *
 * Ogni file viene inserito in due indici Lucene:
 *  - "titolo" : nome del file
 *  - "contenuto" : testo del file
 */
public class Indexer {

    private Path path;
    private Map<String, Directory> directories;
    private Map<String, IndexWriter> writers;
    private Map<String, Analyzer> analyzers;
    private CharArraySet stopWords;

    public Indexer(Path path) throws IOException {

        this.path = path;
        this.directories = new HashMap<>();
        this.writers = new HashMap<>();
        this.analyzers = new HashMap<>();

        stopWords = new CharArraySet(Arrays.asList("in", "dei", "di"), true);

        this.analyzers.put("titolo", new WhitespaceAnalyzer());
        this.analyzers.put("contenuto", new StandardAnalyzer(stopWords));

        for (Map.Entry<String, Analyzer> entry : analyzers.entrySet()) {
            String campo = entry.getKey();
            Directory directory = FSDirectory.open(path.resolve(campo));
            IndexWriterConfig config = new IndexWriterConfig(entry.getValue());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(directory, config); //Specifica che l'indice deve essere creato da zero. Se esiste già, il suo contenuto precedente viene sostituito.
            directories.put(campo, directory);
            writers.put(campo, writer);
        }
    }

    public void indexFiles(String documentsDirectoryPath) throws Exception {

        File files = new File(documentsDirectoryPath);
        List<File> fileList = new ArrayList<>(Arrays.asList(files.listFiles()));

        for (File f : fileList) {
            if (f.isFile() && f.getName().endsWith(".txt")) {

                String id = f.getAbsolutePath();

                Document docTitolo = new Document();
                docTitolo.add(new TextField("titolo", f.getName(), Field.Store.YES));
                docTitolo.add(new StringField("id", id, Field.Store.YES));

                Document docContenuto = new Document();
                docContenuto.add(new TextField("contenuto", Files.readString(f.toPath(), StandardCharsets.UTF_8), Field.Store.YES));
                docContenuto.add(new StringField("id", id, Field.Store.YES));

                writers.get("titolo").updateDocument(new Term("id", id), docTitolo);
                writers.get("contenuto").updateDocument(new Term("id", id), docContenuto);
            }
        }

        for (IndexWriter writer : writers.values()) {
            writer.commit();
        }
    }

    public void evaluate() throws Exception {
        Evaluator evaluator = new Evaluator();
        List<Path> files;
        try (Stream<Path> s = Files.list(Paths.get("demo/documents"))) {
            files = s.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".txt")).sorted().collect(Collectors.toList());
        }
    }

    public void close() {
        try {
            for (IndexWriter writer : writers.values()) {
                writer.close();
            }

            for (Directory directory : directories.values()) {
                directory.close();
            }

            for (Analyzer analyzer : analyzers.values()) {
                analyzer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter and setters

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Directory getDirectory(String campo) {
        return directories.get(campo);
    }

    public IndexWriter getWriter(String campo) {
        return writers.get(campo);
    }

    public Map<String, Analyzer> getAnalyzers() {
        return analyzers;
    }

    public void setAnalyzers(Map<String, Analyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public Analyzer getAnalyzer(String campo) {
        return analyzers.get(campo);
    }

    public CharArraySet getStopWords() {
        return stopWords;
    }

    public void setStopWords(CharArraySet stopWords) {
        this.stopWords = stopWords;
    }
}
