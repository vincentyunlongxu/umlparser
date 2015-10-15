package javaToUML;

import java.util.ArrayList;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class GetClassInterfaceName extends VoidVisitorAdapter {
	private boolean isInterface = false;
	private String classInterfaceName;
	private String parentClass = null;
	private String interFace = null;
	
	
	public void visit(ClassOrInterfaceDeclaration n, Object arg){
		if(n.getName().toString() == null){
			System.err.println("System Error: Not valid Java class file, System Exit");
			System.exit(1);
		}else{
			classInterfaceName = n.getName().toString();
			if(n.isInterface() == true){
				isInterface = true;
			}else{
				if(n.getExtends().toString() != "[]"){
					parentClass = n.getExtends().toString();
				}
				if(n.getImplements().toString() != "[]"){
					interFace = n.getImplements().toString();
				}
			}
		}
	}
	
	public boolean isInterface(){
		return isInterface;
	}
	
	public String getClassInterfaceName(){
		return classInterfaceName;
	}
	
	public String getParentClass(){
		return parentClass;
	}
	
	public String allInterFaceName (){
		return interFace;
	}
}
