package parsers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import datastructures.FormalObject;

public class XMLParser implements NoSQLParser {
	
	private String wantedObjects = "info";
	private String nameAttribute = "type";
	
	public ArrayList<FormalObject> parseFile(String file){
		ArrayList<Element> wantedElements = extractElements(file); //split the input file
		return createFormalObjects(wantedElements); //extract attributes from split objects		
	}

	private ArrayList<Element> extractElements(String file) {
		ArrayList<Element> wantedElements = new ArrayList<Element>();
		ElementFilter ef = new ElementFilter();
		try {
			File inputFile = new File(file);
			SAXBuilder saxBuilder = new SAXBuilder();
			Document document = saxBuilder.build(inputFile);
			Element rootElement = document.getRootElement();
			Iterator<Element> allElementsIterator = rootElement.getDescendants(ef);
			while(allElementsIterator.hasNext()) {
				Element currentElement = allElementsIterator.next();
				if(currentElement.getName().equals(wantedObjects)) wantedElements.add(currentElement);
			}
		}
		catch (JDOMException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		return wantedElements;
	}
	
	private ArrayList<FormalObject> createFormalObjects(ArrayList<Element> elements) {
		ArrayList<FormalObject> parsedObjects = new ArrayList<FormalObject>();
		System.out.print("Parsing objects to context... ");
		//extract the attributes from each object
		for(Element el : elements){
			FormalObject object = new FormalObject();
			ArrayList<String> attributes = new ArrayList<String>();
			List<Element> currentElements = el.getChildren();
			for(Element childEl : currentElements){
				String attribute = childEl.getName().trim();
				if(!attributes.contains(attribute)) attributes.add(attribute);
			}
			object.setAttributes(attributes);
//			object.setName(el.getAttributeValue(nameAttribute)); //specify here which attribute is the name, OR...
			object.setName(el.getChildTextNormalize(nameAttribute)); //... which child element is the name (comment one line out)
			parsedObjects.add(object);
		}
		System.out.println("done.");
		return parsedObjects;
	}
	
	@Override
	public String getTargetContextFilename(String doc) {
		return "xml_" + doc.substring(doc.lastIndexOf("\\")+1, doc.length()-4) + ".cxt"; //hard-coding the file format at the moment
	}

	@Override
	public String getTargetLatticeFilename(String doc) {
		return "xml_" + doc.substring(doc.lastIndexOf("\\")+1, doc.length()-4) + ".dot";
	}
}
