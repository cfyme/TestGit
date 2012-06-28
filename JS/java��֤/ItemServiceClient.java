package com.taobao.item.service.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.alibaba.common.lang.StringUtil;
import com.taobao.item.constant.AppInfoConstants;
import com.taobao.item.constant.ErrorConstants;
import com.taobao.item.constant.ItemConstants;
import com.taobao.item.constant.ItemFeature;
import com.taobao.item.domain.AppInfoDO;
import com.taobao.item.domain.AuctionStoreDO;
import com.taobao.item.domain.ItemAttachDO;
import com.taobao.item.domain.ItemDO;
import com.taobao.item.domain.ItemImageDO;
import com.taobao.item.domain.ItemSkuDO;
import com.taobao.item.domain.ItemUpdateDO;
import com.taobao.item.domain.ItemVideoDO;
import com.taobao.item.domain.PublishItemOptionDO;
import com.taobao.item.domain.SaveItemOptionDO;
import com.taobao.item.domain.SkuStoreDO;
import com.taobao.item.domain.UpdatedPostageDO;
import com.taobao.item.domain.query.AuctionStoreIdDO;
import com.taobao.item.domain.query.SkuStoreIdDO;
import com.taobao.item.domain.result.BaseResultDO;
import com.taobao.item.domain.result.BatchItemVideoResultDO;
import com.taobao.item.domain.result.BatchResultDO;
import com.taobao.item.domain.result.CreateItemResultDO;
import com.taobao.item.domain.result.ItemIdResultDO;
import com.taobao.item.domain.result.ProcessResultDO;
import com.taobao.item.domain.result.ProcessSkuStoreResultDO;
import com.taobao.item.domain.result.ResultDO;
import com.taobao.item.domain.result.SaveItemResultDO;
import com.taobao.item.domain.result.SavePictureFileResultDO;
import com.taobao.item.domain.result.ShelfResultDO;
import com.taobao.item.domain.spu.FeatureDO;
import com.taobao.item.domain.structure.ItemExtendsFieldDO;
import com.taobao.item.exception.IcException;
import com.taobao.item.file.FileManager;
import com.taobao.item.file.PictureFileManager;
import com.taobao.item.service.ItemService;
import com.taobao.item.step.checker.ItemChecker;
import com.taobao.item.util.CheckUtils;
import com.taobao.item.util.CollectionUtils;
import com.taobao.item.util.ItemUtils;
import com.taobao.item.util.StringUtils;
import com.taobao.util.CollectionUtil;
import com.taobao.util.TBStringUtil;
import com.taobao.util.UniqID;
import com.taobao.util.crypter.BlowfishEncrypter;

/**
 * IC操作宝贝接口，主要用于发布、编辑、修改库存等操作
 *	<ul>
 *		<li>上传附件（可选）：attachFileManager</li>
 *		<li>上传图片（可选）：pictureFileManager</li>
 *	</ul>
 * @author dukun
 *
 */
public class ItemServiceClient extends BaseServiceClient {

	private final Log log = LogFactory.getLog(ItemServiceClient.class);
	private static final long MAX_FILE_SIZE = 500 * 1024;//上传文件的最大长度:500K

	private ItemService itemService;
	private ItemService itemServiceL1;
	private ItemService itemServiceL2;

    private FileManager attachFileManager;
    private PictureFileManager pictureFileManager;

    /**
     * 选择ItemService
     * @param s
     * @return
     */
    private ItemService g(ItemService s) {
    	return null == s ? itemService : s;
	}

    /**
     * 按位取与的Options字段加上值
     * @see ItemService#addItemOptions(long, List)
     */
    public ProcessResultDO addItemOptions(long itemId, List<Long> options) throws IcException {
    	//参数检查：options不能为空
    	if( CollectionUtil.isEmpty(options) ) {
    		throw new IllegalArgumentException("options can't be empty");
    	}
    	ProcessResultDO resultDo = g(itemServiceL1).addItemOptions(itemId, options, AppInfoDO.UNKNOWN);
        return dealErrorsMessage(resultDo);// 处理错误信息
    }
    
    /**
     * 按位取与的Options字段加上值
     * @see ItemService#addItemOptions(long, List, AppInfoDO)
     */
    public ProcessResultDO addItemOptions(long itemId, List<Long> options, AppInfoDO app) throws IcException {
    	AppInfoDO.checkPermission(app);
    	//参数检查：options不能为空
    	if( CollectionUtil.isEmpty(options) ) {
    		throw new IllegalArgumentException("options can't be empty");
    	}
    	ProcessResultDO resultDo = g(itemServiceL1).addItemOptions(itemId, options, app);
    	return dealErrorsMessage(resultDo);// 处理错误信息
    }

	/**
	 * 按位取与的Options字段除去值
	 * @see ItemService#removeItemOptions(long, List)
	 */
    public ProcessResultDO removeItemOptions(long itemId, List<Long> options) throws IcException {
    	//参数检查：options不能为空
    	if ( CollectionUtil.isEmpty(options) ) {
			throw new IllegalArgumentException("options can't be empty !");
		}
    	ProcessResultDO resultDo = g(itemServiceL1).removeItemOptions(itemId, options, AppInfoDO.UNKNOWN);
        return dealErrorsMessage(resultDo);// 处理错误信息

    }
    
    /**
     * 按位取与的Options字段除去值
     * @see ItemService#removeItemOptions(long, List, AppInfoDO)
     */
    public ProcessResultDO removeItemOptions(long itemId, List<Long> options, AppInfoDO app) throws IcException {
    	AppInfoDO.checkPermission(app);
    	//参数检查：options不能为空
    	if ( CollectionUtil.isEmpty(options) ) {
    		throw new IllegalArgumentException("options can't be empty !");
    	}
    	ProcessResultDO resultDo = g(itemServiceL1).removeItemOptions(itemId, options, app);
    	return dealErrorsMessage(resultDo);// 处理错误信息
    	
    }

    /**
     * 给发布、编辑宝贝注入应用方信息
     * @param processOption
     * @param appInfo
     */
    private void injectAppInfo(PublishItemOptionDO processOption, AppInfoDO appInfo) {
    	AppInfoDO.checkPermission(appInfo);
    	if (processOption == null) {
            throw new IllegalArgumentException("processOption can't be null");
        }
    	processOption.setAppInfo(appInfo);
    	processOption.setClientAppName(appInfo.getAppName());
    }

    /**
     * @see ItemService#publishItem(ItemDO, PublishItemOptionDO)
     */
    public CreateItemResultDO publishItem(ItemDO item, PublishItemOptionDO publicItemOption, AppInfoDO appInfo) throws IcException {
    	injectAppInfo(publicItemOption, appInfo);
        CreateItemResultDO result = checkItem(item, publicItemOption, true);
        if ( null == result ) {
        	result = g(itemServiceL1).publishItem(item, publicItemOption);
        }
        return dealErrorsMessage(result);
    }

    /**
     * @see ItemService#publishItemPreview(ItemDO, PublishItemOptionDO)
     */
    public CreateItemResultDO publishItemPreview(ItemDO item, PublishItemOptionDO publicItemOption, AppInfoDO appInfo) throws IcException {
    	injectAppInfo(publicItemOption, appInfo);
        CreateItemResultDO result = checkItem(item, publicItemOption, true);
        if ( null == result ) {
        	result = g(itemServiceL1).publishItemPreview(item, publicItemOption);
        }
        return dealErrorsMessage(result);
    }

    /**
     * @see ItemService#publishItemWithOutUserPreview(ItemDO, PublishItemOptionDO)
     */
    public CreateItemResultDO publishItemWithOutUserPreview(ItemDO inputItem, PublishItemOptionDO publicItemOption, AppInfoDO appInfo) throws IcException {
    	injectAppInfo(publicItemOption, appInfo);
    	CreateItemResultDO result = checkItem(inputItem, publicItemOption, false);
        if ( null == result ) {
        	result = g(itemServiceL1).publishItemWithOutUserPreview(inputItem, publicItemOption);
        }
        return dealErrorsMessage(result);
    }

    /**
     * 检查视频列表,一个商品只能对应一个视频<br/>
     * @param videoList
     * @return
     */
	private String checkItemVideoList(List<ItemVideoDO> videoList) {
		if(videoList.size() > 1){
			//那为啥还要List?郁闷
			throw new IllegalArgumentException("The size of video is lt 1.");
		}//if

		for (ItemVideoDO video : videoList) {
			if (StringUtil.isBlank(video.getVideoUrl())) {
				throw new IllegalArgumentException("videoUrl can't be null.");
			}
			if (video.getIsvId() < 1) {
				throw new IllegalArgumentException("isv id is invalid.");
			}
			if (video.getVideoId() < 1) {
				throw new IllegalArgumentException("video id is invalid.");
			}
			if (video.getVideoUrl().getBytes().length > 255) {
				return ErrorConstants.IC_ITEM_VIDEO_URL_IS_TOO_LONG;
			}
		}//for

		return null;
	}

