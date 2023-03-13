package uk.ac.tees.aad.w9596086;

public class PdfFile {
    private String id;
    private String name;
    private String url;
    private String thumbnailUrl;

    public PdfFile() {}

    public PdfFile(String name, String url, String thumbnailUrl) {
        this.name = name;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
