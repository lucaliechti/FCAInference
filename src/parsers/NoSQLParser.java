package parsers;

import java.util.ArrayList;

import datastructures.FormalObject;

public interface NoSQLParser {
	
	public ArrayList<FormalObject> parseFile(String file, int nrObj);

	public String getTargetContextFilename(String doc);
	
	public String getTargetLatticeFilename(String doc);
}
