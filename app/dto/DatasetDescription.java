package dto;


public class DatasetDescription {

    private String name;

    private int recordNum;

    private String recordStartDate;

    private String recordEndDate;

    private String uploadDate;

    private long fileSize;

    public DatasetDescription() {
    }

    public DatasetDescription(String name, int recordNum,
                              String recordStartDate, String recordEndDate,
                              String uploadDate, long fileSize) {
        this.name = name;
        this.recordNum = recordNum;
        this.recordStartDate = recordStartDate;
        this.recordEndDate = recordEndDate;
        this.uploadDate = uploadDate;
        this.fileSize = fileSize;
    }

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

    public String getReadableFileSize() {
        return formatReadableByteCount(fileSize, true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRecordNum() {
        return recordNum;
    }

    public void setRecordNum(int recordNum) {
        this.recordNum = recordNum;
    }

    public String getRecordStartDate() {
        return recordStartDate;
    }

    public void setRecordStartDate(String recordStartDate) {
        this.recordStartDate = recordStartDate;
    }

    public String getRecordEndDate() {
        return recordEndDate;
    }

    public void setRecordEndDate(String recordEndDate) {
        this.recordEndDate = recordEndDate;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
