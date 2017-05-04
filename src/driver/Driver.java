package driver;

import java.util.ArrayList;

import datasets.*;
import parsers.NoSQLParser;
import datastructures.FormalContext;
import datastructures.FormalObject;
import datastructures.Lattice;
import factories.ParserFactory;

public class Driver {
	public static void main(String[] args){
		String inputFolder = System.getProperty("user.dir") + "\\interaction\\input\\";
		String outputFolder = System.getProperty("user.dir") + "\\interaction\\output-context\\";
		String graphvizFolder = System.getProperty("user.dir") + "\\interaction\\output-graph\\";
		ArrayList<SemiStructuredDataset> datasets = new ArrayList<SemiStructuredDataset>();
		ParserFactory factory = new ParserFactory();
		String graphvizOutputFormat = "svg"; //choose from "png", "svg", "jpg", "tif", and many others: http://www.graphviz.org/doc/info/output.html
		
		//add datasets to be processed
//		datasets.add(new BibtexDataset(inputFolder + "caltech_hp.bib"));
		datasets.add(new XMLdataset(inputFolder + "animals.xml", "animal", "name", "attribute"));
		datasets.add(new XMLdataset(inputFolder + "1000Complexity.xml", "info", "type", "element"));
//		datasets.add(new JSONdataset(inputFolder + "alle.js", "items", "file"));
		
		//CONFIGURE HERE: General parameters
		double mergeStop = 0d;
		int numberOfObjects = 1000;
		
		//CONFIGURE HERE: Parameters for the algorithm
		Boolean firstOption = false;
		Boolean secondOption = false;
		Boolean secondSubOption = false;
		Boolean thirdOption = true;
			
		for(SemiStructuredDataset dataset : datasets) {
			parseDocument(dataset, outputFolder, graphvizFolder, factory.makeParser(dataset.getFilePath()), 
					firstOption, secondOption, secondSubOption, thirdOption, mergeStop, numberOfObjects, graphvizOutputFormat);
		}

		System.out.println("All done.");
	}
	
	private static void parseDocument(SemiStructuredDataset dataset, String outputFolder, String graphvizFolder, NoSQLParser parser, 
			Boolean firstOption, Boolean secondOption, Boolean secondSubOption, Boolean thirdOption, double mergeStop, int numberOfObjects, String format){
		String fileName = dataset.getFilePath();
		System.out.println("Parsing file " + fileName);
		//create and save formal context
		ArrayList<FormalObject> importedContext = parser.parseFile(dataset, numberOfObjects);
		FormalContext fc = new FormalContext();
		for(FormalObject object : importedContext) {
			fc.createAndAddObject(object);
		}
		fc.exportContextToFile(outputFolder + parser.getTargetContextFilename(fileName));
		
		LatticeBuilder lb = new LatticeBuilder(fc);
		Lattice lattice = lb.buildLattice();
		lattice.computeAttributeCardinality();
		lattice.exportLatticeToFile(graphvizFolder + "0a_original_" + parser.getTargetLatticeFilename(fileName));
		
		String graphvizString = "::" + fileName(fileName) + "\ndot \"" + graphvizFolder + "0a_original_" + fileName(fileName) + ".dot\" -Tpng -o \"" + graphvizFolder + "images\\0a_original_" + fileName(fileName) + ".png\"\n";
		System.out.println("\nNr\tScore\tObjects\tTypes\tAttr\tNodes\tWithOwn\tedges\tindex\tmajor\tinClean\tnull\tleg\ttime");
		System.out.println("------------------------------------------------------------------------------------------------------------");
		System.out.println("orig\t---" + "\t" + lattice.latticeStats());
		ContextCleanser cc = new ContextCleanser(fc, lattice);
		
//		///RARE ATTRIBUTES///
//		//CURRENTLY NOT USED
//		if(deleteRareAttributes) {
//			cc.removeRareAttributes(0);
//			lattice.clear();
//			lattice = lb.buildLattice();
//			lattice.initialiseBookkeeping();
//			System.out.println("del\t---" + "\t" + lattice.latticeStats());	//if we have deleted rare attributes
//			lattice.exportLatticeToFile(graphvizFolder + "0b_withoutRareAttributes_" + parser.getTargetLatticeFilename(doc));
//			graphvizString += "dot \"" + graphvizFolder + "0b_withoutRareAttributes_" + fileName(doc) + ".dot\" -Tpng -o \"" + graphvizFolder + "output\\0b_withoutRareAttributes_" + fileName(doc) + ".png\"\n";
//		}
		
		///REMOVE UNIQUE///
		if(secondOption){
			cc.removeUniqueObjects();
			lattice.clear();
			lattice = lb.buildLattice();
			System.out.println("noSing2\t---" + "\t" + lattice.latticeStats());	//if we have deleted singleton objects
			lattice.exportLatticeToFile(graphvizFolder + "0c_withoutSingletons2_" + parser.getTargetLatticeFilename(fileName));
			graphvizString += "dot \"" + graphvizFolder + "0c_withoutSingletons2_" + fileName(fileName) + ".dot\" -T" + format + " -o \"" + graphvizFolder + "images\\0c_withoutSingletons2_" + fileName(fileName) + "." + format + "\"\n";
		}
	
		///LATTICEMERGE///
		double score = cc.latticeMerge(firstOption, thirdOption);
		int i = 1;
		while(score > mergeStop) {
			lattice.clear();
			lattice = lb.buildLattice();
			System.out.println(i + "\t" + String.format("%.1f", score) + "\t" + lattice.latticeStats()); /////////////////////this line makes everything super verbose!
			lattice.exportLatticeToFile(graphvizFolder + (i++) + "_" + parser.getTargetLatticeFilename(fileName));
			graphvizString += "dot \"" + graphvizFolder + (i-1) + "_" + fileName(fileName) + ".dot\" -T" + format + " -o \"" + graphvizFolder + "images\\" + (i-1) + "_" + fileName(fileName) + "." + format + "\"\n";
			score = cc.latticeMerge(firstOption, thirdOption);
		}
		System.out.println("final (" + (--i) + ")\t" + lattice.latticeStats());
		
		///FIT UNIQUES BACK IN///
		if(secondOption && secondSubOption) {
			lattice.retrofitSingletons();
			System.out.println("retfit\t\t" + lattice.latticeStats());
			lattice.exportLatticeToFile(graphvizFolder + i + "_retroFit_" + parser.getTargetLatticeFilename(fileName));
			graphvizString += "dot \"" + graphvizFolder + i + "_retroFit_" + fileName(fileName) + ".dot\" -T" + format + " -o \"" + graphvizFolder + "images\\" + i + "_retroFit_" + fileName(fileName) + "." + format + "\"\n";
		}
		
		graphvizString += "\n";
		System.out.println("------------------------------------------------------------------------------------------------------------\n\n");
		System.out.println(graphvizString);
	}

	private static String fileName(String path) {
		int endingLength = path.length() - path.lastIndexOf(".");
		return path.substring(path.lastIndexOf("\\")+1, path.length()-endingLength);
	}
}