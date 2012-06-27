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
 * ��Ʒ���ù�����
 *
 * @author tianhu E-mail:
 * @version ����ʱ�䣺2010-2-9 ����03:21:20
 */
public class ItemUtils {

    private static final Log logger = LogFactory.getLog(ItemUtils.class);

    /**
	 * @deprecated use com.taobao.item.dao.support.NewMasterManager#NEW_MASTER_COUNT instead
	 */
	private final static int newMasterSize = 16; // ��id���зֿ����������
	

	/**
	 * isOnline��ȥ��startsʱ��汾
	 * 
	 * @param item
	 * @return
	 */
    public static boolean isOnlineWithoutStarts(ItemDO item) {
    	if (!(isUpShelf(item) && item.getQuantity() > 0)) {// �����ϼ�״̬�϶�����false��
            return false;
        }
    	return true;
    }

    /**
     * �ж����������Ƿ�����<br/>
     * ��Ϊ�����������漰��endsʱ�䣬������Ҫ�����ж�
     * @param item
     * @return
     */
    public static boolean isBidOnline(ItemDO item) {
    	if( false == item.isAuction() ) {
    		return false;//������������ֱ��false
    	}
    	boolean result = item.isOnline();
    	return result && System.currentTimeMillis() <= item.getEnds().getTime();
    }


    /**
     * 0��1��-9״̬�����ϼ�
     * @return true ��ʾ���������ϼ�״̬
     */
    public static boolean isUpShelf(ItemDO item) {
        return item.isNormalAndPass() || item.isCC();
    }

    /**
     * �ж���Ʒ�Ƿ����ĳ��ֱ�г�
     *
     * @param item ��ƷDO
     * @param vMarket {@link VerticalMarketConstants}
     * @return
     */
    static public boolean isVertical(ItemDO item, long vMarket) {
        return (item.getVerticalMarket() & vMarket) > 0;
    }

    /**
     * �ж���Ʒ�ַ���Id�Ƿ���Ч
     *
     * @param itemIdStr �ַ���Id
     * @return ��16�����ַ�����Ϊ��,���߷�32λ������false
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
     * ����key/value�жϱ����Ƿ��д�feature
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
     * ����key�жϱ����Ƿ��д�feature
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
     * �Ƿ������¼����
     * @param item
     * @return
     */
    public static boolean idSubStockAtBuy(ItemDO item){
       return matchFeature(item, ItemFeature.FEATURE_SUB_STOCK_AT_BUY, "1");
    }



    /**
     * �Ƿ��������web���ʵ���ɱ����
     *
     * @param itemDO
     * @return ֻ�����web�μӵ���ɱ��������򷵻�true
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
     * �Ƿ��������wap���ʵ���ɱ����
     * @param itemDO
     * @return ֻ�����wap�μӵ���ɱ��������򷵻�true
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
     * �Ƿ��������web��wap���ʵ���ɱ����
     * ;secKill:web,wap;
     * ;secKill:wap,web;
     * ;secKill:1,wap;
     * ;secKill:wap,1;
     * @param itemDO
     * @return ͬʱ�����web��wap�μӵ���ɱ��������򷵻�true
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
     * �жϱ����ǲ�����ɱ����������Ƿ���ture���������Ƿ��Ǵ�����ɱ������pc��ɱ������wap��ɱ
     * ���������ʱ��ic�ڲ�ʹ��
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
     * �ж��Ƿ��Ǵ�����ɱ����
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
	 * ͨ��sku��ѯ��Ӧ������ͼƬ���������ڣ��򷵻���ͼ
	 * @author : tieyi.qlr@taobao.com
	 */
	public static String getSkuImageUrlBySkuDO(long skuId, ItemDO item){
		Map<Long,ItemImageDO> skuImageMap = getSkuImageByItem(item);
		return skuImageMap.containsKey(skuId)
				?skuImageMap.get(skuId).getImageUrl()
				:item.getPictUrl();
	}
	
