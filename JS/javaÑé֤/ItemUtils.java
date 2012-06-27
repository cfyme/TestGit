package com.taobao.item.util;

import static com.taobao.item.util.PropertyAliasUtils.setPropertyAlias;
import static com.taobao.item.util.PropertyUtils.convertToFeature;
import static com.taobao.item.util.PropertyUtils.convertToIdPairString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;
import com.taobao.forest.domain.dataobject.std.read.StdCategoryDO;
import com.taobao.forest.domain.dataobject.std.read.StdCategoryPropertyDO;
import com.taobao.forest.domain.feature.FeatureDO;
import com.taobao.item.constant.CategoryFeature;
import com.taobao.item.constant.ItemFeature;
import com.taobao.item.constant.VerticalMarketConstants;
import com.taobao.item.domain.ItemDO;
import com.taobao.item.domain.ItemImageDO;
import com.taobao.item.domain.ItemPVPairDO;
import com.taobao.item.domain.ItemSkuDO;
import com.taobao.item.domain.spu.SpuDO;
import com.taobao.util.Money;
import com.taobao.util.TBStringUtil;

/**
 * 商品常用工具类
 *
 * @author tianhu E-mail:
 * @version 创建时间：2010-2-9 下午03:21:20
 */
public class ItemUtils {

    private static final Log logger = LogFactory.getLog(ItemUtils.class);

    /**
	 * @deprecated use com.taobao.item.dao.support.NewMasterManager#NEW_MASTER_COUNT instead
	 */
	private final static int newMasterSize = 16; // 对id进行分库的主库数量
	

	/**
	 * isOnline的去除starts时间版本
	 * 
	 * @param item
	 * @return
	 */
    public static boolean isOnlineWithoutStarts(ItemDO item) {
    	if (!(isUpShelf(item) && item.getQuantity() > 0)) {// 不是上架状态肯定就是false的
            return false;
        }
    	return true;
    }

    /**
     * 判断拍卖宝贝是否在售<br/>
     * 因为拍卖宝贝有涉及到ends时间，所以需要另外判断
     * @param item
     * @return
     */
    public static boolean isBidOnline(ItemDO item) {
    	if( false == item.isAuction() ) {
    		return false;//不是拍卖宝贝直接false
    	}
    	boolean result = item.isOnline();
    	return result && System.currentTimeMillis() <= item.getEnds().getTime();
    }


    /**
     * 0、1和-9状态都算上架
     * @return true 表示宝贝处于上架状态
     */
    public static boolean isUpShelf(ItemDO item) {
        return item.isNormalAndPass() || item.isCC();
    }

    /**
     * 判断商品是否归属某垂直市场
     *
     * @param item 商品DO
     * @param vMarket {@link VerticalMarketConstants}
     * @return
     */
    static public boolean isVertical(ItemDO item, long vMarket) {
        return (item.getVerticalMarket() & vMarket) > 0;
    }

    /**
     * 判定商品字符串Id是否有效
     *
     * @param itemIdStr 字符串Id
     * @return 非16进制字符串或为空,或者非32位，返回false
     */
    static public boolean isValideStrItemId(String itemIdStr) {
        if (StringUtil.isBlank(itemIdStr)) {
            return false;
        }
//        if (itemIdStr.length() != STRID_LENGTH) {
//            return false;
//        }
        for (char i : itemIdStr.toCharArray()) {
            if (('0' <= i && i <= '9') || ('a' <= i && i <= 'f')) {
                continue;
            }
            return false;
        }

        return true;
    }

    /**
     * 根据key/value判断宝贝是否有此feature
     * @param item
     * @param featureKey
     * @param featureValue
     * @return
     */
    public static boolean matchFeature(ItemDO item, String featureKey, String featureValue) {
        Map<String, String> features = item.getFeatures();
        if (features == null || features.size() == 0) {
            return false;
        }
        if (features.containsKey(featureKey)) {
            String values = features.get(featureKey);
            return values.equals(featureValue);
        }
        return false;
    }

