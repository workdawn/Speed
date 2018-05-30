package com.workdawn.speedlib;

/**
 * Created on 2018/5/30.
 * @author workdawn
 */
public class ErrorCode {

    //Local file operation error, such as: failed to delete old file, delete failed to record, etc.
    public static final int ERROR_LOCAL = 500;
    //Server resource access error, such as: server refused access, misspelled address, etc.
    public static final int ERROR_HTTP = 502;
    //Failure caused by some unknown reason
    public static final int ERROR_UNKNOWN = 504;

    private ErrorCode(){}

}
