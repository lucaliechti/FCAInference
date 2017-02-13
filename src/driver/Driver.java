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
		String iesl100 = "C:\\Users\\Luca Liechti\\Desktop\\IESL100";
		String iesl1000 = "C:\\Users\\Luca Liechti\\Desktop\\IESL1000";
		String repoFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\NoSQL repos\\";
		String outputFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\context files\\";	
		String graphvizFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\graphviz files\\";
		ArrayList<String> docs100 = new ArrayList<String>();
		ArrayList<String> docs1000 = new ArrayList<String>();
		ParserFactory factory = new ParserFactory();
		
		//CONFIGURE HERE
		double mergeStop = 0d;
		Boolean deleteRareAttributes = false;
		Boolean retroFitSingletons = true;
		
		//add XML repos
		docs1000.add(repoFolder + "XML\\DBLP\\1000Complexity.xml");
		docs1000.add(repoFolder + "XML\\DBLP\\1000Database.xml");
		docs1000.add(repoFolder + "XML\\DBLP\\1000Inference.xml");
		docs1000.add(repoFolder + "XML\\DBLP\\1000Lattice.xml");
		docs1000.add(repoFolder + "XML\\DBLP\\1000Library.xml");
		docs1000.add(repoFolder + "XML\\DBLP\\1000Schema.xml");
		
		//add BibTex repos
		docs100.add(repoFolder + "BibTex\\scg.bib");
		docs100.add(repoFolder + "BibTex\\listb.bib");
//		docs100.add(repoFolder + "BibTex\\zbMATH\\100Algebra.bib");
//		docs100.add(repoFolder + "BibTex\\zbMATH\\100Complexity.bib");
//		docs100.add(repoFolder + "BibTex\\zbMATH\\100Groups.bib");
//		docs100.add(repoFolder + "BibTex\\zbMATH\\100Inference.bib");
//		docs100.add(repoFolder + "BibTex\\zbMATH\\100Lattice.bib");
//		docs100.add(repoFolder + "BibTex\\zbMATH\\100Schema.bib");
//		docs100.add(repoFolder + "BibTex\\zbMATH\\500.bib");
		
		//add JSON repos
		docs1000.add(repoFolder + "JSON\\SIRA\\alle.js");

//		//PARSING SINGLE FILES
//		for(String doc : docs100)
//			parseDocument(doc, outputFolder, graphvizFolder, factory.makeParser(doc), retroFitSingletons, deleteRareAttributes, mergeStop, 100);
//		for(String doc : docs1000)
//			parseDocument(doc, outputFolder, graphvizFolder, factory.makeParser(doc), retroFitSingletons, deleteRareAttributes, mergeStop, 1000);
		
		//PARSING ALL FILES IN FOLDER
//		parseFolder(repoFolder + "BibTex\\zbMATH100", outputFolder, graphvizFolder, factory, retroFitSingletons, deleteRareAttributes, mergeStop, 100);
//		parseFolder(repoFolder + "BibTex\\zbMATH500", outputFolder, graphvizFolder, factory, retroFitSingletons, deleteRareAttributes, mergeStop, 500);
		parseFolder(iesl100, outputFolder, graphvizFolder, factory, retroFitSingletons, deleteRareAttributes, mergeStop, 100);
//		parseFolder(iesl1000, outputFolder, graphvizFolder, factory, retroFitSingletons, deleteRareAttributes, mergeStop, 1000);
		
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
		
		Boolean noOwnAttr = true; //prevent merges from happening into nodes with own attributes!
		
		String graphvizString = "::" + fileName(doc) + "\ndot \"" + graphvizFolder + "0a_original_" + fileName(doc) + ".dot\" -Tpng -o \"" + graphvizFolder + "output\\0a_original_" + fileName(doc) + ".png\"\n";
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
			lattice.exportLatticeToFile(graphvizFolder + "0b_withoutRareAttributes_" + parser.getTargetLatticeFilename(doc));
			graphvizString += "dot \"" + graphvizFolder + "0b_withoutRareAttributes_" + fileName(doc) + ".dot\" -Tpng -o \"" + graphvizFolder + "output\\0b_withoutRareAttributes_" + fileName(doc) + ".png\"\n";
		}
		