	/**
	 * 验证SKU的属性以及返回计算出的总库存
	 * @param skuList
	 * @return
	 */
	private int validateSkuPropertyAndReturnQuantity(List<ItemSkuDO> skuList) {
		int quantity = 0;
		for (ItemSkuDO sku : skuList) {
			if (StringUtil.isEmpty(sku.getProperties())) {
				throw new IllegalArgumentException("item.sku.property can't be null");
			}
			if (StringUtils.byteLength(sku.getOuterId()) > 64) {
				throw new IllegalArgumentException("item.sku.outerId length can't large than 64");
			}
			if (StringUtils.byteLength(sku.getDescription()) > 255) {
				throw new IllegalArgumentException("item.sku.description length can't large than 255");
			}
			sku.setStatus(ItemSkuDO.STATUS_NORMAL);// 前端传过来的sku就表示是正常的
			if (sku.getQuantity() < 0) {
				// sku.quantity为0，不删除sku，因为sku.outer_id是商家数据，可为商家系统提供销售中的库存信息。出价时，数量为0是不能出价的
				sku.setQuantity(0);
			}
			quantity += sku.getQuantity();
		}
		return quantity;
	}

	/**
	 * @see ItemService#publishNumberRangeItem(ItemDO, String, PublishItemOptionDO)
	 */
    public CreateItemResultDO publishNumberRangeItem(ItemDO item, String number, PublishItemOptionDO processOption, AppInfoDO appInfo)
			throws IcException {
    	injectAppInfo(processOption, appInfo);
        CreateItemResultDO result = checkNumberItem(item, number, processOption);
        if ( null == result ) {
        	result = g(itemServiceL1).publishNumberRangeItem(item, number, processOption);
        }
        return dealErrorsMessage(result);
    }

    /**
     * @see ItemService#publishNumberRangeItemPreview(ItemDO, String, PublishItemOptionDO)
     */
    public CreateItemResultDO publishNumberRangeItemPreview(ItemDO item, String number, PublishItemOptionDO processOption, AppInfoDO appInfo)
            throws IcException {
    	injectAppInfo(processOption, appInfo);
        CreateItemResultDO result = checkNumberItem(item, number, processOption);
        if ( null == result ) {
        	result = g(itemServiceL1).publishNumberRangeItemPreview(item, number, processOption);
        }
        return dealErrorsMessage(result);
    }

    /**
	 * @see ItemService#sellerSaveNumberItem(long, long, ItemUpdateDO, String, SaveItemOptionDO)
	 */
	public SaveItemResultDO sellerSaveNumberItem(long itemId, long sellerId, ItemUpdateDO inputItem, String number,
			SaveItemOptionDO processOption, AppInfoDO appInfo) throws IcException {
		injectAppInfo(processOption, appInfo);
        SaveItemResultDO result = checkSaveNumberItem(itemId, sellerId, inputItem, number, processOption);
        if ( null == result ) {
        	result = g(itemServiceL1).sellerSaveNumberItem(itemId, sellerId, inputItem, number, processOption);
        }
        return dealErrorsMessage(result);
    }

    /**
     * @see ItemService#sellerSaveItem(long, long, ItemUpdateDO, SaveItemOptionDO)
     */
	public SaveItemResultDO sellerSaveItem(long itemId, long sellerId, ItemUpdateDO inputItem,
			SaveItemOptionDO processOption, AppInfoDO appInfo) throws IcException {
		injectAppInfo(processOption, appInfo);
        SaveItemResultDO result = checkSaveItem(itemId, sellerId, inputItem, processOption);
        if ( result.isSuccess() ) {
        	result = g(itemServiceL1).sellerSaveItem(itemId, sellerId, inputItem, processOption);
        }
        return dealErrorsMessage(result);
    }

	/**
	 * @see ItemService#sellerSaveItemPreview(long, long, ItemUpdateDO, SaveItemOptionDO)
	 */
    public SaveItemResultDO sellerSaveItemPreview(long itemId, long sellerId, ItemUpdateDO updateItem,
            SaveItemOptionDO processOption, AppInfoDO appInfo) throws IcException {
    	injectAppInfo(processOption, appInfo);
        SaveItemResultDO result = checkSaveItem(itemId, sellerId, updateItem, processOption);
        if ( result.isSuccess() ) {
        	result = g(itemServiceL1).sellerSaveItemPreview(itemId, sellerId, updateItem, processOption);
        }
        return dealErrorsMessage(result);
    }

    /**
     * 处理上传的图片列表
     * <ul>
     * 	<li>保存到TFS</li>
     * 	<li>将生成的图片路径等信息保存到ItemUpdateDO对象中</li>
     * </ul>
     * @param inputItem
     * @param result
     * @throws IcException
     */
	private void processImageList(ItemUpdateDO inputItem,
			SaveItemResultDO result) throws IcException {
		// IMPORTANT! 已上传到tfs上的图片不用删除掉，后台出错了，返回前台让用户修正错误的地方，用户也不用重新上传图片了,
		// 并需要能预览上次上传的图片。一些垃圾图片，tfs会定时清理
		List<ItemImageDO> itemImageList = inputItem.getCommonItemImageList();

		if (itemImageList != null) {// 需要处理主图和图片
            ItemImageDO majorImage = saveInputImageList(itemImageList, false, result);
			if (result.isFailure()) {
				return;
			}
			if (majorImage != null) {// 设置主图和主图颜色
				inputItem.setPictUrl(majorImage.getImageUrl());
				inputItem.setMainColor(majorImage.getMainColor());
				itemImageList.remove(majorImage);
			} else {// size == 0 ，表示删除所有图片，清空主图
				inputItem.setPictUrl(StringUtil.EMPTY_STRING); // pictUrl为null，表示不变更主图url，pictUrl为""，表示清空主图
				inputItem.setMainColor(StringUtil.EMPTY_STRING);
			}
		}// 图片列表为null，则原来的数据不变，主图url和mainColor也不变

		itemImageList = inputItem.getPropertyImageList();
		if (null != itemImageList) {
			saveInputImageList(itemImageList, false, result);
		}
	}

	/**
	 * 检查卖家编辑宝贝的参数
	 * @param itemId
	 * @param sellerId
	 * @param inputItem
	 * @param processOption
	 * @return
	 */
	private SaveItemResultDO checkParamForSaveItem(long itemId, long sellerId,
			ItemUpdateDO inputItem, SaveItemOptionDO processOption) {
		if (inputItem == null || processOption == null) {
			throw new IllegalArgumentException("item/SaveItemOptionDO can't be null");
		}
		if (itemId <= 0 || sellerId <= 0) {
			throw new IllegalArgumentException("itemId/sellerId can't be null");
		}

		if (StringUtil.isBlank(processOption.getLang())) {
			throw new IllegalArgumentException("PublishItemOptionDO.lang can't be blank");
		}

		SaveItemResultDO result = new SaveItemResultDO();

		// 宝贝库存数量永远不允许为负数
		if (null != inputItem.getQuantity() && 0 > inputItem.getQuantity()) {
			result.addError(ErrorConstants.IC_QUANTITY_LESS_THAN_ZERO);
			return result;
		}
		
		//编辑时描述可以为null，但是不能为空串     add by qier
		if(null!=inputItem.getDescription()&&StringUtil.isBlank(inputItem.getDescription())){
			 result.addError(ErrorConstants.IC_ITEM_DESC_COULD_NOT_BE_EMPTY);
	         return result;
		}

        if (inputItem.getDescription() != null && inputItem.getDescription().length() > ItemChecker.MAX_ITEM_DESC) {
            result.addError(ErrorConstants.IC_ITEM_DESC_IS_TOO_LONG);
            return result;
        }

		// 一个商品对应一个视频记录
		if (inputItem.getVideoList() != null && inputItem.isModifiedVideoList()) {
			String errorCode = checkItemVideoList(inputItem.getVideoList());
			if (errorCode != null) {
				result.addError(errorCode);
                return result;
			}
		}

        //检验商品的附件信息是否完整
        if (inputItem.getAttachList() != null) {
            String errorCode = checkItemAttachList(inputItem.getAttachList());
            if (errorCode != null) {
                result.addError(errorCode);
                return result;
            }
        }

        //类目id
		if(inputItem.getCategoryId() !=null && inputItem.getCategoryId() == 0){
			result.addError(ErrorConstants.IC_CHECKSTEP_CATEGORY);
            return result;
		}

		//校验是否超出数据库字段长度限制
		inputItem.validateDbFieldLength();

		//多图
		List<ItemImageDO> itemImageList = inputItem.getCommonItemImageList();
		if (itemImageList != null) {
			String errorCode = checkCommonImageList(itemImageList);
			if (errorCode != null) {
				result.addError(errorCode);
                return dealErrorsMessage(result);
			}
		}// 子数据为null就不对子数据作变化

		//属性图片
		itemImageList = inputItem.getPropertyImageList();
		if (itemImageList != null) {
			String errorCode = checkInputImageList(itemImageList);

			if (errorCode != null) {
				result.addError(errorCode);
                return dealErrorsMessage(result);
			}

		}// 子数据为null就不对子数据作变化

		if (null != inputItem.getUpdateFeatureList() && inputItem.getUpdateFeatureList().containsKey(ItemFeature.FEATURE_CCURL)) {
			String ccurl = inputItem.getUpdateFeatureList().get(ItemFeature.FEATURE_CCURL);
			ccurl = (ccurl == null) ? StringUtil.EMPTY_STRING : ccurl;

			// 充值地址的长度不能超过150
			if (ccurl.length() > 150) {
				result.addError(ErrorConstants.AUTO_CCURL_TOO_LEN);
                return dealErrorsMessage(result);
			}
		}
        return dealErrorsMessage(result);
	}