	/**
	 * �����Ʒ��sku��Ӧ������ͼƬ�ļ�ֵ��
	 * @author : tieyi.qlr@taobao.com
	 */
	public static Map</*SKU_ID*/Long,/*����ͼƬDO*/ItemImageDO> getSkuImageByItem(ItemDO item){
		
		Map<Long,ItemImageDO> skuImageMap = new HashMap<Long,ItemImageDO>();
		
		// ����Ƿ���ȡ��SKU������ͼƬ
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
	 * �����Ʒ���Ƿ������������ͼƬ
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
    * ����Ҫ���ֶγ���У��
    */
    public static void validateDbFieldLength(ItemDO item, boolean needCheckUserId) {
        String title = item.getTitle();
        if (StringUtil.isEmpty(title)) {
            throw new IllegalArgumentException("title can't be null");
        }
        if (StringUtils.byteLength(title) > 60) {
            // ��Ʒ���ⲻ�ó���30������
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
            // ���Ա������ܳ���30������
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

        // ��sku����
        // ItemSkuDO soldSku = null; // ���۹����sku
        List<ItemSkuDO> skuList = item.getSkuList();
        if (skuList != null) {
            for (ItemSkuDO sku : skuList) {
                /*
                 * ȡ���˶��߼�(by tianhu)��
                 * �ڻ�ȡ�۸�ʱ��Ȼ��sku�������˸��¡� Ҫ��֤notify��֪ͨ׼ȷsku�ĸ���ҲӦ���ڼ����λ��
                 * if (sku.getSkuId() == skuId) {
                 * soldSku = sku; // buySku��Ϊnull˵����sku����
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
            /** �ײ������Ȼ����feature_cc��Ϊupdate where���������ı���feature��ֵ������������Ԥ�����޸�feature�ı���������ɹ�Ҳ����feature_cc����� */
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
     * ��spu����Ŀid���滻��Ʒ����Ŀid
     */
    public static void setCategoryIdFor(final ItemDO item, SpuDO spu) {
        // ����һ��spu��Ŀ�Ƿ����Ʒһ��
        // ������Ҫ����ģ���������SPU����Ŀ�����޸�(��Ŀ��������޸�ǰ���������һ���ģ������ǻ����)�����Ը�����Ʒ�ġ�
        // ��ʵֻ��B�̼ҲŻ��spu������
        if (null != spu && spu.getCategoryId() != TBStringUtil.getInt(item.getCategoryId())) {
            logger.warn("Fail@CatIsDifferent: spuCatId=" + spu.getCategoryId() + "; itemCatId=" + item.getCategoryId());

            item.setCategoryId((long) spu.getCategoryId());
        }
    }

    /**
     * ��item����������Ϣ
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
        // ����feature
        setFeatureFor(item, inputPropMap);

        // �������Ա���
        setPropertyAlias(item, propertyAlias, catPropMap);

        // ���������û���Ʒ
        item.setProperty(convertToIdPairString(itemPropMap));
        // ����������������
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
     * �жϱ�����options���Ƿ���ĳֵ<br/>
     * ��������Ƚ�ͨ�ã����Է�����common���У���������ҵ����÷�����и���
     * @param item
     * @param mask
     * @return
     */
    public static boolean isOptionsHave(ItemDO item, long mask) {
    	return (item.getOptions() & mask) == mask;
    }

    /**
     * TODO mysql�ֿ���Ҫ����ʵ��
     *
     * @author <a href="mailto:zhenbei@taobao.com">��</a>
     * @since 2010-10-14 ����02:31:20
     *
     * @param itemId
     * @return
     * @deprecated ����ʹ����oracle�ֿ⣬mysql�ֿ�ֱ���ʹ��{@link com.taobao.item.util.ItemIdDbRouteUtil#getDbIndex(long)}
     */
    public static int getDbIndex(long itemId){
    	return (int)(itemId % newMasterSize);
    }

    /**
     * ������Ʒ��feature���ж���Ʒ�Ƿ�Ϊ������Ʒ
     * @param item
     * @return
     */
	public static boolean isVirtualItemByFeature(ItemDO item) {
		return matchFeature(item, ItemFeature.FEATURE_V_TIMEOUT_PAY, "3")
				|| matchFeature(item, ItemFeature.FEATURE_V_TIMEOUT_PAY, "10");
	}
	
	/**
	 * ��ȡ��Ʒ�ϵĶ�Ӧ����
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
     * �Ƿ������Զ�����������
     * B���Һ�C���ҵ��жϱ�Ƿֿ�
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
