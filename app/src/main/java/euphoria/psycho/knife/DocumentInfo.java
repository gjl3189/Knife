package euphoria.psycho.knife;


public class DocumentInfo {

    private final String fileName;
    private final long lastModified;
    private final String path;
    private final long size;
    private final int type;

    private DocumentInfo(Builder builder) {
        this.fileName = builder.fileName;
        this.path = builder.path;
        this.lastModified = builder.lastModified;
        this.type = builder.type;
        this.size = builder.size;
    }

    public String getFileName() {
        return fileName;
    }



    public long getLastModified() {
        return lastModified;
    }


    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public int getType() {
        return type;
    }

    public static Builder fromDownloadInfo(final DocumentInfo downloadInfo) {
        Builder builder = new Builder();
        builder
                .setFileName(downloadInfo.getFileName())
                .setPath(downloadInfo.getPath())
                .setLastModified(downloadInfo.getLastModified())
                .setType(downloadInfo.getType())
                .setSize(downloadInfo.getSize())
        ;
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentInfo that = (DocumentInfo) o;
        return lastModified == that.lastModified &&
                size == that.size &&
                type == that.type &&
                fileName.equals(that.fileName) &&
                path.equals(that.path);
    }

    @Override
    public int hashCode() {

        return path.hashCode();
    }

    @Override
    public String toString() {
        return "DocumentInfo{" +
                "fileName='" + fileName + '\'' +
                ", lastModified=" + lastModified +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", type=" + type +
                '}';
    }

    public static class Builder {
        private String fileName;
        private long lastModified;
        private String path;
        private long size;
        private int type;

        public Builder() {
        }

        public DocumentInfo build() {
            return new DocumentInfo(this);
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setLastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }
    }
}
