package com.github.fenixsoft.bookstore.dto;

public class Discount {

    // 这不是必填项，所以不填也不应该有提示
    // 表示抵扣的金额
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}