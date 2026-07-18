package org.lab.stall_manage.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.config.AliyunOssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class AliyunOssUtil {

    @Autowired
    private AliyunOssProperties  aliyunOssProperties;

    public String upload(MultipartFile file) throws Exception
    {
        String objectName= UUID.randomUUID()+getExtension(file);
        //密钥
        CredentialsProvider credentialsProvider=new DefaultCredentialProvider(aliyunOssProperties.getAccessKeyId(),aliyunOssProperties.getAccessKeySecret());

        //地区
        String endpoint=aliyunOssProperties.getEndpoint();
        String region=endpoint.substring(endpoint.indexOf("-")+1,endpoint.indexOf("."));

        //新版本sdk用v4
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(aliyunOssProperties.getEndpoint())
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(aliyunOssProperties.getBucketName(),objectName,inputStream);
            return "https://"+aliyunOssProperties.getBucketName()+"."+aliyunOssProperties.getEndpoint()+"/"+objectName;
        } catch (OSSException oe) {
            log.warn("文件上传失败"+oe.getMessage());
            throw new OSSException(oe.getMessage());
        } catch (ClientException ce) {
            log.warn("文件上传失败"+ce.getMessage());
            throw new ClientException(ce.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    private String getExtension(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("文件不能为空");
        }
        if(file.getSize() > 2L * 1024 * 1024)
        {
            throw new IllegalArgumentException("文件大小不能超过2MB");
        }
        // 文件名
        String originName = file.getOriginalFilename();
        if(!StringUtils.hasText(originName))
        {
            throw new IllegalArgumentException("文件名不能为空");
        }
        int index = originName.lastIndexOf(".");
        if(index == -1)
        {
            throw new IllegalArgumentException("文件名格式错误");
        }
        String extension=originName.substring(originName.lastIndexOf("."));
        if(!extension.equals(".jpg") && ! extension.equals(".png"))
        {
            throw new IllegalArgumentException("图片格式不合法,只能上传jpg或者png");
        }
        return extension;
    }
}
