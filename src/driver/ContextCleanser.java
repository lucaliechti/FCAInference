package driver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import datastructures.FormalContext;
import datastructures.Lattice;

public class ContextCleanser {
	
	private Lattice lattice;
	private FormalContext context;
	
	public ContextCleanser(FormalContext _context, Lattice _lattice){
		this.lattice = _lattice;
		this.context = _context;
	}
	
	//checks which attributes appear the least amount of times in the data
	//and completely removes these attributes from the context
	public void removeAttributesWithLeastSupport() {
		System.out.println("CLEANSING");
		HashMap<String, Integer> attributeSupport = context.getAttributeSupport();
		HashSet<Integer> supportSet = new HashSet<Integer>();
		supportSet.addAll(attributeSupport.values());
		Integer[] supportArray = supportSet.toArray(new Integer[supportSet.size()]);
		Arrays.sort(supportArray);
		int treshold = supportArray[2];
		System.out.println("Deleting all attribute that occur at most " + treshold + " times.");
		int deleted = 0;
		System.out.println("Nr of attributes before: " + context.numberOfAttributes());
		for(String attr : context.getDictionary().getContents()){
			if(attributeSupport.get(attr) <= treshold){
				deleted++;
				context.removeAttribute(attr);
			}
		}
		System.out.println("Nr of attributes after: " + (context.numberOfAttributes()-deleted));
	}
}
