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
		docs.add(repoFolder + "XML\\DBLP\\1000Lattice.xml");		//
//		docs.add(repoFolder + "XML\\DBLP\\316NoSql.xml");
		docs.add(repoFolder + "XML\\DBLP\\1000FCA.xml");
		docs.add(repoFolder + "XML\\DBLP\\1000Schema.xml");
		
		//add BibTex repos
//		docs.add(repoFolder + "BibTex\\BordatTest.bib");
//		docs.add(repoFolder + "BibTex\\BordatTest3.bib");
		docs.add(repoFolder + "BibTex\\scg.bib");					//
		docs.add(repoFolder + "BibTex\\listb.bib");					//
		docs.add(repoFolder + "BibTex\\zbMATH\\100Lattice.bib");	//
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Schema.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Algebra.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Groups.bib");
		
//		//add JSON repos
//		docs.add(repoFolder + "JSON\\SIRA\\alle.js");				//

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
		lattice.exportLatticeToFile(graphvizFolder + "0a_" + parser.getTargetLatticeFilename(doc));
		
		System.out.println("Nr\tScore\tNodes\tWithOwn\tedges\tindex\tclean\tnull\tleg");
		System.out.println("orig\t---" + "\t" + lattice.latticeStats());
		ContextCleanser cc = new ContextCleanser(fc, lattice);
		cc.removeSingletonObjects();
//		cc.removeRareAttributes(0);
		lattice.clear();
		lattice = lb.buildLattice();
//		System.out.println("del\t---" + "\t" + lattice.latticeStats());	//if we have deleted rare attributes
		System.out.println("2+\t---" + "\t" + lattice.latticeStats());	//if we have deleted singleton objects
		lattice.exportLatticeToFile(graphvizFolder + "0b_" + parser.getTargetLatticeFilename(doc));
		double score = 1d;
		int i = 1;
		while(score > 1d) {
			// the if(score > 0d) line should logically go here, and we would print the result anyways, 
			//except in the first round (because that's already printed above)
			score = cc.tinker();
			lattice.clear();
			lattice = lb.buildLattice();
			if(score > 0d) System.out.println(i + "\t" + String.format("%.2f", score) + "\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + (i++) + "_" + parser.getTargetLatticeFilename(doc));
		}
		lattice.retrofitSingletons();
		System.out.println("final\t\t" + lattice.latticeStats());
		lattice.exportLatticeToFile(graphvizFolder + "final_" + parser.getTargetLatticeFilename(doc));
//		System.out.println("performed " + (i-2) + " merges in total.");
	}
}