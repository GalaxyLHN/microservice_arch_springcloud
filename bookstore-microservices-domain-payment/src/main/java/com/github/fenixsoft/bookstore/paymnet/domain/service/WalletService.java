/*
 * Copyright 2012-2020. the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. More information from:
 *
 *        https://github.com/fenixsoft
 */

package com.github.fenixsoft.bookstore.paymnet.domain.service;

import com.github.fenixsoft.bookstore.paymnet.domain.Wallet;
import com.github.fenixsoft.bookstore.paymnet.domain.repository.WalletRepository;
import com.github.fenixsoft.bookstore.domain.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 用户钱包的领域服务
 * <p>
 * 由于本工程中冻结、解冻款项的方法是为了在微服务中演示TCC事务所准备的，单体服务中由于与本地事务一同提交，无需用到
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/12 20:23
 **/
@Named
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    @Inject
    private WalletRepository repository;

    /**
     * 账户资金减少
     */
    public void decrease(Integer accountId, Double amount) {
        Wallet wallet = repository.findByAccountId(accountId)
                .orElseGet(() -> repository.save(new Wallet(accountId, 0D)));
        // 让 discount 直接参与资金变动
        Double total = amount;
        Double[] discounts = wallet.getDiscounts();
        if (discounts.length == 0) {
            // 没有优惠券
            if (wallet.getMoney() > total) {
                wallet.setMoney(wallet.getMoney() - total);
                repository.save(wallet);
                log.info("支付成功。用户余额：{}，本次消费：{}，未使用优惠券。", wallet.getMoney(), total);
            } else {
                throw new RuntimeException("用户余额不足以支付，请先充值。");
            }
        } else {
            // 选最高且低于总价的优惠券结算
            Integer dIndex = 0;
            for (Integer i = new Integer(0); i < discounts.length; i++) {
                if (discounts[i] <= amount && discounts[i] > discounts[dIndex]) {
                    dIndex = i;
                }
            }
            Double discount = discounts[dIndex];
            total = amount - discount;
            if (wallet.getMoney() > total) {
                wallet.setMoney(wallet.getMoney() - total);
                repository.save(wallet);

                // 计算新的优惠券集
                Double[] newDiscounts = new Double[discounts.length - 1];
                System.arraycopy(discounts, 0, newDiscounts, 0, dIndex);
                System.arraycopy(discounts, dIndex + 1, newDiscounts, dIndex, discounts.length - dIndex - 1);
                wallet.setDiscounts(newDiscounts);

                log.info("支付成功。用户余额：{}，本次消费：{}，使用优惠券减免了：{}。", wallet.getMoney(), total, discount);
            } else {
                throw new RuntimeException("用户余额不足以支付，请先充值。");
            }
        }

    }

    /**
     * 账户资金增加（演示程序，没有做充值入口，实际这个方法无用）
     */
    public void increase(Integer accountId, Double amount) {
    }

    // 以下两个方法是为TCC事务准备的，在单体架构中不需要实现

    /**
     * 账户资金冻结
     * 从正常资金中移动指定数量至冻结状态
     */
    public void frozen(Integer accountId, Double amount) {
    }

    /**
     * 账户资金解冻
     * 从冻结资金中移动指定数量至正常状态
     */
    public void thawed(Integer accountId, Double amount) {
    }

    /**
     * 账户优惠券添加
     * 获得新优惠券
     */
    // 没有前端的原因：前端大概不想让我修改
    public void addDiscount(Integer accountId, Double amount) {
        Wallet wallet = repository.findByAccountId(accountId)
                .orElseGet(() -> repository.save(new Wallet(accountId, 0D)));
        Double[] discounts = wallet.getDiscounts();
        Double[] newDiscounts = new Double[discounts.length - 1];
        System.arraycopy(discounts, 0, newDiscounts, 0, discounts.length);
        newDiscounts[discounts.length] = amount;
        wallet.setDiscounts(newDiscounts);
    }
}
