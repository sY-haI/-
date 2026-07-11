package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.CustomException;
import com.example.entity.Account;
import com.example.entity.Goods;
import com.example.entity.Supplier;
import com.example.mapper.GoodsMapper;
import com.example.mapper.SupplierMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 供货商业务处理
 **/
@Service
public class SupplierService {

    @Resource
    private SupplierMapper supplierMapper;

    @Resource
    private GoodsMapper goodsMapper;

    /**
     * 新增（同时更新商品库存）
     */
    @Transactional
    public void add(Supplier supplier) {
        // 1. 保存供货商信息
        supplierMapper.insert(supplier);

        // 2. 更新商品库存：将进货数量累加到商品库存
        if (supplier.getGoodsId() != null && supplier.getPurchaseQuantity() != null) {
            Goods goods = goodsMapper.selectById(supplier.getGoodsId());
            if (goods != null) {
                int newStore = goods.getStore() + supplier.getPurchaseQuantity();
                goods.setStore(newStore);
                goodsMapper.updateById(goods);
            }
        }
    }

    /**
     * 删除（只删除记录，不影响库存）
     */
    @Transactional
    public void deleteById(Integer id) {
        // 直接删除供货商信息，不扣减库存
        supplierMapper.deleteById(id);
    }

    /**
     * 修改（根据数量变化调整库存）
     */
    @Transactional
    public void updateById(Supplier supplier) {
        // 1. 查询原来的供货商信息
        Supplier oldSupplier = supplierMapper.selectById(supplier.getId());

        // 2. 更新供货商信息
        supplierMapper.updateById(supplier);

        // 3. 如果商品或数量发生变化，调整库存
        if (oldSupplier != null) {
            Integer oldGoodsId = oldSupplier.getGoodsId();
            Integer newGoodsId = supplier.getGoodsId();
            Integer oldQuantity = oldSupplier.getPurchaseQuantity();
            Integer newQuantity = supplier.getPurchaseQuantity();

            // 情况1：商品改变了
            if (oldGoodsId != null && newGoodsId != null && !oldGoodsId.equals(newGoodsId)) {
                // 从原商品扣减库存
                if (oldQuantity != null) {
                    Goods oldGoods = goodsMapper.selectById(oldGoodsId);
                    if (oldGoods != null) {
                        int newStore = oldGoods.getStore() - oldQuantity;
                        if (newStore < 0) newStore = 0;
                        oldGoods.setStore(newStore);
                        goodsMapper.updateById(oldGoods);
                    }
                }
                // 向新商品增加库存
                if (newQuantity != null) {
                    Goods newGoods = goodsMapper.selectById(newGoodsId);
                    if (newGoods != null) {
                        int newStore = newGoods.getStore() + newQuantity;
                        newGoods.setStore(newStore);
                        goodsMapper.updateById(newGoods);
                    }
                }
            }
            // 情况2：商品相同，数量改变了
            else if (oldGoodsId != null && newGoodsId != null && oldGoodsId.equals(newGoodsId) &&
                    oldQuantity != null && newQuantity != null && !oldQuantity.equals(newQuantity)) {
                Goods goods = goodsMapper.selectById(oldGoodsId);
                if (goods != null) {
                    int quantityDiff = newQuantity - oldQuantity;
                    int newStore = goods.getStore() + quantityDiff;
                    if (newStore < 0) newStore = 0;
                    goods.setStore(newStore);
                    goodsMapper.updateById(goods);
                }
            }
        }
    }

    /**
     * 进货（累加进货数量到原有数量上，同时更新商品库存）
     */
    @Transactional
    public void purchase(Supplier supplier) {
        // 1. 查询原来的供货商信息
        Supplier oldSupplier = supplierMapper.selectById(supplier.getId());

        if (oldSupplier == null) {
            throw new CustomException("供货商信息不存在");
        }

        Integer oldQuantity = oldSupplier.getPurchaseQuantity();  // 原进货数量
        Integer addQuantity = supplier.getPurchaseQuantity();     // 本次新增的进货数量
        Integer goodsId = oldSupplier.getGoodsId();

        // 2. 计算新的总进货数量（累加）
        int newTotalQuantity = (oldQuantity != null ? oldQuantity : 0) +
                (addQuantity != null ? addQuantity : 0);

        // 3. 更新进货数量（累加后的总数）和进货时间
        Supplier updateSupplier = new Supplier();
        updateSupplier.setId(supplier.getId());
        updateSupplier.setPurchaseQuantity(newTotalQuantity);      // 设置为累加后的总数
        updateSupplier.setPurchaseTime(supplier.getPurchaseTime());
        supplierMapper.updateById(updateSupplier);

        // 4. 更新商品库存：只累加本次新增的进货数量
        if (goodsId != null && addQuantity != null && addQuantity > 0) {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods != null) {
                int newStore = goods.getStore() + addQuantity;  // 库存只增加本次进货数量
                goods.setStore(newStore);
                goodsMapper.updateById(goods);
            }
        }
    }

    /**
     * 根据ID查询
     */
    public Supplier selectById(Integer id) {
        return supplierMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Supplier> selectAll(Supplier supplier) {
        return supplierMapper.selectAll(supplier);
    }

    /**
     * 分页查询
     */
    public PageInfo<Supplier> selectPage(Supplier supplier, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Supplier> list = supplierMapper.selectAll(supplier);
        return PageInfo.of(list);
    }
}