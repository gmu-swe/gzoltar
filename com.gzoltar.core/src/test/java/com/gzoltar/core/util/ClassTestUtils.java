package com.gzoltar.core.util;

import java.io.IOException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassTestUtils {
	public static ClassNode getClassNode(String className){
		try {
			ClassReader cr = new ClassReader(className);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			return cn;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static MethodNode getMethodNode(String className, String methodName){
		try {
			ClassReader cr = new ClassReader(className);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			for(MethodNode mn : cn.methods)
				if(mn.name.equals(methodName))
					return mn;
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
