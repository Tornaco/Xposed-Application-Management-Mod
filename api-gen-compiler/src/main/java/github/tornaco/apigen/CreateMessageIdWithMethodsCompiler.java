package github.tornaco.apigen;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import github.tornaco.apigen.common.Logger;
import github.tornaco.apigen.common.SettingsProvider;

import static github.tornaco.apigen.SourceFiles.writeSourceFile;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */
@SupportedAnnotationTypes("github.tornaco.apigen.CreateMessageIdWithMethods")
public class CreateMessageIdWithMethodsCompiler extends AbstractProcessor {

    private ErrorReporter mErrorReporter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mErrorReporter = new ErrorReporter(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedElements =
                roundEnvironment.getElementsAnnotatedWith(CreateMessageIdWithMethods.class);

        List<TypeElement> types = new ImmutableList.Builder<TypeElement>()
                .addAll(ElementFilter.typesIn(annotatedElements))
                .build();

        types.forEach(this::processType);

        return true;
    }

    private void processType(TypeElement type) {
        CreateMessageIdWithMethods annotation = type.getAnnotation(CreateMessageIdWithMethods.class);
        if (annotation == null) {
            mErrorReporter.abortWithError("@CreateMessageIdWithMethods annotation is null on Type %s", type);
            return;
        }
        NestingKind nestingKind = type.getNestingKind();
        if (nestingKind != NestingKind.TOP_LEVEL) {
            mErrorReporter.abortWithError("@CreateMessageIdWithMethods" + " only applies to top level class", type);
        }

        checkModifiersIfNested(type);

        String fqClassName = generatedSubclassName(type);
        // class name
        String className = CompilerUtil.simpleNameOf(fqClassName);
        // Create source.
        String source = generateClass(type, className, type.getSimpleName().toString());

        source = Reformatter.fixup(source);
        writeSourceFile(processingEnv, fqClassName, source, type);
    }

    private String generatedSubclassName(TypeElement type) {
        return generatedClassName(type, Strings.repeat("$", 0) + "Messages");
    }

    private String generatedClassName(TypeElement type, String subFix) {
        StringBuilder name = new StringBuilder(type.getSimpleName().toString());
        while (type.getEnclosingElement() instanceof TypeElement) {
            type = (TypeElement) type.getEnclosingElement();
            name.insert(0, type.getSimpleName() + "_");
        }
        String pkg = CompilerUtil.packageNameOf(type);
        String dot = Strings.isNullOrEmpty(pkg) ? "" : ".";
        String prefixChecked = Strings.isNullOrEmpty(null) ? "" : null;
        String subFixChecked = Strings.isNullOrEmpty(subFix) ? "" : subFix;
        return pkg + dot + prefixChecked + name + subFixChecked;
    }


    private String generateClass(TypeElement type, String className, String ifaceToImpl) {
        if (type == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null type", null);
            return null;
        }
        if (className == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null class name", type);
            return null;
        }
        if (ifaceToImpl == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null iface", type);
            return null;
        }

        String pkg = CompilerUtil.packageNameOf(type);

        int base = type.getAnnotation(CreateMessageIdWithMethods.class).base();

        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC);

        MethodSpec.Builder decodeMsthodSpecBuilder = MethodSpec.methodBuilder("decodeMessage")
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TypeName.INT, "wht");

        List<? extends Element> elements = type.getEnclosedElements();
        for (Element e : elements) {
            String fieldName = "MSG_" + e.getSimpleName().toString().toUpperCase();
            subClass.addField(FieldSpec.builder(TypeName.INT, fieldName,
                    Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                    .initializer("$L", base)
                    .build());
            decodeMsthodSpecBuilder.addStatement("if (wht == $L) return $S", base, fieldName);
            Logger.debug("Adding for method:" + fieldName);
            base++;
        }

        decodeMsthodSpecBuilder.addStatement("return $S", type.getAnnotation(CreateMessageIdWithMethods.class).fallbackMessageDecode());
        subClass.addMethod(decodeMsthodSpecBuilder.build());

        JavaFile javaFile = JavaFile.builder(pkg, subClass.build())
                .addFileComment(SettingsProvider.FILE_COMMENT)
                .skipJavaLangImports(true)
                .build();
        return javaFile.toString();
    }

    private void checkModifiersIfNested(TypeElement type) {
        ElementKind enclosingKind = type.getEnclosingElement().getKind();
        if (enclosingKind.isClass() || enclosingKind.isInterface()) {
            if (type.getModifiers().contains(PRIVATE)) {
                mErrorReporter.abortWithError("@CreateMessageIdWithMethods class must not be private", type);
            }
            if (!type.getModifiers().contains(STATIC)) {
                mErrorReporter.abortWithError("Nested @CreateMessageIdWithMethods class must be static", type);
            }
        }
        // In principle type.getEnclosingElement() could be an ExecutableElement (for a class
        // declared inside a method), but since RoundEnvironment.getElementsAnnotatedWith doesn't
        // return such classes we won't see them here.
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

