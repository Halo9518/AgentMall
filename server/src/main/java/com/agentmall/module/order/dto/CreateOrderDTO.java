package com.agentmall.module.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 下单请求
 */
@Schema(description = "下单请求")
public class CreateOrderDTO {

    @Schema(description = "地址快照(JSON): {\"contactName\":\"\",\"phone\":\"\",\"province\":\"\",\"city\":\"\",\"district\":\"\",\"detail\":\"\"}")
    @NotBlank(message = "地址信息不能为空")
    private String addressSnap;

    @Schema(description = "备注")
    private String remark;

    public String getAddressSnap() { return addressSnap; }
    public void setAddressSnap(String addressSnap) { this.addressSnap = addressSnap; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
