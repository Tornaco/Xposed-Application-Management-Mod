package github.tornaco.apigen;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.Method;
import java.util.ArrayList;
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

import github.tornaco.apigen.common.Collections;
import github.tornaco.apigen.common.Consumer;
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
@SupportedAnnotationTypes("github.tornaco.apigen.CreateBinderServiceManager")
public class CreateBinderServiceCompiler extends AbstractProcessor {

    private ErrorReporter mErrorReporter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mErrorReporter = new ErrorReporter(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedElements =
                roundEnvironment.getElementsAnnotatedWith(CreateBinderServiceManager.class);

        List<TypeElement> types = new ImmutableList.Builder<TypeElement>()
                .addAll(ElementFilter.typesIn(annotatedElements))
                .build();

        types.forEach(this::processType);

        return true;
    }

    private void processType(TypeElement type) {
        CreateBinderServiceManager annotation = type.getAnnotation(CreateBinderServiceManager.class);
        if (annotation == null) {
            mErrorReporter.abortWithError("@CreateBinderServiceManager annotation is null on Type %s", type);
            return;
        }
        if (type.getKind() != ElementKind.CLASS) {
            mErrorReporter.abortWithError("@CreateBinderServiceManager" + " only applies to class", type);
        }

        NestingKind nestingKind = type.getNestingKind();
        if (nestingKind != NestingKind.TOP_LEVEL) {
            mErrorReporter.abortWithError("@CreateBinderServiceManager" + " only applies to top level class", type);
        }

        checkModifiersIfNested(type);

        String fqClassName = generatedSubclassName(type, 0, annotation.managerSimpleName());
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
        return pkg + dot + prefixChecked + subFixChecked;
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

        // ServiceManager code.
        // github.tornaco.xposedmoduletest.IAppGuardService
        String serviceName = type.getAnnotation(CreateBinderServiceManager.class).serviceSimpleName();
        String servicePkg = type.getAnnotation(CreateBinderServiceManager.class).servicePkg();
        String serviceRegisterName = type.getAnnotation(CreateBinderServiceManager.class).serviceRegisterName();

        ClassName serviceClassName = ClassName.get(servicePkg, serviceName);

        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC)
                .addField(serviceClassName, "mService", Modifier.FINAL, Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                        .addStatement("android.os.IBinder binder = android.os.ServiceManager.getService($S)",
                                serviceRegisterName)
                        .addStatement("this.mService = " + serviceName + ".Stub.asInterface(binder)")
                        .build())
                .addMethod(MethodSpec.methodBuilder("isServiceAvailable")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.BOOLEAN)
                        .addStatement("return mService != null")
                        .build())
                .addMethod(MethodSpec.methodBuilder("ensureServiceAvailable")
                        .addModifiers(Modifier.PRIVATE)
                        .addStatement("if (!isServiceAvailable()) throw new IllegalStateException($S)",
                                "Service is not available")
                        .build());

        // Add delegate methods.
        List<? extends Element> typeMirrors = type.getEnclosedElements();
        for (Element t : typeMirrors) {

            String methodNameStr = String.valueOf(t.getSimpleName());
            Set<Modifier> modifierSet = t.getModifiers();
            String returnTypePkg = null;
            String returnTypeSimpleName = null;
            boolean isReturnVoid = false;

            List<ParameterSpec> parameterSpecs = new ArrayList<>();

            Logger.debug("t1:" + t.getSimpleName());
            Logger.debug("t2:" + t.asType());
            Object methodType = t.asType();
            for (Method m : methodType.getClass().getDeclaredMethods()) {
                Logger.debug("t3:" + m);
            }
            try {
                Method rtm = ReflectionUtils.findMethod(t.asType().getClass(), "getReturnType");
                ReflectionUtils.makeAccessible(rtm);
                Object rt = ReflectionUtils.invokeMethod(rtm, methodType);
                Logger.debug("rt:" + rt);
                isReturnVoid = String.valueOf(rt).equals("void");
                if (!isReturnVoid) {
                    returnTypePkg = String.valueOf(rt).substring(0, String.valueOf(rt).lastIndexOf("."));
                    returnTypeSimpleName = String.valueOf(rt.toString()).substring(returnTypePkg.length(),
                            String.valueOf(rt.toString()).length() - 1);
                }
                Method ptm = ReflectionUtils.findMethod(t.asType().getClass(), "getParameterTypes");
                ReflectionUtils.makeAccessible(ptm);
                List pt = (List) ReflectionUtils.invokeMethod(ptm, methodType);
                final int[] paramIndex = {1};
                Collections.consumeRemaining(pt, new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        Logger.debug(o.toString());
                    }
                });
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }

//            MethodSpec.methodBuilder(methodNameStr)
//                    .addModifiers(modifierSet)
//                    .returns(isReturnVoid ? TypeName.VOID : ClassName.get(returnTypePkg, returnTypeSimpleName))
//                    .addParameters()
        }

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
                mErrorReporter.abortWithError("@CreateBinderServiceManager class must not be private", type);
            }
            if (!type.getModifiers().contains(STATIC)) {
                mErrorReporter.abortWithError("Nested @CreateBinderServiceManager class must be static", type);
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