	/**
	 * 保存图片数据，并获取第一个图(或设定为major)的主要颜色
	 *
	 * @param imageList 图片列表
	 * @param needMajorImageMainColor 是否需要检测主图颜色，商品图片需要检测，属性图片不需要检测
	 * @return 返回主图对象
	 * 以及接口参数中的result
	 * @throws IcException
	 */
	private ItemImageDO saveInputImageList(List<ItemImageDO> imageList, boolean needMajorImageMainColor,
			final BaseResultDO result) throws IcException {
		if (imageList != null && imageList.size() > 0) {// 肯定有主图

			ItemImageDO firstImage = null; // 存放第一个图的本地变量
			ItemImageDO majorImage = null; // 存放主图的本地变量
			for (ItemImageDO image : imageList) {// 这个循环决定哪个为主图：第一个设置为major的图，或全部不设major而将第一个图设置为major
				if (firstImage == null) {// 存放第一个图
					firstImage = image;
				}
				if (image.isMajor() && majorImage == null) {// 存放第一个主图
					majorImage = image;
				}
				if (majorImage != null) {// 前面已经找到第一个主图，则其它图的主图标记清除，并且这里也会将第一个主图的 主图标记清除(下面再设置回来)
					image.setMajor(false);
				}
			}
			if (majorImage != null) {// 找到第一个主图
			} else {// 找不到主图，则将第一个图设置为主图
				majorImage = firstImage;
			}
			majorImage.setMajor(true);

			for (ItemImageDO image : imageList) {
				if (image.getImageData() != null) {// 有图片数据
					SavePictureFileResultDO subResult;
					if (needMajorImageMainColor) {// 需要检测图片颜色
						subResult = pictureFileManager.saveFileToTfs(image.getImageData(), image.getInputFileName(),
								image.isMajor());
					} else {
						subResult = pictureFileManager.saveFileToTfs(image.getImageData(), image.getInputFileName(),
								false);
					}
					if (subResult.isFailure() || subResult.getPictUrl() == null) {//出现错误终止处理，返回错误
						result.addErrors(subResult.getErrors());
						return null;
					}
					image.setImageUrl(subResult.getPictUrl());
					String mainColor = subResult.getMainColor();
					if (image.isMajor()) {// 设定主图颜色
						image.setMainColor(mainColor);
					}
					// IMPORTANT! 以下代码非常重要，不将500K*5的文件数据序列化传至服务器端。
					image.setImageData(null);
					image.setInputFileName(null);
				}
			}
			return majorImage;
		} else {// size == 0 返回主图为null，表示清空主图
			return null;
		}
	}

    /**
     * 校验图片列表，图片大小超出限制将返回错误码。<br>
     * 图片DO中同时包括或同时未包括图片链接和图片数据，将抛出参数非法异常
     *
     * @param imageList
     * @return 错误码字符串。无校验错误则返回null
     */
    private String checkInputImageList(final List<ItemImageDO> imageList) {
        for (final ItemImageDO image : imageList) {
            if (image.getImageUrl() == null && image.getImageData() == null) {
                throw new IllegalArgumentException("imageUrl/imageData can't be null both");
            }
            if (image.getImageUrl() != null && image.getImageData() != null) {
                // 如果一个图片同时传imageUrl和imageData过来，也不知道要怎么处理，所以抛异常
                throw new IllegalArgumentException("imageUrl/imageData can't be 'not null' both");
            }
            if (image.getImageData() != null && image.getImageData().length > 512000) {
                return ErrorConstants.IC_ITEM_PIC_IS_TOO_LARGE;
            }
        }
        return null;
    }

    /**
     * 校验商品多图图片列表，图片大小超出限制或者图片列表数量超过限制均将返回错误码。<br>
     * @param imageList 商品多图列表
     * @return 错误码字符串。无校验错误则返回null
     */
    private String checkCommonImageList(final List<ItemImageDO> imageList) {
        if(imageList.size() > ItemConstants.ITEM_IMAGE_NUM_UP_LIMIT) {
            return ErrorConstants.IC_ITEM_PIC_NUM_OVERFLOW;
        }
        return checkInputImageList(imageList);
    }

    /**
     * @see ItemService#modifyItemCollectionCount(long, long)
     */
    public ProcessResultDO modifyItemCollectionCount(long itemId, long collectionCount) throws IcException {
        if( 0 >= itemId ) {
        	throw new IllegalArgumentException("itemId can't be le 0 !");
        }
        ProcessResultDO result = g(itemServiceL1).modifyItemCollectionCount(itemId, collectionCount);
        return dealErrorsMessage(result);
    }

    /**
     * @see ItemService#sellerDelItem(long, List, AppInfoDO)
     */
	public ShelfResultDO sellerDelItem(long sellerId, List<Long> itemIds, AppInfoDO appInfo) throws IcException {
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be le 0 !");
		}
		if( CollectionUtil.isEmpty(itemIds) ) {
			throw new IllegalArgumentException("itemIds can't be empty !");
		}
		AppInfoDO.checkPermission(appInfo);
		ShelfResultDO result = this.g(itemServiceL1).sellerDelItem(sellerId, itemIds, appInfo);
        return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#sellerModifyItemQuantity(long, int, long, AppInfoDO)
	 */
    public ProcessResultDO sellerModifyItemQuantity( long itemId, int quantity, long sellerId, AppInfoDO appInfo) throws IcException {
        checkArgsForModifyQuantity(itemId, sellerId, appInfo);
        if( 0 > quantity ) {
            throw new IllegalArgumentException("quantity can't be lt 0 !");
        }
        ProcessResultDO result = g(itemServiceL1).sellerModifyItemQuantity(itemId, quantity, sellerId, appInfo);
        return dealErrorsMessage(result);
    }

	/**
	 * 卖家增量加库存接口。该接口在 应用方都升级ic客户后就会废弃掉
	 * @deprecated
	 * @see ItemServiceClient#sellerIncreaseItemSkuQuantity(long, long, Map, String, AppInfoDO)
	 */
	public ProcessResultDO sellerIncreaseItemQuantity(long itemId,
			int increment, long sellerId, AppInfoDO appInfo) throws IcException {

		checkArgsForModifyQuantity(itemId, sellerId, appInfo);
		ProcessResultDO result = g(itemServiceL1).sellerIncreaseItemQuantity(
				itemId, increment, sellerId, appInfo);
		return dealErrorsMessage(result);
	}

	/**
	 * 卖家增量加库存接口。<br>
	 * 
	 * @see ItemService#sellerIncreaseItemQuantity(long, int, long, String,
	 *      AppInfoDO)
	 */
	public ProcessResultDO sellerIncreaseItemQuantity(long itemId,
			int increment, long sellerId, String uuid, AppInfoDO appInfo)
			throws IcException {
		checkUUID(uuid);
		checkArgsForModifyQuantity(itemId, sellerId, appInfo);
		ProcessResultDO result = g(itemServiceL1).sellerIncreaseItemQuantity(
				itemId, increment, sellerId, uuid, appInfo);
		return dealErrorsMessage(result);
	}

	/**
	 * 卖家增量修改指定商品sku库存。该接口在 应用方都升级ic客户后就会废弃掉
	 * 
	 * @deprecated
	 *  @see ItemServiceClient#sellerIncreaseItemSkuQuantity(long, long, Map, String, AppInfoDO)
	 */
	public ProcessResultDO sellerIncreaseItemSkuQuantity(long sellerId,
			long itemId, Map<Long, Integer> skuIncrementMap, AppInfoDO appInfo)
			throws IcException {

		checkModifyQuantity(sellerId, itemId, skuIncrementMap, appInfo);
		ProcessResultDO result = g(itemServiceL1)
				.sellerIncreaseItemSkuQuantity(sellerId, itemId,
						skuIncrementMap, appInfo);
		return dealErrorsMessage(result);
	}

	/**
	 * 卖家增量修改指定商品sku库存。
	 * 
	 * @see ItemService#sellerIncreaseItemQuantity(long, int, long, String,
	 *      AppInfoDO)
	 */
	public ProcessResultDO sellerIncreaseItemSkuQuantity(long sellerId,
			long itemId, Map<Long, Integer> skuIncrementMap, String uuid,
			AppInfoDO appInfo) throws IcException {
		checkUUID(uuid);
		checkModifyQuantity(sellerId, itemId, skuIncrementMap, appInfo);
		ProcessResultDO result = g(itemServiceL1)
				.sellerIncreaseItemSkuQuantity(sellerId, itemId,
						skuIncrementMap, uuid, appInfo);
		return dealErrorsMessage(result);
	}
	
	/**
	 * uuid参数合法性校验
	 * 
	 * @param uuid
	 *            全局唯一标识符（Universally Unique Identifier），共32个十六进制字符
	 */
	private void checkUUID(String uuid) {
		if (StringUtil.isBlank(uuid)) {
			throw new IllegalArgumentException("uuid can't be null or Empty !");
		}
		if( !uuid.matches("[0-9a-fA-F]{32}") ) {
			throw new IllegalArgumentException("uuid was illegal!");
		}
	}
	
