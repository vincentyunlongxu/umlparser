package javaToUML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.zxing.Writer;

public class umlparser {

	public static void main(String[] args) {
		// get the <classpath> user define on the command line
		String inputFolder = args[0];
		File folder = new File(inputFolder);  ///Users/yunlongxu/Documents/CMPE 202/uml-parser-test-1 2
		// put all files into a File array
		File[] listofFiles = folder.listFiles();
		// store all name of files 
		String consInfo;
		// only store file name before .java
		String spString;
		// define output path "/Users/yunlongxu/Documents/CMPE202"
		String outputPath = args[0];
		String outputFileName = args[1];
		String[] cutString;
		String key = null;
		String value;
		String[] spString1;
		String tempStr;
		String testString;
		String testString1;
		int spIndex;
		ArrayList<String> fileName = new ArrayList<String>();
		ArrayList<String> umlGrammar = new ArrayList<String>();
		ArrayList<String> fieldInfo = new ArrayList<String>();
		ArrayList<String> methodInfo = new ArrayList<String>();
		ArrayList<String> interFaceName = new ArrayList<String>();
		ArrayList<String> checkList = new ArrayList<String>();
		ArrayList<String> relationship = new ArrayList<String>();
		HashMap<String, String> hmap = new HashMap<String, String>();
		ArrayList<String> relation = new ArrayList<String>();
		ArrayList<String> preAsso = new ArrayList<String>();
		
		
		// start UML Grammar
		umlGrammar.add("@startuml");
		umlGrammar.add("skinparam classAttributeIconSize 0");
		
		//load all file name to filename ArrayList
		//load all interface classes if any exists
		for(int k = 0; k < listofFiles.length; k++){
			File file1 = listofFiles[k];
			if(file1.isFile() && file1.getName().endsWith(".java")){
				spIndex = file1.getName().indexOf(".");
				spString = file1.getName().substring(0, spIndex);
				fileName.add(spString);
				FileInputStream in1 = null;
				CompilationUnit cu1 = null;
				// initialize the compilation unit
				try{
					in1 = new FileInputStream(file1.getAbsolutePath());
					cu1 = JavaParser.parse(in1);
				}catch(Exception e){
					System.out.println(e.getCause());
					System.err.println("File path error or Parser Error");
				}finally{
					try{
						in1.close();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				GetClassInterfaceName getClassInterface1 = new GetClassInterfaceName();
				getClassInterface1.visit(cu1, null);
				if(getClassInterface1.isInterface() == true){
					interFaceName.add(getClassInterface1.getClassInterfaceName());
				}
			}
		}
		
			
		// start to read each of single file
		for(int i = 0; i < listofFiles.length; i++){
			File file = listofFiles[i];
			// open all .java files
			if(file.isFile() && file.getName().endsWith(".java")){
				FileInputStream in = null;
				CompilationUnit cu = null;
				// initialize the compilation unit
				try{
					in = new FileInputStream(file.getAbsolutePath());
					cu = JavaParser.parse(in);
				}catch(Exception e){
					System.out.println(e.getCause());
					System.err.println("File path error or Parser Error");
				}finally{
					try{
						in.close();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				
				// add class or interface name to uml
				GetClassInterfaceName getClassInterface = new GetClassInterfaceName();
				getClassInterface.visit(cu, null);
				if(getClassInterface.isInterface() == true){
					umlGrammar.add("interface "+getClassInterface.getClassInterfaceName()+"{");
				}else{
					umlGrammar.add("class "+getClassInterface.getClassInterfaceName()+"{");
				}
				
				// add Field (variable, attribute, modifier) to uml
				FieldVisitor getFiledInfo = new FieldVisitor();
				getFiledInfo.visit(cu, null);
				fieldInfo = getFiledInfo.getFormat(fileName);
				
				
				// add Constructor to UML
				ConstructorVisitor getConsInfo = new ConstructorVisitor();
				getConsInfo.visit(cu, null);
				consInfo = getConsInfo.getConsFormat();
				
				
				// add Method to UML
				MethodVisitor getMethodInfo = new MethodVisitor();
				getMethodInfo.visit(cu, null);
				methodInfo = getMethodInfo.getMethodFormat();
				
				Map<String, Integer> testSetGet = new HashMap<String, Integer>();
				for (int j = 0; j < methodInfo.size(); j++) {
					if (!methodInfo.get(j).substring(0, 1).equals("+")) {
						continue;
					}
					if (methodInfo.get(j).substring(1, 4).toLowerCase().equals("get") || methodInfo.get(j).substring(1, 4).toLowerCase().equals("set")) {
						Integer lastIndex =  testSetGet.put(methodInfo.get(j).substring(4, methodInfo.get(j).indexOf("(")).toLowerCase(), Integer.valueOf(j));
						System.out.println(methodInfo.get(j));
						if (lastIndex != null) {
							for (int k = 0; k < fieldInfo.size(); k++) {
								if (fieldInfo.get(k).substring(1, fieldInfo.get(k).indexOf(":")).toLowerCase().equals(methodInfo.get(j).substring(4, methodInfo.get(j).indexOf("(")).toLowerCase())) {
									fieldInfo.set(k, "+" + fieldInfo.get(k).substring(1));
									methodInfo.remove(j);
									j = lastIndex;
									methodInfo.remove(j);
								}
							}
						}
					}
				}
				
				if (fieldInfo != null) {
					umlGrammar.addAll(fieldInfo);
				}
				
				if (consInfo != null) {
					umlGrammar.add(consInfo);
				}
				
				if (methodInfo != null) {
					umlGrammar.addAll(methodInfo);
				}
				
				
				umlGrammar.add("}");
				
				// create relationship between classes
				DefineRelationship getRelation = new DefineRelationship(getFiledInfo, getClassInterface, fileName, interFaceName, getMethodInfo, getConsInfo);
				getRelation.CreatRelation();
				preAsso.addAll(getRelation.getAssociation());
				checkList = getRelation.getRelationFormat();
				for(int k = 0; k < checkList.size(); k++){
					if(!relationship.contains(checkList.get(k))){
						relationship.add(checkList.get(k));
					}
				}
			}
		}
		if(preAsso.isEmpty() == false){
			for(int i = 0; i < preAsso.size(); i++){
				tempStr = preAsso.get(i);
				spString1 = tempStr.split(":");
				if(spString1[1].indexOf("1") > 0){
					testString = spString1[0] + ":\"1\"--:" + spString1[2];
					testString1 = spString1[0] + ":\"*\"--:" + spString1[2];
					if(preAsso.contains(testString)){
						testString = spString1[0] + ":\"1\"--:\"1\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"*\"--:\"1\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"1\"--:\"*\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"*\"--:\"*\"" + spString1[2])){
							continue;
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else if(preAsso.contains(testString1)){
						testString = spString1[0] + ":\"*\"--:\"1\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
							for(int j = 0 ; j < relationship.size(); j++) {
								if(relationship.get(j) == (spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
									if(!relationship.contains(testString)){
										relationship.add(testString);
									}
									relationship.remove(i);
								}
							}
						}else if(relationship.contains(spString1[0] + ":\"*\"--:\"*\"" + spString1[2])){
							continue;
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else{
						testString = spString1[0] + ":--:\"1\"" + spString1[2];
						if(!relationship.contains(testString)){
							relationship.add(testString);
						}
					}
				}else if(spString1[1].indexOf("*") > 0){
					testString = spString1[0] + ":\"1\"--:" + spString1[2];
					testString1 = spString1[0] + ":\"*\"--:" + spString1[2];
					if(preAsso.contains(testString)){
						testString = spString1[0] + ":\"1\"--:\"*\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
							for(int j = 0 ; j < relationship.size(); j++) {
								if(relationship.get(j) == (spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
									if(!relationship.contains(testString)){
										relationship.add(testString);
									}
									relationship.remove(i);
								}
							}
						}else if(relationship.contains(spString1[0] + ":\"*\"--:\"*\"" + spString1[2])){
							continue;
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else if(preAsso.contains(testString1)){
						testString = spString1[0] + ":\"*\"--:\"*\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"1\"--:\"1\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"1\"--:\"*\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"*\"--:\"1\"" + spString1[2])){
							for(int j = 0 ; j < relationship.size(); j++) {
								if((relationship.get(j) == (spString1[0] + ":\"1\"--:\"1\"" + spString1[2])) || (relationship.get(j) == (spString1[0] + ":\"1\"--:\"*\"" + spString1[2])) || (relationship.get(j) == (spString1[0] + ":\"*\"--:\"1\"" + spString1[2]))){
									if(!relationship.contains(testString)){
										relationship.add(testString);
									}
									relationship.remove(i);
								}
							}
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else{
						testString = spString1[0] + ":--:\"*\"" + spString1[2];
						if(!relationship.contains(testString)){
							relationship.add(testString);
						}
					}
				}else if(spString1[1].indexOf("1") == 0){
					testString = spString1[0] + ":--:\"1\"" + spString1[2];
					testString1 = spString1[0] + ":--:\"*\"" + spString1[2];
					if(preAsso.contains(testString)){
						testString = spString1[0] + ":\"1\"--:\"1\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"*\"--:\"1\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"1\"--:\"*\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"*\"--:\"*\"" + spString1[2])){
							continue;
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else if(preAsso.contains(testString1)){
						testString = spString1[0] + ":\"1\"--:\"*\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
							for(int j = 0 ; j < relationship.size(); j++) {
								if(relationship.get(j) == (spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
									if(!relationship.contains(testString)){
										relationship.add(testString);
									}
									relationship.remove(i);
								}
							}
						}else if(relationship.contains(spString1[0] + ":\"*\"--:\"*\"" + spString1[2])){
							continue;
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else{
						testString = spString1[0] + "\"1\":--:" + spString1[2];
						if(!relationship.contains(testString)){
							relationship.add(testString);
						}
					}
				}else if(spString1[1].indexOf("*") == 0){
					testString = spString1[0] + ":--:\"1\"" + spString1[2];
					testString1 = spString1[0] + ":--:\"*\"" + spString1[2];
					if(preAsso.contains(testString)){
						testString = spString1[0] + ":\"*\"--:\"1\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
							for(int j = 0 ; j < relationship.size(); j++) {
								if(relationship.get(j) == (spString1[0] + ":\"1\"--:\"1\"" + spString1[2])){
									if(!relationship.contains(testString)){
										relationship.add(testString);
									}
									relationship.remove(i);
								}
							}
						}else if(relationship.contains(spString1[0] + ":\"*\"--:\"*\"" + spString1[2])){
							continue;
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else if(preAsso.contains(testString1)){
						testString = spString1[0] + ":\"*\"--:\"*\"" + spString1[2];
						if(relationship.contains(spString1[0] + ":\"1\"--:\"1\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"1\"--:\"*\"" + spString1[2]) || relationship.contains(spString1[0] + ":\"*\"--:\"1\"" + spString1[2])){
							for(int j = 0 ; j < relationship.size(); j++) {
								if((relationship.get(j) == (spString1[0] + ":\"1\"--:\"1\"" + spString1[2])) || (relationship.get(j) == (spString1[0] + ":\"1\"--:\"*\"" + spString1[2])) || (relationship.get(j) == (spString1[0] + ":\"*\"--:\"1\"" + spString1[2]))){
									if(!relationship.contains(testString)){
										relationship.add(testString);
									}
									relationship.remove(i);
								}
							}
						}else{
							if(!relationship.contains(testString)){
								relationship.add(testString);
							}
						}
					}else{
						testString = spString1[0] + "\"*\":--:" + spString1[2];
						if(!relationship.contains(testString)){
							relationship.add(testString);
						}
					}
				}
			}
		}
		for(int i = 0; i < relationship.size(); i++){
			String tempString = relationship.get(i);
			if(tempString == null){
				continue;
			}
			if(!tempString.contains(":") || (tempString.split(":").length <= 2)){
				relation.add(tempString);
			}else{
				cutString = tempString.split(":");
				if(cutString.length < 3){
					continue;
				}
				key = cutString[0]+":"+cutString[2];
				value = cutString[1];
				if(!hmap.containsKey(key)){
					hmap.put(key, value);
				}else{
					if(hmap.get(key).contains("*")){
						continue;
					}else if(value.contains("*")){
						hmap.put(key, value);
					}
				}
			}
		}
		Set keys = hmap.keySet();
		Iterator itr = keys.iterator();
		String groupRelation = null;
		while(itr.hasNext()){
			key = (String)itr.next();
			value = (String)hmap.get(key);
			cutString = key.split(":");
			groupRelation = cutString[0]+value+cutString[1];
			relation.add(groupRelation);
		}
		
		umlGrammar.addAll(relation);
		new umlparser().changeToLollipop(umlGrammar);
		umlGrammar.add("@enduml");
		new umlparser().writeUMLOutput(umlGrammar, outputFileName, outputPath);
		new umlparser().generateUML(outputFileName, outputPath);

		for (int j = 0; j < umlGrammar.size(); j++) {
			if (umlGrammar.get(j) == null) {
				continue;
			} else {
				System.out.println(umlGrammar.get(j)); 
			}
		}
		
	}
	
	public void changeToLollipop(ArrayList<String> umlGrammar) {
		String implementation;
		String concreteClass;
		String interfaceName;
		String client;
		String useCase;
		String testStr;
		String output;
		int count = 0;
		int countUse = 0;
		
		for (int i = 0; i < umlGrammar.size(); i++) {
			// need to decide if the interface has been implemented
			if (umlGrammar.get(i) == null) {
				continue;
			} else if (umlGrammar.get(i).toString().contains("<|..")) {							// find interface and concrete class
				implementation = umlGrammar.get(i);
				implementation = implementation.replaceAll("\\s+", "");
				System.out.println(implementation);
				interfaceName = implementation.substring(0, implementation.indexOf("<"));
				for (int k = 0; k < umlGrammar.size(); k++) {
					if (umlGrammar.get(k) == null) {
						continue;
					} 
					if ((umlGrammar.get(k).contains(interfaceName + " <|.. ")) || (umlGrammar.get(k).contains(interfaceName + "<|.."))) {
						count++;
					}
					if ((umlGrammar.get(k).contains(interfaceName + " :use ")) || (umlGrammar.get(k).contains(interfaceName + ":use"))) {
						countUse++;
					}
				}
				if (count > 1 || countUse > 1) {												// Lollipop cannot be used for multiple user and clients
					count = 0;
					countUse = 0;
					continue;
				}
				concreteClass = implementation.substring(implementation.indexOf(".") + 2, implementation.length());
				testStr = interfaceName + ":use";												// get the name of client
				for (int j = 0; j < umlGrammar.size(); j++) {
					if (umlGrammar.get(j) == null) {
						continue;
					}else if (umlGrammar.get(j).toString().contains(testStr)) {
						useCase = umlGrammar.get(j).toString();
						useCase = useCase.replaceAll("\\s+", "");
						client = useCase.substring(0, useCase.indexOf("."));
						output = concreteClass + "-0)-" + client + ":\"" + interfaceName + "\"";	// draw a relationship between concrete class, interface and client
						
						umlGrammar.add(output);
						umlGrammar.remove(i);
						umlGrammar.remove(j-1);
						removeOriginInterface(umlGrammar, interfaceName);
					}
				}
			}
		}
		
	}
	
	public void removeOriginInterface (ArrayList<String> umlGrammar, String interfaceName) { // since lollipop can replace some interfaces, therefore we need to delete the nonusable interface in our uml grammar.
		int index = 0;
		int endIndex = 0;
		String testStr;
		for (int i = 0; i < umlGrammar.size(); i++) {
			if (umlGrammar.get(i) == null) {
				continue;
			} else {
				if (umlGrammar.get(i).contains("interface "+interfaceName+"{")) {
					index = i;
					for (int k = i; k < umlGrammar.size(); k++) {
						if (umlGrammar.get(k)  == null) {
							continue;
						}else{
							testStr = umlGrammar.get(k).toString();
							if (testStr.compareTo("}") == 0) {
								endIndex = k;
								break;
							}
						}
					}
					for (int k = endIndex; k >= index; k--) {
						umlGrammar.remove(k);
					}
					break;
				} 
			}
		}
	}
	
	public void writeUMLOutput(ArrayList<String> umlGrammar, String outputFileName, String outputPath) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputPath + "/" + outputFileName + ".java","UTF-8");
			for (int i = 0; i < umlGrammar.size(); i++) {
				if (umlGrammar.get(i) == null) {
					continue;
				} else {
					writer.println(umlGrammar.get(i));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.close();
	}
	
	public void generateUML(String outputFileName, String outputPath) {
		File source = new File(outputPath + "/" + outputFileName + ".java");
		SourceFileReader reader = null;
		try {
			reader = new SourceFileReader(source);
			List<GeneratedImage> list = reader.getGeneratedImages();
			File png = list.get(0).getPngFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}