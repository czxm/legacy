package com.intel.soak.utils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javassist.ClassPool;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;

/**
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/20/13 10:59 PM
 */
public enum JavassistAnnotationsHelper {

    INSTANCE;

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    private static final String VISIBLE_TAG = AnnotationsAttribute.visibleTag;
    private static final String INVISIBLE_TAG = AnnotationsAttribute.invisibleTag;


    public static Annotation[] getAnnotationsForClass(ClassFile classFile) {

        AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(VISIBLE_TAG);
        AnnotationsAttribute invisible = (AnnotationsAttribute) classFile.getAttribute(INVISIBLE_TAG);

        Set<Annotation> retVal = new HashSet<Annotation>();

        retVal.addAll(findAnnotationsForAnnotationsAttribute(visible));
        retVal.addAll(findAnnotationsForAnnotationsAttribute(invisible));

        return retVal.toArray(new Annotation[retVal.size()]);
    }

    public static Annotation[] getAnnotationsForMethod(MethodInfo methodInfo) {

        AnnotationsAttribute visible = (AnnotationsAttribute) methodInfo.getAttribute(VISIBLE_TAG);
        AnnotationsAttribute invisible = (AnnotationsAttribute) methodInfo.getAttribute(INVISIBLE_TAG);

        Set<Annotation> retVal = new HashSet<Annotation>();

        retVal.addAll(findAnnotationsForAnnotationsAttribute(visible));
        retVal.addAll(findAnnotationsForAnnotationsAttribute(invisible));

        return retVal.toArray(new Annotation[retVal.size()]);
    }

    public static Annotation[] getAnnotationsForMethodParameter(MethodInfo methodInfo, int index) {

        ParameterAnnotationsAttribute visible = (ParameterAnnotationsAttribute)
                methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag);
        ParameterAnnotationsAttribute invisible = (ParameterAnnotationsAttribute)
                methodInfo.getAttribute(ParameterAnnotationsAttribute.invisibleTag);

        Set<Annotation> retVal = new HashSet<Annotation>();

        retVal.addAll(findAnnotationsForAnnotationsArray(visible.getAnnotations()[index]));
        retVal.addAll(findAnnotationsForAnnotationsArray(invisible.getAnnotations()[index]));

        return retVal.toArray(new Annotation[retVal.size()]);
    }

    public static Annotation[] getAnnotationsForFieldInfo(FieldInfo fieldInfo) {

        AnnotationsAttribute visible = (AnnotationsAttribute) fieldInfo.getAttribute(VISIBLE_TAG);
        AnnotationsAttribute invisible = (AnnotationsAttribute) fieldInfo.getAttribute(INVISIBLE_TAG);

        Set<Annotation> retVal = new HashSet<Annotation>();

        retVal.addAll(findAnnotationsForAnnotationsAttribute(visible));
        retVal.addAll(findAnnotationsForAnnotationsAttribute(invisible));

        return retVal.toArray(new Annotation[retVal.size()]);
    }

    private static Set<Annotation> findAnnotationsForAnnotationsAttribute(AnnotationsAttribute attr) {

        if (attr != null) {
            javassist.bytecode.annotation.Annotation[] anns = attr.getAnnotations();
            return findAnnotationsForAnnotationsArray(anns);
        }
        return Collections.emptySet();
    }

    private static Set<Annotation> findAnnotationsForAnnotationsArray(
            javassist.bytecode.annotation.Annotation[] anns) {

        final Set<Annotation> retVal = new HashSet<Annotation>();
        for (javassist.bytecode.annotation.Annotation next : anns) {

            try {
                final Annotation toAdd = (Annotation) (next.toAnnotationType(
                        JavassistAnnotationsHelper.class.getClassLoader(), CLASS_POOL));
                retVal.add(toAdd);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Problem finding class for annotation: " + e.getMessage(), e);
            }
        }
        return retVal;
    }
}