    /**
     * 根据key判断宝贝是否有此feature
     * @param item
     * @param featureKey
     * @return
     */
    public static boolean matchFeature(ItemDO item, String featureKey) {
    	Map<String, String> features = item.getFeatures();
    	if (features == null || features.size() == 0) {
            return false;
        }
    	return features.containsKey(featureKey);
    }

    /**
     * 是否是拍下减库存
     * @param item
     * @return
     */
    public static boolean idSubStockAtBuy(ItemDO item){
       return matchFeature(item, ItemFeature.FEATURE_SUB_STOCK_AT_BUY, "1");
    }



    /**
     * 是否是允许从web访问的秒杀宝贝
     *
     * @param itemDO
     * @return 只允许从web参加的秒杀活动宝贝，则返回true
     */
    public static boolean isSecondKillFromPC(ItemDO itemDO) {
    	Map<String,String> features = itemDO.getFeatures();
    	if (features == null || features.size() == 0) {
            return false;
        }
        if (features.containsKey(ItemFeature.FEATURE_SECOND_KILL)) {
            String values = features.get(ItemFeature.FEATURE_SECOND_KILL).toLowerCase().trim();
            return values.equals(ItemFeature.FEATURE_SECOND_KILL_FROM_PC) || values.equals("1");
        }
        return false;
    }


    /**
     * 是否是允许从wap访问的秒杀宝贝
     * @param itemDO
     * @return 只允许从wap参加的秒杀活动宝贝，则返回true
     */
    public static boolean isSecondKillFromWap(ItemDO itemDO) {
    	Map<String,String> features = itemDO.getFeatures();
        if (features == null || features.size() == 0) {
            return false;
        }
        if (features.containsKey(ItemFeature.FEATURE_SECOND_KILL)) {
            return features.get(ItemFeature.FEATURE_SECOND_KILL).trim().equalsIgnoreCase(ItemFeature.FEATURE_SECOND_KILL_FROM_WAP);
        }
        return false;
    }

    /**
     * 是否是允许从web和wap访问的秒杀宝贝
     * ;secKill:web,wap;
     * ;secKill:wap,web;
     * ;secKill:1,wap;
     * ;secKill:wap,1;
     * @param itemDO
     * @return 同时允许从web和wap参加的秒杀活动宝贝，则返回true
     */
    public static boolean isSecondKillFromPCAndWap(ItemDO itemDO) {
    	Map<String,String> features = itemDO.getFeatures();
        if (features == null || features.size() == 0) {
            return false;
        }
        if (features.containsKey(ItemFeature.FEATURE_SECOND_KILL)) {
            String[] values = StringUtil.split(features.get(ItemFeature.FEATURE_SECOND_KILL).toLowerCase(),",");
            if(values.length != 2){
                return false;
            }
            return values[0].trim().equals(ItemFeature.FEATURE_SECOND_KILL_FROM_WAP) && values[1].trim().equals(ItemFeature.FEATURE_SECOND_KILL_FROM_PC)
                    || values[1].trim().equals(ItemFeature.FEATURE_SECOND_KILL_FROM_WAP) && values[0].trim().equals(ItemFeature.FEATURE_SECOND_KILL_FROM_PC)
                    || values[0].trim().equals(ItemFeature.FEATURE_SECOND_KILL_FROM_WAP) && values[1].trim().equals("1")
                    || values[1].trim().equals(ItemFeature.FEATURE_SECOND_KILL_FROM_WAP) && values[0].trim().equals("1");
        }
        return false;
    }
    /**
     * 判断宝贝是不是秒杀宝贝，如果是返回ture，不区分是否是大型秒杀，还是pc秒杀，还是wap秒杀
     * 这个方法暂时供ic内部使用
     * @param itemDO
     * @return
     */
    public static boolean isSecondKill(ItemDO itemDO) {
    	Map<String,String> features = itemDO.getFeatures();
        if (features == null || features.size() == 0) {
            return false;
        }
        if (features.containsKey(ItemFeature.FEATURE_SECOND_KILL)) {
            String values = features.get(ItemFeature.FEATURE_SECOND_KILL);
            if(StringUtil.isNotBlank(values)){
            	 return true;
            }
        }
        return false;
    }
    /**
     * 判断是否是大型秒杀宝贝
     * @param itemDO
     * @return
     */
    public static boolean isBigSecondKill(ItemDO itemDO) {
    	return matchFeature(itemDO, ItemFeature.FEATURE_SECOND_KILL, "big");
    }


