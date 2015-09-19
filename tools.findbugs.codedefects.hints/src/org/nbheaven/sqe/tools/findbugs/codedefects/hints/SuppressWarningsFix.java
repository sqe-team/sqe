/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.tools.findbugs.codedefects.hints;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author fvo
 */
final class SuppressWarningsFix implements Fix {
    // SQE-8
    private final String bugType;
    private final ElementHandle<?> handle;
    private final FileObject file;

    SuppressWarningsFix(String bugType, ElementHandle<?> handle, FileObject file) {
        this.bugType = bugType;
        this.handle = handle;
        this.file = file;
    }

    @Override
    public String getText() {
        return "Suppress warning";
    }

    @Override
    public ChangeInfo implement() throws Exception {
        JavaSource.forFileObject(file).runModificationTask(new org.netbeans.api.java.source.Task<WorkingCopy>() {
            @Override
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement sw = null;
                for (ElementHandle<TypeElement> swh : wc.getClasspathInfo().getClassIndex().getDeclaredTypes("SuppressWarnings", ClassIndex.NameKind.SIMPLE_NAME, EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE))) {
                    TypeElement _sw = swh.resolve(wc);
                    if (_sw.getKind() != ElementKind.ANNOTATION_TYPE) {
                        continue;
                    }
                    Retention retention = _sw.getAnnotation(Retention.class);
                    if (retention != null && retention.value() == RetentionPolicy.SOURCE) {
                        continue;
                    }
                    // XXX look up @Target, make sure unspecified or matches element's kind
                    // XXX verify that it has a String[] value() attribute
                    sw = _sw;
                    break;
                }
                if (sw == null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("<html>Could not find a <code>@SuppressWarnings</code> " + "with <code>@Retention(CLASS/RUNTIME)</code> in project classpath.<br>" + "Try <code>findbugs:annotations:*</code> for Maven, Common Annotations API for NetBeans modules, etc.", NotifyDescriptor.INFORMATION_MESSAGE));
                    // XXX try to add such a lib if it can be found somewhere
                    return;
                }
                TreeMaker make = wc.getTreeMaker();
                Element element = handle.resolve(wc);
                if (element == null) {
                    System.err.println("could not find " + handle);
                    return;
                }
                Tree elementTree = wc.getTrees().getTree(element);
                ModifiersTree old;
                if (elementTree.getKind() == Tree.Kind.CLASS) {
                    old = ((ClassTree) elementTree).getModifiers();
                } else if (elementTree.getKind() == Tree.Kind.METHOD) {
                    old = ((MethodTree) elementTree).getModifiers();
                } else if (elementTree.getKind() == Tree.Kind.VARIABLE) {
                    old = ((VariableTree) elementTree).getModifiers();
                } else {
                    System.err.println("unknown tree kind " + elementTree.getKind());
                    return;
                }
                ModifiersTree nue = addSuppressWarnings(make, sw, old);
                nue = GeneratorUtilities.get(wc).importFQNs(nue);
                wc.rewrite(old, nue);
            }
        }).commit();
        return null; // XXX would be polite to implement
    }

    private ModifiersTree addSuppressWarnings(TreeMaker make, TypeElement sw, ModifiersTree original) {
        LiteralTree toAdd = make.Literal(bugType);
        // First try to insert into a value list for an existing annotation:
        List<? extends AnnotationTree> anns = original.getAnnotations();
        for (int i = 0; i < anns.size(); i++) {
            AnnotationTree ann = anns.get(i);
            Tree annotationType = ann.getAnnotationType();
            Tree.Kind kind = annotationType.getKind();
            Name name;
            switch (kind) {
                case IDENTIFIER:
                    name = ((IdentifierTree) annotationType).getName();
                    break;
                case MEMBER_SELECT:
                    name = ((MemberSelectTree) annotationType).getIdentifier();
                    break;
                default:
                    System.err.println("got strange annotation type (" + kind + "): " + annotationType);
                    continue;
            }
            // XXX what if this is the java.lang version? how to distinguish??
            if (name.contentEquals("SuppressWarnings")) {
                List<? extends ExpressionTree> args = ann.getArguments();
                // XXX need to rather skip over non-'value' assignments (e.g. 'justification')
                if (args.size() != 1) {
                    System.err.println("args list for @SW not of size 1: " + args);
                    return original;
                }
                AssignmentTree assign = (AssignmentTree) args.get(0);
                if (!assign.getVariable().toString().equals("value")) {
                    System.err.println("weird attribute for @SW: " + assign);
                    return original;
                }
                ExpressionTree arg = assign.getExpression();
                NewArrayTree arr;
                if (arg.getKind() == Tree.Kind.STRING_LITERAL) {
                    arr = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.singletonList(arg));
                } else if (arg.getKind() == Tree.Kind.NEW_ARRAY) {
                    arr = (NewArrayTree) arg;
                } else {
                    System.err.println("unknown arg kind " + arg.getKind() + ": " + arg);
                    return original;
                }
                for (ExpressionTree existing : arr.getInitializers()) {
                    if (((LiteralTree) existing).getValue().equals(bugType)) {
                        // Already suppressing this warning - perhaps have just not yet reanalyzed.
                        return original;
                    }
                }
                arr = make.addNewArrayInitializer(arr, toAdd);
                ann = make.Annotation(annotationType, Collections.singletonList(arr));
                return make.insertModifiersAnnotation(make.removeModifiersAnnotation(original, i), i, ann);
            }
        }
        // Not found, so create a new annotation:
        ExpressionTree annotationTypeTree = make.QualIdent(sw);
        List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
        arguments.add( /*make.Assignment(make.Identifier("value"), */ toAdd /*)*/ );
        AnnotationTree annTree = make.Annotation(annotationTypeTree, arguments);
        return make.addModifiersAnnotation(original, annTree);
    }
    
}
