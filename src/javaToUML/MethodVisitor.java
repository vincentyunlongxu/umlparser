package javaToUML;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter {
	
	private ArrayList<String> methodName = new ArrayList<String>();
	private ArrayList<String> methodModifier = new ArrayList<String>();
	private ArrayList<String> methodType = new ArrayList<String>();
	private ArrayList<String> paraMeter = new ArrayList<String>();
	private ArrayList<String> methodFormat = new ArrayList<String>();
	private ArrayList<String> paraType = new ArrayList<String>();
	private ArrayList<String> nonPrimitive = new ArrayList<String>();
	private ArrayList<String> reserveTypes = new ArrayList<String>(Arrays.asList("byte","short","int","long","float","double","boolean","char","Integer","String", "Character","void"));
	private ArrayList<String> nonReserveMethodType = new ArrayList<String>();
	private ArrayList<String> bodyType = new ArrayList<String>();
	
	public void visit(MethodDeclaration n, Object arg){
		int startIndex;
		int endIndex = 0;
		int count = 0;
		String replacement;
		String modifyString;
		String[] temp;
		String[] temp_inner;
		String paraFormat = null;
		String subString;
		
		// test if method name is not null means it contains some data we need
		if (n.getName().toString() != null) {
			methodName.add(n.getName().toString());
			if (n.getName().equals("main")) {
				String varBlock = n.getBody().getStmts().get(0).toString();
				String[] lines = varBlock.split(";");
				for(String line : lines){
					temp = line.split(" ");
					bodyType.add(temp[0]);
				}
			}
			methodType.add(n.getType().toString());
			switch(n.getModifiers()) {
				case 0: methodModifier.add("~");
					break;
				case 1: methodModifier.add("+");
					break;
				case 2: methodModifier.add("-");
					break;
				case 4: methodModifier.add("#");
					break;
				case 9: methodModifier.add("+");
					break;
				case 1025: methodModifier.add("+");
					break;
				default: 
					break;
			}
			// fetch out all parameter name and their data type
			if (n.getParameters().isEmpty() == false) {
				modifyString = n.getParameters().toString();
				for (int i = 0; i < modifyString.length(); i++) {
					if (modifyString.charAt(i) == ']') {
						count++;
						endIndex = i;
					}
				}
				if (count > 1) {
					startIndex = modifyString.indexOf("[");
				} else {
					startIndex = modifyString.indexOf("[");
					endIndex = modifyString.indexOf("]");
				}
				replacement = modifyString.substring(startIndex+1, endIndex);
				if (replacement.contains(",") == true) {
					temp = replacement.split(",");
					for (int i = 0; i < temp.length; i++) {
						temp[i] = temp[i].replaceAll("^\\s*|\\s*$", "");
					}
					for (int i = 0; i < temp.length; i++) {
						temp_inner = temp[i].split(" ");
						if (paraFormat == null) {
							paraFormat = temp_inner[1] + ":" + temp_inner[0];
							paraType.add(temp_inner[0]);
						} else if (i == temp.length-1 && i != 0) {
							paraFormat = paraFormat + temp_inner[1]+ ":" + temp_inner[0];
							paraType.add(temp_inner[0]);
						} else {
							paraFormat = ", " + paraFormat + temp_inner[1]+ ":" + temp_inner[0] + ", ";
							paraType.add(temp_inner[0]);
						}
					}
				} else {
					temp = replacement.split(" ");
					for (int i = 0; i < temp.length; i++) {
						temp[i] = temp[i].replaceAll("^\\s*|\\s*$", "");
					}
					if (paraFormat == null) {
						paraFormat = temp[1] + ":" + temp[0];
						paraType.add(temp[0]);
					}
				}
				paraMeter.add(paraFormat);
			} else {
				paraMeter.add(null);
			}
			
			// find non-Reserve data type in the parameter
			for (int i = 0; i < paraType.size(); i++) {
				if (!reserveTypes.contains(paraType.get(i))) {
					if (paraType.get(i).contains("<")) {
						startIndex = paraType.get(i).indexOf("<");
						endIndex = paraType.get(i).indexOf(">");
						if (paraType.get(i).indexOf(">") < 0) {
							System.err.println("Expecting > in the code");
							System.exit(1);
						}
						subString = paraType.get(i).substring(startIndex+1, endIndex);
						if (!nonPrimitive.contains(subString)) {
							nonPrimitive.add(subString);
						}
					} else if (paraType.get(i).contains("[")) {
						endIndex = paraType.get(i).indexOf("[");
						subString = paraType.get(i).substring(0, endIndex);
						if (!nonPrimitive.contains(subString)) {
							if (!reserveTypes.contains(subString)) {
								nonPrimitive.add(subString);
							}
						}
					} else {
						if (!nonPrimitive.contains(paraType.get(i))) {
							nonPrimitive.add(paraType.get(i));
						}
					}
				}
			}
			
			// find non_Reserve data type in the method
			for (int i = 0; i < methodType.size(); i++) {
				if (!reserveTypes.contains(methodType.get(i))) {
					if (methodType.get(i).contains("<")) {
						startIndex = methodType.get(i).indexOf("<");
						endIndex = methodType.get(i).indexOf(">");
						if (methodType.get(i).indexOf(">") < 0) {
							System.err.println("Expecting > in the code");
							System.exit(1);
						}
						subString = methodType.get(i).substring(startIndex+1, endIndex);
						if (!nonReserveMethodType.contains(subString)) {
							nonReserveMethodType.add(subString);
						}
					} else if (methodType.get(i).contains("[")) {
						endIndex = methodType.get(i).indexOf("[");
						subString = methodType.get(i).substring(0, endIndex);
						if (!nonReserveMethodType.contains(subString)) {
							if(!reserveTypes.contains(subString)){
								nonReserveMethodType.add(subString);
							}
						}
					} else {
						if (!nonReserveMethodType.contains(methodType.get(i))) {
							nonReserveMethodType.add(methodType.get(i));
						}
					}
				}
			}
		}else{
			methodFormat = null;
		}
	}
	
	public ArrayList<String> getNonPrimitiveType() {
		return nonPrimitive;
	}
	
	public ArrayList<String> getNonReserveMethodType() {
		return nonReserveMethodType;
	}
	
	public ArrayList<String> getBodyType() {
		return bodyType;
	}
	
	public ArrayList<String> getMethodFormat() {
		if (methodFormat != null) {
			for (int i = 0; i < methodName.size(); i++) {
				if (!methodModifier.get(i).equals("+")) {
					continue;
				}
				if (paraMeter.get(i) == null && methodName.size() > 0) {
					methodFormat.add(methodModifier.get(i)+methodName.get(i)+"():"+methodType.get(i));
				} else if (methodName.size() > 0) {
					methodFormat.add(methodModifier.get(i)+methodName.get(i)+"("+paraMeter.get(i)+"):"+methodType.get(i));
				}
			}
			return methodFormat;
		}
		return methodFormat;
	}
}
