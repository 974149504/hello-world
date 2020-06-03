package GenerateCodeByZxing;

/**
 * @ClassName BarcodeConstant
 * @Author ShiHaiLin
 * @Date 2020/5/19 19:00
 * @Description 条码/二维码常量管理
 */
public interface BarcodeConstant {

    /**
     * 黑
     */
    int BLACK = 0xff000000;

    /**
     * 白
     */
    int WHITE = 0xFFFFFFFF;

    /**
     * 条形码
     */
    int BARCODE = 1;

    /**
     * 二维码
     */
    int QRCODE = 2;

    /**
     * 图片类型
     */
    String PICTURE_TYPE = "png";

    /**
     * 二维码高度
     */
    int QRHEIGHT = 200;

    /**
     * 条形码高度
     */
    int BARCODEHEIGHT = 50;

    /**
     * 宽度
     */
    int WIDTH = 200;

    /**
     * 工具类型码
     */
    int TOOL_TYPE_CODE = 1;

    /**
     * 其他类型码
     */
    int OTHER_TYPE_CODE = 2;

    /**
     * 链接
     */
    int LINK_TYPE_CODE = 3;

    /**
     * 扫码记录名
     */
    String SWEEP_RECORD_NAME = "扫码信息";

    /**
     * 核对记录名
     */
    String checkRecordName = "核对信息";

    /**
     * 扫码状态未核对
     */
    int UN_CHECKED_RECORD = 1;

    /**
     * 扫码状态已核对
     */
    int CHECKED_RECORD = 2;

    /**
     * 扫码状态已补录
     */
    int SUPPLEMENT = 3;

    /**
     * 核对操作类型
     */
    int CHECK_OPERATE_TYPE = 1;

    /**
     * 补录操作类型
     */
    int SUPPLEMENT_OPERATE_TYPE = 2;

    /**
     * 文字编码
     */
    String CODE = "code";

    /**
     * 其他类二维码可编辑字段
     */
    int IS_EDIT_TRUE = 2;

    /**
     * 字符集
     */
    String CHARSET = "utf-8";

    /**
     * 核对未完成
     */
    String NOT_FINISH = "核对未完成";

    /**
     * 核对完成
     */
    String FINISH = "核对完成";

    /**
     * 归还状态
     */
    int RETURN_STATE = 2;

}
