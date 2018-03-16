package io.limberest.api.codegen;

import java.util.List;
import java.util.Map;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.languages.AbstractJavaCodegen;
import io.swagger.codegen.languages.features.BeanValidationFeatures;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

public class SwaggerCodegen extends AbstractJavaCodegen implements BeanValidationFeatures {

    public static final String NAME = "limberest";

    public static final String AUTOGEN_COMMENT = "autogenComment";
    public static final String SERVICE_PATHS = "servicePaths";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getHelp() {
        return "Generates Limberest server code and Jsonable model classes.";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    private boolean useBeanValidation;
    @Override
    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;

    }

    public SwaggerCodegen() {
        super();
        embeddedTemplateDir = "codegen";
        apiTestTemplateFiles.clear(); // TODO: add test template

        // standard opts to be overridden by user options
        outputFolder = "src/main/java";
        apiPackage = "io.limberest.server.service";
        modelPackage = "io.limberest.client.model";

        // limberest-specific
        cliOptions.add(CliOption.newBoolean(AUTOGEN_COMMENT, "Autogen notice with warning not to edit"));
        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));
        cliOptions.add(CliOption.newString(SERVICE_PATHS, "Override default service path annotations"));

        // relevant once we submit a PR to swagger-code to become a java library
        supportedLibraries.put(NAME, getHelp());
        setLibrary(NAME);
        CliOption library = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
        library.setDefault(NAME);
        library.setEnum(supportedLibraries);
        library.setDefault(NAME);
        cliOptions.add(library);
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(AUTOGEN_COMMENT)) {
            this.setAutogenComment(convertPropertyToBoolean(AUTOGEN_COMMENT));
        }
        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
        }
        if (useBeanValidation) {
            writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
        }

        importMapping.put("JSONObject", "org.json.JSONObject");
        importMapping.put("Jsonable", "io.limberest.json.Jsonable");
        importMapping.put("JsonRestService", "io.limberest.json.JsonRestService");
        importMapping.put("ServiceException", "io.limberest.service.ServiceException");

    }

    @Override
    public CodegenModel fromModel(String name, Model model, Map<String,Model> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
        codegenModel.imports.add("JSONObject");
        codegenModel.imports.add("Jsonable");
        return codegenModel;
    }

    public String toApiFilename(String name) {
        String filename = toApiName(name);
        System.out.println("\n\nTO API FILE: " + name + "->" + filename);
        return filename;
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String,Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);
        op.imports.add("JsonRestService");
        op.imports.add("ServiceException");
        System.out.println("PATH: " + path);
        return op;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co,
            Map<String, List<CodegenOperation>> operations) {
        super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        additionalProperties.put(SERVICE_PATHS, resourcePath);
    }

    protected boolean autogenComment = false;
    public void setAutogenComment(boolean autogenComment) { this.autogenComment = autogenComment; }
}
