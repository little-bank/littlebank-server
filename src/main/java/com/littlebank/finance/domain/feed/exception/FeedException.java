package com.littlebank.finance.domain.feed.exception;

import com.littlebank.finance.global.error.exception.BusinessException;
import com.littlebank.finance.global.error.exception.ErrorCode;

public class FeedException extends BusinessException {
    public FeedException(ErrorCode errorCode) {super(errorCode);}
}
