package logic;

import java.io.*;
import java.util.*;
import javax.swing.JTextArea;


//http://imacwww.epfl.ch/Team../Raphael/BookWiley2003/java-illustrations/ID3Standard/ID3.java.html

/**
 *
 * @author Daniel y Ricardo
 */
public class ID3 {

    
        JTextArea area;

        // Número de atributos que tienen los ficheros
	int numAttributes;
        
        //Nombre de los atributos, el último atributo es el atributo de salida (la respuesta)
	String []attributeNames;	
        
        /*
        Un array de vectores
        
        Cada elemento del array indica un atributo (de 0 a n-1 siendo n el nº de atributos) 
        
        Cada elemento del vector, es un estado del atributo
        
        */
	Vector []domains;

	/*  The class to represent a data point consisting of numAttributes values of attributes  */
	public class DataPoint {

		/* The values of all attributes stored in this array.  i-th element in this array
		   is the index to the element in the vector domains representing the symbolic value of
		   the attribute.  For example, if attributes[2] is 1, then the actual value of the
		   2-nd attribute is obtained by domains[2].elementAt(1).  This representation makes
		   comparing values of attributes easier - it involves only integer comparison and
		   no string comparison.
		   The last attribute is the output attribute
		*/
		public int []attributes;

		public DataPoint(int numattributes) {
			attributes = new int[numattributes];
		}
	};


	/* The class to represent a node in the decomposition tree.
	*/
	public class TreeNode {
		public double entropy;			// The entropy of data points if this node is a leaf node
		public Vector data;			// The set of data points if this is a leaf node
		public int decompositionAttribute;	// If this is not a leaf node, the attribute that is used to divide the set of data points
		public int decompositionValue;		// the attribute-value that is used to divide the parent node
		public TreeNode []children;		// If this is not a leaf node, references to the children nodes
		public TreeNode parent;			// The parent to this node.  The root has parent == null

		public TreeNode() {
			data = new Vector();
		}

	};

	/*  The root of the decomposition tree  */
	TreeNode root = new TreeNode();


	/*  This function returns an integer corresponding to the symbolic value of the attribute.
		If the symbol does not exist in the domain, the symbol is added to the domain of the attribute
	*/
	public int getSymbolValue(int attribute, String symbol) {
		int index = domains[attribute].indexOf(symbol);
		if (index < 0) {
			domains[attribute].addElement(symbol);
			return domains[attribute].size() -1;
		}
		return index;
	}

	/*  Returns all the values of the specified attribute in the data set  */
	public int []getAllValues(Vector data, int attribute) {
		Vector values = new Vector();
		int num = data.size();
		for (int i=0; i< num; i++) {
			DataPoint point = (DataPoint)data.elementAt(i);
			String symbol = (String)domains[attribute].elementAt(point.attributes[attribute] );
			int index = values.indexOf(symbol);
			if (index < 0) {
				values.addElement(symbol);
			}
		}

		int []array = new int[values.size()];
		for (int i=0; i< array.length; i++) {
			String symbol = (String)values.elementAt(i);
			array[i] = domains[attribute].indexOf(symbol);
		}
		values = null;
		return array;
	}


	/*  Returns a subset of data, in which the value of the specfied attribute of all data points is the specified value  */
	public Vector getSubset(Vector data, int attribute, int value) {
		Vector subset = new Vector();

		int num = data.size();
		for (int i=0; i< num; i++) {
			DataPoint point = (DataPoint)data.elementAt(i);
			if (point.attributes[attribute] == value) subset.addElement(point);
		}
		return subset;

	}


	/*  Calculates the entropy of the set of data points.
		The entropy is calculated using the values of the output attribute which is the last element in the array attribtues
	*/
	public double calculateEntropy(Vector data) {

		int numdata = data.size();
		if (numdata == 0) return 0;

		int attribute = numAttributes-1;
		int numvalues = domains[attribute].size();
		double sum = 0;
		for (int i=0; i< numvalues; i++) {
			int count=0;
			for (int j=0; j< numdata; j++) {
				DataPoint point = (DataPoint)data.elementAt(j);
				if (point.attributes[attribute] == i) count++;
			}
			double probability = 1.*count/numdata;
			if (count > 0) sum += -probability*Math.log(probability);
		}
		return sum;

	}

