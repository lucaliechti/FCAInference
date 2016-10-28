package driver;

import java.util.ArrayList;

import parsers.NoSQLParser;
import datastructures.FormalContext;
import datastructures.FormalObject;
import factories.ParserFactory;

public class Driver {
	public static void main(String[] args){
		String repoFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\NoSQL repos\\";
		String outputFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\context files\\";		
		ArrayList<String> docs = new ArrayList<String>();
		ParserFactory factory = new ParserFactory();
		
//		//add XML repos
//		docs.add(repoFolder + "XML\\mondial.xml");
//		docs.add(repoFolder + "XML\\SigmodRecord.xml");
//		docs.add(repoFolder + "XML\\ebay.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000Lattice.xml");
//		docs.add(repoFolder + "XML\\DBLP\\316NoSql.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000FCA.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000Schema.xml");
		
		//add BibTex repos
		docs.add(repoFolder + "BibTex\\scg.bib");
		docs.add(repoFolder + "BibTex\\listb.bib");
		docs.add(repoFolder + "BibTex\\zbMATH\\100Lattice.bib");
		docs.add(repoFolder + "BibTex\\zbMATH\\100Schema.bib");
		docs.add(repoFolder + "BibTex\\zbMATH\\100Algebra.bib");
		docs.add(repoFolder + "BibTex\\zbMATH\\100Groups.bib");
		
//		//add JSON repos
//		docs.add(repoFolder + "JSON\\SIRA\\alle.js");
		
		for(String doc : docs)
			parseDocument(doc, outputFolder, factory.makeParser(doc));
		System.out.println("All done.");
	}
	
	private static void parseDocument(String doc, String outputFolder, NoSQLParser parser){
		System.out.println("Parsing file " + doc + "...");
		ArrayList<FormalObject> importedContext = new ArrayList<FormalObject>();
		importedContext = parser.parseFile(doc);
		FormalContext fc = new FormalContext();
		for(FormalObject object : importedContext) fc.addObject(object);
		fc.exportFormatted(outputFolder + parser.getTargetFilename(doc));
		System.out.println("Parsed file " + doc);
	}
}