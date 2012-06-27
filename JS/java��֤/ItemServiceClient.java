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
 * IC���������ӿڣ���Ҫ���ڷ������༭���޸Ŀ��Ȳ���
 *	<ul>
 *		<li>�ϴ���������ѡ����attachFileManager</li>
 *		<li>�ϴ�ͼƬ����ѡ����pictureFileManager</li>
 *	</ul>
 * @author dukun
 *
 */
public class ItemServiceClient extends BaseServiceClient {

	private final Log log = LogFactory.getLog(ItemServiceClient.class);
	private static final long MAX_FILE_SIZE = 500 * 1024;//�ϴ��ļ�����󳤶�:500K

	private ItemService itemService;
	private ItemService itemServiceL1;
	private ItemService itemServiceL2;

    private FileManager attachFileManager;
    private PictureFileManager pictureFileManager;

    /**
     * ѡ��ItemService
     * @param s
     * @return
     */
    private ItemService g(ItemService s) {
    	return null == s ? itemService : s;
	}

    /**
     * ��λȡ���Options�ֶμ���ֵ
     * @see ItemService#addItemOptions(long, List)
     */
    public ProcessResultDO addItemOptions(long itemId, List<Long> options) throws IcException {
    	//������飺options����Ϊ��
    	if( CollectionUtil.isEmpty(options) ) {
    		throw new IllegalArgumentException("options can't be empty");
    	}
    	ProcessResultDO resultDo = g(itemServiceL1).addItemOptions(itemId, options, AppInfoDO.UNKNOWN);
        return dealErrorsMessage(resultDo);// ���������Ϣ
    }
    
    /**
     * ��λȡ���Options�ֶμ���ֵ
     * @see ItemService#addItemOptions(long, List, AppInfoDO)
     */
    public ProcessResultDO addItemOptions(long itemId, List<Long> options, AppInfoDO app) throws IcException {
    	AppInfoDO.checkPermission(app);
    	//������飺options����Ϊ��
    	if( CollectionUtil.isEmpty(options) ) {
    		throw new IllegalArgumentException("options can't be empty");
    	}
    	ProcessResultDO resultDo = g(itemServiceL1).addItemOptions(itemId, options, app);
    	return dealErrorsMessage(resultDo);// ���������Ϣ
    }

	/**
	 * ��λȡ���Options�ֶγ�ȥֵ
	 * @see ItemService#removeItemOptions(long, List)
	 */
    public ProcessResultDO removeItemOptions(long itemId, List<Long> options) throws IcException {
    	//������飺options����Ϊ��
    	if ( CollectionUtil.isEmpty(options) ) {
			throw new IllegalArgumentException("options can't be empty !");
		}
    	ProcessResultDO resultDo = g(itemServiceL1).removeItemOptions(itemId, options, AppInfoDO.UNKNOWN);
        return dealErrorsMessage(resultDo);// ���������Ϣ

    }
    
    /**
     * ��λȡ���Options�ֶγ�ȥֵ
     * @see ItemService#removeItemOptions(long, List, AppInfoDO)
     */
    public ProcessResultDO removeItemOptions(long itemId, List<Long> options, AppInfoDO app) throws IcException {
    	AppInfoDO.checkPermission(app);
    	//������飺options����Ϊ��
    	if ( CollectionUtil.isEmpty(options) ) {
    		throw new IllegalArgumentException("options can't be empty !");
    	}
    	ProcessResultDO resultDo = g(itemServiceL1).removeItemOptions(itemId, options, app);
    	return dealErrorsMessage(resultDo);// ���������Ϣ
    	
    }

