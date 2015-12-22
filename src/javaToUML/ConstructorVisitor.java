package javaToUML;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ConstructorVisitor extends VoidVisitorAdapter{
	
	private String consFormat;
	private String consName;
	private String consModifier;
	private String paraMeter;
	private int count = 0;
	private ArrayList<String> paraType = new ArrayList<String>();
	private ArrayList<String> noReserveType = new ArrayList<String>();
	private ArrayList<String> reserveTypes = new ArrayList<String>(Arrays.asList("byte","short","int","long","float","double","boolean","char","Integer","String", "Character","void"));
	
	public void visit(ConstructorDeclaration n, Object arg) {
		
		int startIndex;
		int endIndex;
		String[] temp;
		String[] temp_inner;
		
        if(n.getName().toString() != null){
        	consName = n.getName().toString();
        	switch(n.getModifiers()){
				case 0: consModifier = "~";
					break;
				case 1: consModifier = "+";
					break;
				case 2: consModifier = "-";
					break;
				case 4: consModifier = "#";
					break;
				default: 
					break;
        	}
        	if(n.getParameters().isEmpty() == false){
	        	paraMeter = n.getParameters().toString();
				for(int i = 0; i < paraMeter.length(); i++){
					if(paraMeter.charAt(i) == ']'){
						count++;
						endIndex = i;
					}
				}
				if(count > 1){
					startIndex = paraMeter.indexOf("[");
				}else{
					startIndex = paraMeter.indexOf("[");
					endIndex = paraMeter.indexOf("]");
				}
	        	startIndex = paraMeter.indexOf("[");
	        	endIndex = paraMeter.indexOf("]");
	        	paraMeter = paraMeter.substring(startIndex+1, endIndex);
	        	temp = paraMeter.split(",");
	        	for(int i = 0; i < temp.length; i++){
	        		temp[i] = temp[i].replaceAll("^\\s*|\\s*$", "");
	        	}
	        	for(int i = 0; i < temp.length; i++){
	        		temp_inner = temp[i].split(" ");
	        		if(!reserveTypes.contains(temp_inner[0])){
	        			noReserveType.add(temp_inner[0]);
	        		}
	        		paraType.add(temp_inner[0]);
	        		if(i == 0){
	        			consFormat = consModifier + consName + "(" + temp_inner[1] + ":" + temp_inner[0];
	        		}else{
	        			consFormat = consFormat + ", " + temp_inner[1] + ":" +temp_inner[0];
	        		}
	        	}
	        	consFormat = consFormat + ")";
        	}else{
        		consFormat = consModifier + consName + "()";
        	}
        }else{
        	consFormat = null;
        }
    }
	
	public String getConsFormat (){
		return consFormat;
	}
	
	public ArrayList<String> getNoReserveType(){
		return noReserveType;
	}
}
