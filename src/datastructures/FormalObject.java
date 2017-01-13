package datastructures;

import java.util.ArrayList;
import java.util.BitSet;

public class FormalObject {
	private String name;
	private BitSet intent;
	private ArrayList<String> attributes;
	
	public FormalObject(){
		this.name = "";
		this.intent = new BitSet();
		this.attributes = new ArrayList<String>();
	}
	
	public boolean isSubsetOf(BitSet bs) {
		BitSet thisIntent = (BitSet)intent.clone();
		thisIntent.and(bs);
		if(thisIntent.equals(intent))
			return true;
		return false;
	}
	
	public boolean isSupersetOf(BitSet bs) {
		BitSet thisIntent = (BitSet)intent.clone();
		thisIntent.and(bs);
		if(thisIntent.equals(bs))
			return true;
		return false;
	}
	
	public ArrayList<String> getAttributes() {
		return attributes;
	}
	public void setAttributes(ArrayList<String> attributes) {
		this.attributes = attributes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BitSet getIntent() {
		return intent;
	}
	public void setIntent(BitSet intent) {
		this.intent = intent;
	}
}
