package com.agentmall.module.address.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 地址请求 DTO
 */
@Schema(description = "地址请求")
public class AddressDTO {

    @Schema(description = "联系人")
    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "省份")
    @NotBlank(message = "省份不能为空")
    private String province;

    @Schema(description = "城市")
    @NotBlank(message = "城市不能为空")
    private String city;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "详细地址")
    @NotBlank(message = "详细地址不能为空")
    private String detail;

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
