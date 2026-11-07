
package com.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class App {

    public static void main(String[] args) throws IOException, ParseException {
       Indexer indexer = new Indexer(Paths.get("/home/elisa/Scrivania/Hw3/IngegneriaDeiDatiHw3/demo/Indexes"));
       indexer.idexFiles("/home/elisa/Scrivania/Hw3/IngegneriaDeiDatiHw3/demo/documents");
        QueryInterrogator interrogator= new QueryInterrogator("contenuto:Deep contenuto:learning", indexer);
        interrogator.interrogator();
        indexer.close();
        interrogator.close();
        }
    
    }    
