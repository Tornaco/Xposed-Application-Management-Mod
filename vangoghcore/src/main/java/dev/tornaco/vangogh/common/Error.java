package dev.tornaco.vangogh.common;

import java.io.FileNotFoundException;
import java.io.IOException;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/8/24.
 * Email: Tornaco@163.com
 */
@Builder
@Getter
@ToString
public class Error {

    public static final int ERR_CODE_FILE_NOT_FOUND = 0x100;
    public static final int ERR_CODE_FILE_NOT_READABLE = 0x101;
    public static final int ERR_CODE_IO = 0x999;
    public static final int ERR_CODE_GENERIC = 0x102;

    public static Error fileNotFound(String path) {
        return Error.builder().errorCode(ERR_CODE_FILE_NOT_FOUND)
                .throwable(new FileNotFoundException(path))
                .build();
    }

    public static Error fileNotReadable(String path) {
        return Error.builder().errorCode(ERR_CODE_FILE_NOT_FOUND)
                .throwable(new IOException(path))
                .build();
    }

    public static Error io(Throwable throwable) {
        return Error.builder().throwable(throwable)
                .errorCode(ERR_CODE_IO)
                .build();
    }

    private int errorCode;
    private Throwable throwable;
}
