package com.example.selfupdate.testjavafxmvci.models;

import com.example.selfupdate.testjavafxmvci.models.DownloadStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class Download {
    private Long id; // Primary key
    private Long episodeId; // Foreign key
    private String fileName;
    private String url;
    private String downloadQuality;
    private String filePath; // Where file is saved
    private long fileSize; // In bytes
    private long downloadedBytes;
    private DownloadStatus status; // PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
    private String errorMessage;
    private double progress; // 0-100

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    // Constructors
    public Download() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = DownloadStatus.PENDING;
        this.progress = 0.0;
    }

    public Download(Long episodeId, String fileName, String url, String downloadQuality) {
        this();
        this.episodeId = episodeId;
        this.fileName = fileName;
        this.url = url;
        this.downloadQuality = downloadQuality;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEpisodeId() { return episodeId; }
    public void setEpisodeId(Long episodeId) { this.episodeId = episodeId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDownloadQuality() { return downloadQuality; }
    public void setDownloadQuality(String downloadQuality) { this.downloadQuality = downloadQuality; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
        if (fileSize > 0) {
            this.progress = (downloadedBytes * 100.0) / fileSize;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public DownloadStatus getStatus() { return status; }
    public void setStatus(DownloadStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == DownloadStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Download download = (Download) o;
        return Objects.equals(id, download.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}