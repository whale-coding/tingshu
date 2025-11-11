package com.atguigu.tingshu.account.service.impl;

import com.atguigu.tingshu.account.mapper.UserAccountDetailMapper;
import com.atguigu.tingshu.account.mapper.UserAccountMapper;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.account.UserAccount;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

	@Autowired
	private UserAccountMapper userAccountMapper;

    @Autowired
    private UserAccountDetailMapper userAccountDetailMapper;

    /**
     * 初始化用户账户
     * @param userId
     */
    @Override
    public void saveUserAccount(Long userId) {
        // 创建账户对象
        UserAccount userAccount=new UserAccount();
        userAccount.setUserId(userId);
        // 初始化账户，赠送部分金额
        userAccount.setTotalAmount(new BigDecimal(100));
        userAccount.setAvailableAmount(new BigDecimal(100));
        userAccount.setTotalIncomeAmount(new BigDecimal(100));

        userAccountMapper.insert(userAccount);

        // 更新账户记录
        this.saveUserAccountDetail(userId,"初始化账户:赠送100", SystemConstant.ACCOUNT_TRADE_TYPE_DEPOSIT,new BigDecimal(100),null);
    }

    /**
     * 更新账户记录
     * @param userId
     * @param title
     * @param tradeType
     * @param amount
     * @param order_no
     */
    @Override
    public void saveUserAccountDetail(Long userId, String title, String tradeType, BigDecimal amount, String order_no) {
        // 创建用户账户明细对象
        UserAccountDetail userAccountDetail=new UserAccountDetail();

        userAccountDetail.setUserId(userId);
        userAccountDetail.setTitle(title);
        userAccountDetail.setTradeType(tradeType);
        userAccountDetail.setAmount(amount);
        userAccountDetail.setOrderNo(order_no);

        userAccountDetailMapper.insert(userAccountDetail);
    }
}
