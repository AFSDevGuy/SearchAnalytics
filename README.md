**Query Analyzer Pipeline Tools**

This codebase implements a chain of tools that perform query analysis. Currently, the
ultimate output of the toolchain is a cluster analysis, but other types of processing
can also be performed.

The toolchain is a series of Java programs that should be executed in sequence.

1. Convert the input Excel file to an XML file via _RawLogXmlConverter_
2. Remove duplicate queries using _DuplicateXmlFilter_
3. Filter out stopwords using _StopwordXmlFilter_
4. Lookup/compute LSI vector for each query with _LsiVectorCreationFilter_. This 
requires that you have a PhaneroDB LSI database instance set up and available. You
provide connection information via the command line.
5. Compute KMeans cluster(s) via _KMeansCreationProcessor_

All of the tools above understand the -infile and -outfile command line options. Note
that the -infile parameter can accept more than one filename, and it must be the **last**
command line parameter.

Some tools (4 and 5 come to mind) require additional command line parameters. Consult the
source file for the specific options and their meaning(s).