    public static ItemSkuDO getSkuFromItemById(ItemDO itemInTair, long skuId) {
		if(skuId<=0 
				|| null == itemInTair
				|| CollectionUtils.isEmpty(itemInTair.getSkuList())){
			return null;
		}
		for (ItemSkuDO sku : itemInTair.getSkuList()) {
			if(skuId == sku.getSkuId()){
				return sku;
			}
		}
		return null;
	}

    /**
	 * 通过sku查询对应的属性图片，若不存在，则返回主图
	 * @author : tieyi.qlr@taobao.com
	 */
	public static String getSkuImageUrlBySkuDO(long skuId, ItemDO item){
		Map<Long,ItemImageDO> skuImageMap = getSkuImageByItem(item);
		return skuImageMap.containsKey(skuId)
				?skuImageMap.get(skuId).getImageUrl()
				:item.getPictUrl();
	}
	
	/**
	 * 获得商品中sku对应的属性图片的键值对
	 * @author : tieyi.qlr@taobao.com
	 */
	public static Map</*SKU_ID*/Long,/*属性图片DO*/ItemImageDO> getSkuImageByItem(ItemDO item){
		
		Map<Long,ItemImageDO> skuImageMap = new HashMap<Long,ItemImageDO>();
		
		// 检查是否能取到SKU的属性图片
		if( ! checkForGetSkuImageByItem(item) ) {
			return skuImageMap;
		}
		List<ItemImageDO> propImages = item.getPropertyImageList();
		List<ItemSkuDO> skus = item.getSkuList();

		for( ItemImageDO propImage : propImages ) {
			for( ItemSkuDO sku : skus ) {
				String imageProps = propImage.getProperties();
				String skuProps = sku.getProperties();
				long skuId = sku.getSkuId();
				if( skuImageMap.containsKey(skuId)
						|| StringUtil.isEmpty(imageProps)
						|| StringUtil.isEmpty(skuProps)) {
					continue;
				}
				for(String skuProp : skuProps.split(";")){
					if(skuProp.equals(imageProps)){
						skuImageMap.put(skuId, propImage);
						break;
					}//if
				}//for
			}//for
		}//for
		return skuImageMap;
	}
	
	/**
	 * 检查商品上是否具有销售属性图片
	 * @author : tieyi.qlr@taobao.com
	 * @param item
	 * @return
	 */
	private static boolean checkForGetSkuImageByItem(ItemDO item) {
		if( null == item 
				|| CollectionUtils.isEmpty(item.getSkuList())
				|| CollectionUtils.isEmpty(item.getPropertyImageList())){
			return false;
		}
		return true;
	}

