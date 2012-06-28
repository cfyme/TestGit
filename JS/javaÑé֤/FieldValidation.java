package com.taobao.uic.common.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.uic.common.domain.BasePaymentAccountDO;
import com.taobao.uic.common.domain.BaseUserDO;
import com.taobao.uic.common.domain.ExtraUserDO;
import com.taobao.uic.common.domain.ResultDO;
import com.taobao.uic.common.domain.UserRelationDO;

public class FieldValidation {
	public static final String REGEX = ",";

	private static final int MAX_NUM = 110;
	// 线上需要 [0-9a-f]{32}
	public static final String REGEX_UID = "[0-9a-f]{32}";

	public static final String INVALID = "invalid:";

	public static boolean validateUid(String uid) {
		if (uid == null) {
			return false;
		}
		uid = uid.trim();
		return uid.matches(REGEX_UID);
	}

	public static boolean checkUid32(ResultDO<?> result, String uid32) {
		if (!FieldValidation.validateUid(uid32)) {
			result.setRetCode(ResultCode.ERROR_UID32_IS_INVALID);
			result.setErrTrace(INVALID + uid32);
			return false;
		}
		return true;
	}

	public static boolean checkUid32s(ResultDO<?> result, String uid32s) {
		if (StringUtil.isBlank(uid32s)) {
			result.setRetCode(ResultCode.ERROR_UID32_ARRAY_IS_INVALID);
			return false;
		}
		String[] idArray = uid32s.trim().split(REGEX);
		if (idArray.length > MAX_NUM) {
			result.setRetCode(ResultCode.ERROR_ARRAY_OVER_LENGTH);
			return false;
		}
		for (String id : idArray) {
			if (!checkUid32(result, id)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkUid32s(ResultDO<?> result, String[] uid32s) {
		if (uid32s == null || uid32s.length == 0) {
			result.setRetCode(ResultCode.ERROR_UID32_ARRAY_IS_INVALID);
			return false;
		}
		if (uid32s.length > MAX_NUM) {
			result.setRetCode(ResultCode.ERROR_ARRAY_OVER_LENGTH);
			return false;
		}
		for (String id : uid32s) {
			if (!checkUid32(result, id)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkUserTag(ResultDO<?> result, long userTag) {
		if (userTag < 0) {
			result.setRetCode(ResultCode.ERROR_USERTAG_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkPromotedType(ResultDO<?> result, long promotedType) {
		if (promotedType < 0) {
			result.setRetCode(ResultCode.ERROR_PROMOTED_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkUserId(ResultDO<?> result, Long userId) {
		if (userId == null || userId < 1) {
			result.setRetCode(ResultCode.ERROR_USERID_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkUserIds(ResultDO<?> result, String userIds) {
		if (StringUtil.isBlank(userIds)) {
			result.setRetCode(ResultCode.ERROR_USERID_ARRAY_IS_INVALID);
			return false;
		}
		String[] idArray = userIds.trim().split(REGEX);
		if (idArray.length > MAX_NUM) {
			result.setRetCode(ResultCode.ERROR_ARRAY_OVER_LENGTH);
			return false;
		}

		for (String id : idArray) {
			Long userId = null;
			try {
				userId = Long.parseLong(id.trim());
			} catch (NumberFormatException e) {
				result.setRetCode(ResultCode.ERROR_USERID_IS_INVALID);
				result.setErrTrace(INVALID + id);
				return false;
			}
			boolean tmp = checkUserId(result, userId);
			if (!tmp) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkUserIds(ResultDO<?> result, long[] userIds) {
		if (userIds == null || userIds.length == 0) {
			result.setRetCode(ResultCode.ERROR_USERID_ARRAY_IS_INVALID);
			return false;
		}
		if (userIds.length > MAX_NUM) {
			result.setRetCode(ResultCode.ERROR_ARRAY_OVER_LENGTH);
			return false;
		}

		return true;
	}

	public static boolean checkNick(ResultDO<?> result, String nick) {
		if (StringUtil.isBlank(nick)) {
			result.setRetCode(ResultCode.ERROR_NICK_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkNick(ResultDO<?> result, String nick, String snick) {
		if (StringUtil.isBlank(nick) || StringUtil.isBlank(snick)) {
			result.setRetCode(ResultCode.ERROR_NICK_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkNicks(ResultDO<?> result, String nicks) {
		if (StringUtil.isBlank(nicks)) {
			result.setRetCode(ResultCode.ERROR_NICKLIST_IS_BLANK);
			return false;
		}
		String[] idArray = nicks.trim().split(REGEX);
		if (idArray.length > MAX_NUM) {
			result.setRetCode(ResultCode.ERROR_ARRAY_OVER_LENGTH);
			return false;
		}
		for (String nick : idArray) {
			if (!checkNick(result, nick.trim())) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkEmail(ResultDO<?> result, String email) {
		if (StringUtil.isBlank(email)) {
			result.setRetCode(ResultCode.ERROR_EMAIL_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkIdcard(ResultDO<?> result, String idcard) {
		if (StringUtil.isBlank(idcard)) {
			result.setRetCode(ResultCode.ERROR_IDCARD_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkMobile(ResultDO<?> result, String mobile) {
		if (StringUtil.isBlank(mobile)) {
			result.setRetCode(ResultCode.ERROR_MOBILE_IS_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkInsertBaseUserDO(ResultDO<?> result, BaseUserDO baseUser) {
		if (baseUser == null) {
			result.setRetCode(ResultCode.ERROR_BASEUSER_IS_NULL);
			return false;
		}
		if (!checkUid32(result, baseUser.getId())) {
			return false;
		}
		if (!checkNick(result, baseUser.getNick(), baseUser.getSnick())) {
			return false;
		}
		return true;
	}

	public static boolean checkUpdateBaseUserDO(ResultDO<?> result, BaseUserDO baseUser) {
		if (baseUser == null) {
			result.setRetCode(ResultCode.ERROR_BASEUSER_IS_NULL);
			return false;
		}
		return true;
	}

	public static boolean checkInsertExtraUserDO(ResultDO<?> result, ExtraUserDO extraUser) {
		if (extraUser == null) {
			result.setRetCode(ResultCode.ERROR_EXTRAUSER_IS_NULL);
			return false;
		}
		if (!checkUid32(result, extraUser.getUserId())) {
			return false;
		}

		return true;
	}

	public static boolean checkUpdateExtraUserDO(ResultDO<?> result, ExtraUserDO extraUser) {
		if (extraUser == null) {
			result.setRetCode(ResultCode.ERROR_EXTRAUSER_IS_NULL);
			return false;
		}
		if (!checkUid32(result, extraUser.getUserId())) {
			return false;
		}
		if (!checkUserId(result, extraUser.getId())) {
			return false;
		}
		return true;
	}

	public static boolean checkPaymentAccountDO(ResultDO<?> result, BasePaymentAccountDO paymentAccountDO) {
		if (paymentAccountDO == null) {
			result.setRetCode(ResultCode.ERROR_PAYMENTACCOUNTDO_IS_NULL);
			return false;
		}
		if (!checkUserId(result, paymentAccountDO.getUserId())) {
			return false;
		}
		if (StringUtil.isBlank(paymentAccountDO.getInstitution())) {
			result.setRetCode(ResultCode.ERROR_PAYMENTACCOUNTDO_INSTITUTION_IS_BLANK);
			return false;
		}

		if (!checkPaymentAccountNo(result, paymentAccountDO.getAccountNo())) {
			return false;
		}
		return true;
	}

	public static boolean checkPaymentAccountNo(ResultDO<?> result, String accountNo) {
		if (StringUtil.isBlank(accountNo)) {
			result.setRetCode(ResultCode.ERROR_PAYMENTACCOUNTDO_ACCOUNTNO_IS_BLANK);
			return false;
		}
		return true;
	}

	public static boolean checkMobilePhoneInfo(ResultDO<?> result, String mobilePhoneInfo) {
		if (mobilePhoneInfo != null && mobilePhoneInfo.length() > 2000) {
			result.setRetCode(ResultCode.ERROR_USERMOBILEPHONEDO_MOBILEPHONEINFO_LENGTH);
			return false;
		}
		return true;
	}

	public static boolean checkDomainKey(ResultDO<?> result, String domainKey) {
		if (StringUtil.isBlank(domainKey)) {
			result.setRetCode(ResultCode.ERROR_DOMAIN_IS_BLANK);
			return false;
		}
		return true;
	}

	public static boolean checkUserData(ResultDO<?> result, String data) {
		if (null == data || data.getBytes().length > 4000) {
			result.setRetCode(ResultCode.ERROR_PARAMETERS_IS_INVALID);
			return false;
		}

		return true;
	}
	public static boolean checkUserRelationStatus(ResultDO<?> result, int status) {
		if(status<=UserRelationStatus.INVALID){
			result.setRetCode(ResultCode.ERROR_USERRELATIONSTATUS_INVALID);
			return false;
		}
		return true;
	}
	public static boolean checkUserRelationTag(ResultDO<?> result, long userTag) {
		if (userTag < 0) {
			result.setRetCode(ResultCode.ERROR_USERELATIONTAG_INVALID);
			return false;
		}
		return true;
	}

	public static boolean checkUserRelation(ResultDO<?> result, UserRelationDO userRelationDO) {
		if(userRelationDO==null){
			result.setRetCode(ResultCode.ERROR_USERRELATION_INVALID);
			return false;
		}
		return true;
	}
}
