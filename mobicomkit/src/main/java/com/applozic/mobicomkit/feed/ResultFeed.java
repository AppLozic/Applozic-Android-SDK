package com.applozic.mobicomkit.feed;

/**
 * @author shanki-091
 */

import java.io.Serializable;

/**
 * Class to be serialized to response
 */
public class ResultFeed implements Serializable {

    private int code;

    private String message;

    private String reason;

    private String detail;

    public ResultFeed() {
        this(RestAPICodes.SUCCESS);
    }

    public ResultFeed(RestAPICodes apiCode) {
        this.code = apiCode.code;
        this.message = apiCode.message;
        this.reason = apiCode.reason;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setRestAPICode(RestAPICodes apiCode) {
        this.code = apiCode.code;
        this.message = apiCode.message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public enum RestAPICodes {

        SUCCESS(200, "Success", "rest.API.message.success"), BAD_REQUEST(400, "Bad Request", "rest.API.message.bad.request"), FORBIDDEN(
                403, "Forbidden", "rest.API.message.forbidden"), FIELD_REQUIRED(405, "Field Required", "rest.API.message.required.field.not.found"), AUTHENTICATION_REQUIRED(401, "Authentication Required", "rest.API.message.authentication.required"),
        NOT_AUTHORIZED(401, "Not Authorized",
                "rest.API.message.not.authorized"), REQUEST_ALREADY_PROCESSED(
                406, "Request Already Processed", "rest.API.message.request.already.processed"), INCOREECT_REQUIRED_FIELD(
                407, "Incorrect Required Field", "rest.API.message.incorrect.required.field"), ACCOUNT_INACTIVE(403, "Account Inactive",
                "rest.API.message.account.inactive"), DAILY_LIMIT_OVER(429, "Account Over Rate Limit",
                "rest.API.message.account.over.daily.limit"), NOT_FOUND(404, "Not Found", "rest.API.message.not.found"), REQUEST_TIMEOUT(
                408, "Request Timeout", "rest.API.message.request.timeout"), INTERNAL_SERVER_ERROR(500,
                "Internal Server Error", "rest.API.message.internal.server.error"), SERVICE_UNAVAILABLE(503,
                "Service Unavailable", "rest.API.message.service.unavailable");

        public final int code;
        public final String message;
        public final String reason;

        RestAPICodes(int code, String message, String reason) {
            this.code = code;
            this.message = message;
            this.reason = reason;
        }
    }
}
