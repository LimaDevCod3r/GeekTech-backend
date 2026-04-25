package com.diegolima.geekTech.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "anime_streaming_availabilities",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_anime_streaming_availability_pair",
                columnNames = {"anime_id", "streaming_platform_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimeStreamingAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streaming_platform_id", nullable = false)
    private StreamingPlatform streamingPlatform;

    @Builder.Default
    @Size(max = 20)
    @ElementCollection
    @CollectionTable(
            name = "anime_streaming_audio_languages",
            joinColumns = @JoinColumn(name = "anime_streaming_availability_id")
    )
    @Column(name = "language", nullable = false, length = 80)
    private List<String> audioLanguages = new ArrayList<>();

    @Builder.Default
    @Size(max = 20)
    @ElementCollection
    @CollectionTable(
            name = "anime_streaming_subtitle_languages",
            joinColumns = @JoinColumn(name = "anime_streaming_availability_id")
    )
    @Column(name = "language", nullable = false, length = 80)
    private List<String> subtitleLanguages = new ArrayList<>();
}
