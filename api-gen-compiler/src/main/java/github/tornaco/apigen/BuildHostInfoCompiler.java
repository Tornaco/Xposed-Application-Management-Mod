package github.tornaco.apigen;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */
@SupportedAnnotationTypes("github.tornaco.apigen.BuildHostInfo")
public class BuildHostInfoCompiler extends AbstractProcessor {

    private ErrorReporter mErrorReporter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mErrorReporter = new ErrorReporter(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedElements =
                roundEnvironment.getElementsAnnotatedWith(BuildHostInfo.class);

        List<TypeElement> types = new ImmutableList.Builder<TypeElement>()
                .addAll(ElementFilter.typesIn(annotatedElements))
                .build();

        types.forEach(this::processType);

        return true;
    }

    private void processType(TypeElement type) {
        BuildHostInfo annotation = type.getAnnotation(BuildHostInfo.class);
        if (annotation == null) {
            mErrorReporter.abortWithError("@BuildHostInfo annotation is null on Type %s", type);
            return;
        }
        if (type.getKind() != ElementKind.CLASS) {
            mErrorReporter.abortWithError("@BuildHostInfo" + " only applies to class", type);
        }

        NestingKind nestingKind = type.getNestingKind();
        if (nestingKind != NestingKind.TOP_LEVEL) {
            mErrorReporter.abortWithError("@BuildHostInfo" + " only applies to top level class", type);
        }

        checkModifiersIfNested(type);

        String fqClassName = generatedSubclassName(type, 0, "BuildHostInfo");
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

        github.tornaco.apigen.service.github.bean.BuildHostInfo info = retrieveBuildInfo();

        Logger.debug("BuildHostInfo:" + info);

        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC)
                .addField(String.class, "BUILD_HOST_NAME", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addStaticBlock(CodeBlock.of("BUILD_HOST_NAME = $S;\n", info.getHostName()))
                .addField(String.class, "BUILD_DATE", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addStaticBlock(CodeBlock.of("BUILD_DATE = $S;\n", info.getDate()));

        // Add type params.
        if (isFinal) subClass.addModifiers(FINAL);

        JavaFile javaFile = JavaFile.builder(pkg, subClass.build())
                .addFileComment(SettingsProvider.FILE_COMMENT)
                .skipJavaLangImports(true)
                .build();
        return javaFile.toString();
    }

    private github.tornaco.apigen.service.github.bean.BuildHostInfo retrieveBuildInfo() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException ignored) {

        }
        String hostName = addr != null ? addr.getHostName() : "UNKNOWN";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return new github.tornaco.apigen.service.github.bean.BuildHostInfo(
                hostName, dateFormat.format(new Date(System.currentTimeMillis())));
    }

    private void checkModifiersIfNested(TypeElement type) {
        ElementKind enclosingKind = type.getEnclosingElement().getKind();
        if (enclosingKind.isClass() || enclosingKind.isInterface()) {
            if (type.getModifiers().contains(PRIVATE)) {
                mErrorReporter.abortWithError("@BuildHostInfo class must not be private", type);
            }
            if (!type.getModifiers().contains(STATIC)) {
                mErrorReporter.abortWithError("Nested @BuildHostInfo class must be static", type);
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

