package github.tornaco.apigen;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

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

import github.tornaco.apigen.common.Logger;
import github.tornaco.apigen.common.SettingsProvider;
import github.tornaco.apigen.service.github.GitHubService;
import github.tornaco.apigen.service.github.bean.Contributor;
import github.tornaco.apigen.service.github.bean.GitLog;

import static github.tornaco.apigen.SourceFiles.writeSourceFile;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by guohao4 on 2017/9/6.
 * Email: Tornaco@163.com
 */
@SupportedAnnotationTypes("github.tornaco.apigen.GithubCommitSha")
public class GithubCommitShaCompiler extends AbstractProcessor {

    private ErrorReporter mErrorReporter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mErrorReporter = new ErrorReporter(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedElements =
                roundEnvironment.getElementsAnnotatedWith(GithubCommitSha.class);

        List<TypeElement> types = new ImmutableList.Builder<TypeElement>()
                .addAll(ElementFilter.typesIn(annotatedElements))
                .build();

        types.forEach(this::processType);

        return true;
    }

    private void processType(TypeElement type) {
        GithubCommitSha annotation = type.getAnnotation(GithubCommitSha.class);
        if (annotation == null) {
            mErrorReporter.abortWithError("@GithubCommitSha annotation is null on Type %s", type);
            return;
        }
        if (type.getKind() != ElementKind.CLASS) {
            mErrorReporter.abortWithError("@GithubCommitSha" + " only applies to class", type);
        }

        NestingKind nestingKind = type.getNestingKind();
        if (nestingKind != NestingKind.TOP_LEVEL) {
            mErrorReporter.abortWithError("@GithubCommitSha" + " only applies to top level class", type);
        }

        checkModifiersIfNested(type);

        // get the fully-qualified class name
        if (annotation.repo().length() == 0) {
            mErrorReporter.abortWithError("repo should not be empty", type);
        }
        if (annotation.user().length() == 0) {
            mErrorReporter.abortWithError("user should not be empty", type);
        }

        String fqClassName = generatedSubclassName(type, 0, "GithubCommitSha");
        // class name
        String className = CompilerUtil.simpleNameOf(fqClassName);
        // Create source.
        String source = generateClass(type, className, type.getSimpleName().toString(), false,
                annotation.user(), annotation.repo());

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


    private String generateClass(TypeElement type, String className, String ifaceToImpl, boolean isFinal, String user, String repo) {
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

        GitLog gitLog = retrieveLatestFirstGitLog(user, repo);

        String sha = gitLog == null ? GithubCommitSha.UNKNOWN : gitLog.getSha();
        String date = gitLog == null ? GithubCommitSha.UNKNOWN : gitLog.getCommit().getCommitter().getDate();

        Logger.debug("SHA:" + sha);
        Logger.debug("DATE:" + date);

        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC)
                .addField(String.class, "LATEST_SHA", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addStaticBlock(CodeBlock.of("LATEST_SHA = $S;\n", sha))
                .addField(String.class, "LATEST_SHA_DATE", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addStaticBlock(CodeBlock.of("LATEST_SHA_DATE = $S;\n", date))
                .addField(String.class, "CONTRIBUTORS", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addStaticBlock(CodeBlock.of("CONTRIBUTORS = $S;\n", retrieveAndFormatLatestContributors(user, repo)));

        // Add type params.
        if (isFinal) subClass.addModifiers(FINAL);

        JavaFile javaFile = JavaFile.builder(pkg, subClass.build())
                .addFileComment(SettingsProvider.FILE_COMMENT)
                .skipJavaLangImports(true)
                .build();
        return javaFile.toString();
    }

    private String retrieveAndFormatLatestContributors(String user, String repo) {
        List<Contributor> contributors = retrieveLatestContributors(user, repo);
        if (contributors == null || contributors.size() == 0) {
            return GithubCommitSha.UNKNOWN;
        }
        StringBuilder sb = new StringBuilder();
        for (Contributor c : contributors) {
            sb.append(c.getLogin()).append(" ");
        }
        return sb.toString();
    }

    private List<Contributor> retrieveLatestContributors(String user, String repo) {
        try {
            GitHubService.GitHub gitHubService = GitHubService.GitHub.Factory.create();
            //noinspection ConstantConditions
            return gitHubService.contributors(user, repo)
                    .execute().body();
        } catch (Throwable e) {
            Logger.debug("FAIL retrieve contributors:" + e.getLocalizedMessage());
            return new ArrayList<>(0);
        }
    }

    private GitLog retrieveLatestFirstGitLog(String user, String repo) {
        try {
            GitHubService.GitHub gitHubService = GitHubService.GitHub.Factory.create();
            //noinspection ConstantConditions
            return gitHubService.commits(user, repo)
                    .execute().body().get(0);
        } catch (Throwable e) {
            Logger.debug("FAIL retrieve GitLog:" + e.getLocalizedMessage());
            return null;
        }
    }

    private void checkModifiersIfNested(TypeElement type) {
        ElementKind enclosingKind = type.getEnclosingElement().getKind();
        if (enclosingKind.isClass() || enclosingKind.isInterface()) {
            if (type.getModifiers().contains(PRIVATE)) {
                mErrorReporter.abortWithError("@GithubCommitSha class must not be private", type);
            }
            if (!type.getModifiers().contains(STATIC)) {
                mErrorReporter.abortWithError("Nested @GithubCommitSha class must be static", type);
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

