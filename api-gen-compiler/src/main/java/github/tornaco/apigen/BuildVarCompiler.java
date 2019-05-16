package github.tornaco.apigen;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
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

import github.tornaco.apigen.common.Collections;
import github.tornaco.apigen.common.Logger;
import github.tornaco.apigen.common.SettingsProvider;
import github.tornaco.apigen.common.StringSetRepo;

import static github.tornaco.apigen.SourceFiles.writeSourceFile;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */
@SupportedAnnotationTypes("github.tornaco.apigen.BuildVar")
public class BuildVarCompiler extends AbstractProcessor {

    private ErrorReporter mErrorReporter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mErrorReporter = new ErrorReporter(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedElements =
                roundEnvironment.getElementsAnnotatedWith(BuildVar.class);

        List<TypeElement> types = new ImmutableList.Builder<TypeElement>()
                .addAll(ElementFilter.typesIn(annotatedElements))
                .build();

        types.forEach(this::processType);

        return true;
    }

    private void processType(TypeElement type) {
        BuildVar annotation = type.getAnnotation(BuildVar.class);
        if (annotation == null) {
            mErrorReporter.abortWithError("@BuildVar annotation is null on Type %s", type);
            return;
        }
        if (type.getKind() != ElementKind.CLASS) {
            mErrorReporter.abortWithError("@BuildVar" + " only applies to class", type);
        }

        NestingKind nestingKind = type.getNestingKind();
        if (nestingKind != NestingKind.TOP_LEVEL) {
            mErrorReporter.abortWithError("@BuildVar" + " only applies to top level class", type);
        }

        checkModifiersIfNested(type);

        String fqClassName = generatedSubclassName(type, 0, "BuildVar");
        // class name
        String className = CompilerUtil.simpleNameOf(fqClassName);
        // Create source.
        String source = generateClass(type, className, type.getSimpleName().toString(), false);

        source = Reformatter.fixup(source);
        writeSourceFile(processingEnv, fqClassName, source, type);
    }

    private String generatedSubclassName(TypeElement type, int depth, String subFix) {
        return generatedClassName(type, null, Strings.repeat("$", depth) + subFix);
    }

    private String generatedClassName(TypeElement type, String prefix, String subFix) {
        String name = type.getSimpleName().toString();
        while (type.getEnclosingElement() instanceof TypeElement) {
            type = (TypeElement) type.getEnclosingElement();
            name = type.getSimpleName() + "_" + name;
        }
        String pkg = CompilerUtil.packageNameOf(type);
        String dot = Strings.isNullOrEmpty(pkg) ? "" : ".";
        String prefixChecked = Strings.isNullOrEmpty(prefix) ? "" : prefix;
        String subFixChecked = Strings.isNullOrEmpty(subFix) ? "" : subFix;
        return pkg + dot + prefixChecked + name + subFixChecked;
    }


    private String generateClass(TypeElement type, String className, String ifaceToImpl, boolean isFinal) {
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

        BuildVar annotation = type.getAnnotation(BuildVar.class);
        String varPath = annotation.config();

        Logger.debug("BuildVar path=" + varPath);

        if (!new File(varPath).exists()) {
            Logger.debug("BuildVar path not exist:" + varPath + ", current dir:" + new File("./").getAbsolutePath());
        } else {
            Logger.debug("BuildVar path exist:" + varPath + ", current dir:" + new File("./").getAbsolutePath());
        }

        StringSetRepo supported = new StringSetRepo(new File(annotation.supportedConfig()));
        Collections.consumeRemaining(supported.getAll(), s -> Logger.debug(s));

        Logger.debug("-----------------------");

        StringSetRepo config = new StringSetRepo(new File(varPath));
        Collections.consumeRemaining(config.getAll(), s -> Logger.debug(s));

        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC)
                .addField(Set.class, "BUILD_VARS", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addStaticBlock(CodeBlock.of("BUILD_VARS = new $T();\n", HashSet.class));

        Collections.consumeRemaining(supported.getAll(), s -> subClass
                .addField(String.class, s.toUpperCase(), Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC));
        Collections.consumeRemaining(supported.getAll(), s -> subClass
                .addStaticBlock(CodeBlock.of(s.toUpperCase() + " = $S;", s)));

        StringBuilder codeBlockToAddVar = new StringBuilder();
        Collections.consumeRemaining(config.getAll(), s -> codeBlockToAddVar.append(String.format("BUILD_VARS.add(%s);\n", s.toUpperCase())));
        subClass.addStaticBlock(CodeBlock.of(codeBlockToAddVar.toString()));

        // Add type params.
        if (isFinal) subClass.addModifiers(FINAL);

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
                mErrorReporter.abortWithError("@BuildVar class must not be private", type);
            }
            if (!type.getModifiers().contains(STATIC)) {
                mErrorReporter.abortWithError("Nested @BuildVar class must be static", type);
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

