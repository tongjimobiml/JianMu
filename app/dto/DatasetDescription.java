package dto;


public class DatasetDescription {

    private String name;

    private int recordNum;

    private String recordStartDate;

    private String recordEndDate;

    private String uploadDate;

    private long fileSize;

    private boolean modified;


    public DatasetDescription() {
    }

    public DatasetDescription(String name, int recordNum,
                              String recordStartDate, String recordEndDate,
                              String uploadDate, long fileSize, boolean modified) {
        this.name = name;
        this.recordNum = recordNum;
        this.recordStartDate = recordStartDate;
        this.recordEndDate = recordEndDate;
        this.uploadDate = uploadDate;
        this.fileSize = fileSize;
        this.modified = modified;
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

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
