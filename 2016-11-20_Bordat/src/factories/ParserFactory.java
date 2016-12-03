package factories;

import parsers.BibTexParser;
import parsers.JSONParser;
import parsers.NoSQLParser;
import parsers.XMLParser;

public class ParserFactory {

	public NoSQLParser makeParser(String doc) {
		switch(doc.substring(doc.lastIndexOf('.')+1)){
		case "xml":
			return new XMLParser();
		case "bib":
			return new BibTexParser();
		case "js":
			return new JSONParser();
		}
		return null;
	}

}
