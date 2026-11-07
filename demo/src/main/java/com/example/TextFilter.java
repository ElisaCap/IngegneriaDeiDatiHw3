package com.example;
import java.io.File; 
import java.io.FileFilter; 
public class TextFilter implements FileFilter{
@Override 
public boolean accept(File pathname){
    return pathname.getName().toLowerCase().endsWith(".txt");
}
}