    /**
     * ���������༭����ע��Ӧ�÷���Ϣ
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
     * �����Ƶ�б�,һ����Ʒֻ�ܶ�Ӧһ����Ƶ<br/>
     * @param videoList
     * @return
     */
	private String checkItemVideoList(List<ItemVideoDO> videoList) {
		if(videoList.size() > 1){
			//��Ϊɶ��ҪList?����
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
	 * ��֤SKU�������Լ����ؼ�������ܿ��
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
			sku.setStatus(ItemSkuDO.STATUS_NORMAL);// ǰ�˴�������sku�ͱ�ʾ��������
			if (sku.getQuantity() < 0) {
				// sku.quantityΪ0����ɾ��sku����Ϊsku.outer_id���̼����ݣ���Ϊ�̼�ϵͳ�ṩ�����еĿ����Ϣ������ʱ������Ϊ0�ǲ��ܳ��۵�
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
     * �����ϴ���ͼƬ�б�
     * <ul>
     * 	<li>���浽TFS</li>
     * 	<li>�����ɵ�ͼƬ·������Ϣ���浽ItemUpdateDO������</li>
     * </ul>
     * @param inputItem
     * @param result
     * @throws IcException
     */
	private void processImageList(ItemUpdateDO inputItem,
			SaveItemResultDO result) throws IcException {
		// IMPORTANT! ���ϴ���tfs�ϵ�ͼƬ����ɾ��������̨�����ˣ�����ǰ̨���û���������ĵط����û�Ҳ���������ϴ�ͼƬ��,
		// ����Ҫ��Ԥ���ϴ��ϴ���ͼƬ��һЩ����ͼƬ��tfs�ᶨʱ����
		List<ItemImageDO> itemImageList = inputItem.getCommonItemImageList();

		if (itemImageList != null) {// ��Ҫ������ͼ��ͼƬ
            ItemImageDO majorImage = saveInputImageList(itemImageList, false, result);
			if (result.isFailure()) {
				return;
			}
			if (majorImage != null) {// ������ͼ����ͼ��ɫ
				inputItem.setPictUrl(majorImage.getImageUrl());
				inputItem.setMainColor(majorImage.getMainColor());
				itemImageList.remove(majorImage);
			} else {// size == 0 ����ʾɾ������ͼƬ�������ͼ
				inputItem.setPictUrl(StringUtil.EMPTY_STRING); // pictUrlΪnull����ʾ�������ͼurl��pictUrlΪ""����ʾ�����ͼ
				inputItem.setMainColor(StringUtil.EMPTY_STRING);
			}
		}// ͼƬ�б�Ϊnull����ԭ�������ݲ��䣬��ͼurl��mainColorҲ����

		itemImageList = inputItem.getPropertyImageList();
		if (null != itemImageList) {
			saveInputImageList(itemImageList, false, result);
		}
	}

	/**
	 * ������ұ༭�����Ĳ���
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

		// �������������Զ������Ϊ����
		if (null != inputItem.getQuantity() && 0 > inputItem.getQuantity()) {
			result.addError(ErrorConstants.IC_QUANTITY_LESS_THAN_ZERO);
			return result;
		}
		
		//�༭ʱ��������Ϊnull�����ǲ���Ϊ�մ�     add by qier
		if(null!=inputItem.getDescription()&&StringUtil.isBlank(inputItem.getDescription())){
			 result.addError(ErrorConstants.IC_ITEM_DESC_COULD_NOT_BE_EMPTY);
	         return result;
		}

        if (inputItem.getDescription() != null && inputItem.getDescription().length() > ItemChecker.MAX_ITEM_DESC) {
            result.addError(ErrorConstants.IC_ITEM_DESC_IS_TOO_LONG);
            return result;
        }

		// һ����Ʒ��Ӧһ����Ƶ��¼
		if (inputItem.getVideoList() != null && inputItem.isModifiedVideoList()) {
			String errorCode = checkItemVideoList(inputItem.getVideoList());
			if (errorCode != null) {
				result.addError(errorCode);
                return result;
			}
		}

        //������Ʒ�ĸ�����Ϣ�Ƿ�����
        if (inputItem.getAttachList() != null) {
            String errorCode = checkItemAttachList(inputItem.getAttachList());
            if (errorCode != null) {
                result.addError(errorCode);
                return result;
            }
        }

        //��Ŀid
		if(inputItem.getCategoryId() !=null && inputItem.getCategoryId() == 0){
			result.addError(ErrorConstants.IC_CHECKSTEP_CATEGORY);
            return result;
		}

		//У���Ƿ񳬳����ݿ��ֶγ�������
		inputItem.validateDbFieldLength();

		//��ͼ
		List<ItemImageDO> itemImageList = inputItem.getCommonItemImageList();
		if (itemImageList != null) {
			String errorCode = checkCommonImageList(itemImageList);
			if (errorCode != null) {
				result.addError(errorCode);
                return dealErrorsMessage(result);
			}
		}// ������Ϊnull�Ͳ������������仯

		//����ͼƬ
		itemImageList = inputItem.getPropertyImageList();
		if (itemImageList != null) {
			String errorCode = checkInputImageList(itemImageList);

			if (errorCode != null) {
				result.addError(errorCode);
                return dealErrorsMessage(result);
			}

		}// ������Ϊnull�Ͳ������������仯

		if (null != inputItem.getUpdateFeatureList() && inputItem.getUpdateFeatureList().containsKey(ItemFeature.FEATURE_CCURL)) {
			String ccurl = inputItem.getUpdateFeatureList().get(ItemFeature.FEATURE_CCURL);
			ccurl = (ccurl == null) ? StringUtil.EMPTY_STRING : ccurl;

			// ��ֵ��ַ�ĳ��Ȳ��ܳ���150
			if (ccurl.length() > 150) {
				result.addError(ErrorConstants.AUTO_CCURL_TOO_LEN);
                return dealErrorsMessage(result);
			}
		}
        return dealErrorsMessage(result);
	}

	/**
	 * ����ͼƬ���ݣ�����ȡ��һ��ͼ(���趨Ϊmajor)����Ҫ��ɫ
	 *
	 * @param imageList ͼƬ�б�
	 * @param needMajorImageMainColor �Ƿ���Ҫ�����ͼ��ɫ����ƷͼƬ��Ҫ��⣬����ͼƬ����Ҫ���
	 * @return ������ͼ����
	 * �Լ��ӿڲ����е�result
	 * @throws IcException
	 */
	private ItemImageDO saveInputImageList(List<ItemImageDO> imageList, boolean needMajorImageMainColor,
			final BaseResultDO result) throws IcException {
		if (imageList != null && imageList.size() > 0) {// �϶�����ͼ

			ItemImageDO firstImage = null; // ��ŵ�һ��ͼ�ı��ر���
			ItemImageDO majorImage = null; // �����ͼ�ı��ر���
			for (ItemImageDO image : imageList) {// ���ѭ�������ĸ�Ϊ��ͼ����һ������Ϊmajor��ͼ����ȫ������major������һ��ͼ����Ϊmajor
				if (firstImage == null) {// ��ŵ�һ��ͼ
					firstImage = image;
				}
				if (image.isMajor() && majorImage == null) {// ��ŵ�һ����ͼ
					majorImage = image;
				}
				if (majorImage != null) {// ǰ���Ѿ��ҵ���һ����ͼ��������ͼ����ͼ����������������Ҳ�Ὣ��һ����ͼ�� ��ͼ������(���������û���)
					image.setMajor(false);
				}
			}
			if (majorImage != null) {// �ҵ���һ����ͼ
			} else {// �Ҳ�����ͼ���򽫵�һ��ͼ����Ϊ��ͼ
				majorImage = firstImage;
			}
			majorImage.setMajor(true);

			for (ItemImageDO image : imageList) {
				if (image.getImageData() != null) {// ��ͼƬ����
					SavePictureFileResultDO subResult;
					if (needMajorImageMainColor) {// ��Ҫ���ͼƬ��ɫ
						subResult = pictureFileManager.saveFileToTfs(image.getImageData(), image.getInputFileName(),
								image.isMajor());
					} else {
						subResult = pictureFileManager.saveFileToTfs(image.getImageData(), image.getInputFileName(),
								false);
					}
					if (subResult.isFailure() || subResult.getPictUrl() == null) {//���ִ�����ֹ�������ش���
						result.addErrors(subResult.getErrors());
						return null;
					}
					image.setImageUrl(subResult.getPictUrl());
					String mainColor = subResult.getMainColor();
					if (image.isMajor()) {// �趨��ͼ��ɫ
						image.setMainColor(mainColor);
					}
					// IMPORTANT! ���´���ǳ���Ҫ������500K*5���ļ��������л������������ˡ�
					image.setImageData(null);
					image.setInputFileName(null);
				}
			}
			return majorImage;
		} else {// size == 0 ������ͼΪnull����ʾ�����ͼ
			return null;
		}
	}

    /**
     * У��ͼƬ�б�ͼƬ��С�������ƽ����ش����롣<br>
     * ͼƬDO��ͬʱ������ͬʱδ����ͼƬ���Ӻ�ͼƬ���ݣ����׳������Ƿ��쳣
     *
     * @param imageList
     * @return �������ַ�������У������򷵻�null
     */
    private String checkInputImageList(final List<ItemImageDO> imageList) {
        for (final ItemImageDO image : imageList) {
            if (image.getImageUrl() == null && image.getImageData() == null) {
                throw new IllegalArgumentException("imageUrl/imageData can't be null both");
            }
            if (image.getImageUrl() != null && image.getImageData() != null) {
                // ���һ��ͼƬͬʱ��imageUrl��imageData������Ҳ��֪��Ҫ��ô�����������쳣
                throw new IllegalArgumentException("imageUrl/imageData can't be 'not null' both");
            }
            if (image.getImageData() != null && image.getImageData().length > 512000) {
                return ErrorConstants.IC_ITEM_PIC_IS_TOO_LARGE;
            }
        }
        return null;
    }

    /**
     * У����Ʒ��ͼͼƬ�б�ͼƬ��С�������ƻ���ͼƬ�б������������ƾ������ش����롣<br>
     * @param imageList ��Ʒ��ͼ�б�
     * @return �������ַ�������У������򷵻�null
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
	 * ���������ӿ��ӿڡ��ýӿ��� Ӧ�÷�������ic�ͻ���ͻ������
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
	 * ���������ӿ��ӿڡ�<br>
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
	 * ���������޸�ָ����Ʒsku��档�ýӿ��� Ӧ�÷�������ic�ͻ���ͻ������
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
	 * ���������޸�ָ����Ʒsku��档
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
	 * uuid�����Ϸ���У��
	 * 
	 * @param uuid
	 *            ȫ��Ψһ��ʶ����Universally Unique Identifier������32��ʮ�������ַ�
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
     * ��������޸ı���������
     * 	1.��鱦��ID
     * 	2.�������ID
     * 	3.�����÷���Ϣ
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
	 * ���SKU��������Ƿ���ȷ
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
	 * ������Ե���������sku������������
	 * @see ItemService#sellerModifyItemSkuQuantity(long, long, Map, AppInfoDO)
	 */
	public ProcessResultDO sellerModifyItemSkuQuantity(long sellerId, long itemId, Map<Long,Integer> skuQuantityMap, AppInfoDO appInfo) throws IcException {
		checkModifyQuantity(sellerId, itemId, skuQuantityMap, appInfo);
		checkSkuQuantity(skuQuantityMap.values());
        ProcessResultDO result = g(itemServiceL1).sellerModifyItemSkuQuantity(sellerId, itemId, skuQuantityMap, appInfo);
        return dealErrorsMessage(result);
	}

    /**
     * ����޸Ŀ�����
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
            throw new IllegalArgumentException("��������ȷ");
        }

        //���skuIds
        for( Long skuId : skuQuantityMap.keySet() ) {
            if( null == skuId || skuId <= 0 ) {
                throw new IllegalArgumentException("skuQuantityMap��������ȷ");
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

		// �Ա�������quantity, ����ı�quantity���ݣ����Ե�quantitiesΪnull��ʱ�򲻶�����������м��
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

		//���ϼܵı�������������
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
	 * ����encodeId������ID���н��ܣ��������жϣ�ͨ���󷵻���ƷID�������ͨ����result����false��
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
		if (id.endsWith("tbtestpublishauction")) { // �Զ������ԱȽ��ѽ��б����ļ��ܣ�����Ǹ��ַ�����β�������н���
			return id;
		}
		// Ϊ�˱���csrf©����ʹ�ü���
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
	 * ��������ID����һ�����������Ʒid��������������
	 * <ol>
	 * <li>��Ʒ������ʱ�򣬲�������������У���룬Ϊ�˱��������ظ������������Σ�Ԥ������һ��ID������ʱ��У��</li>
	 * <li>
	 * �������Ҵ��š������ڷ��������У��������ȵ���Զ���½�����ܻ��л�Ϊ����һ���ʻ�����encodeId�б���������ʱ���û�ID������ʱ����У�飬
	 * ��������
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
		// ���������Ϣ
		// dealErrorsMessage(result);
		return result;
	}

	private static String encodeId(String id, String sellerId) {
		String vid = new StringBuffer(id).append(" ").append(sellerId).append(" ").append(System.currentTimeMillis())
				.toString();
		// Ϊ�˱���csrf©����ʹ�ü���
		BlowfishEncrypter encrypter = BlowfishEncrypter.getEncrypter();
		return encrypter.encrypt(vid);
	}

    /**
     * ����item��outerId��sku��outerId
     *
     * @author <a href="mailto:zhenbei@taobao.com">��</a>
     * @since 2010-8-20 ����09:56:37
     *
     * @param itemId ��������id
     * @param itemUpdateDO ��Ҫ����itemId��outerId��skuList��sku����Ҫ����itemId��skuId��outerId��
     * @param appInfoDO ��ClientAppName�ж����appName
     * @return
     * @throws IcException
     */
    public ProcessResultDO sellerModifyItemSkuOuterId(long itemId, ItemUpdateDO itemUpdateDO, AppInfoDO appInfoDO) throws IcException{
        //����У��
        sellerModifyItemSkuOuterIdCheck(itemId, itemUpdateDO, appInfoDO);

        //����itemUpdateDO���Է������£�
        ItemUpdateDO iu = new ItemUpdateDO();
        iu.setItemId(itemId);
        iu.setOuterId(itemUpdateDO.getOuterId());
        iu.setSkuList(itemUpdateDO.getSkuList());
        ProcessResultDO result = g(itemServiceL1).sellerModifyItemSkuOuterId(itemId, iu, appInfoDO);
        return dealErrorsMessage(result);
    }

    /**
     * ���ұ༭sku_outerУ��
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

    	boolean need2Update = updateFields.getOuterId()!=null; //���Ը���Ϊ�մ�
    	List<ItemSkuDO> skuList = updateFields.getSkuList();
        if (CollectionUtil.isNotEmpty(skuList)) {
        	for(ItemSkuDO sku : skuList){
        		if(sku.getSkuId() < 1){
        			throw new IllegalArgumentException("invalid skuId!");
        		}
        		if(!need2Update && sku.getOuterId()!=null){ //���Ը���Ϊ�մ�
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
        return dealErrorsMessage(result);// ���������Ϣ
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
        return dealErrorsMessage(result);// ���������Ϣ
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

		if (itemImage.getImageId() < 1 && itemImage.getImageUrl() == null) {// Ϊ�������ݣ����봫������ֵ
			throw new IllegalArgumentException("insert image must setup imageUrl or imageData");
		}

		result = g(itemServiceL1).sellerUploadCommonItemImage(itemId, sellerId, itemImage);
        return dealErrorsMessage(result);
	}

	/**
	 * �洢�ϴ�Commonʱ��ͼƬ
	 * @param itemImage
	 * @return
	 * @throws IcException
	 */
	private ResultDO<ItemImageDO> saveUploadCommonItemImage(ItemImageDO itemImage) throws IcException {
		boolean hasMajorImageData = false;// ����ͼ��û�ṩ��ͼ����
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
		if (itemImage.isMajor() && hasMajorImageData == false) {// ��ͼ����ֻ�ṩ��url��û�ṩ���ݣ���mainColor��Ϊnull���ú�˾����Ƿ�Ҫ���mainColor
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
		if (itemImage.getImageId() < 1 && itemImage.getImageUrl() == null) {// Ϊ�������ݣ����봫������ֵ
			throw new IllegalArgumentException("insert image must setup imageUrl or imageData");
		}

		ResultDO<ItemImageDO> result = saveUploadPropertyImage(itemImage);
		if( result.isFailure() ) {
			return result;
		}

		if (itemImage.getImageId() < 1) {// Ϊ�������ݣ����봫������ֵ
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
	 * ���������ϴ�������ͼƬ
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
	 * �ڱ����ȼ��һЩ�����Ƿ�Ϸ�
	 * @param item
	 * @param publicItemOption
	 * @param needCheckUserId �Ƿ���Ҫ���userId
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
         //�������п�     add by qier
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

        // һ����Ʒ��Ӧһ����Ƶ��¼
        if (item.getVideoList() != null) {
            String errorCode = checkItemVideoList(item.getVideoList());
            if (errorCode != null) {
                CreateItemResultDO result = new CreateItemResultDO();
                result.addError(errorCode);
                return dealErrorsMessage(result);
            }
        }

        //������Ʒ�ĸ�����Ϣ�Ƿ�����
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
            item.setSkuList(null); // ������Ʒ������sku
        }

        if (item.isAuction()) {
            item.setSkuList(null); // ������Ʒ������sku
        }

        List<ItemSkuDO> skuList = item.getSkuList();
        if (skuList != null && skuList.size() > 0) {
            item.setQuantity(validateSkuPropertyAndReturnQuantity(skuList));
        }

        List<ItemImageDO> itemImageList = item.getCommonItemImageList();
        // ҵ��˵������ͼ�п���Ϊnull
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
            // ��javadoc: ��ֹ�ڲ���ע�������ӵĴ������NullPointer����
            item.setPropertyImageList(new ArrayList<ItemImageDO>());
        }

        if (StringUtil.isBlank(publicItemOption.getLang())) {
            throw new IllegalArgumentException("PublishItemOptionDO.lang can't be blank");
        }

        if (null != item.getFeatures() && item.getFeatures().containsKey(ItemFeature.FEATURE_CCURL)) {
            String ccurl = item.getFeatures().get(ItemFeature.FEATURE_CCURL);
            ccurl = (ccurl == null) ? "" : ccurl;

            // ��ֵ��ַ�ĳ��Ȳ��ܳ���150
            if (ccurl.length() > 150) {
                CreateItemResultDO result = new CreateItemResultDO();
                result.addError(ErrorConstants.AUTO_CCURL_TOO_LEN);
                return dealErrorsMessage(result);
            }
        }

        // IMPORTANT! ���ϴ���tfs�ϵ�ͼƬ����ɾ��������̨�����ˣ�����ǰ̨���û���������ĵط����û�Ҳ���������ϴ�ͼƬ��,
        // ����Ҫ��Ԥ���ϴ��ϴ���ͼƬ��һЩ����ͼƬ��tfs�ᶨʱ����
        itemImageList = item.getCommonItemImageList();
        CreateItemResultDO result = new CreateItemResultDO();
        ItemImageDO majorImage = saveInputImageList(itemImageList, true, result);

        if (result.isFailure()) {
            return dealErrorsMessage(result);
        }
        if (majorImage != null) {
            item.setPictUrl(majorImage.getImageUrl()); // ������ͼ����ͼ��ɫ
            item.setMainColor(majorImage.getMainColor());

            itemImageList.remove(majorImage);// ���б��������ͼ
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
     * ������ֱ����Ƿ�Ϸ�
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

        // ����ⲻ��Ҫ����У��
        // У����Ŀ��
        if (item.getReservePriceLong() == null || item.getAuctionType() == null
                || item.getDuration() == null || item.getCity() == null || item.getProv() == null
                || item.getQuantity() == null || item.getStuffStatus() == null || null == item.getDescription()) {
            throw new IllegalArgumentException(
                    "category/reservePrice/auctionType/city/prov/quantity/stuffStatus can't be null");
        }

        // �������Ʒ��֧�ֶ�ͼ
        item.setCommonItemImageList(null);
        item.setPropertyImageList(null);
        item.setSkuList(null); // ������Ʒ������sku

        return null;
    }

    /**
     * ���ұ༭��������Ƿ�Ϸ�
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
        // 1 ����У�����
        SaveItemResultDO result = checkParamForSaveItem(itemId, sellerId, inputItem, processOption);
        if (result.isFailure()) {
            return result;
        }

        // 2 ҵ�������
        // 2.1 ���ұ༭����ʱ�����޸ĵ�����
        inputItem.setCurrentBidLong(null);
        inputItem.setAuctionType(null);
        inputItem.clearOrderCost();

        // 2.2 ����SKU������Ʒ����
        List<ItemSkuDO> skuList = inputItem.getSkuList();
        if (skuList != null && skuList.size() > 0) {// �Զ�����һ�����������ܼ���һЩ���󣬵������ܱ������д���
            inputItem.setQuantity(validateSkuPropertyAndReturnQuantity(skuList));
        }

        // 2.3 ����ͼƬ��Ϣ
        processImageList(inputItem, result);
        if (result.isFailure()) {
            return result;
        }

        //2.4��������Ϣ
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
            //������ݵ�ContentDataΪnull��Я����Url��Ϊnull����ֱ��ʹ��ָ����url
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
            	return;//���ִ�����ֹ����
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
     * �༭��Ʒ��չ��Ϣ
     * ��Ʒ��δ�ҵ��Ľṹ��������
     * �Ѵ��ڵģ��򸲸�ԭ�еĽṹ
     * ��remove��updateͬʱ���ڣ����ɾ���ṹ��remove���ȼ�����update
     * @param updateExtendsList ������༭һ����չ��Ϣ��
     * @param removeExtendsKeyList ����keyɾ����Ӧ����չ��Ϣ,��ѡ�ṹ����ɾ��
     * @param app ���÷���Ϣ
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
		Assert.isTrue(!StringUtil.isBlank(userStrId), "user id can not be null or empty!");//�����ö��ԣ���
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
