package javaToUML;

import java.util.ArrayList;

public class DefineRelationship{
	private String parentClass;
	private String currentClass;
	private String interfaceName;
	private ArrayList<String> fileName;
	private ArrayList<String> relationFormat;
	private ArrayList<String> nonPrimitive;
	private ArrayList<String> interFaceName;
	private ArrayList<String> paraType;
	private ArrayList<String> nonMethodType;
	
	public DefineRelationship(FieldVisitor fv, GetClassInterfaceName cin, ArrayList<String> fileName, ArrayList<String> interFaceName, MethodVisitor mv){
		parentClass = cin.getParentClass();
		currentClass = cin.getClassInterfaceName();
		interfaceName = cin.allInterFaceName();
		relationFormat = new ArrayList<String>();
		nonPrimitive = fv.getNonReserve();
		this.fileName = fileName;
		this.interFaceName = interFaceName;
		this.paraType = mv.getNonPrimitiveType();
		nonMethodType = mv.getNonReserveMethodType();
	}
	
	public void CreatRelation(){
		int startIndex;
		int endIndex;
		String tempStr;
		String[] interFace;
		String[] spString;
		
		// define relationship with parent class
		if (parentClass != null){	
			startIndex = parentClass.indexOf("[");
			endIndex = parentClass.indexOf("]");
			parentClass = parentClass.substring(startIndex+1, endIndex);
			// if the name of the parent does not contain in the folder, throw error message
			if(!fileName.contains(parentClass)){
				System.err.println("No class "+parentClass+" exists in the folder");
				System.exit(1);
			}
			relationFormat.add(parentClass+" <|-- "+ currentClass);
		}else{
			relationFormat.add(null);
		}
		
		// define if any relationship with interface class
		if(interfaceName != null){			// 这里我要测试interface class是不是在这个folder里面
			if(interfaceName.split(",").length > 1){
				startIndex = interfaceName.indexOf("[");
				endIndex = interfaceName.indexOf("]");
				interfaceName = interfaceName.substring(startIndex+1, endIndex);
				interFace = interfaceName.split(",");
				for(int i = 0; i < interFace.length; i++){
					interFace[i] = interFace[i].replaceAll("^\\s*|\\s*$", "");
					// test if the interface classes is inside of folder or not
					if(!fileName.contains(interFace[i])){
						System.err.println("No Interface "+interFace[i]+" exists in the folder");
						System.exit(1);
					}
					relationFormat.add(interFace[i] + " <|.. " + currentClass);
				}
			}else{
				startIndex = interfaceName.indexOf("[");
				endIndex = interfaceName.indexOf("]");
				interfaceName = interfaceName.substring(startIndex+1, endIndex);
				// test if the interface class is inside of folder or not
				if(!fileName.contains(interfaceName)){
					System.err.println("No Interface "+interfaceName+" exists in the folder");
					System.exit(1);
				}
				relationFormat.add(interfaceName + " <|.. " + currentClass);
			}
		}
		
		//define the relationship between nonPrimitive type and current class
		if(nonPrimitive != null){
			for(int i = 0; i < nonPrimitive.size(); i++){
				tempStr = nonPrimitive.get(i);
				if(tempStr.contains(":")){
					spString = tempStr.split(":");
					if(interFaceName.contains(spString[0])){
						relationFormat.add(currentClass+"..>"+spString[0]+" : use");
						continue;
					}
					int check = spString[0].compareTo(currentClass);
					if(check < 0)
						relationFormat.add(spString[0]+":"+spString[1]+"--\"1\":"+currentClass);
					else if(check > 0)
						relationFormat.add(currentClass+":\"1\"--"+spString[1]+":"+spString[0]);
				}else{
					int check1 = nonPrimitive.get(i).compareTo(currentClass);
					if(check1 < 0)
						relationFormat.add(nonPrimitive.get(i)+":\"1\"--\"1\":"+currentClass);
					else if(check1 > 0)
						relationFormat.add(currentClass+":\"1\"--\"1\":"+nonPrimitive.get(i));
				}
			}
		}
		
		//define the relationship between nonPrimitive type in method and current classes
		if(paraType != null){
			for(int i = 0; i < paraType.size(); i++){
				// test if the Class file is inside of folder
				if(fileName.contains(paraType.get(i))){
					if(interFaceName.contains(paraType.get(i))){
						relationFormat.add(currentClass+"..>"+paraType.get(i)+" : use");
						System.out.println(currentClass+"..>"+paraType.get(i)+" : use");
						continue;
					}
					int check = paraType.get(i).compareTo(currentClass);
					if(check < 0)
						relationFormat.add(paraType.get(i)+"--"+currentClass);
					else if(check > 0)
						relationFormat.add(currentClass+"--"+paraType.get(i));
				}
			}
		}
		
		if(nonMethodType != null){
			for(int i = 0; i < nonMethodType.size(); i++){
				// test if the Class file is inside of folder
				if(fileName.contains(nonMethodType.get(i))){
					if(interFaceName.contains(nonMethodType.get(i))){
						relationFormat.add(currentClass+"..>"+nonMethodType.get(i)+" : use");
						continue;
					}
					int check = nonMethodType.get(i).compareTo(currentClass);
					if(check < 0)
						relationFormat.add(nonMethodType.get(i)+"--"+currentClass);
					else if(check > 0)
						relationFormat.add(currentClass+"--"+nonMethodType.get(i));
				}
			}
		}
		
	}
	
	public ArrayList<String> getRelationFormat(){
		return relationFormat;
	}
}