//		///SINGLETONS PT. 1 VERSION 1///
//		if(retroFitSingletons){
//			cc.removeSingletonObjects();
//			lattice.clear();
//			lattice = lb.buildLattice();
//			System.out.println("noSing1\t---" + "\t" + lattice.latticeStats());	//if we have deleted singleton objects
//			lattice.exportLatticeToFile(graphvizFolder + "0c_withoutSingletons_" + parser.getTargetLatticeFilename(doc));
//			graphvizString += "dot \"" + graphvizFolder + "0c_withoutSingletons_" + fileName(doc) + ".dot\" -Tpng -o \"" + graphvizFolder + "output\\0c_withoutSingletons_" + fileName(doc) + ".png\"\n";
//		}
		
		///SINGLETONS PT. 1 VERSION 2///
		if(retroFitSingletons){
			cc.removeActualSingletonObjects();
			lattice.clear();
			lattice = lb.buildLattice();
			System.out.println("noSing2\t---" + "\t" + lattice.latticeStats());	//if we have deleted singleton objects
			lattice.exportLatticeToFile(graphvizFolder + "0c_withoutSingletons2_" + parser.getTargetLatticeFilename(doc));
			graphvizString += "dot \"" + graphvizFolder + "0c_withoutSingletons2_" + fileName(doc) + ".dot\" -Tpng -o \"" + graphvizFolder + "output\\0c_withoutSingletons2_" + fileName(doc) + ".png\"\n";
		}		
	
		///TINKER///
		double score = cc.tinker(noOwnAttr);
		int i = 1;
		while(score > mergeStop) {
			lattice.clear();
			lattice = lb.buildLattice();
			System.out.println(i + "\t" + String.format("%.1f", score) + "\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + (i++) + "_" + parser.getTargetLatticeFilename(doc));
			graphvizString += "dot \"" + graphvizFolder + (i-1) + "_" + fileName(doc) + ".dot\" -Tpng -o \"" + graphvizFolder + "output\\" + (i-1) + "_" + fileName(doc) + ".png\"\n";
			score = cc.tinker(noOwnAttr);
		}
		System.out.println("final (" + (--i) + ")\t" + lattice.latticeStats());
		
		///SINGLETONS PT. 2///
		if(retroFitSingletons) {
			lattice.retrofitSingletons();
			System.out.println("retfit\t\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + i + "_retroFit_" + parser.getTargetLatticeFilename(doc));
			graphvizString += "dot \"" + graphvizFolder + i + "_retroFit_" + fileName(doc) + ".dot\" -Tpng -o \"" + graphvizFolder + "output\\" + i + "_retroFit_" + fileName(doc) + ".png\"\n";
		}
		
		graphvizString += "\n";
		System.out.println("------------------------------------------------------------------------------------------------------------\n\n");
		System.out.println(graphvizString);
	}
	
	private static void parseFolder(String inFolder, String outFolder, String gvFolder, ParserFactory fac, Boolean retroFitSingletons, Boolean deleteRareAttributes, double mergeStop, int obj) {
		File fold = new File(inFolder);
		assert(fold.isDirectory());
		String[] inFiles = fold.list();
		for(int i = 0; i < inFiles.length; i++)
			parseDocument(inFolder + "\\" + inFiles[i], outFolder, gvFolder, fac.makeParser(inFiles[i]), retroFitSingletons, deleteRareAttributes, mergeStop, obj);
	}

	private static String fileName(String path) {
		return path.substring(path.lastIndexOf("\\")+1, path.length()-4);
	}
}