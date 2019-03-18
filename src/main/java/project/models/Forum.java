package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {
    private String slug;
    private String title;
    private String user;


    @JsonCreator
    public Forum(
            @JsonProperty("slug") String slug,
            @JsonProperty("title") String title,
            @JsonProperty("user") String user
    ) {
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String master) {
        this.user = master;
    }


}
