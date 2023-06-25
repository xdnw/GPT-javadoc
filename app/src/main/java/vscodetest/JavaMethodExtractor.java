package vscodetest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;

public class JavaMethodExtractor {
    public static class MethodInfo {
        private final String name;
        private final String code;
        private final int line;
        private Javadoc javadoc;
        private boolean isOverride;
        private AccessSpecifier modifier;
        private List<String> methodCalls;

        public MethodInfo(String name, AccessSpecifier modifier, String code, int line, Javadoc javadoc, boolean isOverride, List<String> methodCalls) {
            this.name = name;
            this.code = code;
            this.line = line;
            this.javadoc = javadoc;
            this.isOverride = isOverride;
            this.modifier = modifier;
            this.methodCalls = methodCalls;
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

    public static List<MethodInfo> extractMethods(String mystring)  {
        List<MethodInfo> methodList = new ArrayList<>();

        CompilationUnit cu = StaticJavaParser.parse(mystring);
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
            methodList.add(new MethodInfo(methodName, accessModifier, methodBodyString, line, javadoc, isOverride, extractMethodCalls(method)));
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
}