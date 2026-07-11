package com.example.entity;

public class PurchaseRecord {

    /** 进货记录ID（主键） */
    private Integer id;
    /** 供货商名称 */
    private String supplierName;
    /** 商品名称 */
    private String goodsName;
    /** 商品图片 */
    private String goodsImg;
    /** 进货时间 */
    private String purchaseTime;
    /** 生产日期 */
    private String productionDate;
    /** 保质期（天数） */
    private Integer shelfLife;
    /** 过期日期 */
    private String expirationDate;
    /** 进货数量 */
    private Integer purchaseQuantity;
    /** 关联商品ID */
    private Integer goodsId;
    /** 关联供货商ID */
    private Integer supplierId;
    private Integer handled;   // 0=未处理，1=已处理

    // ==================== getter/setter ====================


    public Integer getHandled() {
        return handled;
    }

    public void setHandled(Integer handled) {
        this.handled = handled;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    public Integer getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(Integer shelfLife) {
        this.shelfLife = shelfLife;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(Integer purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }
}
