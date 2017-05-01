package parsers;

import java.util.ArrayList;

import datasets.SemiStructuredDataset;
import datastructures.FormalObject;

public interface NoSQLParser {
	
	public ArrayList<FormalObject> parseFile(SemiStructuredDataset file, int nrObj);

	public String getTargetContextFilename(String doc);
	
	public String getTargetLatticeFilename(String doc);
}