    /**
     * 检查卖家修改宝贝库存参数
     * 	1.检查宝贝ID
     * 	2.检查卖家ID
     * 	3.检查调用方信息
     * @param itemId
     * @param sellerId
     * @param appInfo
     */
    private void checkArgsForModifyQuantity(long itemId, long sellerId, AppInfoDO appInfo) {
        if( 0 >= itemId ) {
            throw new IllegalArgumentException("itemId can't be le 0 !");
        }
        if( 0 >= sellerId ) {
            throw new IllegalArgumentException("sellerId can't be le 0 !");
        }
        AppInfoDO.checkPermission(appInfo);
    }

	/**
	 * 检查SKU库存数量是否正确
	 * @param skuQuantitys
	 */
	private void checkSkuQuantity(Collection<Integer> skuQuantitys) {
		long totalQuantity = 0;
		for( Integer skuQuantity : skuQuantitys ) {
            if( null == skuQuantity || skuQuantity < 0 || skuQuantity == Integer.MAX_VALUE ) {
                throw new IllegalArgumentException(String.format("sku's quantity illegal, quantity=%s",skuQuantity));
            }
            totalQuantity += skuQuantity;
        }//for
	}

    /**
	 * 卖家针对单个宝贝的sku数量进行设置
	 * @see ItemService#sellerModifyItemSkuQuantity(long, long, Map, AppInfoDO)
	 */
	public ProcessResultDO sellerModifyItemSkuQuantity(long sellerId, long itemId, Map<Long,Integer> skuQuantityMap, AppInfoDO appInfo) throws IcException {
		checkModifyQuantity(sellerId, itemId, skuQuantityMap, appInfo);
		checkSkuQuantity(skuQuantityMap.values());
        ProcessResultDO result = g(itemServiceL1).sellerModifyItemSkuQuantity(sellerId, itemId, skuQuantityMap, appInfo);
        return dealErrorsMessage(result);
	}

    /**
     * 检查修改库存参数
     * @param sellerId
     * @param itemId
     * @param skuQuantityMap
     */
    private void checkModifyQuantity(long sellerId, long itemId, Map<Long,Integer> skuQuantityMap, AppInfoDO appInfo) {
    	AppInfoDO.checkPermission(appInfo);
        if (itemId <= 0
                || null == skuQuantityMap
                || skuQuantityMap.isEmpty()
                || sellerId <= 0
                || null == appInfo) {
            throw new IllegalArgumentException("参数不正确");
        }

        //检查skuIds
        for( Long skuId : skuQuantityMap.keySet() ) {
            if( null == skuId || skuId <= 0 ) {
                throw new IllegalArgumentException("skuQuantityMap参数不正确");
            }
        }
    }

