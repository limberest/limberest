package io.limberest.api.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.limberest.api.codegen.CodegenServices.Service;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
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
    public static final String IMPLICIT_PARAMS = "implicitParams";
    public static final String SQUASH_API_PATHS = "squashApiPaths";

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

    private CodegenServices services = new CodegenServices();

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
        cliOptions.add(CliOption.newString(VALIDATE_REQUEST, "Include validation calls in API implementation methods").defaultValue(Boolean.TRUE.toString()));
        additionalProperties.put(VALIDATE_REQUEST, true);
        cliOptions.add(CliOption.newBoolean(IMPLICIT_PARAMS, "Generate @ApiImplicitParam annotations in API code").defaultValue(Boolean.TRUE.toString()));
        additionalProperties.put(IMPLICIT_PARAMS, true);
        cliOptions.add(CliOption.newBoolean(SQUASH_API_PATHS, "Within a tag, squeeze all paths into a common API class"));
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
        if (additionalProperties.containsKey(IMPLICIT_PARAMS)) {
            this.setImplicitParams(convertPropertyToBoolean(IMPLICIT_PARAMS));
        }
        if (additionalProperties.containsKey(SQUASH_API_PATHS)) {
            this.setSquashApiPaths(convertPropertyToBoolean(SQUASH_API_PATHS));
        }
        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
        }
        if (useBeanValidation) {
            writePropertyBack(USE_BEANVALIDATION, useBeanValidation);
        }

        importMapping.put("Path", "javax.ws.rs.Path");
        importMapping.put("JSONObject", "org.json.JSONObject");
        importMapping.put("Jsonable", "io.limberest.json.Jsonable");
        importMapping.put("JsonRestService", "io.limberest.json.JsonRestService");
        importMapping.put("ServiceException", "io.limberest.service.ServiceException");
        importMapping.put("Request", "io.limberest.service.http.Request");
        importMapping.put("Response", "io.limberest.service.http.Response");
        if (validateRequest) {
            importMapping.put("SwaggerValidator", "io.limberest.api.validate.SwaggerValidator");
            importMapping.put("ValidationException", "io.limberest.validate.ValidationException");
            importMapping.put("Result", "io.limberest.validate.Result");
        }
        importMapping.put("JsonList", "io.limberest.json.JsonList");
        importMapping.put("ArrayList", "java.util.ArrayList");
        importMapping.put("Api", "io.swagger.annotations.Api");
        importMapping.put("ApiImplicitParams", "io.swagger.annotations.ApiImplicitParam");
        importMapping.put("ApiImplicitParam", "io.swagger.annotations.ApiImplicitParams");
        importMapping.put("ApiOperation", "io.swagger.annotations.ApiOperation");

        services.squash = squashApiPaths;
    }

    @Override
    public CodegenModel fromModel(String name, Model model, Map<String,Model> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
        codegenModel.imports.add("JSONObject");
        codegenModel.imports.add("Jsonable");
        return codegenModel;
    }

    /**
     * Name here is resource path.
     */
    @Override
    public String toApiName(String name) {
        return services.forPath(name).getName();
    }

    public String toApiFilename(String name) {
        String filename = toApiName(name);
        return filename;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> postProcessOperations(Map<String,Object> objs) {
        Map<String,Object> operations = (Map<String,Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation op : ops) {
                if (op.returnContainer != null && !op.returnContainer.isEmpty()) {
                    op.returnContainer = Character.toUpperCase(op.returnContainer.charAt(0))
                            + op.returnContainer.substring(1);
                }
            }
        }

        return operations;
    }

    @Override
    public void postProcessParameter(CodegenParameter parameter) {
        super.postProcessParameter(parameter);
        // set param type (TODO: using example field since paramType ("in") is not available)
        if (parameter.isBodyParam)
            parameter.example = "body";
        else if (parameter.isPathParam)
            parameter.example = "path";
        else if (parameter.isQueryParam)
            parameter.example = "query";
        else if (parameter.isHeaderParam)
            parameter.example = "header";
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String,Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);
        op.imports.add("Path");
        op.imports.add("JsonRestService");
        op.imports.add("ServiceException");
        op.imports.add("Request");
        op.imports.add("Response");
        op.imports.add("JSONObject");
        if (validateRequest) {
            op.imports.add("SwaggerValidator");
            op.imports.add("ValidationException");
            op.imports.add("Result");
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
            for (CodegenParameter param : op.allParams) {
                if (!param.isPrimitiveType) {
                    // qualify with model package
                    param.dataType = modelPackage + "." + param.dataType;
                }
            }
        }

        return op;
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co,
            Map<String,List<CodegenOperation>> operations) {

        String servicePath = resourcePath;
        if (squashApiPaths) {
            Service service = services.forTag(tag);
            if (service != null)
                servicePath = service.path;
            co.path = co.path.substring(servicePath.length());
        }

        String method = co.httpMethod.toLowerCase();
        boolean replaceExisting = services.add(servicePath, tag, method);

        List<CodegenOperation> opList = operations.get(servicePath);
        if (opList == null) {
            opList = new ArrayList<>();
            operations.put(servicePath, opList);
        }
        if (replaceExisting) {
            CodegenOperation toRemove = null;
            for (CodegenOperation op : opList) {
                if (op.httpMethod.equals(co.httpMethod)) {
                    toRemove = op;
                    break;
                }
            }
            if (toRemove != null)
                opList.remove(toRemove);
        }
        opList.add(co);

        co.operationId = method;

        co.baseName = resourcePath;
        if (co.baseName.startsWith("/"))
            co.baseName = co.baseName.substring(1);

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

    protected boolean validateRequest = true;
    public void setValidateRequest(boolean validateRequest) { this.validateRequest = validateRequest; }

    protected boolean implicitParams = true;
    public void setImplicitParams(boolean implicitParams) { this.implicitParams = implicitParams; }

    protected boolean squashApiPaths = false;
    public void setSquashApiPaths(boolean squashApiPaths) { this.squashApiPaths = squashApiPaths; }

}
