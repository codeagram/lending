package com.lending.backend.crud.processor;

import com.lending.backend.crud.annotations.CrudEntity;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.lending.backend.crud.annotations.CrudEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class CrudEntityProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
        messager = env.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(CrudEntity.class)) {
            if (element.getKind() != ElementKind.CLASS)
                continue;

            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString();
            String packageName = elementUtils.getPackageOf(typeElement).toString();
            com.lending.backend.crud.annotations.CrudEntity crud = typeElement.getAnnotation(CrudEntity.class);
            String path = crud.path().isEmpty() ? "/api/" + className.toLowerCase() : crud.path();

            messager.printMessage(Diagnostic.Kind.NOTE, "Generating controller for: " + className);

            generateController(packageName, className, path);
        }
        return true;
    }

    private void generateController(String packageName, String entityName, String path) {
        ClassName baseController = ClassName.get("com.example.crud.base", "CrudController");
        ClassName entityClass = ClassName.get(packageName, entityName);

        TypeSpec controller = TypeSpec.classBuilder(entityName + "Controller")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(baseController, entityClass))
                .addAnnotation(ClassName.get("org.springframework.web.bind.annotation", "RestController"))
                .addAnnotation(AnnotationSpec
                        .builder(ClassName.get("org.springframework.web.bind.annotation", "RequestMapping"))
                        .addMember("value", "$S", path).build())
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".generated", controller).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write: " + e.getMessage());
        }
    }
}
