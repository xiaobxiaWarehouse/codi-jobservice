package com.codi.jobservice.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.alibaba.fastjson.JSON;
import com.codi.base.cache.CacheUtil;
import com.codi.base.exception.BaseAppException;
import com.codi.base.spring.SpringContextHolder;
import com.codi.base.util.DateUtils;
import com.codi.base.util.ListUtil;
import com.codi.base.util.MapUtils;
import com.codi.base.util.MoneyUtils;
import com.codi.bus.constant.CacheKeyConst;
import com.codi.bus.constant.FundConst;
import com.codi.bus.core.domain.FundInfo;
import com.codi.bus.core.resultModel.WorkingDayResult;
import com.codi.bus.core.service.FundDetailService;
import com.codi.bus.core.service.FundInfoService;
import com.codi.bus.core.service.WorkingDateService;
import com.codi.bus.core.service.model.FundSortModel;
import com.codi.bus.core.service.util.FundUtil;
import com.codi.fundData.domain.FundNetValue;
import com.codi.fundData.domain.FundNetValuePerformance;
import com.codi.fundData.domain.SecurityProduct;
import com.codi.fundData.service.FundNetValuePerformanceService;
import com.codi.fundData.service.FundNetValueService;
import com.codi.jobservice.common.Const;
import com.codi.jobservice.util.JobUtil;

/**
 * 基金收益、净值排序定时任务，每天执行
 * 
 * @author shi.pengyan
 * @date 2016年11月15日 上午9:44:20
 */
