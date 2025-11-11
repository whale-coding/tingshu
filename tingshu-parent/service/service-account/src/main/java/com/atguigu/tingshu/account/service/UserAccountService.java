package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface UserAccountService extends IService<UserAccount> {
    /**
     * 初始化用户账户
     * @param userId
     */
    void saveUserAccount(Long userId);

    /**
     * 更新账户记录
     * @param userId
     * @param title
     * @param tradeType
     * @param amount
     * @param order_no
     */
    void saveUserAccountDetail(Long userId, String title, String tradeType, BigDecimal amount, String order_no);
}
