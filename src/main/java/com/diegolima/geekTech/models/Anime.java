package com.diegolima.geekTech.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "animes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String synopsis;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String photo;

    @Size(max = 500)
    @Column(name = "photo_public_id", length = 500)
    private String photoPublicId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Builder.Default
    @OneToMany(mappedBy = "anime")
    private List<AnimeStreamingAvailability> streamingAvailabilities = new ArrayList<>();
}
