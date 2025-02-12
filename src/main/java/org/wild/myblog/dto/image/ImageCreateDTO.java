package org.wild.myblog.dto.image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public class ImageCreateDTO {

    @NotBlank(message = "L'URL de l'image ne doit pas être vide")
    @URL(message = "L'URL de l'image doit être valide")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
