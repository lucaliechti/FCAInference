package driver;

import java.util.ArrayList;

import parsers.BibTexParser;
//import parsers.BibTexParser;
import parsers.XMLParser;

import datastructures.FormalContext;
import datastructures.FormalObject;

public class Driver {
	public static void main(String[] args){
		String scgInput = "/home/luca/Dropbox/Uni/!BSc/NoSQL repos/BibTex/scg.bib";
		String mondialInput = "/home/luca/Dropbox/Uni/!BSc/NoSQL repos/XML/mondial.xml";
		String sigmodInput = "/home/luca/Dropbox/Uni/!BSc/NoSQL repos/XML/SigmodRecord.xml";
		String ebayInput = "/home/luca/Dropbox/Uni/!BSc/NoSQL repos/XML/ebay.xml";
		String listbInput = "/home/luca/Dropbox/Uni/!BSc/NoSQL repos/BibTex/listb.bib";
		String dblpInput = "/home/luca/Dropbox/Uni/!BSc/NoSQL repos/BibTex/dblp.bib";
		
		String fileOutputLatticeMiner = "/home/luca/Dropbox/Uni/!BSc/LatticeMiner/LatticeMinerExport.slf";
		String fileOutputConexp = "/home/luca/Dropbox/Uni/!BSc/ConexpExport.cxt";
		
		BibTexParser BTparser = new BibTexParser();
		XMLParser XMLparser	= new XMLParser();
		ArrayList<FormalObject> importedContext = BTparser.parseFile(dblpInput); //specify here which parser to use
		
		FormalContext fc = new FormalContext();
		for(FormalObject object : importedContext) fc.addObject(object);

		fc.exportFormatted(fileOutputConexp);
		System.out.println("All done.");
	}
}