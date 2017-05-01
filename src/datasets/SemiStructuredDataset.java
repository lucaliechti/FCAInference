package datasets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SemiStructuredDataset {
	
	private File file = null;
	private String elementLevel = null;
	private String typeAttribute = null;
	
	//constructor for XML and JSON files with elementLevel and typeAttribute
	public SemiStructuredDataset(String _location, String _elementLevel, String _typeAttribute) {
		try{
			setFile(_location);
		}
		catch(IOException ioe) {ioe.printStackTrace();}
		elementLevel = _elementLevel;
		typeAttribute = _typeAttribute;
	}
	
	//constructor for BibTeX files where no elementLevel and typeAttribute are needed
	public SemiStructuredDataset(String _location) {
		try{
			setFile(_location);
		}
		catch(IOException ioe) {ioe.printStackTrace();}
	}
	
	private void setFile(String location) throws IOException, FileNotFoundException {
		file = new File(location);
	}	
	
	public File getFile() {
		return file;
	}
	
	public String getFilePath() {
		return file.toString();
	}
	
	public String getElementLevel() {
		return elementLevel;
	}
	
	public String getTypeAttribute() {
		return typeAttribute;
	}

}
