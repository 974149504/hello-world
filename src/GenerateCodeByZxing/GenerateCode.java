package GenerateCodeByZxing;

/**
 * @ClassName GenerateCode
 * @Author ShiHaiLin
 * @Date 2020/5/28 9:09
 * @Description
 */
public class GenerateCode {

   /* *//**
     * 创建条码/二维码，同时生成图片放入文件服务器
     * @param barcodeInfo
     * @param userId
     * @return 条码/二维码图片文件ID
     *//*
    private String createCodeImage(BarcodeInfo barcodeInfo, String userId)
    {
        int codeCategory = barcodeInfo.getCodeCategory();
        String fileId = "";
        try
        {
            if (codeCategory == BARCODE)
            {
                fileId = getBarcodeWriteFile(barcodeInfo.getCodeValue(),
                        WIDTH,
                        BARCODEHEIGHT,
                        new File(System.getProperty("user.dir") + File.separator + "temp.png"), userId);
            }
            if (codeCategory == QRCODE)
            {
                fileId = getQrWriteFile(barcodeInfo.getCodeValue(),
                        QRHEIGHT,
                        new File(System.getProperty("user.dir") + File.separator + "temp.png"), userId);
            }
        }
        catch (Exception e)
        {
            log.error("后台创建条码/二维码",e);
            throw new BusinessException(FILE_SERVR_GENERATE_BAR_CODE_ERROR, "文件服务器条码/二维码生成失败");
        }

        return fileId;
    }

    *//**
     * 转换成图片
     *
     * @param matrix matrix
     * @return 图片
     *//*
    private BufferedImage toBufferedImage(BitMatrix matrix)
    {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    *//**
     * 生成二维码
     * @param str    写入内容
     * @param height 二维码高度
     * @return 图片
     *//*
    private BufferedImage getRr(String str, Integer height)
    {
        try
        {
            // 文字编码
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, height, height, hints);

            return toBufferedImage(bitMatrix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    *//**
     * 生成条形码
     * @param str    写入内容
     * @param width  生成条形码宽度
     * @param height 生成条形码高度
     * @return 条形码图片
     *//*
    private BufferedImage getBarcode(String str, Integer width,Integer height)
    {
        try
        {
            // 文字编码
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, CODE);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(str,
                    BarcodeFormat.CODE_128, width, height, hints);

            return toBufferedImage(bitMatrix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    *//**
     * 生成条形码，写到文件中,并上传至服务器
     * @param str    写入内容
     * @param width  宽度
     * @param height 高度
     * @param file   文件
     * @throws IOException io错误
     *//*
    private String getBarcodeWriteFile(String str, Integer width,
                                       Integer height, File file, String userId) throws IOException
    {
        BufferedImage image = getBarcode(str, width, height);
        ImageIO.write(image, PICTURE_TYPE, file);
        return doPost(file, PICTURE_TYPE, userId);
    }

    *//**
     * 生成二维码，写到文件中，并上传至服务器返回文件ID
     * @param str    内容
     * @param height 高度
     * @param file   文件
     * @return 文件ID
     * @throws IOException io错误
     *//*
    private String getQrWriteFile(String str, Integer height, File file, String userId) throws IOException
    {
        BufferedImage image = getRr(str, height);
        ImageIO.write(image, PICTURE_TYPE, file);
        return doPost(file, PICTURE_TYPE, userId);
    }

    *//**
     * 发送文件至文件服务器
     * @param file 需要上传的文件
     *//*
    private String doPost(File file, String fileType, String userId)
    {
        GetUploadUrlRequest getUploadUrlRequest = new GetUploadUrlRequest();
        getUploadUrlRequest.setAgent(MediaConst.AgentType.INNER);
        GetUploadUrlResponse getUploadUrlResponse =
                uploadManageService.getUploadUrl(getUploadUrlRequest.forUser(userId));
        if (!getUploadUrlResponse.isSuccess())
        {
            throw new BusinessException(UPLOAD_FILE_ADDRESS_ERROR, "上传文件地址出错");
        }
        try
        {
            int result = httpService.uploadFile(getUploadUrlResponse.getUploadUrl(),
                    file,
                    "file." + fileType,
                    "image/" + fileType,
                    null);
            if (result != 0)
            {
                throw new IllegalStateException("upload file fail.");
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("upload file fail.", e);
        }
        return getUploadUrlResponse.getFileId();
    }

    *//**
     * 删除在文件服务器上图片（条码、二维码、附件图片）
     * @param fileId
     * @param userId
     * @return
     *//*
    public DelUploadFileResponse deleteFileONServer(String fileId,String userId){
        log.info("enter deleteFileONServer");
        DelUploadFileResponse response = new DelUploadFileResponse();
        DelUploadFileRequest delUploadFileRequest = new DelUploadFileRequest();
        if(StringUtils.isNotBlank(fileId) && StringUtils.isNotBlank(userId)){
            try{
                delUploadFileRequest.setFileIds(fileId);
                response = uploadManageService.delUploadFiles(delUploadFileRequest.forUser(userId);
            }catch(Exception e){
                log.error("delete file Error",e);
                throw new BusinessException(DEL_BAR_CODE_IMAGE_ERROR, "删除文件服务器条码/二维码图片出错");
            }
        }else{
            throw new BusinessException(DEL_FILE_PARAM_ERROR, "删除文件服务器条码/二维码参数出错");
        }
        return response;
    }
*/
}
