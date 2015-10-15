package javaToUML;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class FieldVisitor extends VoidVisitorAdapter{
	
	private ArrayList<String> modifier = new ArrayList<String>();
	private ArrayList<String> getType = new ArrayList<String>();
	private ArrayList<String> getVariable = new ArrayList<String>();
	private ArrayList<String> fieldFormat = new ArrayList<String>();
	private ArrayList<String> nonPrimitive = new ArrayList<String>();
	private ArrayList<String> reserveTypes = new ArrayList<String>(Arrays.asList("byte","short","int","long","float","double","boolean","char","Integer","String", "Character"));
	
	
	public void visit(FieldDeclaration n, Object arg){
		int startIndex;
		int endIndex;
		String variableName;
		String replacement = null;
		String subString = null;
		// if variable name is null meaning no variable available
		if(n.getVariables().toString() != null){
			variableName = n.getVariables().toString();
			startIndex = variableName.indexOf("[");
			endIndex = variableName.indexOf("]");
			replacement = variableName.substring(startIndex+1, endIndex);
			// all variable name array list
			getVariable.add(replacement);
			// all data type array list
			getType.add(n.getType().toString());
			switch(n.getModifiers()){
				case 0: modifier.add("~");
						break;
				case 1: modifier.add("+");
						break;
				case 2: modifier.add("-");
						break;
				case 4: modifier.add("#");
						break;
				default: 
						break;
			}
			// get all Non reserved data type
			for(int i = 0; i < getType.size(); i++){
				if(!reserveTypes.contains(getType.get(i))){
					//System.out.println(getType.get(i));
					if(getType.get(i).contains("<")){
						//System.out.println(getType.get(i));
						startIndex = getType.get(i).indexOf("<");
						endIndex = getType.get(i).indexOf(">");
						if(getType.get(i).indexOf(">") < 0){
							System.err.println("Expecting > in the code");
							System.exit(1);
						}
						subString = getType.get(i).substring(startIndex+1, endIndex);
						if(reserveTypes.contains(subString)){
							continue;
						}
						subString = subString+":\"*\"";
						//System.out.println(subString);
						if(!nonPrimitive.contains(subString)){
							nonPrimitive.add(subString);
						}
					}else if(getType.get(i).contains("[")){
						endIndex = getType.get(i).indexOf("[");
						subString = getType.get(i).substring(0, endIndex);
						if(reserveTypes.contains(subString)){
							continue;
						}
						subString = subString+":\"*\"";
						if(!nonPrimitive.contains(subString)){
							if(!reserveTypes.contains(subString)){
								nonPrimitive.add(subString);
							}
						}
					}else{
						if(!nonPrimitive.contains(getType.get(i))){
							nonPrimitive.add(getType.get(i));
						}
					}
				}
			}
		}else{
			fieldFormat = null;
		}
		
	}
	
	public ArrayList<String> getNonReserve(){
		return nonPrimitive;
	}
	
	public ArrayList<String> getFormat(){
		if(getVariable.size() > 0){
			for(int i = 0; i < getVariable.size(); i++){
				fieldFormat.add(modifier.get(i)+getVariable.get(i)+":"+getType.get(i));
			}
			return fieldFormat;
		}
		
		return fieldFormat;
	}
}
