package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用于描述数据文件的属性
 */
@NoArgsConstructor
@AllArgsConstructor
public class DatasetDescription {

    @Setter
    private String name;

    @Setter
    private int recordNum;

    @Setter
    private String recordStartDate;

    @Setter
    private String recordEndDate;

    @Setter
    private String uploadDate;

    @Getter
    @Setter
    private long fileSize;

    /**
     * Convert size.
     *
     * @param bytes bytes size
     * @param si    true for use si, unit in 1000; false for use binary, unit in 1024
     * @return readable String size
     */
    public String formatReadableByteCount(long bytes, boolean si) {
        if (bytes == -1) {
            return "unknown";
        }
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * 以方便阅读的形式返回数据文件的大小
     *
     * @return 表示文件大小的字符串
     */
    public String getReadableFileSize() {
        return formatReadableByteCount(fileSize, true);
    }

    /*
     * getters
     * 由于要在模板文件（index.scala.html）中使用，因此不能使用 lombok 注解的方式
     */

    public String getName() {
        return name;
    }

    public int getRecordNum() {
        return recordNum;
    }

    public String getRecordStartDate() {
        return recordStartDate;
    }

    public String getRecordEndDate() {
        return recordEndDate;
    }

    public String getUploadDate() {
        return uploadDate;
    }
}
