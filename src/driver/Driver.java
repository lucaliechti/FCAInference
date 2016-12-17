package driver;

import java.util.ArrayList;

import parsers.NoSQLParser;
import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;
import factories.ParserFactory;

public class Driver {
	public static void main(String[] args){
		String repoFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\NoSQL repos\\";
		String outputFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\context files\\";	
		String graphvizFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\graphviz files\\";
		ArrayList<String> docs = new ArrayList<String>();
		ParserFactory factory = new ParserFactory();
		
//		//add XML repos
//		docs.add(repoFolder + "XML\\mondial.xml");
//		docs.add(repoFolder + "XML\\SigmodRecord.xml");
//		docs.add(repoFolder + "XML\\ebay.xml");
		docs.add(repoFolder + "XML\\DBLP\\1000Lattice.xml");
//		docs.add(repoFolder + "XML\\DBLP\\316NoSql.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000FCA.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000Schema.xml");
		
		//add BibTex repos
//		docs.add(repoFolder + "BibTex\\BordatTest.bib");
//		docs.add(repoFolder + "BibTex\\scg.bib");
//		docs.add(repoFolder + "BibTex\\listb.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Lattice.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Schema.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Algebra.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Groups.bib");
		
//		//add JSON repos
//		docs.add(repoFolder + "JSON\\SIRA\\alle.js");

		for(String doc : docs)
			parseDocument(doc, outputFolder, graphvizFolder, factory.makeParser(doc));
		System.out.println("All done.");
	}
	
	private static void parseDocument(String doc, String outputFolder, String graphvizFolder, NoSQLParser parser){
		System.out.println("Parsing file " + doc);
		ArrayList<FormalObject> importedContext = parser.parseFile(doc);
		FormalContext fc = new FormalContext();
		for(FormalObject object : importedContext){
			fc.addObject(object);
		}
		fc.exportContextToFile(outputFolder + parser.getTargetContextFilename(doc));
		
		LatticeBuilder lb = new LatticeBuilder(fc);
		Lattice lattice = lb.buildLattice();
		lattice.exportLatticeToFile(graphvizFolder + parser.getTargetLatticeFilename(doc));
		
		ContextCleanser lc = new ContextCleanser(fc, lattice);
		lc.mergeNodes(10, 1);
		LatticeBuilder lb2 = new LatticeBuilder(fc);
		Lattice lattice2 = lb2.buildLattice();
		System.out.println("Lattice stats before: " + lattice2.latticeStats());
		System.out.println("---END CLEANSING---");
		lattice2.exportLatticeToFile(graphvizFolder + "1" + parser.getTargetLatticeFilename(doc));
	}
}