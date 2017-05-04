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

import datasets.SemiStructuredDataset;
import datastructures.FormalObject;

public class XMLParser implements NoSQLParser {
	
	private String wantedObjects = null;
	private String nameAttribute = null;
	
	public ArrayList<FormalObject> parseFile(SemiStructuredDataset dataset, int MAX_OBJECTS){
		wantedObjects = dataset.getElementLevel();
		nameAttribute = dataset.getTypeAttribute();
		ArrayList<Element> wantedElements = extractElements(dataset.getFilePath(), MAX_OBJECTS); //split the input file
		return createFormalObjects(wantedElements, dataset.getTypeType()); //extract attributes from split objects		
	}

	private ArrayList<Element> extractElements(String file, int MAX_OBJECTS) {
		ArrayList<Element> wantedElements = new ArrayList<Element>();
		ElementFilter ef = new ElementFilter();
		int numberOfParsedObjects = 0;
		try {
			File inputFile = new File(file);
			SAXBuilder saxBuilder = new SAXBuilder();
			Document document = saxBuilder.build(inputFile);
			Element rootElement = document.getRootElement();
			Iterator<Element> allElementsIterator = rootElement.getDescendants(ef);
			while(allElementsIterator.hasNext()) {
				Element currentElement = allElementsIterator.next();
				if(currentElement.getName().equals(wantedObjects) && (MAX_OBJECTS == 0 || numberOfParsedObjects < MAX_OBJECTS)) {
					wantedElements.add(currentElement);
					numberOfParsedObjects++;
				}
			}
		}
		catch (JDOMException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		assert ((MAX_OBJECTS == 0 || numberOfParsedObjects <= MAX_OBJECTS) && wantedElements.size() <= MAX_OBJECTS);
		return wantedElements;
	}
	
	//typeType tells us where the type information is stored. "attribute" means in an attribute of name typeAttribute, "element" means it's the value of a child element of that name.
	private ArrayList<FormalObject> createFormalObjects(ArrayList<Element> elements, String typeType) {
		ArrayList<FormalObject> parsedObjects = new ArrayList<FormalObject>();
//		System.out.print("Parsing objects to context... ");
		
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
			if(typeType.equals("attribute")) {
				object.setName(el.getAttributeValue(nameAttribute));
			}
			else if(typeType.equals("element")) {
				object.setName(el.getChildTextNormalize(nameAttribute));
			}
			else {
				object.setName("NO TYPE FOUND");
			}
			parsedObjects.add(object);
		}
//		System.out.println("done.");
		return parsedObjects;
	}
	
	@Override
	public String getTargetContextFilename(String doc) {
		return "xml_" + doc.substring(doc.lastIndexOf("\\")+1, doc.length()-4) + ".cxt"; //hard-coding the file format at the moment
	}

	@Override
	public String getTargetLatticeFilename(String doc) {
		return doc.substring(doc.lastIndexOf("\\")+1, doc.length()-4) + ".dot";
	}
}
