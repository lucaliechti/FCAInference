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
		String folder = "C:\\Users\\Luca Liechti\\Desktop\\IESL";
		String repoFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\NoSQL repos\\";
		String ieslFolder = "C:\\Users\\Luca Liechti\\Desktop\\IESL\\";
		String outputFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\context files\\";	
		String graphvizFolder = "C:\\Users\\Luca Liechti\\Dropbox\\Uni\\!BSc\\graphviz files\\";
		ArrayList<String> docs = new ArrayList<String>();
		ParserFactory factory = new ParserFactory();
		
		//CONFIGURE HERE
		double mergeStop = 0d;
		Boolean retroFitSingletons = false;
		Boolean deleteRareAttributes = true;
		
//		//add XML repos
//		docs.add(repoFolder + "XML\\mondial.xml");
//		docs.add(repoFolder + "XML\\SigmodRecord.xml");
//		docs.add(repoFolder + "XML\\ebay.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000Lattice.xml");		//
//		docs.add(repoFolder + "XML\\DBLP\\316NoSql.xml");
//		docs.add(repoFolder + "XML\\DBLP\\1000FCA.xml");			//
//		docs.add(repoFolder + "XML\\DBLP\\1000Schema.xml");			//
		
		//add IESL repos
//		docs.add(ieslFolder + "gp-bibliography.bib");
//		docs.add(ieslFolder + "visinfo.zib.de#EVlib#Bibliography#EVL-1998.bib");
		
		//add BibTex repos
//		docs.add(repoFolder + "BibTex\\BordatTest.bib");
//		docs.add(repoFolder + "BibTex\\Test2.bib");
//		docs.add(repoFolder + "BibTex\\scg.bib");					//
//		docs.add(repoFolder + "BibTex\\listb.bib");					//
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Lattice.bib");	//
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Schema.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Algebra.bib");
//		docs.add(repoFolder + "BibTex\\zbMATH\\100Groups.bib");
		
//		//add JSON repos
//		docs.add(repoFolder + "JSON\\SIRA\\alle.js");				//

		//PARSING SINGLE FILES
//		for(String doc : docs)
//			parseDocument(doc, outputFolder, graphvizFolder, factory.makeParser(doc), retroFitSingletons, deleteRareAttributes, mergeStop);
		
		//PARSING ALL FILES IN FOLDER
		parseFolder(folder, outputFolder, graphvizFolder, factory, retroFitSingletons, deleteRareAttributes, mergeStop);
		
		System.out.println("All done.");
	}
	
	private static void parseDocument(String doc, String outputFolder, String graphvizFolder, NoSQLParser parser, Boolean retroFitSingletons, Boolean deleteRareAttributes, double mergeStop){
		System.out.println("Parsing file " + doc);
		ArrayList<FormalObject> importedContext = parser.parseFile(doc);
		FormalContext fc = new FormalContext();
		for(FormalObject object : importedContext)
			fc.addObject(object);
		fc.exportContextToFile(outputFolder + parser.getTargetContextFilename(doc));
		
		LatticeBuilder lb = new LatticeBuilder(fc);
		Lattice lattice = lb.buildLattice();
		lattice.exportLatticeToFile(graphvizFolder + "0a_original_" + parser.getTargetLatticeFilename(doc));
		
		System.out.println("\nNr\tScore\tNodes\tWithOwn\tedges\tindex\tclean\tnull\tleg");
		System.out.println("-------------------------------------------------------------------");
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
		int i = 0;
		while(score > mergeStop) {
			lattice.clear();
			lattice = lb.buildLattice();
			System.out.println(i + "\t" + String.format("%.2f", score) + "\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + (i++) + "_" + parser.getTargetLatticeFilename(doc));
			score = cc.tinker();
		}
		
		///SINGLETONS PT. 2///
		if(retroFitSingletons) {
			lattice.retrofitSingletons();
			System.out.println("retfit\t\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + i + "_retroFit_" + parser.getTargetLatticeFilename(doc));
		}
		
		System.out.println("-------------------------------------------------------------------\n\n");
	}
	
	private static void parseFolder(String inFolder, String outFolder, String gvFolder, ParserFactory fac, Boolean retroFitSingletons, Boolean deleteRareAttributes, double mergeStop) {
		File fold = new File(inFolder);
		assert(fold.isDirectory());
		String[] inFiles = fold.list();
		for(int i = 0; i < inFiles.length; i++)
			parseDocument(inFolder + "\\" + inFiles[i], outFolder, gvFolder, fac.makeParser(inFiles[i]), retroFitSingletons, deleteRareAttributes, mergeStop);
	}
}