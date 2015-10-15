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

	public void visit(MethodDeclaration n, Object arg){
		int startIndex;
		int endIndex;
		String replacement;
		String modifyString;
		String[] temp;
		String[] temp_inner;
		String paraFormat = null;
		String subString;
		
		// test if method name is not null means it contains some data we need
		if(n.getName().toString() != null){
			methodName.add(n.getName().toString());
			methodType.add(n.getType().toString());
			switch(n.getModifiers()){
				case 0: methodModifier.add("~");
					break;
				case 1: methodModifier.add("+");
					break;
				case 2: methodModifier.add("-");
					break;
				case 4: methodModifier.add("#");
					break;
				default: 
					break;
			}
			// fetch out all parameter name and their data type
			if(n.getParameters().isEmpty() == false){
				modifyString = n.getParameters().toString();
				startIndex = modifyString.indexOf("[");
				endIndex = modifyString.indexOf("]");
				replacement = modifyString.substring(startIndex+1, endIndex);
				temp = replacement.split(",");
				for(int i = 0; i < temp.length; i++){
					temp[i] = temp[i].replaceAll("^\\s*|\\s*$", "");
				}
				for(int i = 0; i < temp.length; i++){
					temp_inner = temp[i].split(" ");
					if(paraFormat == null){
						paraFormat = temp_inner[1]+ ":" + temp_inner[0];
						paraType.add(temp_inner[0]);
					}else if(i == temp.length-1 && i != 0){
						paraFormat = paraFormat + temp_inner[1]+ ":" + temp_inner[0];
						paraType.add(temp_inner[0]);
					}else{
						paraFormat = ", "+paraFormat + temp_inner[1]+ ":" + temp_inner[0] + ", ";
						paraType.add(temp_inner[0]);
					}
					
				}
				paraMeter.add(paraFormat);
			}else{
				paraMeter.add(null);
			}
			
			// find non-Reserve data type in the parameter
			if(!reserveTypes.contains(paraType.get(0))){
				if(paraType.get(0).contains("<")){
					startIndex = paraType.get(0).indexOf("<");
					endIndex = paraType.get(0).indexOf(">");
					if(paraType.get(0).indexOf(">") < 0){
						System.err.println("Expecting > in the code");
						System.exit(1);
					}
					subString = paraType.get(0).substring(startIndex+1, endIndex);
					if(!nonPrimitive.contains(subString)){
						nonPrimitive.add(subString);
					}
				}else if(paraType.get(0).contains("[")){
					endIndex = paraType.get(0).indexOf("[");
					subString = paraType.get(0).substring(0, endIndex);
					if(!nonPrimitive.contains(subString)){
						if(!reserveTypes.contains(subString)){
							nonPrimitive.add(subString);
						}
					}
				}else{
					if(!nonPrimitive.contains(paraType.get(0))){
						nonPrimitive.add(paraType.get(0));
					}
				}
			}
			
			// find non_Reserve data type in the method
			if(!reserveTypes.contains(methodType.get(0))){
				if(methodType.get(0).contains("<")){
					startIndex = methodType.get(0).indexOf("<");
					endIndex = methodType.get(0).indexOf(">");
					if(methodType.get(0).indexOf(">") < 0){
						System.err.println("Expecting > in the code");
						System.exit(1);
					}
					subString = methodType.get(0).substring(startIndex+1, endIndex);
					if(!nonReserveMethodType.contains(subString)){
						nonReserveMethodType.add(subString);
					}
				}else if(methodType.get(0).contains("[")){
					endIndex = methodType.get(0).indexOf("[");
					subString = methodType.get(0).substring(0, endIndex);
					if(!nonReserveMethodType.contains(subString)){
						if(!reserveTypes.contains(subString)){
							nonReserveMethodType.add(subString);
						}
					}
				}else{
					if(!nonReserveMethodType.contains(methodType.get(0))){
						nonReserveMethodType.add(methodType.get(0));
					}
				}
			}
		}else{
			methodFormat = null;
		}
	}
	
	public ArrayList<String> getNonPrimitiveType(){
		return nonPrimitive;
	}
	
	public ArrayList<String> getNonReserveMethodType(){
		return nonReserveMethodType;
	}
	
	public ArrayList<String> getMethodFormat(){
		if(methodFormat != null){
			for(int i = 0; i < methodName.size(); i++){
				if(paraMeter.get(i) == null){
					methodFormat.add(methodModifier.get(i)+methodName.get(i)+"():"+methodType.get(i));
				}else{
					methodFormat.add(methodModifier.get(i)+methodName.get(i)+"("+paraMeter.get(i)+"):"+methodType.get(i));
				}
			}
			return methodFormat;
		}
		return methodFormat;
	}
}
