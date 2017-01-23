import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class TranslatorAgentToAutomaton {
	Set<Perception> perception_list = new HashSet<Perception>();
	int perception_counter =0;
	Map<Integer, Set<Integer>> vertex = new HashMap<Integer, Set<Integer>>();
	BufferedReader state_file;
	ArrayList<Integer> removable_vertices = new ArrayList<Integer>();
	BufferedReader transition_file;
	ArrayList<Pair<Integer, Integer>> edge = new ArrayList<Pair<Integer, Integer>>();
	ArrayList<SortedSet<Integer>> adjList;
	int maxStateNum=-1;
	
	public TranslatorAgentToAutomaton(BufferedReader perception_file, BufferedReader perception_type, BufferedReader state_file, BufferedReader transition_file){
		try {
			this.state_file = state_file;
			this.transition_file = transition_file;
			String rawLine= perception_file.readLine();
			String p_type= perception_type.readLine();
			while(rawLine != null && p_type != null){
				String line = rawLine.replaceAll("\\s", "");
				perception_list.add(new Perception(line, perception_counter, Integer.parseInt(p_type)));				
				perception_counter++;
				rawLine = perception_file.readLine();
				p_type= perception_type.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public void setVertex(){
		boolean negativeState= false;
		int stateNumber=-1;
		Set<Integer> statePerceptions = new HashSet<Integer>();
		String pattern = "(-*)(\\d+)(:)";
		Pattern r = Pattern.compile(pattern);
		try{
			String rawLine = state_file.readLine();
			while(rawLine!=null){
				String line = rawLine.replaceAll("\\s","");
				Matcher m = r.matcher(line);
				if(vertex.containsKey(5)){
				//	System.out.println("v "+vertex.size() + vertex.get(5).toString());
				}
				if(m.find()){
					if(stateNumber> -1){
						if(statePerceptions.isEmpty() || statePerceptions == null){
							if(!negativeState){
								if(stateNumber>maxStateNum){
									maxStateNum= stateNumber;
								}
								vertex.put(stateNumber, null);
								removable_vertices.add(stateNumber);									
							}
						}else{
							if(!negativeState){
								if(stateNumber>maxStateNum){
									maxStateNum= stateNumber;
								}
								vertex.put(stateNumber, statePerceptions);									
							}
							//System.out.println(stateNumber+ " / "+ statePerceptions.toString());
						}
					}
					if(m.group(1).equals("-")){
						negativeState = true;
					}else{
						negativeState = false;
					}
					stateNumber = Integer.parseInt(m.group(2));
					statePerceptions = new HashSet<Integer>();
				}else{
					for(Perception p: perception_list){
						if(line.equals(p.getPerception()) && p.isExternal()){
							statePerceptions.add(p.getLable());
							break;
						}
					}
				}	
				rawLine = state_file.readLine();
			}
			//put the last state in the vertex list
			
			if(!negativeState){
				if(stateNumber> maxStateNum){
					maxStateNum= stateNumber;
				}
				if(statePerceptions.isEmpty() || statePerceptions == null){
					vertex.put(stateNumber, null);
					removable_vertices.add(stateNumber);														
				}else{
					vertex.put(stateNumber, statePerceptions);
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	
	public void setEdge(){
		String pattern = "(\\d+)(-->)(\\d+)";
		Pattern r = Pattern.compile(pattern);
		try{
			String rawLine = transition_file.readLine();
			while(rawLine!=null){
				String line = rawLine.replaceAll("\\s","");
				Matcher m = r.matcher(line);
				if(m.find()){
					if(!(m.group(1).equals(m.group(3)))){
						//adjList[Integer.parseInt(m.group(1))].add(Integer.parseInt(m.group(3)));
						edge.add(new Pair<Integer, Integer>(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(3))));
					}
				}
				rawLine = transition_file.readLine();
			}
		}catch(IOException e){
			e.printStackTrace();
		}

	}
	
	public void clean_duplicate_edge(){
		//System.out.println("edge number before cleaning duplicates "+ edge.size());
		Set<Pair<Integer, Integer>> hs = new HashSet<Pair<Integer, Integer>>();
		hs.addAll(edge);
		edge.clear();
		edge.addAll(hs);
		//System.out.println("edge number after cleaning duplicates "+ edge.size());
		
		//System.out.println("the size of edge after cleaning duplicates (x->y) and (x->y)"+ edge.size());
		//System.out.println("the size of vertices after cleaning duplicates (x->y) and (x->y)"+ vertex.size());
		edge.removeIf(m-> m.getKey().equals(m.getValue()));
		//System.out.println("the size of edge after removing self loop (x->x)"+ edge.size());
		//System.out.println("the size of vertices after removing self loop (x->x)"+ vertex.size());
	}
	
	// for all removable states, remove their input/out transitions and add the required replacements
	public void removeEmpty(){
		printSize("original generated transitions: ");
		System.out.println("removable_vertices "+ removable_vertices.size());
		for(Integer i: removable_vertices){
			removeVertex(i);
		}
		printSize("after removeEmpty function: ");
	}
	
	public void removeVertex(int i){
		clean_duplicate_edge();
		ArrayList<Pair<Integer, Integer>> removableEdges = new ArrayList<Pair<Integer, Integer>>();
		int numNewAddedEdges;
		ArrayList<Integer> tempIn = new ArrayList<Integer>();
		ArrayList<Integer> tempOut = new ArrayList<Integer>();
		numNewAddedEdges=0;
		for(Pair<Integer, Integer> e : edge){
			if((e.getKey()).equals(i)){
				tempOut.add(e.getValue());
				removableEdges.add(e);
//				System.out.println("removable vertex "+ i+ " removed output transition: " + e.getKey()+ "-->"+ e.getValue());
			}
			if(e.getValue().equals(i)){
				tempIn.add(e.getKey());
				//removableEdges.add(e);
				Pair<Integer, Integer> p = new Pair<Integer, Integer>(e.getKey(), e.getValue());
				//System.out.println(e);
				//System.out.println(p);
				removableEdges.add(p);
//				System.out.println("removable vertex "+ i+ " removed input transition: " + e.getKey()+ "-->"+ e.getValue());
			}
		}
		//System.out.println("the number of transitions "+ edge.size()+" before removing empty vertices: " + removableEdges.size()+ " for "+ i);			
		edge.removeAll(removableEdges);
		//System.out.println("the number of transitions after removing empty vertices: " + edge.size());
		//System.out.println("removable edges are "+ removableEdges.toString());
		for(Integer tIn: tempIn){
			for(Integer tOut: tempOut){
				//System.out.println("adding "+ tIn + "-->"+ tOut);
				edge.add(new Pair<Integer, Integer>(tIn, tOut));
				numNewAddedEdges++;
			}
		}
		//System.out.println("the number of transitions after removing empty and adding replacements: " + edge.size());
		vertex.remove(i);
//		System.out.println("statistic: node "+ i+" with "+ tempIn.toString()+ " input and " +tempOut.toString()+" output, added "
//				+ ""+ numNewAddedEdges);
		clean_duplicate_edge();
	}
	
	
	
	public void trimEdge(int first, int iter){
		//System.out.println("iteration on "+ iter);
		if(iter != first){
			//System.out.println("Iteration "+ iter+ "removing equivalent output transitions");
			int tempSize = edge.size();
			edge.remove(new Pair<Integer,Integer>(first, iter));
			edge.removeIf(s -> s.getKey().equals(iter));
			if(edge.size()< tempSize && iter==100){
				System.out.println("key transitions of vertex 100 are removed");
			}
			tempSize = edge.size();
			//System.out.println("Iteration "+ iter+ "edge list size after removing is: "+edge.size());
			edge.replaceAll(s -> s.getValue().equals(iter) ? new Pair<Integer, Integer>(s.getKey(),first) : s);
			if(edge.size()< tempSize && iter==100){
				System.out.println("value transitions of vertex 100 are removed");
			}
			//System.out.println("Iteration "+ iter+ " edge list size after adding: "+edge.size());
			vertex.remove(iter);
			if(iter==100){
				System.out.println("vertext "+ iter+ " is removed due to equivalency with "+ first);			
			}			
		}
	}
	
	public void writeToFileIntermediate(){
		try {
			sort_edge();
			String home = System.getenv("HOME");
			File file = new File(home+"/Desktop/TranslatorConfig/tranformedTransitionsIntermediate.txt");
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter br = new BufferedWriter(new FileWriter(file));
			for (Pair<Integer, Integer> entry : edge)
			{
					br.write(entry.getKey() + "-->" + entry.getValue()+ "\n");
			}
			System.out.println("writing intermediate step done");
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	
	
	
	public void trimConsecutiveEdge(int first, int iter){
		//System.out.println("state "+ iter+ "is replaced with"+ first);
		edge.remove(new Pair<Integer, Integer>(first, iter));
		edge.replaceAll(s -> s.getValue().equals(iter) ? new Pair<Integer, Integer>(s.getKey(),first) : s);
		edge.replaceAll(s -> s.getKey().equals(iter) ? new Pair<Integer, Integer>(first, s.getValue()) : s);
		vertex.remove(iter);
		if(iter==100){
			System.out.println("vertext "+ iter+ " is removed due to consecutive equivalency");

		}
	}
	
	public void aggrConsecutiveEdges(int begin, int end){
		for(int i= begin; i< end; i++){
			edge.remove(new Pair<Integer, Integer>(i, i+1));
			final int innerIplus1 = i+1;
			edge.replaceAll(s -> s.getValue().equals(innerIplus1) ? new Pair<Integer, Integer>(s.getKey(),begin) : s);
			edge.replaceAll(s -> s.getKey().equals(innerIplus1) ? new Pair<Integer, Integer>(begin, s.getValue()) : s);
			vertex.remove(innerIplus1);
			if(innerIplus1==100){
				System.out.println("vertext "+ innerIplus1+ " is removed due to aggregation of consecutive equivalent vertices");
			}
		}
		edge.removeIf(s -> s.getKey().equals(s.getValue()));
	}
	
	public void sort_edge(){
		edge.sort(new Comparator<Pair<Integer, Integer>>() {
			@Override
			public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
				if (o1.getKey() < o2.getKey()) {
					return -1;
				} else if (o1.getKey().equals(o2.getKey())) {
					return 0; // You can change this to make it then look at the
                          //words alphabetical order
				} else {
					return 1;
				}
			}
    });

	}
	
	public void subsetVertices(){
		int edgeSize = 0;		
		while(edgeSize< getEdgeSize()){
			edgeSize= getEdgeSize();
			ArrayList<Pair<Integer, Integer>> subsetRemoving = new ArrayList<Pair<Integer, Integer>>();
			adjList = new ArrayList<SortedSet<Integer>>(maxStateNum+1);
			for (int i = 0; i <= maxStateNum; i++) {
	            adjList.add(i, new TreeSet<Integer>());
			}		
		
			// UPDATE adjList AFTER REMOVING EMPTY VERTICES
			for(Pair<Integer, Integer> e: edge){
				adjList.get(e.getKey()).add(e.getValue());	
				//Collections.sort(adjList.get(e.getKey()));
			}

			for (Map.Entry<Integer, Set<Integer>> v: vertex.entrySet()){
				for(int i: adjList.get(v.getKey())){
					if(v.getValue().equals(vertex.get(i))){
						if(adjList.get(v.getKey()).containsAll(adjList.get(i))){
							subsetRemoving.add(new Pair<Integer, Integer>(v.getKey(),i));
						}
					}
				}
			}

			//System.out.println("the set of removing vertices "+ subsetRemoving.toString());

			for(Pair<Integer,Integer> p: subsetRemoving){
				trimEdge(p.getKey(), p.getValue());
			}
		}
	}
	
	public int getVertexSize(){
		return vertex.size();
	}
	
	public int getEdgeSize(){
		return edge.size();
	}
	
	
	public void equivalentVertices(){
		int before_rm_equivalent_edge = 0;		
		int counter=0;
		while(before_rm_equivalent_edge!= edge.size()){
			//System.out.println("eqVertices function runs for "+ (counter++) + " times");
			//System.out.println("the size of edge after merging equivalent vertices "+ edge.size());
			//System.out.println("the size of vertices after merging equivalent vertices "+ vertex.size());
			before_rm_equivalent_edge = edge.size();
			clean_duplicate_edge();
			sort_edge();

			Multimap<Set<Integer>, Integer> multiMapVertex = HashMultimap.create();
			printSize("before equivalentVertices function: ");
			//System.out.println("the size of transitions before removing equivalent: "+ edge.size());
			//System.out.println("the size of vertices before removing equivalent: "+ vertex.size());
			for (Map.Entry<Integer, Set<Integer>> v : vertex.entrySet()) {
				multiMapVertex.put(v.getValue(), v.getKey());
			}

			adjList = new ArrayList<SortedSet<Integer>>(maxStateNum+1);
			for (int i = 0; i <= maxStateNum; i++) {
				adjList.add(i, new TreeSet<Integer>());
			}		
 		
			// UPDATE adjList AFTER REMOVING EMPTY VERTICES
			for(Pair<Integer, Integer> e: edge){
				adjList.get(e.getKey()).add(e.getValue());	
				//Collections.sort(adjList.get(e.getKey()));
			}

			Multimap<Collection<Integer>, Integer> mMapAdjList = HashMultimap.create();
			for (int i = 0; i <= maxStateNum; i++) {
				mMapAdjList.put((Collection<Integer>) adjList.get(i), i);
			}
			for(Map.Entry<Collection<Integer>, Collection<Integer>> e: mMapAdjList.asMap().entrySet()){
				//System.out.println(e.toString());
				int eqCounter=0;
				if(e.getValue().size()>1){
					//System.out.println("found pairs with equivalent output "+ e.getValue());
					Collection<Integer> eqTransitions = e.getValue();
					//System.out.println("the size of transitions before removing equivalent: "+ edge.size());
					for(Map.Entry<Set<Integer>, Collection<Integer>> v: multiMapVertex.asMap().entrySet()){
						//System.out.println(v.toString());
						if(v.getValue().containsAll(eqTransitions)){
							//System.out.println(vertex.get(303)+ " "+ adjList[303] + " "+ vertex.get(39)+ " "+ adjList[39]);
							//System.out.println("equal states " + eqTransitions.toString());
							int first = Collections.min(eqTransitions);
							//System.out.println("the first element is " + first);
							eqTransitions.forEach(iter -> trimEdge(first, (Integer) iter));
							eqCounter++;
						}else{
							Collection<Integer> eqTransitions_2 = new ArrayList<Integer>();
							for(int i : eqTransitions){
								if(v.getValue().contains(i)){
									eqTransitions_2.add(i);
								}
							}
							if(eqTransitions_2.size()>1){
								int first_2 = Collections.min(eqTransitions_2);
								eqTransitions_2.forEach(iter -> trimEdge(first_2, (Integer) iter));								
								//System.out.println("eqTransitions_2 for "+ v.getKey()+ " perceptions is "
								//		+ eqTransitions_2.toString()+" replaced with "+ first_2);
							}
						}					
					}
					//System.out.println("the size of transitions after removing equivalent: "+ edge.size()+ " and removed "+ eqCounter);
				}
			}
		edge = (ArrayList<Pair<Integer, Integer>>) edge.stream().distinct().collect(Collectors.toList());
		clean_duplicate_edge();
		}
		printSize("after equivalentVertices function: ");
	}
	
	
	public void mergeEqVertexOutgoingToEqVertices(){
		Multimap<Set<Integer>, Integer> multiMapVertex = HashMultimap.create();
		for (Map.Entry<Integer, Set<Integer>> v : vertex.entrySet()) {
			  multiMapVertex.put(v.getValue(), v.getKey());
		}
		adjList = new ArrayList<SortedSet<Integer>>(maxStateNum+1);
		for (int i = 0; i <= maxStateNum; i++) {
			adjList.add(i, new TreeSet<Integer>());
		}				
		// UPDATE adjList AFTER REMOVING EMPTY VERTICES
		for(Pair<Integer, Integer> e: edge){
			if(e.getKey()==0){
				//System.out.println("edge list has these pairs for 0 "+ e.getValue());
			}
			
			adjList.get(e.getKey()).add(e.getValue());			
		}
		//System.out.println("adjList: "+ adjList.toString());
		
		Multimap<Collection<Integer>, Integer> mMapAdjList = HashMultimap.create();
        for (int i = 0; i <= maxStateNum; i++) {
 			mMapAdjList.put((Collection<Integer>) adjList.get(i), i);
		}

		ArrayList<Integer> countEq = new ArrayList<Integer>();
		
		for(Map.Entry<Set<Integer>, Collection<Integer>> x: multiMapVertex.asMap().entrySet()){
			Collection<Integer> eqVertices = x.getValue();
			//System.out.println("mapVertex is "+ eqVertices);			
			for(int e1 : eqVertices){
				//System.out.println("check for vertex "+ e1+ " with neighbors "+ adjList.get(e1));
				if(eqVertices.containsAll(adjList.get(e1)) & !adjList.get(e1).isEmpty()){
					countEq.add(e1);
					//System.out.println("vertex "+ e1 + "can be potentially merged with some other eq vertices"+ adjList[e1]);
					removeVertex(e1);
				}
			}
		}
		//System.out.println("the set of merged ones "+ countEq.toString()+ " and total "+ countEq.size()); 
		printSize("after mergeEqVertexOutgoingToEqVertices function: ");
	}
	
	public void printSize(String s){
		System.out.println(s+ " vertices: "+ vertex.size()+ " edges: "+ edge.size());
	}
	
	public void pathGenerator(){
		int begin = 0;
		
		adjList = new ArrayList<SortedSet<Integer>>(maxStateNum+1);
		//System.out.println("vertex size is "+ vertex.size());
		//System.out.println("max number is "+ maxStateNum);
        for (int i = 0; i <= maxStateNum; i++) {
            adjList.add(i, new TreeSet<Integer>());
        }

		// UPDATE adjList AFTER REMOVING EMPTY VERTICES
		for(Pair<Integer, Integer> e: edge){
			adjList.get(e.getKey()).add(e.getValue());			
		}
		
		ArrayList< Pair<Integer, Integer>> generatedPath = new ArrayList< Pair<Integer, Integer>>();
		
		while(!adjList.get(begin).isEmpty()){
			int end = Collections.min(adjList.get(begin));
			Pair<Integer, Integer> t = new Pair<Integer, Integer>(begin, end);
			generatedPath.add(t);
			System.out.println("added transition "+ t.toString());
			begin= end;
			
		}
	}
	
	
	public void vertexPrint(){
//		System.out.println(vertex.keySet());
//		System.out.println(vertex.size());
		for (Map.Entry<Integer, Set<Integer>> entry : vertex.entrySet())
		{
		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}
		System.out.println("The number of empty states: "+ removable_vertices.size());
	}
	
	public void edgePrint(){
//        for (int i = 0; i < adjList.length; ++i) {
//        	System.out.println("node "+i +" has "+ adjList[i]+ " output transitions");
//        }
//        System.out.println("The number of transitions: "+ adjList.length);
		sort_edge();
		for (Pair<Integer, Integer> entry : edge)
		{
				System.out.println(entry.getKey() + "/" + entry.getValue());
		}
		System.out.println("The number of transitions: "+ edge.size());
	}
	
	public void writeToFile(){
		try {
			
			sort_edge();
			String home = System.getenv("HOME");
			File file = new File(home+"/Desktop/TranslatorConfig/tranformedTransitions.txt");
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter br = new BufferedWriter(new FileWriter(file));
			for (Pair<Integer, Integer> entry : edge)
			{
					br.write(entry.getKey() + "-->" + entry.getValue()+ "\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void wrtieDotFile(String label){
		ArrayList<Set<Integer>> vertexClass = new ArrayList<Set<Integer>>();
		for (Map.Entry<Integer, Set<Integer>> entry : vertex.entrySet()){
			if(!vertexClass.contains(entry.getValue())){
				vertexClass.add(entry.getValue());
				//System.out.println("new class is "+ entry.getValue());
			}
		}
		//System.out.println(vertexClass.size());	
		ArrayList<String> shape = new ArrayList<String>(vertexClass.size());
		
		String[] color = {"aliceblue", "aquamarine4", "bisque3", "chartreuse", "cornflowerblue",
				"cornsilk4",	"darkorchid1",	"darkslategray1",	"gold",	"gray",
				"lightblue",	"lightpink",	"red",	"violet",	"orchid4",
				"olivedrab1",	"firebrick",	"goldenrod4",	"cyan",	"coral1",
				"firebrick4",	"floralwhite",	"forestgreen",	"gainsboro",	"ghostwhite",
				  "burlywood4",  "aquamarine2",	"bisque1",	"deeppink3",	"gold4",
				"darksalmon",	"gray72",	"deeppink4",	"olivedrab4",	"royalblue1",
				   "wheat1",
				   "black", "black", "black", "black", "black", "black", "black"};
		
		

		try {
			sort_edge();
			String home = System.getenv("HOME");
			File file = new File(home+"/Desktop/TranslatorConfig/tranformedTransitions.dot");
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter br = new BufferedWriter(new FileWriter(file));
			br.write("digraph { \n \t rankdir=LR; \n");
			br.write("\t graph [label=\" "+ label+ " \", labelloc=t, fontsize=30]; ");
			adjList = new ArrayList<SortedSet<Integer>>(maxStateNum+1);
			
			//System.out.println("mapVertex is "+ multiMapVertex.toString());
			for (int i = 0; i <= maxStateNum; i++) {
				adjList.add(i, new TreeSet<Integer>());
			}				
			// UPDATE adjList AFTER REMOVING EMPTY VERTICES
			for(Pair<Integer, Integer> e: edge){
				adjList.get(e.getKey()).add(e.getValue());			
			}

			for (Map.Entry<Integer, Set<Integer>> entry : vertex.entrySet())
			{
				br.write("\t "+ entry.getKey()+ " [color="+ color[vertexClass.indexOf(entry.getValue())]+",style=filled]; \n");
			}

			for (int i=0; i<= maxStateNum; i++)
			{
//				//if(i== 73){
//				//	br.write("\t "+ i+ " [color=lightblue,style=filled]; \n");
//				//}
//				//if(i== 101){
//				//	br.write("\t "+ i+ " [color=grey,shape=box,style=filled]; \n");
//				//}
//				if(i== 102){
//					br.write("\t "+ i+ " [color=lightblue,style=filled]; \n");
//				}
//				if(i== 74){
//					br.write("\t "+ i+ " [color=yellow,shape=triangle,style=filled]; \n");
//				}
//				
//				
				if(!adjList.get(i).isEmpty()){
					br.write(" \t "+ i + " -> { ");
					//Collections.sort(adjList.get(i));
					for(int j: adjList.get(i)){
						br.write(" "+ j);
					}
					br.write( " }; \n");

				}
			}
			br.write("\t }");
			br.close();
			//System.out.println("1712 state: "+ vertex.get(1712));
			//System.out.println("1709 state: "+ vertex.get(1709));
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public void writeToFileVertex(){
		try {
			String home = System.getenv("HOME");
			File file = new File(home+"/Desktop/TranslatorConfig/tranformedVertices.txt");
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter br = new BufferedWriter(new FileWriter(file));
			for (Map.Entry<Integer, Set<Integer>> v : vertex.entrySet()) {
					br.write(v.getKey()+ "\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(Pair<Integer,Integer> p: edge){
			if(!vertex.containsKey(p.getKey())){
				System.out.println("Key Node "+ p.getKey()+ " does not exist in vertex list");
			}
			if(!vertex.containsKey(p.getValue())){
				System.out.println("Value Node "+ p.getValue()+ " does not exist in vertex list"+ p.toString());
			}
		}
	}
	
	
	
	
	public void perceptionListPrint(){
		for(Perception p: perception_list){
			System.out.println("The perception is "+ p.getPerception()+ " its label is "+ p.getLable()+ " if is external "+ p.isExternal());
		}
	}
	
	// PRIVATE INNER CLASS
	private class Perception{
		String p;
		int p_l;
		int external= 0;

		public Perception(String p, int p_l){
			this.p = p;
			this.p_l = p_l;			
		}
		
		public Perception(String p, int p_l, int e){
			this.p = p;
			this.p_l = p_l;
			this.external = e;
		}
		
		public void setExternal(int ext){
			if(ext==1){
				this.external =1;
			}
		}
		public boolean isExternal(){
			if(external == 1){
				return true;
			}else{
				return false;
			}
		}
		public String getPerception(){
			return p;
		}
		public int getLable(){
			return p_l;
		}
	}
	
	
	public static void main(String[] args){
		int edgeSize = 0;
		int vertexSize =0;
		String home = System.getenv("HOME");
		try(BufferedReader br = new BufferedReader(new FileReader(home+"/Desktop/TranslatorConfig/perception_list.txt"))) {
			try(BufferedReader perType = new BufferedReader(new FileReader(home+"/Desktop/TranslatorConfig/perception_type.txt"))){
				try(BufferedReader state_file = new BufferedReader(new FileReader(home+"/Desktop/TranslatorConfig/BLAHgeneratedPlatoon.txt"))){
					try(BufferedReader transition_file = new BufferedReader(new FileReader(home+"/Desktop/TranslatorConfig/generatedPlatoon.txt"))){
						TranslatorAgentToAutomaton translator = new TranslatorAgentToAutomaton(br,perType,state_file,transition_file);
						//translator.perceptionListPrint();
						translator.setVertex();
						//translator.vertexPrint();
						translator.setEdge();
						//translator.wrtieDotFile("MCAPL generated Graph");
						//translator.edgePrint();
						translator.removeEmpty();
						//translator.edgePrint();
						//translator.writeToFileIntermediate();
						while(edgeSize!= translator.getEdgeSize()){
							edgeSize = translator.getEdgeSize();
							translator.equivalentVertices();
							//translator.wrtieDotFile("After merging equivalences");
							//translator.writeToFileIntermediate();
							translator.mergeEqVertexOutgoingToEqVertices();
						}	
						System.out.println("after first while "+ translator.getVertexSize()+ " edge"+ translator.getEdgeSize());
							//translator.equivalentVertices();
							//translator.mergeEqVertexOutgoingToEqVertices();
						edgeSize= 0;
						while(edgeSize!= translator.getEdgeSize()){
							edgeSize = translator.getEdgeSize();
							translator.subsetVertices();
							translator.mergeEqVertexOutgoingToEqVertices();
							translator.equivalentVertices();
						}
						//translator.edgePrint();
						//translator.pathGenerator();
						translator.clean_duplicate_edge();
						translator.writeToFile();
						translator.wrtieDotFile("Final Graph");
						translator.writeToFileVertex();
					
					}catch (IOException x){
						System.err.println("transition list file does not exist");
					}
				}catch (IOException x){
					System.err.println("states list file does not exist");
				}
			}catch (IOException x){
				System.err.println("perception type (internal/external) file does not exist");
			}	
		}catch (IOException x){
			System.err.println("perception list file does not exist");
		}
	}

}