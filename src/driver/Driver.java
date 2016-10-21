package driver;

import java.util.ArrayList;

import parsers.BibTexParser;
import parsers.JSONParser;
import parsers.XMLParser;

import datastructures.FormalContext;
import datastructures.FormalObject;

public class Driver {
	public static void main(String[] args){
		String repoFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\NoSQL repos\\";
		String outputFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\context files\\";		
		ArrayList<String> docs = new ArrayList<String>();
		
//		//add XML repos
//		docs.add(repoFolder + "XML\\mondial.xml");
//		docs.add(repoFolder + "XML\\SigmodRecord.xml");
//		docs.add(repoFolder + "XML\\ebay.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000Lattice.xml");
//		docs.add(repoFolder + "XML\\DBLP\\316NoSql.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000FCA.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000Schema.xml");
		
//		//add BibTex repos
//		docs.add(repoFolder + "BibTex\\scg.bib");
//		docs.add(repoFolder + "BibTex\\listb.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Lattice.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Schema.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Algebra.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Groups.bib");
		
		//add JSON repos
		docs.add(repoFolder + "JSON\\SIRA\\sira-bfh.js");
		
		for(String doc : docs)
			parseDocument(doc, outputFolder);
		System.out.println("All done.");
	}
	
	private static void parseDocument(String doc, String outputFolder){
		System.out.println("Parsing file " + doc + "...");
		BibTexParser BTparser = new BibTexParser();
		XMLParser XMLparser = new XMLParser();
		JSONParser JSONparser = new JSONParser();
		ArrayList<FormalObject> importedContext = new ArrayList<FormalObject>();
		//TODO: use factory pattern to obtain right parser
		if(doc.substring(doc.length()-3).equalsIgnoreCase("bib"))
			importedContext = BTparser.parseFile(doc);
		else if (doc.substring(doc.length()-3).equalsIgnoreCase("xml"))
			importedContext = XMLparser.parseFile(doc);	
		else if (doc.substring(doc.length()-2).equalsIgnoreCase("js"))
			importedContext = JSONparser.parseFile(doc);	
		FormalContext fc = new FormalContext();
		for(FormalObject object : importedContext) fc.addObject(object);
		fc.exportFormatted(outputFolder + doc.substring(doc.length()-3) + "_" + doc.substring(doc.lastIndexOf("\\")+1, doc.lastIndexOf(".")) + ".cxt");
		System.out.println("Parsed file " + doc);
	}
}