    /**
    * 作必要的字段长度校验
    */
    public static void validateDbFieldLength(ItemDO item, boolean needCheckUserId) {
        String title = item.getTitle();
        if (StringUtil.isEmpty(title)) {
            throw new IllegalArgumentException("title can't be null");
        }
        if (StringUtils.byteLength(title) > 60) {
            // 商品标题不得超过30个汉字
            throw new IllegalArgumentException("title length must less than 60 byte.");
        }

        String pictUrl = item.getPictUrl();
        if (pictUrl != null && pictUrl.length() > 256) {
            throw new IllegalArgumentException("pictUrl length must less than 256 byte.");
        }

        Money reservePrice = item.getReservePrice();
        String auctionType = item.getAuctionType();
        Integer duration = item.getDuration();
        String city = item.getCity();
        String prov = item.getProv();
        Integer quantity = item.getQuantity();
        Integer stuffStatus = item.getStuffStatus();


        Long userId = item.getUserId();
        if( needCheckUserId
        		&& (userId == null || userId == 0) ) {
        	throw new IllegalArgumentException("userId can't be null");
        }

        Integer auctionStatus = item.getAuctionStatus();
//        Integer repostCount = item.getRepostCount();
        if (reservePrice == null || auctionType == null
                || duration == null || StringUtil.isEmpty(city)
                || StringUtil.isEmpty(prov) || quantity == null
                || stuffStatus == null
                || auctionStatus == null) {
            throw new IllegalArgumentException(
                    "category/reservePrice/auctionType/city/prov/quantity/stuffStatus/auctionStatus/repostCount can't be null");
        }

        String shopCategoriesIdList = item.getShopCategoriesIdList();
        if (auctionType.length() > 1 || city.length() > 30 || prov.length() > 20
                || (shopCategoriesIdList != null && shopCategoriesIdList.length() > 256)) {
            throw new IllegalArgumentException(
                    "auctionType/city/prov/shopCategoriesIdList length exceed db field length.");
        }

        List<ItemPVPairDO> propertyAliasList = item.getPropertyAliasList();
        if (propertyAliasList != null) {
            // 属性别名不能超过30个汉字
            for (ItemPVPairDO property : propertyAliasList) {
                if (StringUtil.isEmpty(property.getValueAliasText())) {
                    throw new IllegalArgumentException("alias of property is empty.");
                }
                if (StringUtils.byteLength(property.getValueAliasText()) > 60) {
                    throw new IllegalArgumentException("alias of property length must less than 60 byte.");
                }
            }
        }

        String outerId = item.getOuterId();
        if (StringUtils.byteLength(outerId) > 64) {
            throw new IllegalArgumentException("outerId length exceed db field length[64].");
        }
    }

    public static long getReservePrice(final ItemDO item, final Long skuId, final int inQuantity) {
        long skuMinPrice = 0;
        if (skuId == null || skuId == 0) { return 0; }

        // 按sku购买
        // ItemSkuDO soldSku = null; // 出价购买的sku
        List<ItemSkuDO> skuList = item.getSkuList();
        if (skuList != null) {
            for (ItemSkuDO sku : skuList) {
                /*
                 * 取消此段逻辑(by tianhu)；
                 * 在获取价格时居然对sku库存进行了更新… 要保证notify的通知准确sku的更新也应放在减库存位置
                 * if (sku.getSkuId() == skuId) {
                 * soldSku = sku; // buySku不为null说明按sku购买
                 * soldSku.setQuantity(soldSku.getQuantity() - buyQuantity);
                 * }
                 */
                if (sku.getSkuId() == skuId) {
                    if ((sku.getQuantity() - inQuantity) <= 0) {
                        continue;
                    }
                }
                if (sku.getQuantity() == 0) {
                    continue;
                }

                if (skuMinPrice > sku.getPrice() || skuMinPrice == 0) {
                    skuMinPrice = sku.getPrice();
                }
            }
        }

        return item.getReservePriceLong() < skuMinPrice ? skuMinPrice : 0;
    }

    public static Long getFeatureCC(final ItemDO item, String feature) {
        Long featureCc = null;
        if (StringUtil.isNotBlank(feature)) {
            /** 底层代码虽然不用feature_cc作为update where条件，但改变了feature的值后让其他并发预读后修改feature的保存操作不成功也是有feature_cc意义的 */
            featureCc = item.getFeatureCc();
            featureCc = featureCc == null ? 0L : featureCc + 1L;
        }
        return featureCc;
    }

    public static void setFeatureFor(final ItemDO item, final Map<Integer, List<ItemPVPairDO>> inputPropMap) {
        final String featureVal = convertToFeature(inputPropMap);
        if (null == item.getFeatures()) {
            final Map<String, String> fs = new HashMap<String, String>();
            item.setFeatures(fs);
        }
        item.addFeature(ItemFeature.FEATURE_INPUT_PROPERTY, featureVal);
    }

