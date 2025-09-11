package ru.yandex.practicum.filmorate.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ToString
@ConfigurationProperties("filmorate")
public class AppConfig {
    private int defaultNumberOfTopFilms;

    public int getDefaultNumberOfTopFilms () {
        return defaultNumberOfTopFilms;
    }
}
