package com.sales.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String userId;            // 用户ID
    private String productId;         // 商品ID
    private String productName;       // 商品名称
    private BigDecimal price;        // 商品单价
    private Integer quantity;         // 数量
    private Boolean selected;         // 是否选中
    private String image;            // 商品图片
    private LocalDateTime addTime;   // 添加时间
    private LocalDateTime updateTime; // 更新时间
    
    // 计算小计金额
    public BigDecimal getSubtotal() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(new BigDecimal(quantity));
    }
    
    // 增加数量
    public void increaseQuantity(Integer delta) {
        if (delta != null && delta > 0) {
            this.quantity = (this.quantity == null ? 0 : this.quantity) + delta;
            this.updateTime = LocalDateTime.now();
        }
    }
    
    // 减少数量
    public boolean decreaseQuantity(Integer delta) {
        if (delta != null && delta > 0 && this.quantity != null) {
            if (this.quantity > delta) {
                this.quantity -= delta;
                this.updateTime = LocalDateTime.now();
                return true;
            } else if (this.quantity.equals(delta)) {
                return false; // 数量为0，应该删除
            }
        }
        return false;
    }
    
    // 设置数量
    public void setQuantity(Integer quantity) {
        if (quantity != null && quantity > 0) {
            this.quantity = quantity;
            this.updateTime = LocalDateTime.now();
        }
    }
    
    // 切换选中状态
    public void toggleSelected() {
        this.selected = this.selected == null ? true : !this.selected;
        this.updateTime = LocalDateTime.now();
    }
}
