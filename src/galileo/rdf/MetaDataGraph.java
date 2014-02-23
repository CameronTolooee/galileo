package galileo.rdf;

import galileo.comm.QueryResponse;
import galileo.config.SystemConfig;
import galileo.dataset.BlockMetadata;

import java.util.ArrayList;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class MetaDataGraph {
	QueryResponse metaTree;
	private static final String prefix = "galileo://";
	private static final String root = "QueryResponse";
	private static final Property parentOf = ResourceFactory.createProperty(prefix+"ParentOf");
	private static final Property isType = ResourceFactory.createProperty(prefix+"isType");
	private static final BaseDatatype year = new BaseDatatype(prefix+"TemporalYear");
	private static final BaseDatatype month = new BaseDatatype(prefix+"TemporalMonth");
	private static final BaseDatatype day = new BaseDatatype(prefix+"TemporalDay");
	private static final BaseDatatype loc = new BaseDatatype(prefix+"SpatialGeoHash");
	private static final BaseDatatype data = new BaseDatatype(prefix+"GalileoData");
	


	
	public MetaDataGraph(QueryResponse tree) {
		metaTree = tree;
	}
	
	public Model toRDF(){
		ArrayList<String> paths = getPaths();
		Model rdf = ModelFactory.createDefaultModel();
		for (String path : paths) {
			// remove entire path to storage root
			String rootPath = SystemConfig.getStorageRoot();
			int index = path.indexOf(rootPath);
			if (index != -1) {
				path = path.substring(rootPath.length()+1);
			}
			createRoot(rdf);
			String[] nodes = path.split("/");
			addNode(MetaDataGraph.root, nodes[0], rdf, year);
			for(int i = 0; i < nodes.length - 1; ++i) {
				BaseDatatype bdt = null;
				switch(i){
				case 0: // year
					bdt = month;
					break;
				case 1: // month
					bdt = day;
					break;
				case 2: // day
					bdt = loc;
					break;
				case 3: // loc
					bdt = data;
					break;
				default: // unknown
					
				}
				addNode(nodes[i], nodes[i+1], rdf, bdt);
			}
		}
		//rdf.write(System.out);
		return rdf;
	}
	
	private void createRoot(Model model) {
		assert model.isEmpty();
		model.createResource(root);
	}
	
	private void addNode(String prev, String node, Model model, BaseDatatype type){
		if(!model.contains(model.getResource(prefix+node), parentOf)) { 
			model.add(model.getResource(prefix+prev), parentOf, model.createResource(prefix+node)
					.addProperty(isType, ResourceFactory.createTypedLiteral(type)));
		}
	}
	
	private ArrayList<String> getPaths() {
		ArrayList<String> paths = new ArrayList<String>();
		for(BlockMetadata m : metaTree.getMetadata()) {
			paths.add(m.getRuntimeMetadata().getPhysicalGraphPath());
        }
		return paths;
	}
	
	public void printTriples(Model model){
		StmtIterator iter = model.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
			Statement stmt      = iter.nextStatement();  // get next statement
			Resource  subject   = stmt.getSubject();     // get the subject
			Property  predicate = stmt.getPredicate();   // get the predicate
			RDFNode   object    = stmt.getObject();      // get the object

			System.out.print(subject.toString());
			System.out.print(" " + predicate.toString() + " ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");
		}
	
//	public static void main(String args[]) {
//		Model model = ModelFactory.createDefaultModel();
//
//		model.createResource("http://galileo/2011")
//				.addProperty(parentOf, model.createResource("http://galileo/08"))
//				.addProperty(isType, ResourceFactory.createTypedLiteral(year));
//		model.add(model.getResource("http://galileo/08"), parentOf,  model.createResource("http://galileo/02"));
//		model.add(model.getResource("http://galileo/08"), parentOf,  model.createResource("http://galileo/05"));
//		//model.add(model.getResource("http://galileo/03"), parentOf,  model.createResource("http://galileo/31"));
//		System.out.println(model.contains(model.getResource("http://galileo/08"), parentOf));
//
//		
//
//		//RDFNode node = ResourceFactory.createPlainLiteral("08");
//	//	Statement s = ResourceFactory.createStatement(tree);
//		//model.add();
////		StmtIterator iter = model.listStatements();
////
////		// print out the predicate, subject and object of each statement
////		while (iter.hasNext()) {
////		    Statement stmt      = iter.nextStatement();  // get next statement
////		    Resource  subject   = stmt.getSubject();     // get the subject
////		    Property  predicate = stmt.getPredicate();   // get the predicate
////		    RDFNode   object    = stmt.getObject();      // get the object
////
////		    System.out.print(subject.toString());
////		    System.out.print(" " + predicate.toString() + " ");
////		    if (object instanceof Resource) {
////		       System.out.print(object.toString());
////		    } else {
////		        // object is a literal
////		        System.out.print(" \"" + object.toString() + "\"");
////		    }
////
////		    System.out.println(" .");
////		    
////
////
////		}
//		
//    model.write(System.out);
//
	}
}
