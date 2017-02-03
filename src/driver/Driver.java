package driver;

import java.io.File;
import java.util.ArrayList;

import parsers.NoSQLParser;
import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;
import factories.ParserFactory;

public class Driver {
	public static void main(String[] args){
		String folder200 = "C:\\Users\\Luca Liechti\\Desktop\\IESL200";
		String folder2000 = "C:\\Users\\Luca Liechti\\Desktop\\IESL2000";
		String repoFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\NoSQL repos\\";
		String outputFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\context files\\";	
		String graphvizFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\graphviz files\\";
		ArrayList<String> docs200 = new ArrayList<String>();
		ArrayList<String> docs2000 = new ArrayList<String>();
		ParserFactory factory = new ParserFactory();
		
		//CONFIGURE HERE
		double mergeStop = 0d;
		Boolean deleteRareAttributes = false;
		Boolean retroFitSingletons = false;
		
//		//add XML repos
//		docs2000.add(repoFolder + "XML\\DBLP\\1000Lattice.xml");
//		docs2000.add(repoFolder + "XML\\DBLP\\316NoSql.xml");
//		docs2000.add(repoFolder + "XML\\DBLP\\1000FCA.xml");
//		docs2000.add(repoFolder + "XML\\DBLP\\1000Schema.xml");
		
		//add BibTex repos
//		docs200.add(repoFolder + "BibTex\\scg.bib");
//		docs200.add(repoFolder + "BibTex\\listb.bib");
//		docs2000.add(repoFolder + "BibTex\\zbMATH\\100Lattice.bib");
//		docs2000.add(repoFolder + "BibTex\\zbMATH\\100Schema.bib");
//		docs2000.add(repoFolder + "BibTex\\zbMATH\\100Algebra.bib");
//		docs2000.add(repoFolder + "BibTex\\zbMATH\\100Groups.bib");
		
//		//add JSON repos
		docs2000.add(repoFolder + "JSON\\SIRA\\alle.js");

		//PARSING SINGLE FILES
		for(String doc : docs200)
			parseDocument(doc, outputFolder, graphvizFolder, factory.makeParser(doc), retroFitSingletons, deleteRareAttributes, mergeStop, 200);
		for(String doc : docs2000)
			parseDocument(doc, outputFolder, graphvizFolder, factory.makeParser(doc), retroFitSingletons, deleteRareAttributes, mergeStop, 2000);
		
		//PARSING ALL FILES IN FOLDER
//		parseFolder(folder200, outputFolder, graphvizFolder, factory, retroFitSingletons, deleteRareAttributes, mergeStop, 200);
//		parseFolder(folder2000, outputFolder, graphvizFolder, factory, retroFitSingletons, deleteRareAttributes, mergeStop, 200);
		
		System.out.println("All done.");
	}
	
	private static void parseDocument(String doc, String outputFolder, String graphvizFolder, NoSQLParser parser, Boolean retroFitSingletons, Boolean deleteRareAttributes, double mergeStop, int obj){
		System.out.println("Parsing file " + doc);
		ArrayList<FormalObject> importedContext = parser.parseFile(doc, obj);
		FormalContext fc = new FormalContext();
		for(FormalObject object : importedContext)
			fc.createAndAddObject(object);
		fc.exportContextToFile(outputFolder + parser.getTargetContextFilename(doc));
		
		LatticeBuilder lb = new LatticeBuilder(fc);
		Lattice lattice = lb.buildLattice();
		lattice.exportLatticeToFile(graphvizFolder + "0a_original_" + parser.getTargetLatticeFilename(doc));
		
		System.out.println("\nNr\tScore\tObjects\tTypes\tAttr\tNodes\tWithOwn\tedges\tindex\tmajor\tinClean\tnull\tleg\ttime");
		System.out.println("------------------------------------------------------------------------------------------------------------");
		System.out.println("orig\t---" + "\t" + lattice.latticeStats());
		ContextCleanser cc = new ContextCleanser(fc, lattice);
		
		///RARE ATTRIBUTES///
		if(deleteRareAttributes) {
			cc.removeRareAttributes(0);
			lattice.clear();
			lattice = lb.buildLattice();
			lattice.initialiseBookkeeping();
			System.out.println("del\t---" + "\t" + lattice.latticeStats());	//if we have deleted rare attributes
			lattice.exportLatticeToFile(graphvizFolder + "0c_withoutRareAttributes_" + parser.getTargetLatticeFilename(doc));
		}
		
		///SINGLETONS PT. 1///
		if(retroFitSingletons){
			cc.removeSingletonObjects();
			lattice.clear();
			lattice = lb.buildLattice();
			System.out.println("noSing\t---" + "\t" + lattice.latticeStats());	//if we have deleted singleton objects
			lattice.exportLatticeToFile(graphvizFolder + "0b_withoutSingletons_" + parser.getTargetLatticeFilename(doc));
		}
	
		///TINKER///
		double score = cc.tinker();
		int i = 1;
		while(score > mergeStop) {
			lattice.clear();
			lattice = lb.buildLattice();
//			System.out.println(i + "\t" + String.format("%.2f", score) + "\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + (i++) + "_" + parser.getTargetLatticeFilename(doc));
			score = cc.tinker();
		}
		if(!retroFitSingletons) System.out.println("final (" + (--i) + ")\t" + lattice.latticeStats());
		
		///SINGLETONS PT. 2///
		if(retroFitSingletons) {
			lattice.retrofitSingletons();
			System.out.println("retfit\t\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + i + "_retroFit_" + parser.getTargetLatticeFilename(doc));
		}
		
		System.out.println("------------------------------------------------------------------------------------------------------------\n\n");
	}
	
	private static void parseFolder(String inFolder, String outFolder, String gvFolder, ParserFactory fac, Boolean retroFitSingletons, Boolean deleteRareAttributes, double mergeStop, int obj) {
		File fold = new File(inFolder);
		assert(fold.isDirectory());
		String[] inFiles = fold.list();
		for(int i = 0; i < inFiles.length; i++)
			parseDocument(inFolder + "\\" + inFiles[i], outFolder, gvFolder, fac.makeParser(inFiles[i]), retroFitSingletons, deleteRareAttributes, mergeStop, obj);
	}
}