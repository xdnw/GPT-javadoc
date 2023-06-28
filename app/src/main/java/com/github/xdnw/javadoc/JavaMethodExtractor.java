package com.github.xdnw.javadoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.base.Function;

public class JavaMethodExtractor {
    public static class MethodInfo {
        private final String name;
        private final String code;
        private final int line;
        private Javadoc javadoc;
        private boolean isOverride;
        private AccessSpecifier modifier;
        private List<String> methodCalls;
        private MethodDeclaration declaration;

        public MethodInfo(MethodDeclaration declaration, String name, AccessSpecifier modifier, String code, int line, Javadoc javadoc, boolean isOverride, List<String> methodCalls) {
            this.declaration = declaration;
            this.name = name;
            this.code = code;
            this.line = line;
            this.javadoc = javadoc;
            this.isOverride = isOverride;
            this.modifier = modifier;
            this.methodCalls = methodCalls;
        }

        public MethodDeclaration getDeclaration() {
            return declaration;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public int getLine() {
            return line;
        }

        public Javadoc getJavadoc() {
            return javadoc;
        }

        public boolean isOverride() {
            return isOverride;
        }

        public AccessSpecifier getModifier() {
            return modifier;
        }

        public List<String> getMethodCalls() {
            return methodCalls;
        }
    }

    public static String getClassJavadoc(CompilationUnit cu) throws IOException {
        ClassOrInterfaceDeclaration classDeclaration = cu.getClassByName(cu.getTypes().get(0).getNameAsString()).orElse(null);
        if (classDeclaration != null) {
            Javadoc javadoc = classDeclaration.getJavadoc().orElse(null);
            return javadoc != null ? javadoc.toText() : null;
        }
        return null;
    }

    public static String extractPackageName(String content) {
        Pattern pattern = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    public static Map<String, Integer> extractMethodCalls(String javaCode) {
        Map<String, Integer> methodCalls = new HashMap<>();
        Pattern pattern = Pattern.compile("\\b([a-zA-Z0-9_]+)\\s*\\(");
        Matcher matcher = pattern.matcher(javaCode);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (methodCalls.containsKey(methodName)) {
                methodCalls.put(methodName, methodCalls.get(methodName) + 1);
            } else {
                methodCalls.put(methodName, 1);
            }
        }
        return methodCalls;
    }
    
    private static final Map<File, CompilationUnit> compilationCache = new ConcurrentHashMap<>();

    public static CompilationUnit getCompilationUnit(File file) throws IOException {
        Function<File, CompilationUnit> compilationFunc = f -> {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(f.toPath()));
                // print content up until `class`
                return StaticJavaParser.parse(content);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse file " + f.getAbsolutePath(), e);
            }
        };
        return compilationCache.computeIfAbsent(file, compilationFunc);
    }

    public static List<String> getImportsInPackage(CompilationUnit cu, String packageName) {
        return cu.getImports().stream()
                .map(ImportDeclaration::getNameAsString)
                .filter(importName -> importName.startsWith(packageName + "."))
                .collect(Collectors.toList());
    }

    public static List<MethodInfo> extractMethods(CompilationUnit cu)  {
        List<MethodInfo> methodList = new ArrayList<>();
        
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration method : methods) {
            String methodName = method.getNameAsString();
            Javadoc javadoc = method.getJavadoc().orElse(null);
            boolean isOverride = method.getAnnotationByName("Override").isPresent();
            AccessSpecifier accessModifier = method.getAccessSpecifier();
            BlockStmt methodBody = method.getBody().orElse(null);
            if (methodBody == null) {
                System.out.println("Invalid method body " + methodName);
            }
            String methodBodyString = methodBody == null ? null : methodBody.toString();
            int line = method.getBegin().get().line;
            methodList.add(new MethodInfo(method, methodName, accessModifier, methodBodyString, line, javadoc, isOverride, extractMethodCalls(method)));
        }
        return methodList;
    }

    public static List<String> extractMethodCalls(MethodDeclaration method) {
        List<String> methodCalls = new ArrayList<>();
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr n, Void arg) {
                super.visit(n, arg);
                methodCalls.add(n.getNameAsString());
            }
        }, null);
        return methodCalls;
    }

    private static void insertJavadocs(File file, Map<Integer, String> methodJavadocs) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder fileContent = new StringBuilder();
        String line;
        int lineNumber = 1;

        while ((line = reader.readLine()) != null) {
            if (methodJavadocs.containsKey(lineNumber)) {
                fileContent.append(methodJavadocs.get(lineNumber)).append("\n");
            }
            fileContent.append(line).append("\n");
            lineNumber++;
        }

        reader.close();

        FileWriter writer = new FileWriter(file);
        writer.write(fileContent.toString());
        writer.close();
    }
    


    public static List<File> iterateFilesRecursively(File rootDir, String extension) {
        List<File> fileList = new ArrayList<>();
        if (rootDir.isDirectory()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        fileList.addAll(iterateFilesRecursively(file, extension));
                    } else if (file.getName().endsWith(extension)) {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }
}