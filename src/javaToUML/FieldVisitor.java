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
		String[] spString;
		String[] splitStr;
		boolean skip = false;
		// if variable name is null meaning no variable available
		if (n.getVariables().toString() != null) {
			variableName = n.getVariables().toString();
			startIndex = variableName.indexOf("[");
			endIndex = variableName.indexOf("]");
			replacement = variableName.substring(startIndex+1, endIndex);
			if (replacement.contains("=")) {
				spString = replacement.split("=");
				spString[0] = spString[0].replaceAll("^\\s*|\\s*$", "");
				spString[1] = spString[1].replaceAll("^\\s*|\\s*$", "");
				if (spString[1].contains("<") == true) {
					if (spString[1].contains(">") == false) {
						System.err.println("Expecting >");
						System.exit(1);
					} else {
						startIndex = spString[1].indexOf("<");
						endIndex = spString[1].indexOf("(");
						subString = spString[1].substring(startIndex, endIndex);
						getType.add(subString);
						skip = true;
					}
				} else if (spString[1].contains("[") == true) {
					if (spString[1].contains("]") == false) {
						System.err.println("Expecting ]");
						System.exit(1);
					} else {
						splitStr = spString[1].split(" ");
						endIndex = splitStr[1].indexOf("(");
						subString = splitStr[1].substring(0, endIndex);
						getType.add(subString);
						skip = true;
					}
				} else {
					getVariable.add(spString[0]);
				}
			} else {
				// all variable name array list
				getVariable.add(replacement);
			}
			// all data type array list
			if(skip == false){
				getType.add(n.getType().toString());
			}
			skip = false;
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
					if(getType.get(i).contains("<")){
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
	
	public ArrayList<String> getFormat(ArrayList<String> fileName){
		String subStr;
		int startIndex;
		int endIndex;
		if (getVariable.size() > 0) {
			for (int i = 0; i < getVariable.size(); i++) {
				if (getType.get(i).contains("<") && getType.get(i).contains(">")) {
					startIndex = getType.get(i).indexOf("<");
					endIndex = getType.get(i).indexOf(">");
					subStr = getType.get(i).substring(startIndex+1, endIndex);
					if (fileName.contains(subStr)) {
						continue;
					}
				} else if (getType.get(i).contains("<") && !getType.get(i).contains(">")) {
					System.err.println("Expecting >");
					System.exit(1);
				}
				
				if (fileName.contains(getType.get(i))) {
					continue;
				}
				if (modifier.get(i).equals("#") || modifier.get(i).equals("~")){
					continue;
				}	
				fieldFormat.add(modifier.get(i)+getVariable.get(i)+":"+getType.get(i));
			}
			return fieldFormat;
		}
		
		return fieldFormat;
	}
}
