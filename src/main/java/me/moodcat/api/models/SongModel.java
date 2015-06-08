package me.moodcat.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.moodcat.database.entities.Artist;
import me.moodcat.database.entities.Song;

/**
 * Song Model.
 */
@Data
@EqualsAndHashCode
public class SongModel {

    /**
     * The unique id of the song.
     *
     * @param id
     *            The new Id to set.
     * @return The id of the song.
     */
    private Integer id;

    /**
     * The corresponding soundcloud id, in order to be used by the frontend.
     *
     * @param soundCloudId
     *            The new SoundCloudId to set.
     * @return The SoundCloudId of this song.
     */
    private Integer soundCloudId;

    /**
     * The artist that composed this song.
     *
     * @param artist
     *            The new artist to set.
     * @return The artist that composed this song.
     */
    private Artist artist;

    /**
     * The textual name of the song.
     *
     * @param name
     *            The new name to set.
     * @return The textual name of the song.
     */
    private String name;

    /**
     * The duration in seconds of the song.
     *
     * @param duration
     *            The new duration to set.
     * @return The duration in seconds of the song.
     */
    private int duration;

    /**
     * The link to the artwork image of the song.
     *
     * @param artworkUrl
     *            The new artworkURL to set.
     * @return The url to the artwork image.
     */
    private String artworkUrl;

    @Deprecated
    private double valence;

    @Deprecated
    private double arousal;

    /**
     * Transform a database {@link Song} into a {@code SongModel}.
     *
     * @param song
     *            Song to transform
     * @return Transformed model
     */
    public static SongModel transform(final Song song) {
        if (song == null) {
            return null;
        }
        final SongModel songModel = new SongModel();
        songModel.setId(song.getId());
        songModel.setName(song.getName());
        songModel.setArtist(song.getArtist());
        songModel.setArtworkUrl(song.getArtworkUrl());
        songModel.setDuration(song.getDuration());
        songModel.setSoundCloudId(song.getSoundCloudId());
        songModel.setValence(song.getValenceArousal().getValence());
        songModel.setArousal(song.getValenceArousal().getArousal());
        return songModel;
    }

}
