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
import java.util.Set;

import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.zxing.Writer;

public class umlparser {

	public static void main(String[] args) {
		// get the <classpath> user define on the command line
		File folder = new File("/Users/yunlongxu/Documents/CMPE 202/uml-parser-test-1");  ///Users/yunlongxu/Documents/CMPE 202/uml-parser-test-1 2
		// put all files into a File array
		File[] listofFiles = folder.listFiles();
		// store all name of files 
		String consInfo;
		// only store file name before .java
		String spString;
		// define output path
		String output = "/Users/yunlongxu/Documents/CMPE 202";
		String[] cutString;
		String key = null;
		String value;
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
				fieldInfo = getFiledInfo.getFormat();
				if(fieldInfo != null){
					umlGrammar.addAll(fieldInfo);
				}else{
					umlGrammar.add(null);
				}
				
				// add Constructor to UML
				ConstructorVisitor getConsInfo = new ConstructorVisitor();
				getConsInfo.visit(cu, null);
				consInfo = getConsInfo.getConsFormat();
				umlGrammar.add(consInfo);
				
				
				// add Method to UML
				MethodVisitor getMethodInfo = new MethodVisitor();
				getMethodInfo.visit(cu, null);
				methodInfo = getMethodInfo.getMethodFormat();
				if(methodInfo != null){
					umlGrammar.addAll(methodInfo);
				}else{
					umlGrammar.add(null);
				}
				umlGrammar.add("}");
				
				// create relationship between classes
				DefineRelationship getRelation = new DefineRelationship(getFiledInfo, getClassInterface, fileName, interFaceName, getMethodInfo);
				getRelation.CreatRelation();
				checkList = getRelation.getRelationFormat();
				for(int k = 0; k < checkList.size(); k++){
					if(!relationship.contains(checkList.get(k))){
						relationship.add(checkList.get(k));
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
		umlGrammar.add("@enduml");
		new umlparser().writeUMLOutput(umlGrammar, output);
		new umlparser().generateUML(output);
		
		for(int j = 0; j < umlGrammar.size(); j++){
			if(umlGrammar.get(j) == null){
				continue;
			}else{
				System.out.println(umlGrammar.get(j)); 
			}
		}
		
	}
	
	public void writeUMLOutput(ArrayList<String> umlGrammar, String outputPath){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputPath+"/UMLOutPut.java","UTF-8");
			for(int i = 0; i < umlGrammar.size(); i++){
				if(umlGrammar.get(i) == null){
					continue;
				}else{
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
	
	public void generateUML(String outputPath){
		File source = new File(outputPath+"/UMLOutPut.java");
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