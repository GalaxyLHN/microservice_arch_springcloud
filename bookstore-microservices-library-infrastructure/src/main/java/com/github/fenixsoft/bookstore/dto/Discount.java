package com.github.fenixsoft.bookstore.dto;

public class Discount {

    // 这不是必填项，所以不填也不应该有提示
    // 表示抵扣的金额
    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}
