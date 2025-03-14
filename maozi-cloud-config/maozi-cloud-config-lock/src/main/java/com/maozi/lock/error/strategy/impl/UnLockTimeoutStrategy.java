package com.maozi.lock.error.strategy.impl;

import com.maozi.base.error.code.SystemErrorCode;
import com.maozi.common.result.error.exception.BusinessResultException;
import com.maozi.lock.error.strategy.UnLockTimeoutHandler;

public enum UnLockTimeoutStrategy implements UnLockTimeoutHandler {

    NO_OPERATION() {

        @Override
        public void handle() {}

    },

    FAIL_FAST() {

        @Override
        public void handle() {
            throw new BusinessResultException(SystemErrorCode.CURRENT_LIMITING_ERROR,429);
        }

    }

}