	/*  This function checks if the specified attribute is used to decompose the data set
		in any of the parents of the specfied node in the decomposition tree.
		Recursively checks the specified node as well as all parents
	*/
	public boolean alreadyUsedToDecompose(TreeNode node, int attribute) {
		if (node.children != null) {
			if (node.decompositionAttribute == attribute )
				return true;
		}
		if (node.parent == null) return false;
		return alreadyUsedToDecompose(node.parent, attribute);
	}

	/*  This function decomposes the specified node according to the ID3 algorithm.
		Recursively divides all children nodes until it is not possible to divide any further
                I have changed this code from my earlier version. I believe that the code
                in my earlier version prevents useless decomposition and results in a better decision tree!
                This is a more faithful implementation of the standard ID3 algorithm
	*/
	public void decomposeNode(TreeNode node) {

		double bestEntropy=0;
		boolean selected=false;
		int selectedAttribute=0;

		int numdata = node.data.size();
		int numinputattributes = numAttributes-1;
                node.entropy = calculateEntropy(node.data);
		if (node.entropy == 0) return;

		/*  In the following two loops, the best attribute is located which
			causes maximum decrease in entropy
		*/
		for (int i=0; i< numinputattributes; i++) {
			int numvalues = domains[i].size();
                        if ( alreadyUsedToDecompose(node, i) ) continue;
                        // Use the following variable to store the entropy for the test node created with the attribute i
                        double averageentropy = 0;
			for (int j=0; j< numvalues; j++) {
				Vector subset = getSubset(node.data, i, j);
				if (subset.size() == 0) continue;
				double subentropy = calculateEntropy(subset);
                                averageentropy += subentropy * subset.size();  // Weighted sum
			}

                        averageentropy = averageentropy / numdata;   // Taking the weighted average
                        if (selected == false) {
                          selected = true;
                          bestEntropy = averageentropy;
                          selectedAttribute = i;
                        } else {
                          if (averageentropy < bestEntropy) {
                            selected = true;
                            bestEntropy = averageentropy;
                            selectedAttribute = i;
                          }
                        }

		}

		if (selected == false) return;

		// Now divide the dataset using the selected attribute
                int numvalues = domains[selectedAttribute].size();
		node.decompositionAttribute = selectedAttribute;
		node.children = new TreeNode [numvalues];
                for (int j=0; j< numvalues; j++) {
                  node.children[j] = new TreeNode();
                  node.children[j].parent = node;
                  node.children[j].data = getSubset(node.data, selectedAttribute, j);
                  node.children[j].decompositionValue = j;
                }

		// Recursively divides children nodes
                for (int j=0; j< numvalues; j++) {
                  decomposeNode(node.children[j]);
                }

		// There is no more any need to keep the original vector.  Release this memory
		node.data = null;		// Let the garbage collector recover this memory

	}


   	/**
         * Lee los datos de los fichero
         * @param attributes_file_name Nombre del fichero que contiene los atributos
         * @param values_file_name Nombre del fichero que contiene los valores
         * @return Si la lectura ha sido correcta o no
         * @throws Exception La excepción relacionada con la lectura de los ficheros
         */
   	public boolean readData(String attributes_file_name, String values_file_name)  throws Exception {

                
                //Nos indica si la lectura se ha realizado correctamente o no
                boolean readStatus = false;
            
                //Abrimos el fichero que contiene los atributos (AtributosJuego.txt)
                
      		FileInputStream in = null;

      		try {
         		File inputFile = new File(attributes_file_name);
	 		in = new FileInputStream(attributes_file_name);
      		} catch ( Exception e) {
			System.err.println( "Incapaz de abrir el fichero: " + attributes_file_name + "\n" + e);
			return readStatus;
      		}

      		BufferedReader attributes_file = new BufferedReader(new InputStreamReader(in) );
                
                //Leemos la línea del fichero
                
                String input;
               
                input = attributes_file.readLine();
                if (input == null) {
                        System.err.println( "No se han encontado datos en el fichero: " + attributes_file_name + "\n");
                        return readStatus;
                }

                //Creamos el StringTokenizer que separe los tokens por comas
                //Los atributos están separados por comas
     		StringTokenizer tokenizer = new StringTokenizer(input, ",");
                
                //Leemos el número de tokens
		numAttributes = tokenizer.countTokens();
		if (numAttributes <= 1) {
			System.err.println( "Ha ocurrido un error al leer la primera línea del fichero: " + attributes_file_name);
                        System.err.println( "La línea que ha fallado es la siguiente: " + attributes_file_name);
                        return readStatus;
                }

                //Creamos las estructuras correspondientes
		domains = new Vector[numAttributes];
                
		for (int i=0; i < numAttributes; i++) domains[i] = new Vector();
		attributeNames = new String[numAttributes];

                //Se introducen los nombres de los atributos en la estructura correspondiente
     		for (int i=0; i < numAttributes; i++) {
         		attributeNames[i]  = tokenizer.nextToken();
     		}
                
                //Abrimos el fichero que contiene los atributos (AtributosJuego.txt)

      		try {
         		File inputFile = new File(values_file_name);
	 		in = new FileInputStream(values_file_name);
      		} catch ( Exception e) {
			System.err.println( "Incapaz de abrir el fichero: " + attributes_file_name + "\n" + e);
			return readStatus;
      		}

      		BufferedReader values_file = new BufferedReader(new InputStreamReader(in) );


                input = values_file.readLine();
                
                //Leemos las líneas del fichero
                //Mientras haya algo
      		while(input != null) {


                        //Creamos el StringTokenizer que separe los tokens por comas
                         //Los atributos están separados por comas
			tokenizer = new StringTokenizer(input,",");
                        
			int numtokens = tokenizer.countTokens();
                        
			if (numtokens != numAttributes) {
                            System.err.println( "Ha ocurrido un error al leer la línea del fichero: " + values_file_name);
                            System.err.println( "La línea que ha fallado es la siguiente: " + values_file_name);
                            return readStatus;
                        }

                        //Se añaden los datos a la estructura
                        
			DataPoint point = new DataPoint(numAttributes);
     			for (int i=0; i < numAttributes; i++) {
         			point.attributes[i]  = getSymbolValue(i, tokenizer.nextToken() );
     			}
			root.data.addElement(point);
                        
                        input = values_file.readLine();
		}

                attributes_file.close();
		values_file.close();

                readStatus = true;
                
      		return readStatus;
   	}	// End of function readData
   	
