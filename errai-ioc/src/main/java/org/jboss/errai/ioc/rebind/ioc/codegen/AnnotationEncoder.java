package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ObjectBuilder;

import java.lang.annotation.Annotation;

public class AnnotationEncoder {
    public static String encode(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();

        ObjectBuilder builder = ObjectBuilder.newInstanceOf(annotationClass);
        ClassStructureBuilder classStructureBuilder = builder.extend();
//
//        MetaMethod m = builder.getType().getMethod("annotationType");
//        Statement statement = new StringStatement("return " + annotationClass.getName() + ".class;");
//
//        classStructureBuilder.publicOverridesMethod(m, statement);
//        builder.integrateClassStructure(classStructureBuilder);

        return builder.generate();
    }


}
