package org.lab.stall_manage;

import org.junit.jupiter.api.Test;
import org.lab.stall_manage.utils.AliyunOssUtil;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AliyunOssUtilTest {

    private final AliyunOssUtil aliyunOssUtil = new AliyunOssUtil();

    @Test
    void uploadRejectsNullFile()
    {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> aliyunOssUtil.upload(null));

        assertEquals("文件不能为空", ex.getMessage());
    }

    @Test
    void uploadRejectsEmptyFile()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> aliyunOssUtil.upload(file));

        assertEquals("文件不能为空", ex.getMessage());
    }

    @Test
    void uploadRejectsFileLargerThanTwoMegabytes()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE,
                new byte[2 * 1024 * 1024 + 1]);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> aliyunOssUtil.upload(file));

        assertEquals("文件大小不能超过2MB", ex.getMessage());
    }

    @Test
    void uploadRejectsBlankOriginalFileName()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "", MediaType.IMAGE_PNG_VALUE, "image-content".getBytes());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> aliyunOssUtil.upload(file));

        assertEquals("文件名不能为空", ex.getMessage());
    }

    @Test
    void uploadRejectsFileNameWithoutExtension()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar", MediaType.IMAGE_PNG_VALUE, "image-content".getBytes());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> aliyunOssUtil.upload(file));

        assertEquals("文件名格式错误", ex.getMessage());
    }

    @Test
    void uploadRejectsUnsupportedExtension()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.gif", MediaType.IMAGE_GIF_VALUE, "image-content".getBytes());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> aliyunOssUtil.upload(file));

        assertEquals("图片格式不合法,只能上传jpg或者png", ex.getMessage());
    }
}