	/*  This function prints the decision tree in the form of rules.
		The action part of the rule is of the form
			outputAttribute = "symbolicValue"
		or
			outputAttribute = { "Value1", "Value2", ..  }
		The second form is printed if the node cannot be decomposed any further into an homogenous set
	*/
	public void printTree(TreeNode node, String tab) {

		int outputattr = numAttributes-1;

		if (node.children == null) {
			int []values = getAllValues(node.data, outputattr );
			if (values.length == 1) {
                                area.append(tab + "\t" + attributeNames[outputattr] + " = \"" + domains[outputattr].elementAt(values[0]) + "\";\n");
				System.out.println(tab + "\t" + attributeNames[outputattr] + " = \"" + domains[outputattr].elementAt(values[0]) + "\";");
				return;
			}
                        area.append(tab + "\t" + attributeNames[outputattr] + " = {");
			System.out.print(tab + "\t" + attributeNames[outputattr] + " = {");
			for (int i=0; i < values.length; i++) {
                                area.append("\"" + domains[outputattr].elementAt(values[i]) + "\" ");
				System.out.print("\"" + domains[outputattr].elementAt(values[i]) + "\" ");
				if ( i != values.length-1 ){
                                    area.append(" , ");
                                    System.out.print( " , " );
                                }
			}
                        area.append(" };\n");
			System.out.println( " };");
			return;
		}

		int numvalues = node.children.length;
                for (int i=0; i < numvalues; i++) {
                  area.append(tab + "  if( " + attributeNames[node.decompositionAttribute] + " == \"" +
                          domains[node.decompositionAttribute].elementAt(i) + "\") {\n");
                  System.out.println(tab + "  if( " + attributeNames[node.decompositionAttribute] + " == \"" +
                          domains[node.decompositionAttribute].elementAt(i) + "\") {" );
                  printTree(node.children[i], tab + "\t");
                  if (i != numvalues-1){
                      area.append(tab +  "  } else ");
                      System.out.print(tab +  "  } else ");
                  }
                  else {
                      area.append(tab +  "  }\n");
                      System.out.println(tab +  "  }");
                  }
                }


	}

	/**
         * Crea e imprime por la salida estándar el árbol de decisión
         */
	public void createAndPrintDecisionTree() {
                
            decomposeNode(root);
            //printTree(root, "");
	}

        public int getNumAttributes() {
            return numAttributes;
        }

        public String[] getAttributeNames() {
            return attributeNames;
        }

        public Vector[] getDomains() {
            return domains;
        }

        public TreeNode getRoot() {
            return root;
        }         

    public JTextArea getArea() {
        return area;
    }

    public void setArea(JTextArea area) {
        this.area = area;
    }    
}