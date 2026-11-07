package com.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.util.stream.Stream;
 
/**
 * Indicizza tutti i file .txt contenuti in una directory.
 *
 * Ogni file diventa un Document Lucene con due Field:
 *  - "nome_file"  : nome del file (es. "Rapporto_Finale-2024.txt")
 *  - "contenuto"  : testo del file
 */
public class Indexer {
    private Path path; //Dove si trovano gli indici
    private Directory directory;//Directory contenente gli indici
    private IndexWriter writer;
    private Map<String,Analyzer> analyzers;
    private IndexWriterConfig config;
    CharArraySet stopWords;
   


    public Indexer(Path path) throws IOException {

    this.path = path;
    this.directory = FSDirectory.open(path);

    this.analyzers = new HashMap<>();

    stopWords = new CharArraySet(
        Arrays.asList("in", "dei", "di"), true
    );

    this.analyzers.put("titolo", new WhitespaceAnalyzer());
    this.analyzers.put("contenuto", new StandardAnalyzer(stopWords));

    Analyzer perFieldAnalyzer =
        new PerFieldAnalyzerWrapper(new ItalianAnalyzer(), analyzers);

    this.config = new IndexWriterConfig(perFieldAnalyzer);

    this.config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

    this.writer = new IndexWriter(directory, this.config);
}


    public void idexFiles(String documentsDirectoryPath) throws IOException{
        File files=new File(documentsDirectoryPath);
        List<File> fileList=new ArrayList<>(Arrays.asList(files.listFiles()));
        for(File f: fileList){
            if(f.isFile()){
                Document doc = new Document();
                doc.add(new TextField(
                    "titolo",
                    f.getName(),
                    Field.Store.YES
                ));

                doc.add(new TextField(
                    "contenuto",
                    Files.readString(f.toPath(), StandardCharsets.UTF_8) ,
                    Field.Store.YES
                ));
    
                String id = f.getAbsolutePath();
                doc.add(new StringField("id", id, Field.Store.YES));
                this.writer.updateDocument(new Term("id", f.getAbsolutePath()), doc);
                this.writer.commit();

            }
        }
} 

     public void close() {
        try {
            this.writer.close();
            this.directory.close();
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


    public Directory getDirectory() {
        return directory;
    }


    public void setDirectory(Directory directory) {
        this.directory = directory;
    }


    public IndexWriter getWriter() {
        return writer;
    }


    public void setWriter(IndexWriter writer) {
        this.writer = writer;
    }


    public Map<String, Analyzer> getAnalyzers() {
        return analyzers;
    }


    public void setAnalyzers(Map<String, Analyzer> analyzers) {
        this.analyzers = analyzers;
    }


    public IndexWriterConfig getConfig() {
        return config;
    }


    public void setConfig(IndexWriterConfig config) {
        this.config = config;
    }


    public CharArraySet getStopWords() {
        return stopWords;
    }


    public void setStopWords(CharArraySet stopWords) {
        this.stopWords = stopWords;
    }


   




}