    /**
     * 用spu的类目id来替换商品的类目id
     */
    public static void setCategoryIdFor(final ItemDO item, SpuDO spu) {
        // 检验一下spu类目是否和商品一致
        // 本来是要报错的，不过考虑SPU的类目可能修改(类目拆分引起，修改前后的属性是一样的，否则还是会出错)，所以覆盖商品的。
        // 其实只有B商家才会把spu传过来
        if (null != spu && spu.getCategoryId() != TBStringUtil.getInt(item.getCategoryId())) {
            logger.warn("Fail@CatIsDifferent: spuCatId=" + spu.getCategoryId() + "; itemCatId=" + item.getCategoryId());

            item.setCategoryId((long) spu.getCategoryId());
        }
    }

    /**
     * 给item设置其他信息
     * @param item
     * @param inputPropMap
     * @param itemPropMap
     * @param catPropMap
     * @param propertyAlias
     * @param spu
     */
    public static void setFieldFor(final ItemDO item, final Map<Integer, List<ItemPVPairDO>> inputPropMap,
            final Map<Integer, List<Integer>> itemPropMap,
            Map<Integer, ? extends StdCategoryPropertyDO> catPropMap,
            final Map<String, Map<String, String>> propertyAlias, SpuDO spu) {
        // 设置feature
        setFeatureFor(item, inputPropMap);

        // 处理属性别名
        setPropertyAlias(item, propertyAlias, catPropMap);

        // 将属性设置回商品
        item.setProperty(convertToIdPairString(itemPropMap));
        // 设置所有输入属性
        item.setUserInputItemPVPProperties(inputPropMap);

        setSpuFor(item, spu);
    }

    private static void setSpuFor(final ItemDO item, SpuDO spu) {
        if (null != spu) {
            item.setSpu(spu);
            item.setSpuId(spu.getId());
        } else {
            item.setSpu(null);
            item.setSpuId(0L);
        }
    }

    /**
     * 判断宝贝的options上是否有某值<br/>
     * 这个方法比较通用，所以方到了common包中，方便其他业务调用方面进行复用
     * @param item
     * @param mask
     * @return
     */
    public static boolean isOptionsHave(ItemDO item, long mask) {
    	return (item.getOptions() & mask) == mask;
    }

    /**
     * TODO mysql分库需要重新实现
     *
     * @author <a href="mailto:zhenbei@taobao.com">震北</a>
     * @since 2010-10-14 下午02:31:20
     *
     * @param itemId
     * @return
     * @deprecated 仅仅使用于oracle分库，mysql分库分表请使用{@link com.taobao.item.util.ItemIdDbRouteUtil#getDbIndex(long)}
     */
    public static int getDbIndex(long itemId){
    	return (int)(itemId % newMasterSize);
    }

    /**
     * 根据商品的feature来判断商品是否为虚拟商品
     * @param item
     * @return
     */
	public static boolean isVirtualItemByFeature(ItemDO item) {
		return matchFeature(item, ItemFeature.FEATURE_V_TIMEOUT_PAY, "3")
				|| matchFeature(item, ItemFeature.FEATURE_V_TIMEOUT_PAY, "10");
	}
	
	/**
	 * 获取商品上的对应属性
	 * @param item
	 * @param propertyId
	 * @return
	 */
	public static ItemPVPairDO getItemProperty(ItemDO item,long propertyId){
		if(item!=null){
			for(ItemPVPairDO pv:item.getItemProperties()){
				if(propertyId==pv.getPropertyId()){
					return pv;
				}
			}
		}
		return null;
	}
	
	/**
     * 是否允许自定义销售属性
     * B卖家和C卖家的判断标记分开
     * @param catDO
     * @param isBSeller
     * @return
     */
    public static boolean isPermissionForUserDefine(StdCategoryDO catDO, boolean isBSeller) {
    	FeatureDO SaleFeature = null;
    	if(catDO == null){
    		return false;
    	}
    	if(isBSeller){
			SaleFeature = catDO.getFeature(CategoryFeature.USER_DEFINED_SALEPROP_FOR_B);
		}else{
			SaleFeature = catDO.getFeature(CategoryFeature.USER_DEFINED_SALEPROP);
		}
    	if(SaleFeature!=null){
			return CategoryFeature.defaultFeatureValue.equals(SaleFeature.getValue());
		}
		return false;
	}
}
