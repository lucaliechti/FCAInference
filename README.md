## Synopsis

This tool reads XML, JSON, and BibTeX files and performs some schema cleaning operations. The following actions are performed:
1. From an input file, a formal context is created and output to Burmeister's .cxt file format.
2. From the formal context, the concept lattice is computed and output as a .dot file that can be converted to various file formats with GraphViz.
3. Our parametrizable algorithm is used to simplify the formal context obtained from the files. The lattice is recomputed after every step and output as a .dot file as well.
4. Commands are printed out to that allow conversion of the .dot files to other file formats.

FCAInference was created as part of Luca Liechti's B.Sc. thesis at the University of Bern. In these notes, it is assumed that whoever wants to work with FCAInference is familiar with the main concepts and ideas of the thesis.

## Installation

We do not provide a special installer. You will run the tool right from Eclipse. Simply clone the GitHub repo and import the resulting folder as an Eclipse Java Project. All used libraries are in the `lib` folder. You will need to have [Graphviz](http://www.graphviz.org/Download.php) installed on your system in order to convert the .dot files.

FCAInference is tested under Windows 10 and with Eclipse 4.6.3.

## Working with FCAInference

FCAInference has a lot of options for customisation. All parametrization logic is in the `main` method in `Driver.java`, which is in the `driver` package.

**Adding semi-structured data files**

It is possible to infer a schema for multiple files of different formats at the same time. We use an ArrayList named `datasets` and simply process all `SemiStructuredDataset` type objects it holds.
The default location for input files is the `input` folder in the project directory. You can change this, and similarly the output folders for the context and lattice data, by changing the values of the variables `inputFolder`, `outputFolder`, and `graphvizFolder`.
In order to add a file from your input folder to the list of files to be processed, write statements like the following:

```java
datasets.add(new BibtexDataset(inputFolder + "some_bib_filename.bib"));
datasets.add(new JSONdataset(inputFolder + "semistructured_json_file.js", "book", "type"));
datasets.add(new XMLdataset(inputFolder + "animals.xml", "animal", "name", "attribute"));
```

For BibTeX datasets, only the filename is needed in addition to the input folder. For JSON and XML files, we pass two more arguments; first the name of the elements we view as our base level, and second the name of the element where type information is stored. This may not exist in each such file---simply pass `""` then. XML files have yet another parameter: a string that must either be "attribute" or "element". Depending on that value, the type is read from an attribute (of the base level element) or of one of its child elements with the name specified in the second parameter.
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

Passing the arguments `"animal", "name", "attribute"` to the `XMLdataset` constructor means that we want the objects of our context to be all `animal` elements, the attributes all child elements of `animal`, and that we want each `animal`'s `name` attribute---not  child element---to be its type. Measuring whether we mix different types together or whether we keep the type ontology clean is one of the main goals of this tool and is reflected in several of its output indexes.

**Parametrizing the inference**

There are 6 parameters that can be set by the user:

```java
double mergeStop = 0d;
int numberOfObjects = 1000;
		
Boolean firstOption = false;
Boolean secondOption = false;
Boolean secondSubOption = false;
Boolean thirdOption = true;
```

1. The `mergeStop` variable sets the *mergeScore* that a potential merge must at least have in order for us to perform it.
2. `numberOfObjects` sets an upper bound to the number of objects considered for the lattice. This is important because large lattices are extremely time costly to compute. Note that this parameter has no influence on the number of objects considered for the formal context file---this file always takes the whole dataset as input.
3. The four boolean variables correspond to the parameters we tested the algorithm with, them being `true` meaning that the option is "turned on", i.e. considered by the algorithm. These booleans are freely combineable, except for `secondSubOption`, whose value has no influence on the algorithm if `secondOption` is `false`.

## A word on architecture

Without going into too much detail, we are giving the interested reader a short account of how FCAInference is structured.

**Package structure**

Three packages are especially important for the inference logic. In the `driver` package, the `Driver.java` class, as its name would suggest, "drives" the whole process. It calls all important methods and keeps track of the state of the inference. In the `datastructures` package, we have all the logic that has to do with formal contexts and lattices. And in `parsers`, we have a parser for each file format; these parsers are all implementing a common interface.

**Program flow**

In the `main` method of `Driver.java`, the datasets that are to be processed are selected and all import parameters are set (see above). This method then calls the `parseDocument` method that processes each of these files and saves the results to disk.

First, a `FormalContext` object is created to which the parsed `FormalObject` objects are added. Then, the logic for computing the lattice from that context is externalized to a `LatticeBuilder` (from the `driver` package). The computed lattice (`Lattice` object from the `datastructures` package) is passed to a `ContextCleanser` (`Driver` package) that in turn executes our algorithm according to parametrization.  Lastly, the control returns to the `Driver` class that outputs commands for automatically converting the obtained lattices as well as statistics from the merge process. The lattice and context files themselves are saved by their respective classes (`Lattice` and `FormalContext`).

## License

This project is released under the [WTFPL](http://www.wtfpl.net/).

