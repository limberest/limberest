package io.limberest.validate;

import static io.limberest.service.http.Status.OK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.limberest.service.http.Status;

/**
 * Validation result from one or more statuses.
 */
public class Result {
    
    public static final int MIN_ERROR_CODE = Status.BAD_REQUEST.getCode();
    
    private int maxErrors;
    public int getMaxErrors() { return maxErrors; }
    /**
     * Zero means unlimited.
     */
    public void setMaxErrors(int maxErrors) { this.maxErrors = maxErrors; }
    
    public boolean isMaxErrors() {
        return maxErrors > 0 && errorCount >= maxErrors;
    }
    
    private int errorCount;
    public int getErrorCount() { return errorCount; }
    
    public Result() {
        statusMessages.put(OK.getCode(), new ArrayList<>(Arrays.asList(new String[]{OK.getMessage()})));
    }
    
    public Result(Status status) throws MaxErrorsException {
        addStatusMessage(status);
        if (isError(status))
            errorCount++;
        if (isMaxErrors())
            throw new MaxErrorsException();
            
    }
    
    public Result(Status status, String message) throws MaxErrorsException {
        this(new Status(status, message));
    }
    
    Result(int code, String message) {
        statusMessages.put(code, new ArrayList<>(Arrays.asList(new String[]{message})));
    }
    
    public int getWorstCode() {
        int worstCode = 0;
        for (int code : statusMessages.keySet()) {
            if (code > worstCode)
                worstCode = code;
        }
        return worstCode;
    }
    
    public boolean isError() {
        return getWorstCode() >= MIN_ERROR_CODE;
    }
    
    public boolean isError(Status status) {
        return status.getCode() >= MIN_ERROR_CODE;
    }

    private Map<Integer,List<String>> statusMessages = new TreeMap<>();
    public Map<Integer,List<String>> getStatusMessages() { return statusMessages; }
    
    protected void addStatusMessage(Status status) throws MaxErrorsException {
        addStatusMessage(status.getCode(), status.getMessage());
    }

    protected void addStatusMessage(Integer code, String message) throws MaxErrorsException {
        List<String> msgs = statusMessages.get(code);
        if (msgs == null) {
            msgs = new ArrayList<>();
            statusMessages.put(code, msgs);
        }
        msgs.add(message);
        if (code >= MIN_ERROR_CODE)
            errorCount++;
        if (isMaxErrors())
            throw new MaxErrorsException();
    }

    /**
     * @param result to add
     * @return this
     * @throws MaxErrorsException if no further evaluation should occur
     */
    public Result also(Result result) throws MaxErrorsException {
        for (int code : result.getStatusMessages().keySet()) {
            // add messages one at a time in case maxErrors
            for (String message : result.getStatusMessages().get(code)) {
                addStatusMessage(code, message);
            }
        }
        return this;
    }
    
    /**
     * @param status to add
     * @return this
     * @throws MaxErrorsException if no further evaluation should occur
     */
    public Result also(Status status, String message) throws MaxErrorsException {
        addStatusMessage(status.getCode(), message);
        return this;
    }
    
    /**
     * @return Status assembled using default consolidator (worst code with newline-separated error messages).
     */
    public Status getStatus() {
        return getStatus(r -> {
            int worstCode = 0;
            String combinedMessage = "";
            for (int code : statusMessages.keySet()) {
                if (code > worstCode)
                    worstCode = code;
                List<String> messages = statusMessages.get(code);
                for (String message : messages) {
                    if (code >= MIN_ERROR_CODE) {
                        if (combinedMessage.length() > 0)
                            combinedMessage += "\n";
                        combinedMessage += message;
                    }
                }
            }
            return new Status(worstCode, combinedMessage.isEmpty() ? OK.getMessage() : combinedMessage);
        });
    }

    public Status getStatus(Consolidator consolidator) {
        return consolidator.getStatus(this);
    }
    
    public class MaxErrorsException extends ValidationException {
        public MaxErrorsException() {
            super(Result.this);
        }
    }
    
    /**
     * Consolidates results into a combined status.
     */
    @FunctionalInterface
    public interface Consolidator {
        public Status getStatus(Result result);
    }

}
