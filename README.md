## Synopsis

This tool reads XML, JSON, and BibTeX files and performs some schema cleaning operations. The following actions are performed:
1. From an input file, a formal context is created and output to Burmeister's .cxt file format.
2. From the formal context, the concept lattice is computed and output as a .dot file that can be converted to various file formats with GraphViz.
3. Our parametrizable algorithm is used to simplify the formal context obtained from the files. The lattice is recomputed after every step and output as a .dot file as well.
4. Commands are printed out to that allow conversion of the .dot files to other file formats.

## Installation

We do not provide a special installer. You will run the tool right from Eclipse. Simply clone the GitHub repo and import the resulting folder as an Eclipse Java Project. All used libraries are in the `lib` folder. You will need to have [Graphviz](http://www.graphviz.org/Download.php) installed on your system in order to convert the .dot files.
The tool is tested under Windows 10 and with Eclipse 4.6.3.

## Working with FCAInference

FCAInference has a lot of options for customisation. All parametrization logic is in the `main` method in `Driver.java`, which is in the `Drivers` package.

###Adding semi-structured data files

It is possible to infer a schema for multiple files of different formats at the same time. We use an ArrayList named `datasets` and simply process all `SemiStructuredDataset` type objects it holds.
The default location for input files is the `input` folder in the project directory. You can change this, and similarly the output folders for the context and lattice data, by changing the values of the variables `inputFolder`, `outputFolder`, and `graphvizFolder`.
In order to add a file from your input folder to the list of files to be processed, write statements like the following:

```java
datasets.add(new BibtexDataset(inputFolder + "some_bib_filename.bib"));
datasets.add(new XMLdataset(inputFolder + "animals.xml", "animal", "name"));
datasets.add(new JSONdataset(inputFolder + "semistructured_json_file.js", "book", "type"));
```

For BibTeX datasets, only the filename is needed in addition to the input folder. For JSON and XML files, we pass two more arguments; first the name of the elements we view as our base level, and second the name of the element where type information is stored. This may not exist in each such file---simply pass `""` then.
We include in the repo an example XML file, animals.xml:

```xml
<zoo>
	<animal name = "Cat">
		<four-legged></four-legged>
		<hair-covered></hair-covered>
	</animal>
	<animal name = "Dog">
		<four-legged></four-legged>
		<hair-covered></hair-covered>
	</animal>
	<animal name = "Gibbon">
		<hair-covered></hair-covered>
		<thumbed></thumbed>
		<intelligent></intelligent>
	</animal>
	<animal name = "Human">
		<thumbed></thumbed>
		<intelligent></intelligent>
	</animal>
	<animal name = "Dolphin">
		<intelligent></intelligent>
		<marine></marine>
	</animal>
	<animal name = "Whale">
		<intelligent></intelligent>
		<marine></marine>
	</animal>
</zoo>
```

Passing the arguments `"animal", "name"` to the `XMLdataset` constructor means that we want the objects of our context to be all `animal` elements, the attributes all child elements of `animal`, and that we want each `animal`'s `name`---where it exists---to be its type. Measuring whether we mix different types together or whether we keep the type ontology clean is one of the main goals of this tool and is reflected in several of its output indexes.

###Parametrizing the inference


Parametrization instructions here.

## License

This project is `released under the WTFPL (http://www.wtfpl.net/).

