package com.pspt.cardetection;

import com.pspt.detection.AlarmBusiness;
import com.pspt.detection.Constants;
import com.pspt.detection.HikBean;
import com.pspt.detection.HikCache;
import org.apache.log4j.Logger;

public class CarAlarmBusiness extends AlarmBusiness {
    private static final Logger logger = Logger.getLogger(CarAlarmBusiness.class);
    public HikBusiness hikBusiness = new HikBusiness();

    public static void startProgress() {
        for (int i = 0; i < HikCache.ipList.length; i++) {
            new CarDetectionThread(HikCache.ipList[i], HikCache.mHikBean, Constants.ACTION_TYPE_OPEN).start();
        }
    }

    public static void endProgress() {
        for (int i = 0; i < HikCache.ipList.length; i++) {
            new CarDetectionThread(HikCache.ipList[i], HikCache.mHikBean, Constants.ACTION_TYPE_CLOSE).start();
        }
    }

    public boolean open(String ip, HikBean hikBean) {
        if (!hikBusiness.init(ip, hikBean)) {
            logger.error("初始化失败, ip:" + ip);
            return false;
        }
        logger.info("初始化成功");
        if (!hikBusiness.setDVRMessageCallBack()) {
            logger.error("设置回调失败, ip:" + ip);
            return false;
        }
        logger.info("设置回调成功");
        if (!hikBusiness.login()) {
            logger.error("登录失败, ip:" + ip);
            return false;
        }
        logger.info("登录成功");
        if (!hikBusiness.setupAlarmChan()) {
            logger.error("设置布防失败, ip:" + ip);
            return false;
        }
        logger.info("设置布防成功");
        MainProgress.frame.insertText(ip + ", 成功");
        return true;
    }

    public boolean close(String ip) {
        if (!HikCache.hikBusinessMap.containsKey(ip)) {
            logger.error("设备未开启, ip:" + ip);
            return false;
        }
        hikBusiness = (HikBusiness) HikCache.hikBusinessMap.get(ip);
        if (hikBusiness.lAlarmHandle.intValue() > -1 && !hikBusiness.closeAlarmChan()) {
            logger.error("撤防失败, ip:" + ip);
            return false;
        }
        logger.info("撤防成功");
        if (!hikBusiness.logout(hikBusiness.lUserID)) {
            logger.error("登出失败, ip:" + ip);
            return false;
        }
        logger.info("登出成功");
        if (!hikBusiness.cleanup()) {
            logger.error("释放失败, ip:" + ip);
            return false;
        }
        logger.info("释放成功");
        return true;
    }
}
