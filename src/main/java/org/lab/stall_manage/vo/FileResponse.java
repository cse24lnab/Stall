package org.lab.stall_manage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private Integer fileId;
    private String url;
    private String filename;
    private String contentType;
    private long size;
    private Integer uploadedBy;
    private LocalDateTime createTime;
}
