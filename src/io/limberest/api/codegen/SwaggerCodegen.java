package io.limberest.api.codegen;

import java.util.ArrayList;
import java.util.HashMap;
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
    public static final String VALIDATE_REQUEST = "validateRequest";

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
        cliOptions.add(CliOption.newBoolean(AUTOGEN_COMMENT, "Insert autogen notice with warning not to edit"));
        cliOptions.add(CliOption.newString(VALIDATE_REQUEST, "Include validation calls in API implementation methods"));
        cliOptions.add(CliOption.newBoolean(USE_BEANVALIDATION, "Use BeanValidation API annotations"));

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
        if (additionalProperties.containsKey(VALIDATE_REQUEST)) {
            this.setValidateRequest(convertPropertyToBoolean(VALIDATE_REQUEST));
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
        importMapping.put("Request", "io.limberest.service.http.Request");
        importMapping.put("Response", "io.limberest.service.http.Response");
        if (validateRequest) {
            importMapping.put("SwaggerValidator", "io.limberest.api.validate.SwaggerValidator");
            importMapping.put("ValidationException", "io.limberest.demo.model.ValidationException");
        }
        importMapping.put("JsonList", "io.limberest.json.JsonList");
        importMapping.put("ArrayList", "json.util.ArrayList");
        importMapping.put("Api", "io.swagger.annotations.Api");
        importMapping.put("ApiImplicitParams", "io.swagger.annotations.ApiImplicitParam");
        importMapping.put("ApiImplicitParam", "io.swagger.annotations.ApiImplicitParams");
        importMapping.put("ApiOperation", "io.swagger.annotations.ApiOperation");
    }

    @Override
    public CodegenModel fromModel(String name, Model model, Map<String,Model> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
        codegenModel.imports.add("JSONObject");
        codegenModel.imports.add("Jsonable");
        return codegenModel;
    }

    @Override
    public String toApiName(String name) {
        return super.toApiName(pathToTag.get(name));
    }

    public String toApiFilename(String name) {
        String filename = toApiName(name);
        return filename;
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String,Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);
        op.imports.add("JsonRestService");
        op.imports.add("ServiceException");
        op.imports.add("Request");
        op.imports.add("Response");
        op.imports.add("JSONObject");
        if (validateRequest) {
            op.imports.add("SwaggerValidator");
            op.imports.add("ValidationException");
        }
        if (op.isListContainer) {
            op.imports.add("JsonList");
            op.imports.add("ArrayList");
        }
        op.imports.add("Api");
        op.imports.add("ApiOperation");
        if (op.hasParams) {
            op.imports.add("ApiImplicitParams");
            op.imports.add("ApiImplicitParam");
        }

        return op;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> postProcessOperations(Map<String,Object> objs) {
        objs = super.postProcessOperations(objs);
        Map<String,Object> operations = (Map<String,Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation operation : ops) {
                if (operation.returnContainer != null && !operation.returnContainer.isEmpty()) {
                    operation.returnContainer = Character.toUpperCase(operation.returnContainer.charAt(0))
                            + operation.returnContainer.substring(1);
                }
            }
        }

        return operations;
    }

    private Map<String,String> pathToTag = new HashMap<>();

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co,
            Map<String,List<CodegenOperation>> operations) {
        List<CodegenOperation> opList = operations.get(resourcePath);
        if (opList == null) {
            opList = new ArrayList<CodegenOperation>();
            operations.put(resourcePath, opList);
        }
        co.operationId = co.httpMethod.toLowerCase();
        opList.add(co);

        co.baseName = resourcePath;
        if (co.baseName.startsWith("/"))
            co.baseName = co.baseName.substring(1);

        pathToTag.put(resourcePath, tag);

        // restful flags -- need to be set here after rejigging baseName
        co.isRestfulShow = co.isRestfulShow();
        co.isRestfulIndex = co.isRestfulIndex();
        co.isRestfulCreate = co.isRestfulCreate();
        co.isRestfulUpdate = co.isRestfulUpdate();
        co.isRestfulDestroy = co.isRestfulDestroy();
        co.isRestful = co.isRestful();
    }

    protected boolean autogenComment = false;
    public void setAutogenComment(boolean autogenComment) { this.autogenComment = autogenComment; }

    protected boolean validateRequest = false;
    public void setValidateRequest(boolean validateRequest) { this.validateRequest = validateRequest; }

}