	/**
	 * @see ItemService#sellerSaveItemCharity(long, String, long)
	 */
	public void sellerSaveItemCharity(long itemId, String zoo, long sellerId) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be le 0 !");
		}
		if( null == zoo ) {
			throw new IllegalArgumentException("zoo can't be null !");
		}
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be le 0 !");
		}
		g(itemServiceL1).sellerSaveItemCharity(itemId, zoo, sellerId);
	}

	/**
	 * @see ItemService#sellerSaveItemPostage(long, List, long, long, long, long)
	 */
	public ShelfResultDO sellerSaveItemPostage(long sellerId, List<Long> itemIds, long postageId, long minAllFee, long minFastFee, long minEmsFee) throws IcException {
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be lt 0 !");
		}
		if( null == itemIds || itemIds.isEmpty()) {
			throw new IllegalArgumentException("itemIds can't be null !");
		}
		return g(itemServiceL1).sellerSaveItemPostage(sellerId, itemIds, postageId, minAllFee, minFastFee, minEmsFee);
	}

	/**
	 * @see ItemService#sellerSaveItemRecommed(long, List)
	 */
	public ShelfResultDO sellerSaveItemRecommed(long sellerId, List<Long> itemIds) throws IcException {
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be le 0 !");
		}
		if( CollectionUtil.isEmpty(itemIds) ) {
			throw new IllegalArgumentException("itemIds can't be empty !");
		}
		ShelfResultDO result = g(itemServiceL1).sellerSaveItemRecommed(sellerId, itemIds);
        return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#sellerSaveItemUnRecommed(long, List)
	 */
	public ShelfResultDO sellerSaveItemUnRecommed(long sellerId, List<Long> itemIds) throws IcException {
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be le 0 !");
		}
		if( CollectionUtil.isEmpty(itemIds) ) {
			throw new IllegalArgumentException("itemIds can't be empty !");
		}
		ShelfResultDO result = this.g(itemServiceL1).sellerSaveItemUnRecommed(sellerId, itemIds);
        return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#sellerUpShelfItem(long, List, List, AppInfoDO)
	 */
	public ProcessResultDO sellerUpShelfItem(long sellerId, List<Long> itemIds, List<Integer> quantities, AppInfoDO appInfo) throws IcException {
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be le 0 !");
		}

		if( CollectionUtil.isEmpty(itemIds) ) {
			throw new IllegalArgumentException("itemIds can't be empty !");
		}

		AppInfoDO.checkPermission(appInfo);

		// 淘宝助理不传quantity, 不会改变quantity数据，所以当quantities为null的时候不对这个参数进行检查
		if( null != quantities ) {

			if( CollectionUtil.isEmpty(quantities) ) {
				throw new IllegalArgumentException("quantities can't be empty !");
			}

			if( itemIds.size() != quantities.size() ) {
				throw new IllegalArgumentException("itemIds.size() != quantities.size() !");
			}
			
			if(CollectionUtils.hasNullElement(quantities)){
				throw new IllegalArgumentException("quantity can't be null !");
			}

		}

		//对上架的宝贝数量做限制
		if(itemIds.size()>20){
			throw new IllegalArgumentException("itemIds.size()>20,please input less than 20 item id");
		}

		ProcessResultDO result = this.g(itemServiceL1).sellerUpShelfItem( sellerId, itemIds, quantities, appInfo);
		return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#sellerDownShelfItem(long, List, AppInfoDO)
	 */
	public ShelfResultDO sellerDownShelfItem(long sellerId, List<Long> itemIds, AppInfoDO appInfo) throws IcException {
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be le 0 !");
		}
		if( CollectionUtil.isEmpty(itemIds) ) {
			throw new IllegalArgumentException("itemIds can't be empty !");
		}
		AppInfoDO.checkPermission(appInfo);
		ShelfResultDO result = this.g(itemServiceL1).sellerDownShelfItem(sellerId, itemIds, appInfo);
        return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#removeItemZoo(long, Map)
	 */
	public ProcessResultDO removeItemZoo(long itemId, Map<Long, Long> zooPairList) throws IcException {
		if (itemId <= 0 || null == zooPairList || zooPairList.isEmpty() ) {
			throw new IllegalArgumentException("itemId,zooPairList can't be null");
		}
		return g(itemServiceL1).removeItemZoo(itemId, zooPairList);
	}

	/**
	 * @see ItemService#updateItemZoo(long, Map)
	 */
	public ProcessResultDO updateItemZoo(long itemId, Map<Long, Long> zooPairList) throws IcException {
		if (itemId <= 0 || null == zooPairList || zooPairList.isEmpty() ) {
			throw new IllegalArgumentException("itemId,zooPairList can't be null");
		}
		return g(itemServiceL1).updateItemZoo(itemId, zooPairList);
	}

	/**
	 * 根据encodeId和卖家ID进行解密，并进行判断，通过后返回商品ID，如果不通过，result返回false。
	 *
	 * @param encodeId
	 * @param sellerId
	 * @return
	 */
    public ItemIdResultDO decodeItemId(String encodeId, String sellerId) {
        ItemIdResultDO result = new ItemIdResultDO();
        result.setEncodeId(encodeId);
        result.setSellerId(sellerId);
        result.setSuccess(true);
        try {
            result.setItemId(decodeId(encodeId, 7200000L, sellerId));
        } catch (Exception e) {
            result.setSuccess(false);
            result.setModule(e);
        }
        return result;
    }

	private String decodeId(String id, long time, String sellerId) {
		if (id.endsWith("tbtestpublishauction")) { // 自动话测试比较难进行宝贝的加密，如果是该字符串结尾，不进行解密
			return id;
		}
		// 为了避免csrf漏洞，使用加密
		BlowfishEncrypter encrypter = BlowfishEncrypter.getEncrypter();
		String vid = encrypter.decrypt(id);
		String[] s = StringUtil.split(vid, " ");
		if (null == s || s.length != 3) {
			throw new IllegalArgumentException("format error");
		}
		String oldSellerId = s[1];
		if (!sellerId.equals(oldSellerId)) {
			throw new IllegalArgumentException("sellerId error,old:" + oldSellerId + ", new:" + sellerId);
		}
		long t = TBStringUtil.getLong(s[2], 0);
		if (time > 0 && System.currentTimeMillis() - t > time) {
			throw new IllegalArgumentException("timeout:" + t + ", o:" + id + ", s:" + s[1]);
		}
		return s[0];
	}

	/**
	 * 根据卖家ID生成一个编码过的商品id，作用有两个：
	 * <ol>
	 * <li>商品发布的时候，不能让卖家输入校验码，为了避免卖家重复点击，发布多次，预先生成一个ID，保存时会校验</li>
	 * <li>
	 * 避免卖家串号。卖家在发布过程中，从旺旺等点击自动登陆，可能会切换为另外一个帐户，在encodeId中保存了生成时的用户ID，发布时会做校验，
	 * 避免问题
	 * </li>
	 * </ol>
	 *
	 * @param sellerId
	 * @return
	 */
	public ItemIdResultDO makeEncodeItemId(String sellerId) {
		String id = UniqID.getInstance().getUniqIDHash();
		String encodeId = encodeId(id, sellerId);
		ItemIdResultDO result = new ItemIdResultDO();
		result.setEncodeId(encodeId);
		result.setItemId(id);
		result.setSellerId(sellerId);
		result.setSuccess(true);
		// 处理错误信息
		// dealErrorsMessage(result);
		return result;
	}

	private static String encodeId(String id, String sellerId) {
		String vid = new StringBuffer(id).append(" ").append(sellerId).append(" ").append(System.currentTimeMillis())
				.toString();
		// 为了避免csrf漏洞，使用加密
		BlowfishEncrypter encrypter = BlowfishEncrypter.getEncrypter();
		return encrypter.encrypt(vid);
	}

    /**
     * 更新item的outerId和sku的outerId
     *
     * @author <a href="mailto:zhenbei@taobao.com">震北</a>
     * @since 2010-8-20 上午09:56:37
     *
     * @param itemId 宝贝数字id
     * @param itemUpdateDO 需要设置itemId、outerId和skuList（sku中需要设置itemId、skuId和outerId）
     * @param appInfoDO 见ClientAppName中定义的appName
     * @return
     * @throws IcException
     */
    public ProcessResultDO sellerModifyItemSkuOuterId(long itemId, ItemUpdateDO itemUpdateDO, AppInfoDO appInfoDO) throws IcException{
        //参数校验
        sellerModifyItemSkuOuterIdCheck(itemId, itemUpdateDO, appInfoDO);

        //重置itemUpdateDO，以防随便更新！
        ItemUpdateDO iu = new ItemUpdateDO();
        iu.setItemId(itemId);
        iu.setOuterId(itemUpdateDO.getOuterId());
        iu.setSkuList(itemUpdateDO.getSkuList());
        ProcessResultDO result = g(itemServiceL1).sellerModifyItemSkuOuterId(itemId, iu, appInfoDO);
        return dealErrorsMessage(result);
    }

    /**
     * 卖家编辑sku_outer校验
     * @param itemId
     * @param updateFields
     * @param infoDO
     * @throws IllegalArgumentException
     */
    private void sellerModifyItemSkuOuterIdCheck(long itemId, ItemUpdateDO updateFields, AppInfoDO infoDO)throws IllegalArgumentException{
    	AppInfoDO.checkPermission(infoDO);
    	if (itemId < 0 || updateFields == null || infoDO == null) {
            throw new IllegalArgumentException("itemId/itemUpdateDO/appInfoDO can't be null");
        }

    	AppInfoDO.checkPermission(infoDO);

    	updateFields.validateDbFieldLength();

    	boolean need2Update = updateFields.getOuterId()!=null; //可以更新为空串
    	List<ItemSkuDO> skuList = updateFields.getSkuList();
        if (CollectionUtil.isNotEmpty(skuList)) {
        	for(ItemSkuDO sku : skuList){
        		if(sku.getSkuId() < 1){
        			throw new IllegalArgumentException("invalid skuId!");
        		}
        		if(!need2Update && sku.getOuterId()!=null){ //可以更新为空串
        			need2Update = true;
        		}
        		if (StringUtils.byteLength(sku.getOuterId()) > 64) {
    				throw new IllegalArgumentException("ItemSkuDO#outerId can't longer than 64 byte(s)");
    			}
        	}
        }
        if(!need2Update){
        	throw new IllegalArgumentException("no outerId?!");
        }
    }

	/**
	 * @see ItemService#sellerMoveItemToStock(long, List)
	 */
	public ShelfResultDO sellerMoveItemToStock(long sellerId, List<Long> itemIds) throws IcException {
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be le 0 !");
		}
		if( CollectionUtil.isEmpty(itemIds) ) {
			throw new IllegalArgumentException("itemIds can't be empty !");
		}
		return this.g(itemServiceL1).sellerMoveItemToStock(sellerId, itemIds);
	}

	/**
	 * @see ItemService#sellerSaveNumberItemPreview(long, long, ItemUpdateDO, String, SaveItemOptionDO)
	 */
    public SaveItemResultDO sellerSaveNumberItemPreview(long itemId, long sellerId, ItemUpdateDO updateItem, String number,
            SaveItemOptionDO processOption, AppInfoDO appInfo) throws IcException {
    	injectAppInfo(processOption, appInfo);
        SaveItemResultDO result = checkSaveNumberItem(itemId, sellerId, updateItem, number, processOption);
        if ( null == result ) {
        	result = g(itemServiceL1).sellerSaveNumberItemPreview(itemId, sellerId, updateItem, number, processOption);
        }
        return dealErrorsMessage(result);
    }

    /**
     * @see ItemService#updateTaobaoSubway(long, boolean)
     */
	public ProcessResultDO updateTaobaoSubway(long itemId, boolean isSubway) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be lt 0 !");
		}
		ProcessResultDO result = g(itemServiceL1).updateTaobaoSubway(itemId, isSubway);
        return dealErrorsMessage(result);// 处理错误信息
	}

    /**
     * @see ItemService#updateTaobaoSubway(long, int)
     */
	public ProcessResultDO updateTaobaoSubway(long itemId, int P4Pflags) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be lt 0 !");
		}
		if( !CheckUtils.isIn(P4Pflags,
				ItemConstants.POINT_PRICE_NOT_SUBWAY,
				ItemConstants.POINT_PRICE_SUBWAY_ONLINE,
				ItemConstants.POINT_PRICE_SUBWAY_NOT_ONLINE) ) {
			throw new IllegalArgumentException("P4Pflags is illegal");
		}
		ProcessResultDO result = g(itemServiceL1).updateTaobaoSubway(itemId, P4Pflags);
        return dealErrorsMessage(result);// 处理错误信息
	}

	/**
	 * @see ItemService#sellerSaveItemBonus(long, String, long, long)
	 */
	public ProcessResultDO sellerSaveItemBonus(long itemId,String zoo,long sellerId, long options) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be lt 0 !");
		}
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be lt 0 !");
		}
		ProcessResultDO result = this.g(itemServiceL1).sellerSaveItemBonus(itemId, zoo, sellerId, options);
        return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#updateCategoryInShop(long, String)
	 */
	public ProcessResultDO updateCategoryInShop(long itemId, String shopCategoriesList) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be lt 0 !");
		}
		if( null == shopCategoriesList ) {
			throw new IllegalArgumentException( "shopCategoriesList can't be null" );
		}
		return g(itemServiceL1).updateCategoryInShop(itemId, shopCategoriesList);
	}

	/**
	 * @see ItemService#sellerUploadCommonItemImage(long, long, ItemImageDO)
	 */
	public ResultDO<ItemImageDO> sellerUploadCommonItemImage(long itemId, long sellerId, ItemImageDO itemImage) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be lt 0 !");
		}
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be lt 0 !");
		}
		if( null == itemImage ) {
			throw new IllegalArgumentException("itemImage can't be null !");
		}
		if (itemImage.getType() != ItemConstants.ITEM_IMAGE_TYPE_COMMON) {
			throw new IllegalArgumentException("ItemImageDO.type must ItemConstants.ITEM_IMAGE_TYPE_COMMON");
		}

		ResultDO<ItemImageDO> result = saveUploadCommonItemImage(itemImage);
		if( result.isFailure() ) {
			return result;
		}

		if (itemImage.getImageId() < 1 && itemImage.getImageUrl() == null) {// 为插入数据，必须传入所有值
			throw new IllegalArgumentException("insert image must setup imageUrl or imageData");
		}

		result = g(itemServiceL1).sellerUploadCommonItemImage(itemId, sellerId, itemImage);
        return dealErrorsMessage(result);
	}

	/**
	 * 存储上传Common时的图片
	 * @param itemImage
	 * @return
	 * @throws IcException
	 */
	private ResultDO<ItemImageDO> saveUploadCommonItemImage(ItemImageDO itemImage) throws IcException {
		boolean hasMajorImageData = false;// 是主图但没提供主图数据
		ResultDO<ItemImageDO> result = new ResultDO<ItemImageDO>();
		if (itemImage.getImageData() != null) {
			if (itemImage.getImageData().length > MAX_FILE_SIZE) {
				result.addError(ErrorConstants.IC_ITEM_PIC_IS_TOO_LARGE);
                return dealErrorsMessage(result);
			}

			SavePictureFileResultDO subResult = pictureFileManager.saveFileToTfs(itemImage.getImageData(), itemImage
					.getInputFileName(), itemImage.isMajor());
			if (subResult.isFailure()) {
				result.setSuccess(false);
				result.setResultCode(ErrorConstants.IC_CHECKSTEP_FAIL);
				result.addErrors(subResult.getErrors());
				result.getContext().putAll(subResult.getContext());
                return dealErrorsMessage(result);
			}

			if (subResult.getPictUrl() == null) {
				String msg = "tfsManager can't save file.";
				throw new IcException(msg);
			}
			itemImage.setImageUrl(subResult.getPictUrl());
			itemImage.setMainColor(subResult.getMainColor());
			itemImage.setImageData(null);
			if (itemImage.isMajor()) {
				hasMajorImageData = true;
			}
		}
		if (itemImage.isMajor() && hasMajorImageData == false) {// 主图对象只提供了url，没提供数据，则将mainColor至为null，让后端决定是否要清空mainColor
			itemImage.setMainColor(null);
		}
		return result;
	}


	/**
	 * @see ItemService#saveImageToTfs(String, byte[], boolean)
	 */
	public SavePictureFileResultDO saveImageToTfs(String fileName, byte[] imageData, boolean needMainColor) {
		return pictureFileManager.saveFileToTfs(imageData, fileName, needMainColor);
	}

	/**
	 * @see ItemService#sellerDelItemImage(long, long, long)
	 */
	@SuppressWarnings("rawtypes")
	public ResultDO sellerDelItemImage(long itemId, long itemImageId, long sellerId) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be lt 0 !");
		}
		if( 0 >= itemImageId ) {
			throw new IllegalArgumentException("itemImageId can't be lt 0 !");
		}
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be lt 0 !");
		}
		ResultDO result = g(itemServiceL1).sellerDelItemImage(itemId, itemImageId, sellerId);
        return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#sellerUploadPropertyImage(long, long, ItemImageDO)
	 */
	public ResultDO<ItemImageDO> sellerUploadPropertyImage(long itemId, long sellerId, ItemImageDO itemImage) throws IcException {
		if( 0 >= itemId ) {
			throw new IllegalArgumentException("itemId can't be lt 0 !");
		}
		if( 0 >= sellerId ) {
			throw new IllegalArgumentException("sellerId can't be lt 0 !");
		}
		if( null == itemImage ) {
			throw new IllegalArgumentException("itemImage can't be null !");
		}
		if (itemImage.getType() != ItemConstants.ITEM_IMAGE_TYPE_PROPERTY) {
			throw new IllegalArgumentException("ItemImageDO.type must ItemConstants.ITEM_IMAGE_TYPE_PROPERTY");
		}
		if (itemImage.getImageId() < 1 && itemImage.getImageUrl() == null) {// 为插入数据，必须传入所有值
			throw new IllegalArgumentException("insert image must setup imageUrl or imageData");
		}

		ResultDO<ItemImageDO> result = saveUploadPropertyImage(itemImage);
		if( result.isFailure() ) {
			return result;
		}

		if (itemImage.getImageId() < 1) {// 为插入数据，必须传入所有值
			if (itemImage.getImageUrl() == null) {
				throw new IllegalArgumentException("insert image must setup imageUrl or imageData");
			}
			if (StringUtil.isBlank(itemImage.getProperties())) {
				throw new IllegalArgumentException("insert image must setup properties");
			}
		}
		result = g(itemServiceL1).sellerUploadPropertyImage(itemId, sellerId, itemImage);
        return dealErrorsMessage(result);

	}

	/**
	 * 保存卖家上传的属性图片
	 * @param itemImage
	 * @return
	 * @throws IcException
	 */
	private ResultDO<ItemImageDO> saveUploadPropertyImage(ItemImageDO itemImage) throws IcException {
		ResultDO<ItemImageDO> result = new ResultDO<ItemImageDO>();
		if (itemImage.getImageData() != null) {
			if (itemImage.getImageData().length > MAX_FILE_SIZE) {
				result.addError(ErrorConstants.IC_ITEM_PIC_IS_TOO_LARGE);
                return dealErrorsMessage(result);
			}

			SavePictureFileResultDO subResult = pictureFileManager.saveFileToTfs(itemImage.getImageData(), itemImage
					.getInputFileName(), false);
			if (subResult.isFailure()) {
				result.setSuccess(false);
				result.setResultCode(ErrorConstants.IC_CHECKSTEP_FAIL);
				result.addErrors(subResult.getErrors());
				result.getContext().putAll(subResult.getContext());
                return dealErrorsMessage(result);
			}

			String imageUrl = subResult.getPictUrl();
			if (imageUrl == null) {
				String msg = "tfsManager can't save file.";
				log.error(msg);
				throw new IcException(msg);
			}
			itemImage.setImageUrl(imageUrl);
			itemImage.setImageData(null);
		}
		return result;
	}

	/**
	 * @see ItemService#closeBidAuction(long, String)
	 */
	public void closeBidAuction(long itemId, String sellerId) {
		g(itemServiceL1).closeBidAuction(itemId, sellerId);
	}

	/**
	 * @see ItemService#refreshItemPostage(List)
	 */
    public int refreshItemPostage(List<UpdatedPostageDO> list) {
        return itemServiceL2.refreshItemPostage(list);
    }

    /**
     * @see ItemService#delVideoItems(List)
     */
	public BatchItemVideoResultDO delVideoItems(List<Long> itemIds) throws IcException {
		if( CollectionUtil.isEmpty(itemIds) ) {
			throw new IllegalArgumentException("itemIds can not be empty !");
		}
		for( Long itemId : itemIds ) {
			if( null == itemId || itemId <= 0 ) {
				throw new IllegalArgumentException("itemId can not be lt 0!");
			}
		}
		BatchItemVideoResultDO result = g(itemServiceL2).delVideoItems(itemIds);
        return dealErrorsMessage(result);
	}

	/**
	 * @see ItemService#delVideoItemByVideoId(long[], long)
	 */
	public BatchItemVideoResultDO delVideoItemByVideoId(long[] videoIds,
			long isvId) throws IcException {
		if (null == videoIds || videoIds.length < 1) {
			throw new IllegalArgumentException("videoIds is null or length is not larger zero.");
		}

		if (videoIds.length > 10) {
			throw new IllegalArgumentException("videoIds length is larger 10.");
		}
		if (isvId < 1) {
			throw new IllegalArgumentException("isv is invalid.");
		}

		BatchItemVideoResultDO result = g(itemServiceL1).delVideoItemByVideoId(videoIds, isvId);
        return dealErrorsMessage(result);
	}

	/**
	 * 在本地先检查一些参数是否合法
	 * @param item
	 * @param publicItemOption
	 * @param needCheckUserId 是否需要检查userId
	 * @return
	 * @throws IcException
	 */
    private CreateItemResultDO checkItem(ItemDO item, PublishItemOptionDO publicItemOption, boolean needCheckUserId) throws IcException {
        if (item == null || publicItemOption == null) {
            throw new IllegalArgumentException("item/publicItemOption can't be null");
        }

        if (needCheckUserId && item.isSellerIdEmpty()) {
            throw new IllegalArgumentException("ItemDO.userId can't be empty");
        }
         //对描述判空     add by qier
		if(StringUtil.isBlank(item.getDescription())){
			 CreateItemResultDO result = new CreateItemResultDO();
			 result.addError(ErrorConstants.IC_ITEM_DESC_COULD_NOT_BE_EMPTY);
	            return result;
		}

        if (item.getDescription() != null && item.getDescription().length() > ItemChecker.MAX_ITEM_DESC) {
            CreateItemResultDO result = new CreateItemResultDO();
            result.addError(ErrorConstants.IC_ITEM_DESC_IS_TOO_LONG);
            return dealErrorsMessage(result);
        }

        // 一个商品对应一个视频记录
        if (item.getVideoList() != null) {
            String errorCode = checkItemVideoList(item.getVideoList());
            if (errorCode != null) {
                CreateItemResultDO result = new CreateItemResultDO();
                result.addError(errorCode);
                return dealErrorsMessage(result);
            }
        }

        //检验商品的附件信息是否完整
        if (item.getAttachList() != null) {
            String errorCode = checkItemAttachList(item.getAttachList());
            if (errorCode != null) {
                CreateItemResultDO result = new CreateItemResultDO();
                result.addError(errorCode);
                return dealErrorsMessage(result);
            }
        }
        if (item.getCategoryId() == null || item.getCategoryId() == 0) {
            CreateItemResultDO result = new CreateItemResultDO();
            result.addError(ErrorConstants.IC_CHECKSTEP_CATEGORY);
            return dealErrorsMessage(result);
        }

        ItemUtils.validateDbFieldLength(item, needCheckUserId);

        if (item.isAutoConsignment()) {
            item.setSkuList(null); // 卡密商品不能有sku
        }

        if (item.isAuction()) {
            item.setSkuList(null); // 拍卖商品不能有sku
        }

        List<ItemSkuDO> skuList = item.getSkuList();
        if (skuList != null && skuList.size() > 0) {
            item.setQuantity(validateSkuPropertyAndReturnQuantity(skuList));
        }

        List<ItemImageDO> itemImageList = item.getCommonItemImageList();
        // 业务说明：主图有可能为null
        if (itemImageList != null) {
            String errorCode = checkCommonImageList(itemImageList);
            if (errorCode != null) {
                CreateItemResultDO result = new CreateItemResultDO();
                result.addError(errorCode);
                return dealErrorsMessage(result);
            }
        } else {
            item.setCommonItemImageList(new ArrayList<ItemImageDO>());
        }
        itemImageList = item.getPropertyImageList();
        if (itemImageList != null) {
            String errorCode = checkInputImageList(itemImageList);
            if (errorCode != null) {
                CreateItemResultDO result = new CreateItemResultDO();
                result.addError(errorCode);
                return dealErrorsMessage(result);
            }
        } else {
            // 看javadoc: 防止内部不注意新增加的代码出现NullPointer错误。
            item.setPropertyImageList(new ArrayList<ItemImageDO>());
        }

        if (StringUtil.isBlank(publicItemOption.getLang())) {
            throw new IllegalArgumentException("PublishItemOptionDO.lang can't be blank");
        }

        if (null != item.getFeatures() && item.getFeatures().containsKey(ItemFeature.FEATURE_CCURL)) {
            String ccurl = item.getFeatures().get(ItemFeature.FEATURE_CCURL);
            ccurl = (ccurl == null) ? "" : ccurl;

            // 充值地址的长度不能超过150
            if (ccurl.length() > 150) {
                CreateItemResultDO result = new CreateItemResultDO();
                result.addError(ErrorConstants.AUTO_CCURL_TOO_LEN);
                return dealErrorsMessage(result);
            }
        }

        // IMPORTANT! 已上传到tfs上的图片不用删除掉，后台出错了，返回前台让用户修正错误的地方，用户也不用重新上传图片了,
        // 并需要能预览上次上传的图片。一些垃圾图片，tfs会定时清理
        itemImageList = item.getCommonItemImageList();
        CreateItemResultDO result = new CreateItemResultDO();
        ItemImageDO majorImage = saveInputImageList(itemImageList, true, result);

        if (result.isFailure()) {
            return dealErrorsMessage(result);
        }
        if (majorImage != null) {
            item.setPictUrl(majorImage.getImageUrl()); // 设置主图或主图颜色
            item.setMainColor(majorImage.getMainColor());

            itemImageList.remove(majorImage);// 从列表中清除主图
        }

        itemImageList = item.getPropertyImageList();
        if (itemImageList != null) {
            saveInputImageList(itemImageList, false, result);
            if (result.isFailure()) {
                return result;
            }
        }

        if (item.getAttachList() != null) {
            saveItemAttachList(item.getAttachList(), result);
            if (result.isFailure()) {
                return result;
            }
        }
        return null;
    }

    /**
     * 检查数字宝贝是否合法
     * @param item
     * @param number
     * @param processOption
     * @return
     */
    private CreateItemResultDO checkNumberItem(ItemDO item, String number, PublishItemOptionDO processOption) {
        if (item == null || StringUtil.isBlank(number) || processOption == null) {
            throw new IllegalArgumentException("item/number/PublishItemOptionDO can't be null");
        }

        if (StringUtil.isBlank(processOption.getLang())) {
            throw new IllegalArgumentException("PublishItemOptionDO.lang can't be blank");
        }
        if (item.getCategoryId() == null || item.getCategoryId() == 0) {
            CreateItemResultDO result = new CreateItemResultDO();
            result.addError(ErrorConstants.IC_CHECKSTEP_CATEGORY);
            return result;
        }

        // 号码库不需要标题校验
        // 校验类目等
        if (item.getReservePriceLong() == null || item.getAuctionType() == null
                || item.getDuration() == null || item.getCity() == null || item.getProv() == null
                || item.getQuantity() == null || item.getStuffStatus() == null || null == item.getDescription()) {
            throw new IllegalArgumentException(
                    "category/reservePrice/auctionType/city/prov/quantity/stuffStatus can't be null");
        }

        // 号码库商品不支持多图
        item.setCommonItemImageList(null);
        item.setPropertyImageList(null);
        item.setSkuList(null); // 号码商品不能有sku

        return null;
    }

    /**
     * 卖家编辑宝贝检查是否合法
     * @param itemId
     * @param sellerId
     * @param inputItem
     * @param processOption
     * @return
     * @throws IcException
     */
    private SaveItemResultDO checkSaveItem(long itemId, long sellerId, ItemUpdateDO inputItem,
            SaveItemOptionDO processOption)
            throws IcException {
        // 1 参数校验代码
        SaveItemResultDO result = checkParamForSaveItem(itemId, sellerId, inputItem, processOption);
        if (result.isFailure()) {
            return result;
        }

        // 2 业务处理代码
        // 2.1 卖家编辑保存时不能修改的数据
        inputItem.setCurrentBidLong(null);
        inputItem.setAuctionType(null);
        inputItem.clearOrderCost();

        // 2.2 根据SKU设置商品数量
        List<ItemSkuDO> skuList = inputItem.getSkuList();
        if (skuList != null && skuList.size() > 0) {// 自动设置一下总数量，能减少一些错误，但并不能避免所有错误
            inputItem.setQuantity(validateSkuPropertyAndReturnQuantity(skuList));
        }

        // 2.3 处理图片信息
        processImageList(inputItem, result);
        if (result.isFailure()) {
            return result;
        }

        //2.4处理附件信息
        saveItemAttachList(inputItem.getAttachList(), result);
        if (result.isFailure()) {
            return result;
        }

        return result;
    }

    private SaveItemResultDO checkSaveNumberItem(long itemId, long sellerId, ItemUpdateDO inputItem, String number,
            SaveItemOptionDO processOption) {
        if (inputItem == null || number == null) {
            throw new IllegalArgumentException("item/number can't be null");
        }
        if (itemId == 0 || sellerId == 0) {
            throw new IllegalArgumentException("itemId/sellerId can't be null");
        }

        if (StringUtil.isBlank(processOption.getLang())) {
            throw new IllegalArgumentException("PublishItemOptionDO.lang can't be blank");
        }

        inputItem.setCommonItemImageList(null);
        inputItem.setPropertyImageList(null);
        inputItem.setSkuList(null);

        if (inputItem.getCategoryId() != null && inputItem.getCategoryId() == 0) {
            SaveItemResultDO result = new SaveItemResultDO();
            result.addError(ErrorConstants.IC_CHECKSTEP_CATEGORY);
            return result;
        }
        return null;
    }

    private void saveItemAttachList(List<ItemAttachDO> attachDOList, BaseResultDO result) {
        if (attachDOList == null || attachDOList.size() == 0) {
            return;
        }

        for (ItemAttachDO attachDO : attachDOList) {
            //如果传递的ContentData为null而携带的Url不为null，则直接使用指定的url
            if(attachDO.getContentData() == null || 0 == attachDO.getContentData().length)
            {
                if(attachDO.getAttachUrl()!= null )
                {
                continue;
                }
            }
            ResultDO<String> subResult = attachFileManager.saveFileToTfs(attachDO.getContentData(), attachDO.getAttachSuffix());
            if(result.isSuccess()){
            	attachDO.setAttachUrl(subResult.getModule());
            }else {
            	result.addErrors(subResult.getErrors());
            	return;//出现错误终止处理
            }
        }
    }

    private String checkItemAttachList(List<ItemAttachDO> attachDOList) {
        String errorCode = null;
        if (attachDOList.size() > ItemConstants.ITEM_ATTACH_NUM_UP_LIMIT) {
            errorCode = ErrorConstants.IC_ITEM_ATTACH_NUM_OVERFLOW;
            return errorCode;
        }
        for (ItemAttachDO attachDO : attachDOList) {
            if(attachDO == null){
                throw new IllegalArgumentException("attach can't be null");
            }
            if (StringUtil.isBlank(attachDO.getAttachSuffix())) {
                throw new IllegalArgumentException("attach suffix can't be null");
            }
            if (attachDO.getAttachUrl() == null && (null == attachDO.getContentData() || 0 == attachDO.getContentData().length)) {
                throw new IllegalArgumentException("attach url and CoytentData can't be both null");
            }
        }
        return errorCode;
    }

	public ProcessResultDO saveItemFeatures(Map<Long, List<FeatureDO>> updateFeaturesMap,
			Map<Long, List<String>> removeFeaturesKeyMap) throws IcException {
        if ((null == updateFeaturesMap || updateFeaturesMap.size() == 0) && (removeFeaturesKeyMap == null || removeFeaturesKeyMap.size() == 0)) {
            throw new IllegalArgumentException("input feature map can not be null.");
        }
        Set<Long> itemIdSet = new HashSet<Long>();
        if (updateFeaturesMap != null && updateFeaturesMap.size() != 0) {
            itemIdSet.addAll(updateFeaturesMap.keySet());
        }

        if (removeFeaturesKeyMap != null && removeFeaturesKeyMap.size() != 0) {
            itemIdSet.addAll(removeFeaturesKeyMap.keySet());
        }

        if (itemIdSet.size() == 0) {
            return new ProcessResultDO();
        }

        if (itemIdSet.size() > 20) {
            throw new IllegalArgumentException("features can not be more than 20.");
        }

        ProcessResultDO result = g(itemServiceL1).saveItemFeatures(updateFeaturesMap, removeFeaturesKeyMap, AppInfoDO.UNKNOWN);
        dealErrorsMessage(result);
        return result;
	}
	
	public ProcessResultDO saveItemFeatures(Map<Long, List<FeatureDO>> updateFeaturesMap,
			Map<Long, List<String>> removeFeaturesKeyMap, AppInfoDO app) throws IcException {
		AppInfoDO.checkPermission(app);
		if ((null == updateFeaturesMap || updateFeaturesMap.size() == 0) && (removeFeaturesKeyMap == null || removeFeaturesKeyMap.size() == 0)) {
			throw new IllegalArgumentException("input feature map can not be null.");
		}
		Set<Long> itemIdSet = new HashSet<Long>();
		if (updateFeaturesMap != null && updateFeaturesMap.size() != 0) {
			itemIdSet.addAll(updateFeaturesMap.keySet());
		}
		
		if (removeFeaturesKeyMap != null && removeFeaturesKeyMap.size() != 0) {
			itemIdSet.addAll(removeFeaturesKeyMap.keySet());
		}
		
		if (itemIdSet.size() == 0) {
			return new ProcessResultDO();
		}
		
		if (itemIdSet.size() > 20) {
			throw new IllegalArgumentException("features can not be more than 20.");
		}
		
		ProcessResultDO result = g(itemServiceL1).saveItemFeatures(updateFeaturesMap, removeFeaturesKeyMap, app);
		dealErrorsMessage(result);
		return result;
	}

	/**
     * 编辑商品扩展信息
     * 商品中未找到的结构，会新增
     * 已存在的，则覆盖原有的结构
     * 当remove和update同时存在，则会删除结构，remove优先级大于update
     * @param updateExtendsList 新增或编辑一个扩展信息。
     * @param removeExtendsKeyList 按照key删除对应的扩展信息,必选结构不能删除
     * @param app 调用方信息
     * @return
     * @throws IcException
     */
	public SaveItemResultDO saveItemExtends(long itemId,List<ItemExtendsFieldDO> updateExtendsList,List<String> removeExtendsKeyList, AppInfoDO app)
			throws IcException {
		AppInfoDO.checkPermission(app);
		if ((null == updateExtendsList || updateExtendsList.size() == 0) && (removeExtendsKeyList == null || removeExtendsKeyList.size() == 0)) {
			throw new IllegalArgumentException("input extends map can not be null.");
		}
		
		SaveItemResultDO result = g(itemServiceL1).saveItemExtends(itemId, updateExtendsList, removeExtendsKeyList, app);
		dealErrorsMessage(result);
		return result;
	}
	
    public BaseResultDO updateSumAuctionQuantity(Long itemNumId, int auctionQuantity) throws IcException {
    	if(itemNumId==null || itemNumId == 0){
    		throw new IllegalArgumentException("itemNumerId cant null,itemNumerId="+itemNumId);
    	}
    	if(auctionQuantity<=0){
    		throw new IllegalArgumentException("auctionQuantity Cant <=0,auctionQuantity="+auctionQuantity);
    	}
    	return g(itemService).updateSumAuctionQuantity(itemNumId, auctionQuantity);
    }

	public long generateItemId(String userStrId, AppInfoDO appInfo) throws IcException {
		Assert.isTrue(!StringUtil.isBlank(userStrId), "user id can not be null or empty!");//这里用断言？？
		if( !CheckUtils.isIn(appInfo.getAppName(),
				AppInfoConstants.NAME_VIC,
				AppInfoConstants.NAME_SELL,
				AppInfoConstants.NAME_TMALLSELL,
				AppInfoConstants.NAME_LIFE,
				AppInfoConstants.NAME_EJU,
				AppInfoConstants.NAME_FANG) ) {
			throw new IllegalArgumentException("The user can not call this method!");
		}
    	return g(itemService).generateItemId(userStrId, appInfo);
	}


	/**
	 * @see ItemService#sellerCreateSkuStore(long, SkuStoreDO, AppInfoDO)
	 * @deprecated {@link ItemServiceClient#sellerCreateAuctionStore(long, AuctionStoreDO, AppInfoDO)}
	 */
	@Deprecated
    public ResultDO<SkuStoreDO> sellerCreateSkuStore(long sellerId, SkuStoreDO skuStore, AppInfoDO appInfo) throws IcException {
    	if( sellerId <= 0 ) {
    		throw new IllegalArgumentException("sellerId can't be le 0");
    	}
    	if( null == skuStore
    			|| StringUtil.isBlank(skuStore.getStoreCode())
    			|| 0 >= skuStore.getSkuId()
    			|| 0 >  skuStore.getQuantity()
    			|| 0 >= skuStore.getAuctionId()
    			|| 0 >= skuStore.getSellerId()) {
    		throw new IllegalArgumentException("skuStore is illegal");
    	}
    	AppInfoDO.checkPermission(appInfo);
    	ResultDO<SkuStoreDO> result = g(itemServiceL1).sellerCreateSkuStore(sellerId, skuStore, appInfo);
    	return dealErrorsMessage(result);
    }

	/**
	 * @see ItemService#sellerModifySkuStoreQuantity(long, long, Map, AppInfoDO)
	 * @deprecated {@link ItemServiceClient#sellerModifyAuctionStoreQuantity(long, Map, AppInfoDO)}
	 */
    @Deprecated
	public ProcessSkuStoreResultDO sellerModifySkuStoreQuantity(long sellerId, long itemId, 
			Map<SkuStoreIdDO,Integer> skuStoreQuantities, AppInfoDO appInfo) throws IcException {
		if( sellerId <= 0 ) {
    		throw new IllegalArgumentException("sellerId can't be le 0");
    	}
		if( itemId <= 0 ) {
    		throw new IllegalArgumentException("itemId can't be le 0");
    	}
		if( CollectionUtil.isEmpty(skuStoreQuantities) ) {
			throw new IllegalArgumentException("skuStoreQuantities can't be empty");
		}
		for(Map.Entry<SkuStoreIdDO, Integer> entry : skuStoreQuantities.entrySet()) {
			if( null == entry.getKey() || null == entry.getValue() || entry.getValue() < 0 ) {
				throw new IllegalArgumentException("skuStoreQuantities is illegal");
			}
		}//for
    	AppInfoDO.checkPermission(appInfo);
    	ProcessSkuStoreResultDO result = g(itemServiceL1).sellerModifySkuStoreQuantity(sellerId, itemId, skuStoreQuantities, appInfo);
    	return dealErrorsMessage(result);
	}
	
	/**
	 * @see {@link ItemService#sellerCreateAuctionStore(long, AuctionStoreDO, AppInfoDO)}
	 */
    public ResultDO<AuctionStoreDO> sellerCreateAuctionStore(long sellerId, AuctionStoreDO auctionStore, AppInfoDO appInfo) throws IcException {
    	if( sellerId <= 0 ) {
    		throw new IllegalArgumentException("sellerId can't be le 0");
    	}
    	if( null == auctionStore
    			|| StringUtil.isBlank(auctionStore.getStoreCode())
    			|| 0 >  auctionStore.getSkuId()
    			|| 0 >  auctionStore.getQuantity()
    			|| 0 >= auctionStore.getAuctionId()
    			|| 0 >= auctionStore.getSellerId()) {
    		throw new IllegalArgumentException("auctionStore is illegal");
    	}
    	AppInfoDO.checkPermission(appInfo);
    	ResultDO<AuctionStoreDO> result = g(itemServiceL1).sellerCreateAuctionStore(sellerId, auctionStore, appInfo);
    	return dealErrorsMessage(result);
    }
    
    /**
     * @see {@link ItemService#sellerDeleteAuctionStore(long, List, AppInfoDO)}
     */
    public BatchResultDO<AuctionStoreIdDO> sellerDeleteAuctionStore(long sellerId, 
    		List<AuctionStoreIdDO> auctionStoreIds, AppInfoDO appInfo) throws IcException {
    	
    	if( sellerId <= 0 ) {
    		throw new IllegalArgumentException("sellerId can't be le 0");
    	}
		if( CollectionUtil.isEmpty(auctionStoreIds) ) {
			throw new IllegalArgumentException("skuStoreIds can't be empty");
		}
		if( auctionStoreIds.size() > 20 ) {
			throw new IllegalArgumentException("skuStoreIds size > 20");
		}
    	AppInfoDO.checkPermission(appInfo);
    	BatchResultDO<AuctionStoreIdDO> result = g(itemServiceL1).sellerDeleteAuctionStore(sellerId, auctionStoreIds, appInfo);
    	
    	return dealErrorsMessage(result);
    }
	
	/**
	 * @see {@link ItemService#sellerModifyAuctionStoreQuantity(long, Map, AppInfoDO)}
	 */
	public BatchResultDO<AuctionStoreIdDO> sellerModifyAuctionStoreQuantity(long sellerId, 
			Map<AuctionStoreIdDO, Integer> auctionStoreQuantities, AppInfoDO appInfo) throws IcException {
		
		if( sellerId <= 0 ) {
    		throw new IllegalArgumentException("sellerId can't be le 0");
    	}
		if( CollectionUtil.isEmpty(auctionStoreQuantities) ) {
			throw new IllegalArgumentException("skuStoreQuantities can't be empty");
		}
		for(Map.Entry<AuctionStoreIdDO, Integer> entry : auctionStoreQuantities.entrySet()) {
			if( null == entry.getKey() || null == entry.getValue() || entry.getValue() < 0 ) {
				throw new IllegalArgumentException("auctionStoreQuantities is illegal");
			}
		}//for
    	AppInfoDO.checkPermission(appInfo);
    	BatchResultDO<AuctionStoreIdDO> result = g(itemServiceL1).sellerModifyAuctionStoreQuantity(sellerId, auctionStoreQuantities, appInfo);
    	
    	return dealErrorsMessage(result);
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public void setItemServiceL1(ItemService itemServiceL1) {
		this.itemServiceL1 = itemServiceL1;
	}

	public void setItemServiceL2(ItemService itemServiceL2) {
		this.itemServiceL2 = itemServiceL2;
	}

	public void setAttachFileManager(FileManager attachFileManager) {
        this.attachFileManager = attachFileManager;
    }

	public void setPictureFileManager(PictureFileManager picFileManager) {
		this.pictureFileManager = picFileManager;
	}



}
