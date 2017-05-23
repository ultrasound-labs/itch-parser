
package org.bund;

import amay22.ParseDS;
import amay22.Parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;

public class ParseITCH {

    private InputStream input;
    private PrintWriter output;
    private Parsers parsers;
    private ParseDS parseDS;
    
    public ParseITCH(String itchFile, String yamlFile, String outFile) {
        this(itchFile, yamlFile);
        
        try {
		output = new PrintWriter(new FileWriter(outFile));
        } catch (IOException e) {
            System.out.println("File (" + outFile +
			       ") could not be created...Check Filename");
        }
    }
    
    public ParseITCH(String itchFile, String yamlFile) {
    
        // Via YAML, create the Data Structures
        try {
            parseDS = new ParseDS(yamlFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found...Check Filename");
        }
        // Create the parsers with the YAML data structures
        parsers = new Parsers(parseDS);
        // Open the input stream...
        try {
	    if (itchFile.length() > 0) {
		// From the file's path if specified...
		input = new FileInputStream(new File(itchFile));
	    } else {
		/// ...or stdin otherwise
		input = System.in;
	    }
        } catch (FileNotFoundException e) {
            System.out.println("File (" + itchFile +
			       ") not found...Check Filename");
        }
    
    }
    
    public byte[] byteParse() throws IOException, InterruptedException {
        if (input.read() == -1) { // EOF
            return null;
        }
        int payLength = input.read();
        byte[] payBytes = new byte[payLength]; // Get the payload

	int offset = 0;  // Loop until we've read the full payload size
	while (offset < payLength) {
	    offset += input.read(payBytes, offset, payLength - offset);
	}
     
        return payBytes;
    }
    
    public ArrayList<byte[]> byteBulkParse() throws IOException, InterruptedException {
     ArrayList<byte[]> payLoad = new ArrayList<byte[]>();
     while (input.read() != -1) {
         payLoad.add(byteParse());
     }

        return payLoad;
    }
    
    public String[] stringParse() throws IOException, InterruptedException {
        if (input.read() == -1) { // EOF
            return null;
        }
        ArrayList<String> payStrings = parsers.messageIn(byteParse());
        return payStrings.toArray(new String[payStrings.size()]);
    }
    
    public ArrayList<String[]> stringBulkParse() throws IOException, InterruptedException {
     ArrayList<String[]> payStrings = new ArrayList<String[]>();
     while (input.read() != -1) {
         payStrings.add(stringParse());
     }
            return payStrings;

    }
    
    public void parseAndWrite() throws IOException, InterruptedException {
            ArrayList<String[]> payStrings = stringBulkParse();
            for (int i = 0; i < payStrings.size(); i++) {
                output.println(String.join(",",payStrings.get(i)));
            }
            output.close();  
    }
    
    
}