@DisallowConcurrentExecution
public class FundSortJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(FundSortJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        FundInfoService fundInfoService = SpringContextHolder.getBean("fundInfoService");

        // 1.全部排序,排除货基
        List<Integer> fundTypeCodes = new ArrayList<>(7);
        fundTypeCodes.add(FundConst.FUND_TYPE_CODE_STOCK);
        fundTypeCodes.add(FundConst.FUND_TYPE_CODE_MIXIN);
        fundTypeCodes.add(FundConst.FUND_TYPE_CODE_BOND);
        fundTypeCodes.add(FundConst.FUND_TYPE_CODE_GUARANTEE);
        fundTypeCodes.add(FundConst.FUND_TYPE_CODE_INDEX);
        fundTypeCodes.add(FundConst.FUND_TYPE_CODE_OTHER);
        fundTypeCodes.add(FundConst.FUND_TYPE_CODE_QDII);

        List<FundInfo> fundInfos = fundInfoService.queryFundInfoByTypes(fundTypeCodes);
        setCache(fundInfos, 0);

        // 股票
        fundInfos = fundInfoService.queryFundInfoByType(FundConst.FUND_TYPE_CODE_STOCK);
        setCache(fundInfos, FundConst.FUND_TYPE_CODE_STOCK);

        // 混合+保本
        List<Integer> fundTypeCodes2 = new ArrayList<>(2);
        fundTypeCodes2.add(FundConst.FUND_TYPE_CODE_MIXIN);
        fundTypeCodes2.add(FundConst.FUND_TYPE_CODE_GUARANTEE);

        fundInfos = fundInfoService.queryFundInfoByTypes(fundTypeCodes2);
        setCache(fundInfos, FundConst.FUND_TYPE_CODE_MIXIN);

        // 债券型
        fundInfos = fundInfoService.queryFundInfoByType(FundConst.FUND_TYPE_CODE_BOND);
        setCache(fundInfos, FundConst.FUND_TYPE_CODE_BOND);

        // QDII
        fundInfos = fundInfoService.queryFundInfoByType(FundConst.FUND_TYPE_CODE_QDII);
        setCache(fundInfos, FundConst.FUND_TYPE_CODE_QDII);

        // 指数型
        fundInfos = fundInfoService.queryFundInfoByType(FundConst.FUND_TYPE_CODE_INDEX);
        setCache(fundInfos, FundConst.FUND_TYPE_CODE_INDEX);
        // 其他型 算到全部中

        // 货币型
        fundInfos = fundInfoService.queryFundInfoByType(FundConst.FUND_TYPE_CODE_CURRENCY);
        setCache(fundInfos, FundConst.FUND_TYPE_CODE_CURRENCY);

        // 短期理财类型（表现形式是货基）
        fundInfos = fundInfoService.queryFundInfoByType(FundConst.FUND_TYPE_CODE_SHORT_TERM_FINANCING);
        setCache(fundInfos, FundConst.FUND_TYPE_CODE_SHORT_TERM_FINANCING);

        // 更新JOB执行时间
        JobUtil.updateJobExecuteTime(CacheKeyConst.MF_FUND_NET_VALUE_PERFORMANCE_SORT);
    }

    /**
     * 设置缓存
     * 
     * @param fundInfos
     *            基金列表
     * @param fundTypeCode
     *            基金类型编码
     */
    private void setCache(List<FundInfo> fundInfos, Integer fundTypeCode) {
        logger.debug("fundTypeCode={}", fundTypeCode);

        if (ListUtil.isEmpty(fundInfos)) {
            logger.warn("fund infos is null");
            return;
        }

        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");

        List<String> result = null;
        String key;

        try {
            int[] sorts = { 0, 1 }; // 升序, 降序

            if (FundUtil.isCurrency(fundTypeCode)) {
                int statisType = 0; // 货基统计类型默认为0
                for (int sort : sorts) {
                    result = getFundNetValue(fundInfos, fundTypeCode, statisType, sort);
                    key = CacheUtil.getKey(CacheKeyConst.MF_FUND_NET_VALUE_PERFORMANCE_SORT, fundTypeCode, statisType,
                            sort);
                    redisTemplate.delete(key);
                    if (ListUtil.isNotEmpty(result)) {
                        ListOperations<String, String> listOp = redisTemplate.opsForList();
                        listOp.rightPushAll(key, result);// 尾部插入

                        logger.debug("set cache key={}.", key);
                    } else {
                        logger.warn("cache key={},result is null", key);
                    }
                }
            } else {
                for (int sort : sorts) {
                    // 0-8: 日,周,月,季,半年,一年,两年,三年,今年以来(货币类型和短期理财忽略此参数)
                    for (int statisType = 0; statisType <= 8; statisType++) {
                        result = getFundNetValue(fundInfos, fundTypeCode, statisType, sort);
                        key = CacheUtil.getKey(CacheKeyConst.MF_FUND_NET_VALUE_PERFORMANCE_SORT, fundTypeCode,
                                statisType, sort);

                        redisTemplate.delete(key);
                        if (ListUtil.isNotEmpty(result)) {
                            ListOperations<String, String> listOp = redisTemplate.opsForList();
                            listOp.rightPushAll(key, result);// 尾部插入

                            logger.debug("set cache key={}.", key);
                        } else {
                            logger.warn("cache key={},result is null", key);
                        }
                    }
                }
            }
        } catch (BaseAppException e) {
            logger.error("Fail to query", e);
        }
    }

    /**
     * 获取统计信息
     * 
     * @param fundInfos
     *            基金列表
     * @param fundTypeCode
     *            基金编码类型
     * @param statisType
     *            统计类型
     * @param sort
     *            升降序
     * @return
     * @throws BaseAppException
     */
    private List<String> getFundNetValue(List<FundInfo> fundInfos, Integer fundTypeCode, Integer statisType,
            Integer sort) throws BaseAppException {

        logger.debug("fundTypeCode={},statisType={},sort={}", fundTypeCode, statisType, sort);

        WorkingDateService workingDateService = SpringContextHolder.getBean("workingDateService");

        FundDetailService fundDetailService = SpringContextHolder.getBean("fundDetailService");

        FundNetValueService fundNetValueService = SpringContextHolder.getBean("fundNetValueService");
        FundNetValuePerformanceService fundNetValuePerformanceService = SpringContextHolder
                .getBean("fundNetValuePerformanceService");

        List<String> fundCodes = new ArrayList<>();
        for (FundInfo fundInfo : fundInfos) {
            if (fundInfo.getFundTypeCode() == null) {
                logger.warn("fundCode={} has no fund type code.", fundInfo.getFundCode());
                continue;
            }
            logger.debug("fundCode={}", fundInfo.getFundCode());
            fundCodes.add(fundInfo.getFundCode());
        }

        if (ListUtil.isEmpty(fundCodes)) {
            logger.warn("They has no fundCodes.");
            return null;
        }

        List<SecurityProduct> products = fundDetailService.getSecurityProducts(fundCodes);

        List<Integer> innerCodes = new ArrayList<>(products.size());
        for (SecurityProduct product : products) {
            innerCodes.add(product.getInnerCode());
        }

        boolean isCurrency = FundUtil.isCurrency(fundTypeCode);
        List<String> result = null;

        if (ListUtil.isEmpty(innerCodes)) {
            return null;
        }

        Date tradingDay = null; // 查询最新的交易日(常规交易日就是前一交易日)
        List<Map<String, Object>> allRecords = new ArrayList<>(innerCodes.size()); // Service返回结果
        String staticTypeValue = "NVDailyGrowthRate"; // 默认字段

        WorkingDayResult workingDayResult = workingDateService.getLastWorkingDay(new Date());
        logger.debug("query work day result={}", workingDayResult);
        if (workingDayResult != null && workingDayResult.getSuccess()) {
            tradingDay = workingDayResult.getLastWorkingDay();
        }
        if (tradingDay == null) {
            logger.warn("tradingDay is null, do nothing.");
            return null;
        }

        if (isCurrency) { // 货基

            logger.debug("tradingDay={}", DateUtils.formatDate(tradingDay));
            queryNetValue4Currency(allRecords, innerCodes, tradingDay, sort);

        } else { // 非货基
            staticTypeValue = FundUtil.getStatisType(statisType);
            logger.debug("tradingDay={}", DateUtils.formatDate(tradingDay));
            queryNetValueExcludeCurrency(allRecords, innerCodes, statisType, tradingDay, sort);
        }

        if (ListUtil.isEmpty(allRecords)) {
            return null;
        }
        result = new ArrayList<>(allRecords.size());
        List<String> other = new ArrayList<>(); // 不需要排序的基金

        for (Map<String, Object> map : allRecords) {
            FundSortModel model = new FundSortModel();

            model.setFundCode(MapUtils.getStr(map, "FundCode"));
            model.setFundName(MapUtils.getStr(map, "ChiNameAbbr"));
            model.setFundNameAbbr(MapUtils.getStr(map, "SecuAbbr"));
            model.setInnerCode(MapUtils.getInteger(map, "InnerCode"));

            boolean hasTradingDay = true;
            Date recentlyTradingDay = null;
            String rate, value;

            if (isCurrency) { // 货基
                recentlyTradingDay = MapUtils.getDate(map, "EndDate");
                rate = MoneyUtils.calcRateToFrontEnd(MapUtils.getBigDecimal(map, "LatestWeeklyYield"));
                value = MoneyUtils.formatMoney(MapUtils.getBigDecimal(map, "DailyProfit"), 4);

                if (recentlyTradingDay == null) { // 查询最近一次的净值

                    logger.warn("fundCode={},innerCode={} has no tradingDay,then query recently latest.",
                            model.getFundCode(), model.getInnerCode());

                    FundNetValue netValue = fundNetValueService.selectLatest(model.getInnerCode());
                    if (netValue == null) {
                        logger.warn("fund has no latest netvalue,innerCode={}", model.getInnerCode());
                        continue;
                    } else {
                        recentlyTradingDay = netValue.getEndDate();
                        rate = MoneyUtils.calcRateToFrontEnd(netValue.getLatestWeeklyYield());
                        value = MoneyUtils.formatMoney(netValue.getDailyProfit(), 4);
                    }
                }
            } else { // 非货基
                recentlyTradingDay = MapUtils.getDate(map, "TradingDay");
                rate = MoneyUtils.formatMoney(MapUtils.getBigDecimal(map, staticTypeValue), 2);
                value = MoneyUtils.formatMoney(MapUtils.getBigDecimal(map, "UnitNV"), 4);

                if (recentlyTradingDay == null) { // 获取最近一次的净值
                    hasTradingDay = false;

                    logger.warn("innerCode={},recently TradingDay is null", model.getFundCode());
                    FundNetValuePerformance netValue = fundNetValuePerformanceService
                            .selectLatest(model.getInnerCode());

                    if (netValue == null) {
                        logger.warn("recently NetValuePerformance is null,innerCode={}", model.getInnerCode());
                        continue;
                    } else {
                        recentlyTradingDay = netValue.getTradingDay();
                        rate = MoneyUtils.formatMoney(FundUtil.getStatisType(netValue, statisType), 2);
                        value = MoneyUtils.formatMoney(netValue.getUnitNV(), 4);
                    }
                }
            }

            model.setTradingDay(recentlyTradingDay);
            model.setRate(rate);
            model.setValue(value);

            if (hasTradingDay) {
                result.add(JSON.toJSONString(model));
            } else {
                other.add(JSON.toJSONString(model));
            }
        }

        if (ListUtil.isNotEmpty(other)) {
            result.addAll(other);
        }

        return result;
    }

    /**
     * 查询货币相关的净值
     * 
     * @param allRecords
     *            所有记录
     * @param innerCodes
     *            内部编码
     * @param tradingDay
     *            交易日
     * @param sort
     *            排序
     * @throws BaseAppException
     */
    private void queryNetValue4Currency(List<Map<String, Object>> allRecords, List<Integer> innerCodes,
            Date tradingDay, int sort) throws BaseAppException {
        FundNetValueService fundNetValueService = SpringContextHolder.getBean("fundNetValueService");

        Integer pageIndex = 1;
        boolean isCurrency = true;

        while (true) {
            List<Map<String, Object>> temp = fundNetValueService.selectSort(innerCodes, isCurrency, tradingDay,
                    pageIndex, Const.PAGE_SIZE, sort);
            if (ListUtil.isEmpty(temp)) {
                break;
            } else {
                allRecords.addAll(temp);
                if (temp.size() < Const.PAGE_SIZE) {
                    break;
                }
                temp = null;
            }
            pageIndex++;
        }
    }

    /**
     * 查询非货币相关的净值
     * 
     * @param allRecords
     *            所有记录
     * @param innerCodes
     *            内部编码
     * @param statisType
     *            统计类型
     * @param tradingDay
     *            交易日
     * @param sort
     *            排序
     * @throws BaseAppException
     */
    private void queryNetValueExcludeCurrency(List<Map<String, Object>> allRecords, List<Integer> innerCodes,
            Integer statisType, Date tradingDay, int sort) throws BaseAppException {
        FundNetValuePerformanceService fundNetValuePerformanceService = SpringContextHolder
                .getBean("fundNetValuePerformanceService");
        Integer pageIndex = 1;

        while (true) {
            List<Map<String, Object>> temp = fundNetValuePerformanceService.selectSort(innerCodes, statisType,
                    tradingDay, pageIndex, Const.PAGE_SIZE, sort);

            if (ListUtil.isEmpty(temp)) {
                break;
            } else {
                allRecords.addAll(temp);
                if (temp.size() < Const.PAGE_SIZE) {
                    break;
                }
                temp = null;
            }
            pageIndex++;
        }
    }

}
